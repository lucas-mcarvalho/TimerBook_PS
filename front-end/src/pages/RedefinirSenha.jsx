import { useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { passwordRecoveryService } from "../features/auth/passwordRecoveryService.js";

export default function RedefinirSenha() {
  const navigate = useNavigate();
  
  // Extrai o ?token=... da URL
  const [searchParams] = useSearchParams();
  const token = searchParams.get("token");

  const [formData, setFormData] = useState({
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

    if (!token) {
      setError("Token inválido ou ausente na URL.");
      return;
    }

    setLoading(true);
    setError(null);
    setSuccess(false);

    try {
      await passwordRecoveryService.resetPassword(token, formData.password);
      setSuccess(true);
      
      // Redireciona para o login após 2 segundos
      setTimeout(() => navigate('/login'), 2000);
    } catch (err) {
      setError(err.response?.data?.message || "Erro ao redefinir a senha. O link pode ter expirado.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1>Criar Nova Senha</h1>

      {success && <div style={{ color: 'green' }}>Senha alterada com sucesso! Redirecionando...</div>}
      {error && <div style={{ color: 'red' }}>X {error}</div>}

      <form>
        <div>
          <label>Nova Senha *</label>
          <input
            type="password"
            name="password"
            value={formData.password}
            onChange={handleInputChange}
            required
            minLength="6"
          />
        </div>

        <br />
        <button type="submit" onClick={handleSubmit} disabled={loading || success}>
          {loading ? "Salvando..." : "Salvar nova senha"}
        </button>
      </form>
    </div>
  );
}