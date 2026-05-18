import axios from "axios";

// Direct connection to Python AI service
const iaApi = axios.create({
  baseURL: "http://localhost:8000/api/v1",
});


export function buildPdfPath(dataPath) {
  console.log("Building PDF path for:", dataPath);
  return `${dataPath}`;
}

/**
 * Ask a question about the current page of a PDF.
 * Python automatically extracts currentPage ± 2 as context and sends to Ollama.
 *
 * @param {string} pdfPath  - absolute container path: /app/uploads/pdfs/file.pdf
 * @param {number} page     - current page the user is reading
 * @param {string} question - user's question
 * @returns {Promise<string>} AI answer
 */
export async function askAI(pdfPath, page, question) {
  const response = await iaApi.post("/ask", {
    pdf_path: pdfPath,
    page,
    question,
  });
  return response.data.answer;
}

/**
 * Search for a term across the entire PDF document.
 *
 * @param {string} pdfPath - absolute container path
 * @param {string} query   - search term
 * @returns {Promise<Array<{ page: number, excerpt: string }>>}
 */
export async function searchPDF(pdfPath, query) {
  const response = await iaApi.post("/search", {
    pdf_path: pdfPath,
    query,
  });
  return response.data.results;
}

/**
 * Get the plain text of a single PDF page.
 * Used by the PDF viewer text mode.
 *
 * @param {string} pdfPath    - absolute container path
 * @param {number} pageNumber - page to extract (1-indexed)
 * @returns {Promise<string>} plain text of the page
 */
export async function getPageText(pdfPath, pageNumber) {
  const response = await iaApi.get("/page-text", {
    params: {
      pdf_path: pdfPath,
      page: pageNumber,
    },
  });
  return response.data.text;
}