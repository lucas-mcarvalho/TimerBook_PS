import { useState } from "react";
// Importe o serviço do Axios que criamos (ajuste o caminho se necessário)
import { passwordRecoveryService } from "../features/auth/passwordRecoveryService.js";
export default function EsqueceuSenha() {
  const [formData, setFormData] = useState({
    email: "",
  });

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null); // Guardará a mensagem de sucesso

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading) return;

    setLoading(true);
    setError(null);
    setSuccess(null);

    try {
      // Chama a função do Axios passando o email
      const response = await passwordRecoveryService.requestRecovery(formData.email);
      setSuccess(response.message || "E-mail de recuperação enviado com sucesso!");
      setFormData({ email: "" }); // Limpa o input após o envio
    } catch (err) {
      // Pega a mensagem de erro do backend ou exibe uma genérica
      setError(err.response?.data?.message || "Erro ao solicitar recuperação. Tente novamente.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <h1>Recuperar Senha</h1>

      {/* Mesma estrutura de feedback visual do seu Login */}
      {success && <div style={{ color: 'green' }}>V {success}</div>}
      {error && <div style={{ color: 'red' }}>X {error}</div>}

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
        <button type="submit" onClick={handleSubmit} disabled={loading}>
          {loading ? "Enviando aguarde..." : "Enviar link de recuperação"}
        </button>
      </form>

      <br />
      <div>
        <a href="/login">Voltar para o Login</a>
      </div>
    </div>
  );
}