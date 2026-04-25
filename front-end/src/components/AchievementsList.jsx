import { useState, useEffect } from 'react';
import api from '../features/axiosApi';

export default function AchievementsList({ userId }) {
  const [medals, setMedals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!userId) return;

    const fetchMedals = async () => {
      try {
        const response = await api.get(`/achievements/user/${userId}`);
        setMedals(response.data);
      } catch (err) {
        console.error("Erro ao carregar conquistas:", err);
        setError("Não foi possível carregar as conquistas.");
      } finally {
        setLoading(false);
      }
    };

    fetchMedals();
  }, [userId]);

  if (loading) return <div className="achievements-loading">Carregando conquistas...</div>;
  if (error) return <div className="achievements-error">{error}</div>;

  return (
    <div className="achievements-card">
      <h3>🏆 Minhas Conquistas</h3>
      
      {medals.length === 0 ? (
        <p className="no-medals-text">Você ainda não possui conquistas. Continue lendo para desbloqueá-las!</p>
      ) : (
        <div className="medals-grid">
          {medals.map((medal, index) => (
            <div key={index} className="medal-card" title={medal.nome}>
              <span className="medal-icon">{medal.icone}</span>
              <span className="medal-name">{medal.nome}</span>
            </div>
          ))}
        </div>
      )}
    </div>
  );
}