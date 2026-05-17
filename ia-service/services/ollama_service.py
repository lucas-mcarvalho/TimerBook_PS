import os
import httpx

OLLAMA_URL = os.getenv("OLLAMA_URL", "http://host.docker.internal:11434/api/generate")
MODEL = os.getenv("OLLAMA_MODEL", "qwen2.5:14b")
TIMEOUT = float(os.getenv("OLLAMA_TIMEOUT", "400.0"))

async def ask_model(context: str, question: str) -> str:
    prompt = f"""Você é um assistente de leitura. Use apenas o conteúdo abaixo para responder.

CONTEÚDO DO DOCUMENTO:
{context}

PERGUNTA: {question}

Responda de forma clara e baseada somente no conteúdo fornecido."""

    try:
        async with httpx.AsyncClient(timeout=TIMEOUT) as client:
            response = await client.post(OLLAMA_URL, json={
                "model": MODEL,
                "prompt": prompt,
                "stream": False,
            })
            response.raise_for_status()
            data = response.json()
    except httpx.HTTPStatusError as exc:
        raise RuntimeError(f"Ollama respondeu com status {exc.response.status_code}.") from exc
    except httpx.HTTPError as exc:
        raise RuntimeError("Nao foi possivel conectar ao Ollama.") from exc

    answer = data.get("response")
    if not answer:
        raise RuntimeError("Resposta invalida do Ollama.")

    return answer
