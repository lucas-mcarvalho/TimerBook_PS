import { useState, useEffect } from "react";
import { registerUser } from "../features/auth/user.js";
export default function CadastrarUsuario() {
  const [formData, setFormData] = useState({
    username: "",
    email: "",
    password: "",
    photo: null
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
       
      await registerUser(
        formData.username,
        formData.email,
        formData.password,
        formData.photo
      );
      setSuccess(true);
      setFormData({ username: "", email: "", password: "", photo: null });
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
          <label>Username*</label>
          <input
            type="text"
            name="username"
            value={formData.username}
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
        <button type="submit" disabled={loading} onClick={handleSubmit}>
          {loading ? "Cadastrando..." : "Cadastrar"}
        </button>
      </form>

      <br />
      <div>
        <a href="/login">Já tenho uma conta</a>
      </div>
    </div>
  );
}