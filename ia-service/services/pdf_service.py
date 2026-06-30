# services/pdf_service.py
import fitz  # PyMuPDF


def extract_page_text(pdf_path: str, page_number: int) -> str:
    """Extract text from a single page (1-indexed)."""
    doc = fitz.open(pdf_path)
    text = doc[page_number - 1].get_text("text")
    doc.close()
    return text


def extract_page_range(pdf_path: str, start: int, end: int) -> str:
    """Extract text from a range of pages (1-indexed, inclusive)."""
    doc = fitz.open(pdf_path)
    chunks = []
    for i in range(start - 1, min(end, len(doc))):
        chunks.append(f"--- Página {i + 1} ---\n{doc[i].get_text('text')}")
    doc.close()
    return "\n\n".join(chunks)


def get_page_count(pdf_path: str) -> int:
    """Return the total number of pages in the PDF."""
    doc = fitz.open(pdf_path)
    count = len(doc)
    doc.close()
    return count