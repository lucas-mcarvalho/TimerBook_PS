import api from "../axiosApi";

export async function registerBook(userId, book, coverFile, pdfFile) {
  const formData = new FormData();

  formData.append(
    "book",
    new Blob([JSON.stringify(book)], { type: "application/json" })
  );

  formData.append("userId", userId);
  
  if (coverFile) {
    formData.append("cover", coverFile);
  }

  if (pdfFile) {
    formData.append("pdf", pdfFile);
  } 
  

  try {
    const response = await api.post(`/book/create`, formData);

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
    
    try{
      const response = await api.get("/book");
      return response.data;
    }catch(error) {
      console.error("Erro ao buscar livros:", error.response?.data || error.message);
      throw error;
    }
  }

  export async function getBookByUserId(id) {
    try {
      const response = await api.get(`/book/user/${id}`);
      return response.data;
    } catch (error) {
      console.error("Erro ao buscar livros do usuário:", error.response?.data || error.message);
      throw error;
    }
  }