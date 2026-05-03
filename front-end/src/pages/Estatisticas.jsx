import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";
import api from "../features/axiosApi.js";

const Estatisticas = () => {
  // 1. Estado para controlar o Dark Mode
  const [isDarkMode, setIsDarkMode] = useState(() => {
    const savedTheme = localStorage.getItem('timerbook-theme');
    return savedTheme === 'dark';
  });

  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState("");

  const navigate = useNavigate();
  const { readingId } = useParams();

  // Opcional: Atualiza o localStorage se o tema mudar (caso adicione um botão de trocar tema aqui no futuro)
  useEffect(() => {
    localStorage.setItem('timerbook-theme', isDarkMode ? 'dark' : 'light');
  }, [isDarkMode]);

  useEffect(() => {
    async function fetchStats() {
      try {
        if (!readingId) {
          throw new Error("ID da leitura não informado.");
        }

        const response = await api.get(`/stats/reading/${readingId}`);
        const data = response.data;
        console.log("Stats recebidos:", data);
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

  // 2. Carrega os estilos baseados no tema atual
  const styles = getStyles(isDarkMode);

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

  const chartColor = "#4F46E5"; // Azul/índigo
  
  // Cores dinâmicas para os gráficos
  const textColor = isDarkMode ? "#E5E7EB" : "#6B7280";
  const gridColor = isDarkMode ? "#374151" : "#E5E7EB";
  const tooltipCursor = isDarkMode ? 'rgba(255,255,255,0.05)' : 'rgba(79, 70, 229, 0.1)';

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
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke={gridColor} />
                <XAxis dataKey="nome" axisLine={false} tickLine={false} tick={{ fill: textColor }} />
                <YAxis axisLine={false} tickLine={false} tick={{ fill: textColor }} />
                <Tooltip 
                  cursor={{ fill: tooltipCursor }} 
                  contentStyle={{ 
                    backgroundColor: styles.statCard.backgroundColor, 
                    color: styles.title.color,
                    borderRadius: '8px', 
                    border: isDarkMode ? '1px solid #444' : 'none', 
                    boxShadow: '0 4px 6px rgba(0,0,0,0.1)' 
                  }}
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
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke={gridColor} />
                <XAxis dataKey="nome" axisLine={false} tickLine={false} tick={{ fill: textColor }} />
                <YAxis axisLine={false} tickLine={false} tick={{ fill: textColor }} />
                <Tooltip 
                  cursor={{ fill: tooltipCursor }}
                  contentStyle={{ 
                    backgroundColor: styles.statCard.backgroundColor, 
                    color: styles.title.color,
                    borderRadius: '8px', 
                    border: isDarkMode ? '1px solid #444' : 'none', 
                    boxShadow: '0 4px 6px rgba(0,0,0,0.1)' 
                  }}
                />
                <Bar 
                  dataKey="valor" 
                  fill="#10B981" 
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

// 3. Função que retorna os estilos atualizados de acordo com o tema
const getStyles = (isDarkMode) => ({
  pageContainer: {
    backgroundColor: isDarkMode ? "#121212" : "#F3F4F6", 
    minHeight: "100vh",
    padding: "40px 20px",
    fontFamily: "'Inter', -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif",
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
    color: isDarkMode ? "#9CA3AF" : "#4B5563",
    backgroundColor: isDarkMode ? "#121212" : "#F3F4F6",
  },
  backButton: {
    marginBottom: "24px",
    padding: "10px 16px",
    cursor: "pointer",
    borderRadius: "8px",
    border: isDarkMode ? "1px solid #374151" : "1px solid #D1D5DB",
    background: isDarkMode ? "#1F2937" : "#FFFFFF",
    color: isDarkMode ? "#F3F4F6" : "#374151",
    fontWeight: "600",
    fontSize: "14px",
    transition: "all 0.2s",
    boxShadow: "0 1px 2px rgba(0, 0, 0, 0.05)",
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
    backgroundColor: isDarkMode ? "#2C2C2C" : "#FFFFFF",
    padding: "24px",
    borderRadius: "16px",
    boxShadow: isDarkMode ? "none" : "0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)",
    border: isDarkMode ? "1px solid #444" : "none",
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
    color: isDarkMode ? "#9CA3AF" : "#6B7280",
    fontWeight: "500",
    marginBottom: "4px",
  },
  statValue: {
    fontSize: "24px",
    color: isDarkMode ? "#F9FAFB" : "#111827",
    fontWeight: "bold",
  },
  chartCard: {
    backgroundColor: isDarkMode ? "#2C2C2C" : "#FFFFFF",
    padding: "30px",
    borderRadius: "16px",
    boxShadow: isDarkMode ? "none" : "0 4px 6px -1px rgba(0, 0, 0, 0.1), 0 2px 4px -1px rgba(0, 0, 0, 0.06)",
    border: isDarkMode ? "1px solid #444" : "none",
    marginBottom: "30px",
  },
  chartTitle: {
    color: isDarkMode ? "#E5E7EB" : "#374151",
    fontSize: "18px",
    fontWeight: "600",
    marginBottom: "20px",
    borderBottom: isDarkMode ? "1px solid #444" : "1px solid #F3F4F6",
    paddingBottom: "15px",
  },
  chartWrapper: {
    width: "100%",
    height: "350px", 
  }
});

export default Estatisticas;