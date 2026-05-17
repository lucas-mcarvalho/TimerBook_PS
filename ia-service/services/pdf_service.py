# services/pdf_service.py
import os
from pathlib import Path

import fitz  # PyMuPDF


def _resolve_pdf_path(pdf_path: str) -> Path:
    path = Path(pdf_path)
    if path.is_file():
        return path

    uploads_dir = os.getenv("UPLOADS_DIR")
    if uploads_dir and not path.is_absolute():
        path_parts = path.parts
        relative_path = Path(*path_parts[1:]) if path_parts and path_parts[0] == "uploads" else path
        upload_path = Path(uploads_dir) / relative_path

        if upload_path.is_file():
            return upload_path

    raise FileNotFoundError(pdf_path)


def _open_pdf(pdf_path: str):
    try:
        return fitz.open(str(_resolve_pdf_path(pdf_path)))
    except (fitz.FileDataError, fitz.EmptyFileError) as exc:
        raise ValueError("Arquivo PDF invalido ou vazio.") from exc


def extract_page_text(pdf_path: str, page_number: int) -> str:
    """Extract text from a single page (1-indexed)."""
    if page_number < 1:
        raise IndexError("A pagina deve ser maior ou igual a 1.")

    with _open_pdf(pdf_path) as doc:
        if page_number > len(doc):
            raise IndexError(f"Pagina {page_number} nao existe neste PDF.")

        return doc[page_number - 1].get_text("text")


def extract_page_range(pdf_path: str, start: int, end: int) -> str:
    """Extract text from a range of pages (1-indexed, inclusive)."""
    if start < 1 or end < 1:
        raise ValueError("As paginas devem ser maiores ou iguais a 1.")
    if start > end:
        raise ValueError("A pagina inicial nao pode ser maior que a pagina final.")

    with _open_pdf(pdf_path) as doc:
        if start > len(doc):
            raise IndexError(f"Pagina {start} nao existe neste PDF.")

        chunks = []
        for i in range(start - 1, min(end, len(doc))):
            chunks.append(f"--- Página {i + 1} ---\n{doc[i].get_text('text')}")

        return "\n\n".join(chunks)


def get_page_count(pdf_path: str) -> int:
    """Return the total number of pages in the PDF."""
    with _open_pdf(pdf_path) as doc:
        return len(doc)
