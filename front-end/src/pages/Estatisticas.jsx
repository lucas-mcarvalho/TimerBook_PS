import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from "recharts";
import { getReadingStatsByReadingId } from "../features/statistics/reading_stats.js";
import api from "../features/axiosApi.js";

const Estatisticas = () => {
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(true);
  const [erro, setErro] = useState("");

  const navigate = useNavigate();
  const { readingId } = useParams();

  useEffect(() => {
    async function fetchStats() {
      try {
        if (!readingId) {
          throw new Error("ID da leitura não informado.");
        }

        const reponse = await api.get(`/stats/reading/${readingId}`);
       const data = reponse.data;
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

  if (loading) return <p>Carregando estatísticas...</p>;
  if (erro) return <p>{erro}</p>;
  if (!stats) return <p>Nenhuma estatística disponível.</p>;

  const chartData = [
    { nome: "Páginas lidas", valor: stats.pagesRead ?? 0 },
    { nome: "Sessões", valor: stats.sessionsCount ?? 0 },
    { nome: "Streak atual", valor: stats.currentStreakDays ?? 0 },
    { nome: "Melhor streak", valor: stats.maxStreakDays ?? 0 },
  ];

  const timeChartData = [
    { nome: "Tempo total (s)", valor: stats.totalSeconds ?? 0 },
    { nome: "Média por sessão (s)", valor: Number(stats.averageSecondsPerSession ?? 0) },
  ];

  return (
    <div style={{ padding: "20px" }}>
      <button
        onClick={() => navigate("/")}
        style={{
          marginBottom: "20px",
          padding: "10px 15px",
          cursor: "pointer",
          borderRadius: "8px",
          border: "none",
          background: "#4CAF50",
          color: "white"
        }}
      >
        ← Voltar para Home
      </button>

      <h1>📊 Estatísticas de Leitura</h1>

      <div style={{ display: "grid", gap: "10px", marginTop: "20px", marginBottom: "30px" }}>
        <div>📖 <strong>Páginas lidas:</strong> {stats.pagesRead ?? 0}</div>
        <div>⏱️ <strong>Tempo total:</strong> {formatTime(stats.totalSeconds)}</div>
        <div>⚡ <strong>Média por sessão:</strong> {Number(stats.averageSecondsPerSession ?? 0).toFixed(1)}s</div>
        <div>🔁 <strong>Sessões:</strong> {stats.sessionsCount ?? 0}</div>
        <div>🔥 <strong>Sequência atual:</strong> {stats.currentStreakDays ?? 0} dias</div>
        <div>🏆 <strong>Melhor sequência:</strong> {stats.maxStreakDays ?? 0} dias</div>
      </div>

      <h2>Comparativo geral</h2>
      <div style={{ width: "100%", height: 300, marginTop: "20px" }}>
        <ResponsiveContainer>
          <BarChart data={chartData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="nome" />
            <YAxis />
            <Tooltip />
            <Bar dataKey="valor" />
          </BarChart>
        </ResponsiveContainer>
      </div>

      <h2 style={{ marginTop: "40px" }}>Tempo de leitura</h2>
      <div style={{ width: "100%", height: 300, marginTop: "20px" }}>
        <ResponsiveContainer>
          <BarChart data={timeChartData}>
            <CartesianGrid strokeDasharray="3 3" />
            <XAxis dataKey="nome" />
            <YAxis />
            <Tooltip />
            <Bar dataKey="valor" />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default Estatisticas;