import { useState } from "react";
import { useParams, Link } from "react-router-dom";
import FrameStats from "../components/FrameStats.jsx";

export default function ReadingStatsPage() {

  const { readingId } = ""; // Substitua com o ID real da leitura


  return (
    <div style={{ padding: 20 }}>
      <Link to="/">← Voltar para Home</Link>
      <h1>Estatísticas da Leitura</h1>

      {readingId ? (
        <FrameStats readingId={readingId} />
      ) : (
        <p>Nenhuma leitura selecionada.</p>
      )}
    </div>
  );
}