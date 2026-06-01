import api from "../axiosApi.js";

const IA_BASE_PATH = "/api/v1";

export function buildPdfPath(dataPath) {
  return `${dataPath}`;
}

/**
 * Ask a question about the current page of a PDF.
 * Java automatically extracts currentPage ± 2 as context and sends to Ollama.
 *
 * @param {string} pdfPath  - absolute container path: /app/uploads/pdfs/file.pdf
 * @param {number} page     - current page the user is reading
 * @param {string} question - user's question
 * @returns {Promise<string>} AI answer
 */
export async function askAI(pdfPath, page, question) {
  const response = await api.post(`${IA_BASE_PATH}/ask`, {
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
  const response = await api.post(`${IA_BASE_PATH}/search`, {
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
  const response = await api.get(`${IA_BASE_PATH}/page-text`, {
    params: {
      pdf_path: pdfPath,
      page: pageNumber,
    },
  });
  return response.data.text;
}

export async function translatePageText(pdfPath, page, targetLanguage = "pt-BR") {
  const response = await api.post(`${IA_BASE_PATH}/translate`, {
    pdf_path: pdfPath,
    page,
    target_language: targetLanguage,
  });
  return response.data.translation;
}
