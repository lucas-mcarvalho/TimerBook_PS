import { useState, useEffect } from "react";
import { getReadingStatsByReadingId, getReadingStreakByReadingId } from "../features/statistics/reading_stats.js";

export default function FrameStats({ readingId }) {
  const [stats, setStats] = useState(null);
  const [streak, setStreak] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!readingId) return;

    const fetchStats = async () => {
      setLoading(true);
      setError(null);

      try {
        const [statsData, streakData] = await Promise.all([
          getReadingStatsByReadingId(readingId),
          getReadingStreakByReadingId(readingId)
        ]);

        setStats(statsData);
        setStreak(streakData);
      } catch (err) {
        console.error("Erro ao buscar estatísticas:", err);
        setError("Não foi possível carregar as estatísticas.");
      } finally {
        setLoading(false);
      }
    };

    fetchStats();
  }, [readingId]);

  if (loading) return <p>Carregando estatísticas...</p>;
  if (error) return <p>{error}</p>;
  if (!stats) return <p>Nenhuma estatística disponível.</p>;

  // Exemplo de como mostrar algumas estatísticas
  const { totalTime, averageTimePerSession, totalSessions } = stats;

  return (
    <div className="frame-stats" style={{ border: "1px solid #ccc", padding: 15, borderRadius: 8, maxWidth: 400 }}>
      <h2>Estatísticas da Leitura</h2>

      <p><strong>Total de sessões:</strong> {totalSessions ?? 0}</p>
      <p><strong>Tempo total de leitura:</strong> {totalTime ? `${Math.round(totalTime / 60)} min` : "0 min"}</p>
      <p><strong>Média por sessão:</strong> {averageTimePerSession ? `${Math.round(averageTimePerSession / 60)} min` : "0 min"}</p>

      {streak && (
        <p><strong>Streak de leitura:</strong> {streak.currentStreak ?? 0} dias</p>
      )}
    </div>
  );
}