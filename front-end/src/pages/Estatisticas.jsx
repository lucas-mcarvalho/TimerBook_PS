import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";
import api from "../features/axiosApi.js";
// import { getReadingStatsByReadingId } from "../features/statistics/reading_stats.js"; // Comentado se não estiver em uso



const Estatisticas = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState(null);

  const [isDarkMode, setIsDarkMode] = useState(() => {
    return localStorage.getItem("timerbook-theme") === "dark";
  });

  //  escuta mudança do tema
  useEffect(() => {
    const interval = setInterval(() => {
      const currentTheme = localStorage.getItem("timerbook-theme") === "dark";
      setIsDarkMode(currentTheme);
    }, 300);

    return () => clearInterval(interval);
  }, []);

  const styles = getStyles(isDarkMode);
  const navigate = useNavigate();
  const { readingId } = useParams();

  useEffect(() => {
    async function fetchStats() {
      try {
        if (!readingId) {
          throw new Error("ID da leitura não informado.");
        }
        const response = await api.get(`/stats/reading/${readingId}`);
        const data = response.data;
        console.log("Stats recebidos:", data);
        setErro(null);
        setStats(data);
      } catch (error) {
        console.error("Erro ao buscar stats:", error);
        setErro("Não foi possível carregar as estatísticas dessa leitura.");
      } finally {
        setLoading(false);
      }
    }

    fetchStats();
  }, [readingId]);

  const formatTime = (seconds) => {
    if (!seconds) return "0h 0min";

    const hours = Math.floor(seconds / 3600);
    const minutes = Math.floor((seconds % 3600) / 60);

    return `${hours}h ${minutes}min`;
  };

  if (loading) return <div style={styles.centerMsg}>Carregando estatísticas...</div>;
  if (erro) return <div style={styles.centerMsg}>{erro}</div>;
  if (!stats) return <div style={styles.centerMsg}>Nenhuma estatística disponível.</div>;

  const chartData = [
    { nome: "Páginas lidas", valor: stats.pagesRead ?? 0 },
    { nome: "Sessões", valor: stats.sessionsCount ?? 0 },
    { nome: "Streak atual", valor: stats.currentStreakDays ?? 0 },
    { nome: "Melhor streak", valor: stats.maxStreakDays ?? 0 },
  ];

  const timeChartData = [
    { nome: "Tempo total", valor: stats.totalSeconds ?? 0 },
    { nome: "Média/sessão", valor: Number(stats.averageSecondsPerSession ?? 0) },
  ];

  const chartColor = "#4F46E5"; // Cor azul/índigo agradável

  return (
    <div style={styles.pageContainer}>
      <div style={styles.contentWrapper}>
        
        <button onClick={() => navigate("/meus-livros")} style={styles.backButton}>
          ← Voltar
        </button>

        <h1 style={styles.title}>📊 Estatísticas de Leitura</h1>

        {/* Grid de Cards de Informação */}
        <div style={styles.gridContainer}>
          <div style={styles.statCard}>
            <span style={styles.statIcon}>📖</span>
            <span style={styles.statLabel}>Páginas lidas</span>
            <span style={styles.statValue}>{stats.pagesRead ?? 0}</span>
          </div>
          <div style={styles.statCard}>
            <span style={styles.statIcon}>⏱️</span>
            <span style={styles.statLabel}>Tempo total</span>
            <span style={styles.statValue}>{formatTime(stats.totalSeconds)}</span>
          </div>
          <div style={styles.statCard}>
            <span style={styles.statIcon}>⚡</span>
            <span style={styles.statLabel}>Média por sessão</span>
            <span style={styles.statValue}>{Number(stats.averageSecondsPerSession ?? 0).toFixed(1)}s</span>
          </div>
          <div style={styles.statCard}>
            <span style={styles.statIcon}>🔁</span>
            <span style={styles.statLabel}>Sessões</span>
            <span style={styles.statValue}>{stats.sessionsCount ?? 0}</span>
          </div>
          <div style={styles.statCard}>
            <span style={styles.statIcon}>🔥</span>
            <span style={styles.statLabel}>Sequência atual</span>
            <span style={styles.statValue}>{stats.currentStreakDays ?? 0} dias</span>
          </div>
          <div style={styles.statCard}>
            <span style={styles.statIcon}>🏆</span>
            <span style={styles.statLabel}>Melhor sequência</span>
            <span style={styles.statValue}>{stats.maxStreakDays ?? 0} dias</span>
          </div>
        </div>

        {/* Gráfico 1 - Comparativo Geral */}
        <div style={styles.chartCard}>
          <h2 style={styles.chartTitle}>Comparativo geral</h2>
          <div style={styles.chartWrapper}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={chartData} margin={{ top: 20, right: 30, left: 0, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E5E7EB" />
                <XAxis dataKey="nome" axisLine={false} tickLine={false} tick={{ fill: '#6B7280' }} />
                <YAxis axisLine={false} tickLine={false} tick={{ fill: '#6B7280' }} />
                <Tooltip 
                  cursor={{ fill: 'rgba(79, 70, 229, 0.1)' }} 
                  contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}
                />
                <Bar 
                  dataKey="valor" 
                  fill={chartColor} 
                  barSize={45} 
                  radius={[8, 8, 0, 0]} 
                />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Gráfico 2 - Tempo de Leitura */}
        <div style={styles.chartCard}>
          <h2 style={styles.chartTitle}>Tempo de leitura (segundos)</h2>
          <div style={styles.chartWrapper}>
            <ResponsiveContainer width="100%" height="100%">
              <BarChart data={timeChartData} margin={{ top: 20, right: 30, left: 0, bottom: 5 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E5E7EB" />
                <XAxis dataKey="nome" axisLine={false} tickLine={false} tick={{ fill: '#6B7280' }} />
                <YAxis axisLine={false} tickLine={false} tick={{ fill: '#6B7280' }} />
                <Tooltip 
                  cursor={{ fill: 'rgba(79, 70, 229, 0.1)' }}
                  contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px rgba(0,0,0,0.1)' }}
                />
                <Bar 
                  dataKey="valor" 
                  fill="#10B981" /* Verde para diferenciar o segundo gráfico */
                  barSize={45} 
                  radius={[8, 8, 0, 0]} 
                />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </div>

      </div>
    </div>
  );
};

// Objeto de estilos para limpar o JSX e facilitar a manutenção
const getStyles = (isDarkMode) => ({
  pageContainer: {
    backgroundColor: isDarkMode ? "#0F172A" : "#F3F4F6",
    minHeight: "100vh",
    padding: "40px 20px",
    fontFamily:
      "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif",
    color: isDarkMode ? "#E5E7EB" : "#111827",
  },

  contentWrapper: {
    maxWidth: "1000px",
    margin: "0 auto",
  },

  centerMsg: {
    display: "flex",
    justifyContent: "center",
    alignItems: "center",
    height: "100vh",
    fontSize: "1.2rem",
    color: isDarkMode ? "#E5E7EB" : "#4B5563",
    backgroundColor: isDarkMode ? "#0F172A" : "#F3F4F6",
  },

  backButton: {
    marginBottom: "24px",
    padding: "10px 16px",
    cursor: "pointer",
    borderRadius: "8px",
    border: isDarkMode ? "1px solid #334155" : "1px solid #D1D5DB",
    background: isDarkMode ? "#1E293B" : "#FFFFFF",
    color: isDarkMode ? "#E5E7EB" : "#374151",
    fontWeight: "600",
    fontSize: "14px",
    transition: "all 0.2s",
    boxShadow: isDarkMode
      ? "0 4px 10px rgba(0, 0, 0, 0.35)"
      : "0 1px 2px rgba(0, 0, 0, 0.05)",
  },

  title: {
    color: isDarkMode ? "#F9FAFB" : "#111827",
    fontSize: "28px",
    fontWeight: "bold",
    marginBottom: "30px",
  },

  gridContainer: {
    display: "grid",
    gridTemplateColumns: "repeat(auto-fit, minmax(250px, 1fr))",
    gap: "20px",
    marginBottom: "40px",
  },

  statCard: {
    backgroundColor: isDarkMode ? "#1E293B" : "#FFFFFF",
    padding: "24px",
    borderRadius: "16px",
    border: isDarkMode ? "1px solid #334155" : "none",
    boxShadow: isDarkMode
      ? "0 10px 25px rgba(0, 0, 0, 0.35)"
      : "0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)",
    display: "flex",
    flexDirection: "column",
    alignItems: "flex-start",
  },

  statIcon: {
    fontSize: "24px",
    marginBottom: "12px",
  },

  statLabel: {
    fontSize: "14px",
    color: isDarkMode ? "#94A3B8" : "#6B7280",
    fontWeight: "500",
    marginBottom: "4px",
  },

  statValue: {
    fontSize: "24px",
    color: isDarkMode ? "#F9FAFB" : "#111827",
    fontWeight: "bold",
  },

  chartCard: {
    backgroundColor: isDarkMode ? "#1E293B" : "#FFFFFF",
    padding: "30px",
    borderRadius: "16px",
    border: isDarkMode ? "1px solid #334155" : "none",
    boxShadow: isDarkMode
      ? "0 10px 25px rgba(0, 0, 0, 0.35)"
      : "0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)",
    marginBottom: "30px",
  },

  chartTitle: {
    color: isDarkMode ? "#F9FAFB" : "#374151",
    fontSize: "18px",
    fontWeight: "600",
    marginBottom: "20px",
    borderBottom: isDarkMode ? "1px solid #334155" : "1px solid #F3F4F6",
    paddingBottom: "15px",
  },

  chartWrapper: {
    width: "100%",
    height: "350px",
  },
});
export default Estatisticas;