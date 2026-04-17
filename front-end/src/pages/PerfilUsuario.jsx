import { useState, useEffect } from "react";

export default function PerfilUsuario() {
  const [userInfo, setUserInfo] = useState(null);

  const [formData, setFormData] = useState({
    name: "",
    username: "",
    email: "",
  });

  const [loading, setLoading] = useState(false);
  const [fetching, setFetching] = useState(true);
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    async function fetchUser() {
      try {
        const userData = await getProfile();
        setUserInfo(userData);
        setFormData({ 
          name: userData.name || "", 
          username: userData.username || "",
          email: userData.email || "" 
        });
      } catch (err) {
        setError("Erro ao carregar dados do usuário");
      } finally {
        setFetching(false);
      }
    }
    fetchUser();
  }, []);

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
      await updateProfile(formData);
      setSuccess(true);
      setUserInfo(formData); 
    } catch (err) {
      setError(err instanceof Error ? err.message : "Erro ao atualizar perfil");
    } finally {
      setLoading(false);
    }
  };

  if (fetching) return <div>Carregando perfil...</div>;

  return (
    <div>
      <h1>Meu Perfil</h1>

      {success && <div>Perfil atualizado com sucesso!</div>}
      {error && <div>X {error}</div>}

      {userInfo && (
        <div style={{ marginBottom: "20px", padding: "15px", border: "1px solid #ccc" }}>
          <h2>Informações da Conta</h2>
          <p><strong>Nome:</strong> {userInfo.name}</p>
          <p><strong>Nome de Usuário:</strong> {userInfo.username}</p>
          <p><strong>Email:</strong> {userInfo.email}</p>
        </div>
      )}

      <form>
        <div>
          <label>Nome</label>
          <input
            type="text"
            name="name"
            value={formData.name}
            onChange={handleInputChange}
            required
          />
        </div>

        <div>
          <label>Nome de Usuário</label>
          <input
            type="text"
            name="username"
            value={formData.username}
            onChange={handleInputChange}
            required
          />
        </div>

        <div>
          <label>Email</label>
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
          {loading ? "Salvando..." : "Salvar Alterações"}
        </a>
      </form>
      
      <br />
      <div>
        <a href="/">Voltar para Home</a>
      </div>
    </div>
  );
}