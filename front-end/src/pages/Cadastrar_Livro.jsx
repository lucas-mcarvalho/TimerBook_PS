import { useState } from "react";
import { registerBook } from "../features/books/booksApi.js";

export default function CadastrarLivro() {

  const [formData, setFormData] = useState({
    name: "",
    author: "",
    isbn: "",
    publicationYear: "",
    description: "",
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

      const bookData = {
        name: formData.name,
        description: formData.description,
      };

      await registerBook(
        bookData,
        coverFile ?? undefined,
        pdfFile ?? undefined
      );

      setSuccess(true);

      setFormData({
        name: "",
        description: ""
      });

      setCoverFile(null);
      setPdfFile(null);

    } catch (err) {
      setError(
        err instanceof Error
          ? err.message
          : "Erro ao cadastrar livro"
      );
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
        </div>x

        <div>
          <label>Descrição</label>
          <textarea
            name="description"
            value={formData.description}
            onChange={handleInputChange}
            rows={4}
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