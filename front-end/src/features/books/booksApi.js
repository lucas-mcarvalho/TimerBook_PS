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

  const response = await fetch("http://localhost:8080/book/create", {
    method: "POST",
    body: formData
  });

  if (!response.ok) {
    throw new Error("Erro ao cadastrar livro");
  }

  return response.json();
}

export async function deleteBook(id) {
  const response = await fetch(`http://localhost:8080/book/${id}`, {
    method: "DELETE"
  });

  if (!response.ok) {
    throw new Error("Erro ao deletar livro");
  }

  return response.json();
}   

export async function getBooks() {
  const response = await fetch("http://localhost:8080/book");

  if (!response.ok) {
    throw new Error("Erro ao buscar livros");
  }

  return response.json();
} 

