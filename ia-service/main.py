from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from routers.chat import router

app = FastAPI(
    title="PDF AI Service",
    description="Extração de texto e perguntas sobre PDFs via Ollama.",
    version="1.0.0",
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=False,  # importante: precisa ser False quando allow_origins=["*"]
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(router)


@app.get("/health")
def health():
    return {"status": "ok"}