import { useState, useEffect } from 'react';
import api from '../features/axiosApi';

import LoginIcon from '../assets/conquistas/login.svg';
import LoginImage from '../assets/conquistas/login.webp';
import ReadingIcon from '../assets/conquistas/leitura1.svg';
import ReadingImage from '../assets/conquistas/leitura1.png';

export default function AchievementsList({ userId }) {
  const [medals, setMedals] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedAchievement, setSelectedAchievement] = useState(null);

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

  const getAchievementDetails = (medal) => {
    const name = medal.nome.toLowerCase();
    
    let visualAssets = {
      icon: LoginIcon,
      image: LoginImage
    };

    if (name.includes('leitor') || name.includes('leitura') || name.includes('livro')) {
      visualAssets = {
        icon: ReadingIcon,
        image: ReadingImage
      };
    }

    return {
      ...visualAssets,
      description: medal.description || medal.descricao || "Conquista desbloqueada por sua dedicação na plataforma."
    };
  };

  if (error) return <div className="achievements-error">{error}</div>;

  return (
    <div className="achievements-container" style={{ minHeight: loading ? '150px' : 'auto' }}>
      <h3>🏆 Minhas Conquistas</h3>
      
      {!loading && (
        medals.length === 0 ? (
          <p className="no-medals-text">Você ainda não possui conquistas. Continue lendo para desbloqueá-las!</p>
        ) : (
          <div className="medals-grid">
            {medals.map((medal, index) => {
              const details = getAchievementDetails(medal);
              return (
                <div 
                  key={index} 
                  className="achievement-card-square" 
                  onClick={() => setSelectedAchievement({ ...medal, ...details })}
                >
                  <div className="achievement-icon-wrapper">
                    <img src={details.icon} alt={medal.nome} />
                  </div>
                  <span className="achievement-name-label">{medal.nome}</span>
                </div>
              );
            })}
          </div>
        )
      )}

      {selectedAchievement && (
        <div className="achievement-modal-overlay" onClick={() => setSelectedAchievement(null)}>
          <div className="achievement-modal-content" onClick={(e) => e.stopPropagation()}>
            <button className="achievement-modal-close" onClick={() => setSelectedAchievement(null)}>&times;</button>
            <div className="achievement-modal-header">
              <img src={selectedAchievement.image} alt={selectedAchievement.nome} className="achievement-modal-img" />
            </div>
            <div className="achievement-modal-body">
              <h4>{selectedAchievement.nome}</h4>
              <p>{selectedAchievement.description}</p>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}