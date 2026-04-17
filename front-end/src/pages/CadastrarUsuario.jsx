import { useState, useEffect } from "react";

export default function CadastrarUsuario() {
  const [formData, setFormData] = useState({
    name: "",
    email: "",
    password: "",
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
      await registerUser(formData);
      setSuccess(true);
      setFormData({ name: "", email: "", password: "" });
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao cadastrar usuário");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1>Cadastrar Usuário</h1>

      {success && <div>Usuário cadastrado com sucesso</div>}
      {error && <div>X {error}</div>}

      <form>
        <div>
          <label>Nome Completo *</label>
          <input
            type="text"
            name="name"
            value={formData.name}
            onChange={handleInputChange}
            required
          />
        </div>

        <div>
          <label>Email *</label>
          <input
            type="email"
            name="email"
            value={formData.email}
            onChange={handleInputChange}
            required
          />
        </div>

        <div>
          <label>Senha *</label>
          <input
            type="password"
            name="password"
            value={formData.password}
            onChange={handleInputChange}
            required
          />
        </div>

        <br />
        <a href="#" onClick={handleSubmit}>
          {loading ? "Cadastrando..." : "Cadastrar"}
        </a>
      </form>

      <br />
      <div>
        <a href="/login">Já tenho uma conta</a>
      </div>
    </div>
  );
}