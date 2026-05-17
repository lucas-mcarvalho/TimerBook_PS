# main.py
from fastapi import FastAPI
from routers.chat import router

app = FastAPI(
    title="PDF AI Service",
    description="Extração de texto e perguntas sobre PDFs via Ollama.",
    version="1.0.0",
)

app.include_router(router)


@app.get("/health")
def health():
    return {"status": "ok"}