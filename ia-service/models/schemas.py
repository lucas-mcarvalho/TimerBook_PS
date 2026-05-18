# models/schemas.py
from pydantic import BaseModel

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

class PageTextRequest(BaseModel):
    pdf_path: str
    page: int

class PageTextResponse(BaseModel):
    text: str