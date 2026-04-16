import { useState, useEffect } from "react";
import { loginUser } from "../features/auth/user.js";
import { useNavigate } from "react-router-dom";

export default function Login() {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
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
      await loginUser(
        formData.email,
        formData.password
      );
      setSuccess(true);
      navigate('/')
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao fazer login");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1>Login</h1>

      {success && <div>Login realizado com sucesso!</div>}
      {error && <div>X {error}</div>}

      <form>
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
        <button type="submit" onClick={handleSubmit} disabled={loading}>
          {loading ? "Entrando..." : "Entrar"}
        </button>
      </form>

      <br />
      <div>
        <a href="/esqueceu-senha">Esqueceu a senha?</a>
        <br />
        <a href="/cadastrar-usuario">Não tem conta? Cadastre-se</a>
      </div>
    </div>
  );
}