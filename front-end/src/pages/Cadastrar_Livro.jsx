import { useState } from "react";
import { registerBook } from "../features/books/booksApi.js";
import { getUser } from "../features/user/userApi.js";
import { useToast } from "../components/ToastContext.js";

export default function CadastrarLivro() {
  const { showAchievementToast } = useToast();

  const [formData, setFormData] = useState({
    name: "",
    // Removido campos não enviados ao backend
  });

  const [coverFile, setCoverFile] = useState(null);
  const [pdfFile, setPdfFile] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value
    }));
  };

  const handleCoverChange = (e) => {
    const file = e.target.files?.[0];
    if (file) setCoverFile(file);
  };

  const handlePdfChange = (e) => {
    const file = e.target.files?.[0];
    if (file) setPdfFile(file);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setError(null);
    setSuccess(false);
    try {
      const response = await getUser();
      const userId = response.data?.id || response.id;
      const bookData = {
        name: formData.name,
        description: ""
      };
      const savedBook = await registerBook(
        userId,
        bookData,
        coverFile ?? undefined,
        pdfFile ?? undefined
      );
      const novas = savedBook?.novasConquistas || [];
      if (novas.length > 0) {
        novas.forEach((conquista) => showAchievementToast(conquista));
      }
      setSuccess(true);
      setFormData({
        name: ""
      });
      setCoverFile(null);
      setPdfFile(null);
    } catch (err) {
      const message = err.response?.data;
      if (typeof message === "string") {
        setError(message);
      } else {
        setError(message?.message || "Erro ao cadastrar livro. Verifique os dados e tente novamente.");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>

      <h1>Cadastrar Livro</h1>

      {success && <div>✓ Livro cadastrado com sucesso!</div>}
      {error && <div>✗ {error}</div>}

      <form onSubmit={handleSubmit}>

        <div>
          <label>Nome *</label>
          <input
            type="text"
            name="name"
            value={formData.name}
            onChange={handleInputChange}
            required
          />
        </div>

        <div>
          <label>Capa (opcional)</label>
          <input
            type="file"
            accept="image/*"
            onChange={handleCoverChange}
          />
          {coverFile && <p>✓ {coverFile.name}</p>}
        </div>

        <div>
          <label>Arquivo PDF (opcional)</label>
          <input
            type="file"
            accept=".pdf"
            onChange={handlePdfChange}
          />
          {pdfFile && <p>✓ {pdfFile.name}</p>}
        </div>

        <button type="submit" disabled={loading}>
          {loading ? "Cadastrando..." : "Cadastrar Livro"}
        </button>

      </form>

    </div>
  );
}
