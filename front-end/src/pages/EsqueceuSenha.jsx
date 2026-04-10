import { useState, useEffect } from "react";

export default function EsqueceuSenha() {
  const [formData, setFormData] = useState({
    email: "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading) return;

    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      await resetPassword(formData.email);
      setSuccess(true);
      setFormData({ email: "" });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao solicitar recuperação");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1>Recuperar Senha</h1>

      {success && <div>Link de recuperação enviado para o seu email</div>}
      {error && <div>X {error}</div>}

      <form>
        <div>
          <label>Email cadastrado *</label>
          <input
            type="email"
            name="email"
            value={formData.email}
            onChange={handleInputChange}
            required
          />
        </div>

        <br />
        <a href="#" onClick={handleSubmit}>
          {loading ? "Enviando..." : "Enviar link de recuperação"}
        </a>
      </form>

      <br />
      <div>
        <a href="/login">Voltar ao Login</a>
      </div>
    </div>
  );
}