import httpx

OLLAMA_URL = "http://host.docker.internal:11434/api/generate"
MODEL = "qwen2.5:14b"

async def ask_model(context: str, question: str) -> str:
    prompt = f"""Você é um assistente de leitura. Use apenas o conteúdo abaixo para responder.

CONTEÚDO DO DOCUMENTO:
{context}

PERGUNTA: {question}

Responda de forma clara e baseada somente no conteúdo fornecido."""

    async with httpx.AsyncClient(timeout=120.0) as client:
        response = await client.post(OLLAMA_URL, json={
            "model": MODEL,
            "prompt": prompt,
            "stream": False,
        })
        response.raise_for_status()
        return response.json()["response"]