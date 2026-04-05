import { pdfjs } from "react-pdf";

// Configura o worker para usar o arquivo local
pdfjs.GlobalWorkerOptions.workerSrc = "/pdf.worker.min.js";

let cachedPdf = null;

export async function extractPDFRange(file, startPage, endPage) {
  try {
    const arrayBuffer = await file.arrayBuffer();
    
    if (!cachedPdf) {
      cachedPdf = await pdfjs.getDocument({ data: arrayBuffer }).promise;
    }
    
    const pdf = cachedPdf;
    let rangeText = "";

    // Garante que startPage não seja menor que 1 e endPage não ultrapasse o total
    const actualStart = Math.max(1, startPage);
    const actualEnd = Math.min(pdf.numPages, endPage);

    for (let i = actualStart; i <= actualEnd; i++) {
      const page = await pdf.getPage(i);
      const textContent = await page.getTextContent();
      const pageText = textContent.items.map((item) => item.str).join(" ");
      rangeText += `[Página ${i}]\n${pageText}\n\n`;
    }

    return rangeText;
  } catch (error) {
    console.error("Erro ao extrair range do PDF:", error);
    throw error;
  }
}

export async function extractPDFText(file) {
  try {
    const arrayBuffer = await file.arrayBuffer();
    const pdf = await pdfjs.getDocument({ data: arrayBuffer }).promise;
    let fullText = "";

    for (let i = 1; i <= pdf.numPages; i++) {
      const page = await pdf.getPage(i);
      const textContent = await page.getTextContent();
      const pageText = textContent.items.map((item) => item.str).join(" ");
      fullText += pageText + "\n";
    }

    return fullText;
  } catch (error) {
    console.error("Erro ao extrair PDF:", error);
    throw error;
  }
}
