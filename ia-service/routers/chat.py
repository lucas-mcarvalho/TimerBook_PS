# routers/chat.py
from fastapi import APIRouter, HTTPException
from models.schemas import (
    AskRequest, AskResponse,
    SearchRequest, SearchResponse, SearchResult,
    PageTextRequest, PageTextResponse,
)
from services.pdf_service import (
    extract_page_text,
    extract_page_range,
    get_page_count,
)
from services.ollama_service import ask_model

router = APIRouter(prefix="/api/v1", tags=["chat"])


@router.post("/ask", response_model=AskResponse)
async def ask(req: AskRequest):
    """
    Main chat endpoint.
    If 'page' is given  → extract that page only.
    If 'start_page' and 'end_page' are given → extract that range.
    Otherwise           → extract pages 1-10 as default context.
    """
    try:
        if req.page is not None:
            context = extract_page_text(req.pdf_path, req.page)
        elif req.start_page is not None and req.end_page is not None:
            context = extract_page_range(req.pdf_path, req.start_page, req.end_page)
        else:
            context = extract_page_range(req.pdf_path, 1, 10)

        if not context.strip():
            raise HTTPException(status_code=422, detail="Nenhum texto encontrado nas páginas solicitadas.")

        answer = await ask_model(context, req.question)
        return AskResponse(answer=answer)

    except FileNotFoundError:
        raise HTTPException(status_code=404, detail=f"PDF não encontrado: {req.pdf_path}")
    except (IndexError, ValueError) as exc:
        raise HTTPException(status_code=422, detail=str(exc))
    except RuntimeError as exc:
        raise HTTPException(status_code=502, detail=str(exc))


@router.post("/search", response_model=SearchResponse)
async def search(req: SearchRequest):
    """
    Full-document text search.
    Returns every page that contains the query, with a short excerpt.
    """
    try:
        total = get_page_count(req.pdf_path)
    except FileNotFoundError:
        raise HTTPException(status_code=404, detail=f"PDF não encontrado: {req.pdf_path}")
    except (ValueError, RuntimeError) as exc:
        raise HTTPException(status_code=422, detail=str(exc))

    results = []
    query_lower = req.query.lower()

    for page_num in range(1, total + 1):
        text = extract_page_text(req.pdf_path, page_num)
        text_lower = text.lower()
        match_index = text_lower.find(query_lower)

        if match_index >= 0:
            start = max(0, match_index - 45)
            end = min(len(text), match_index + len(req.query) + 75)
            excerpt = text[start:end].strip()
            results.append(SearchResult(page=page_num, excerpt=excerpt))

    return SearchResponse(results=results)


@router.get("/page-text", response_model=PageTextResponse)
async def page_text(pdf_path: str, page: int):
    """
    Returns the plain text of a single page.
    Used by the front-end text mode.
    """
    try:
        text = extract_page_text(pdf_path, page)
        return PageTextResponse(text=text)
    except FileNotFoundError:
        raise HTTPException(status_code=404, detail=f"PDF não encontrado: {pdf_path}")
    except IndexError:
        raise HTTPException(status_code=422, detail=f"Página {page} não existe neste PDF.")
    except ValueError as exc:
        raise HTTPException(status_code=422, detail=str(exc))
