import api from "../axiosApi";

export async function registerBook(book, coverFile, pdfFile) {
  const formData = new FormData();

  formData.append(
    "book",
    new Blob([JSON.stringify(book)], { type: "application/json" })
  );

  if (coverFile) {
    formData.append("cover", coverFile);
  }

  if (pdfFile) {
    formData.append("pdf", pdfFile);
  }

  try {
    const response = await api.post("/book/create", formData);

    return response.data;
  } catch (error) {
    console.error("Erro:", error.response?.data || error.message);
    throw error;
  }
}
export async function deleteBook(id) {
  try {
    const response = await api.delete(`/book/${id}`);
    return response.data;
  } catch (error) {
    console.error("Erro ao deletar livro:", error.response?.data || error.message);
    throw error;
  }
}


export async function getBooks() {
  const response = await fetch("http://localhost:8080/book");

  if (!response.ok) {
    throw new Error("Erro ao buscar livros");
  }

  return response.json();
} 

