import modal
import subprocess
import time
from pathlib import Path

# ── App & imagem ──────────────────────────────────────────────────────────────
app = modal.App("timerbook-ai")

image = (
    modal.Image.debian_slim(python_version="3.11")
    .apt_install("curl", "zstd")
    .run_commands(
        "curl -fsSL https://ollama.com/install.sh | sh",
    )
    .pip_install(
        "fastapi",
        "uvicorn",
        "httpx",
        "pymupdf",       # fitz
        "pydantic",
    )
)

# Volume para armazenar os PDFs enviados pelo usuário
pdf_volume = modal.Volume.from_name("timerbook-pdfs", create_if_missing=True)
PDF_DIR = Path("/pdfs")

# Volume para cachear o modelo Ollama (evita baixar a cada cold start)
model_volume = modal.Volume.from_name("timerbook-ollama-models", create_if_missing=True)
OLLAMA_MODELS_DIR = Path("/ollama-models")


# ── Services ──────────────────────────────────────────────────────────────────

def extract_page_text(pdf_path: str, page_number: int) -> str:
    import fitz
    doc = fitz.open(pdf_path)
    text = doc[page_number - 1].get_text("text")
    doc.close()
    return text


def extract_page_range(pdf_path: str, start: int, end: int) -> str:
    import fitz
    doc = fitz.open(pdf_path)
    chunks = []
    for i in range(start - 1, min(end, len(doc))):
        chunks.append(f"--- Página {i + 1} ---\n{doc[i].get_text('text')}")
    doc.close()
    return "\n\n".join(chunks)


def get_page_count(pdf_path: str) -> int:
    import fitz
    doc = fitz.open(pdf_path)
    count = len(doc)
    doc.close()
    return count


async def ask_model(prompt: str) -> str:
    import httpx
    OLLAMA_URL = "http://localhost:11434/api/generate"
    MODEL = "qwen2.5:14b"

    async with httpx.AsyncClient(timeout=120.0) as client:
        response = await client.post(OLLAMA_URL, json={
            "model": MODEL,
            "prompt": prompt,
            "stream": False,
        })
        response.raise_for_status()
        return response.json()["response"]


# ── FastAPI app ───────────────────────────────────────────────────────────────

def build_fastapi_app():
    from fastapi import FastAPI, HTTPException
    from fastapi.middleware.cors import CORSMiddleware
    from pydantic import BaseModel

    # Schemas
    class AskRequest(BaseModel):
        pdf_path: str
        question: str
        page: int | None = None
        start_page: int | None = None
        end_page: int | None = None

    class AskResponse(BaseModel):
        answer: str

    class SearchRequest(BaseModel):
        pdf_path: str
        query: str

    class SearchResult(BaseModel):
        page: int
        excerpt: str

    class SearchResponse(BaseModel):
        results: list[SearchResult]

    class PageTextResponse(BaseModel):
        text: str

    # Schema compatível com o formato que o Spring envia
    class OllamaGenerateRequest(BaseModel):
        model: str
        prompt: str
        stream: bool = False

    PAGE_RANGE = 2
    api = FastAPI(
        title="PDF AI Service",
        description="Extração de texto e perguntas sobre PDFs via Ollama.",
        version="1.0.0",
    )

    api.add_middleware(
        CORSMiddleware,
        allow_origins=["*"],
        allow_credentials=False,
        allow_methods=["*"],
        allow_headers=["*"],
    )

    @api.get("/health")
    def health():
        return {"status": "ok"}

    # ── Rota compatível com o Spring (AiOllamaService) ──
    @api.post("/api/generate")
    async def ollama_generate(req: OllamaGenerateRequest):
        answer = await ask_model(req.prompt)
        return {"response": answer}

    # ── Rotas do microsserviço Python ──
    @api.post("/api/v1/ask", response_model=AskResponse)
    async def ask(req: AskRequest):
        pdf_path = str(PDF_DIR / req.pdf_path)
        try:
            if req.page is not None:
                start = max(1, req.page - PAGE_RANGE)
                end = req.page + PAGE_RANGE
                context = extract_page_range(pdf_path, start, end)
            elif req.start_page is not None and req.end_page is not None:
                context = extract_page_range(pdf_path, req.start_page, req.end_page)
            else:
                context = extract_page_range(pdf_path, 1, 10)

            if not context.strip():
                raise HTTPException(status_code=422, detail="Nenhum texto encontrado nas páginas solicitadas.")

            prompt = f"""Você é um assistente de leitura. Use apenas o conteúdo abaixo para responder.

CONTEÚDO DO DOCUMENTO:
{context}

PERGUNTA: {req.question}

Responda de forma clara e baseada somente no conteúdo fornecido."""

            answer = await ask_model(prompt)
            return AskResponse(answer=answer)
        except FileNotFoundError:
            raise HTTPException(status_code=404, detail=f"PDF não encontrado: {req.pdf_path}")

    @api.post("/api/v1/search", response_model=SearchResponse)
    async def search(req: SearchRequest):
        pdf_path = str(PDF_DIR / req.pdf_path)
        try:
            total = get_page_count(pdf_path)
        except FileNotFoundError:
            raise HTTPException(status_code=404, detail=f"PDF não encontrado: {req.pdf_path}")

        results = []
        query_lower = req.query.lower()
        for page_num in range(1, total + 1):
            text = extract_page_text(pdf_path, page_num)
            match_index = text.lower().find(query_lower)
            if match_index >= 0:
                start = max(0, match_index - 45)
                end = min(len(text), match_index + len(req.query) + 75)
                excerpt = text[start:end].strip()
                results.append(SearchResult(page=page_num, excerpt=excerpt))

        return SearchResponse(results=results)

    @api.get("/api/v1/page-text", response_model=PageTextResponse)
    async def page_text(pdf_path: str, page: int):
        full_path = str(PDF_DIR / pdf_path)
        try:
            text = extract_page_text(full_path, page)
            return PageTextResponse(text=text)
        except FileNotFoundError:
            raise HTTPException(status_code=404, detail=f"PDF não encontrado: {pdf_path}")
        except IndexError:
            raise HTTPException(status_code=422, detail=f"Página {page} não existe neste PDF.")

    return api


# ── Modal entrypoint ──────────────────────────────────────────────────────────

@app.function(
    image=image,
    gpu="A10G",
    volumes={
        str(PDF_DIR): pdf_volume,
        str(OLLAMA_MODELS_DIR): model_volume,
    },
    scaledown_window=300,
    timeout=300,
)
@modal.concurrent(max_inputs=4)
@modal.asgi_app()
def fastapi_app():
    env = {"OLLAMA_MODELS": str(OLLAMA_MODELS_DIR)}
    subprocess.Popen(["ollama", "serve"], env={**__import__("os").environ, **env})
    time.sleep(3)

    subprocess.run(["ollama", "pull", "qwen2.5:14b"], check=True)

    return build_fastapi_app()