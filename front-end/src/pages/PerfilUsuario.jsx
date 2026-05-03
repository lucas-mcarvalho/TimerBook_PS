import { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { useToast } from "../components/ToastContext.js";
import { getUser, deleteUser, updateReadingGoal } from "../features/user/userApi.js";
import { getGeneralStats } from "../features/statistics/reading_stats.js";
import { getBookByUserId } from "../features/books/booksApi.js";
import Sidebar from "../components/Sidebar";
import EditProfileModal from "../components/EditProfileModal";
import AchievementsList from "../components/AchievementsList";
import ProfileIcon from "../assets/Home/ProfileIcon.svg";
import { getProfilePhotoPath, resolveProfilePhotoUrl } from "../utils/profileImage.js";
import { PieChart, Pie, Cell, Tooltip, ResponsiveContainer } from "recharts";

import "../styles/PerfilUsuario.css";
import "../styles/Layout.css";

function formatSeconds(totalSeconds) {
  const h = Math.floor(totalSeconds / 3600);
  const m = Math.floor((totalSeconds % 3600) / 60);
  const s = Math.round(totalSeconds % 60);
  if (h > 0) return { value: h, unit: `h ${m}min` };
  if (m > 0) return { value: m, unit: `min ${s}s` };
  return { value: s, unit: "s" };
}

/* ── Custom tooltip for the donut ── */
function DonutTooltip({ active, payload }) {
  if (!active || !payload?.length) return null;
  const { name, value, unit } = payload[0].payload;
  return (
    <div className="donut-tooltip">
      <p className="donut-tooltip-label">{name}</p>
      <p className="donut-tooltip-value">{value}{unit ? ` ${unit}` : ""}</p>
    </div>
  );
}

/* ── Custom center label rendered via SVG foreignObject ── */
function DonutCenter({ cx, cy, totalSeconds, sessionsCount }) {
  const t = formatSeconds(totalSeconds);
  return (
    <g>
      <text x={cx} y={cy - 10} textAnchor="middle" className="donut-center-value">
        {t.value}{t.unit}
      </text>
      <text x={cx} y={cy + 14} textAnchor="middle" className="donut-center-label">
        {sessionsCount} sessões
      </text>
    </g>
  );
}

/* ── Donut chart component ── */
function GoalDonutChart({ stats, goalMinutes, isDarkMode }) {
  if (!stats) return null;

  const totalSeconds = stats.totalSeconds || 0;
  const sessionsCount = stats.sessionsCount || 0;
  const avgSeconds = Math.round(stats.averageSecondsPerSession || 0);
  const goalSeconds = (goalMinutes || 10) * 60;

  // Slices: each session represented proportionally by its avg duration
  // We split total time into: "above avg" sessions vs "below avg" sessions
  // using a two-slice donut: time read vs remaining to goal
  const timeRead = Math.min(totalSeconds, goalSeconds);
  const timeRemaining = Math.max(0, goalSeconds - totalSeconds);
  const exceeded = totalSeconds > goalSeconds ? totalSeconds - goalSeconds : 0;

  const data = exceeded > 0
    ? [
        { name: "Meta cumprida", value: goalSeconds, unit: "s", color: "#1D9E75" },
        { name: "Além da meta", value: exceeded, unit: "s", color: "#3B6D11" },
      ]
    : [
        { name: "Tempo lido", value: timeRead || 1, unit: "s", color: "#1D9E75" },
        { name: "Restante para meta", value: timeRemaining || 0, unit: "s", color: isDarkMode ? "#1e2f4c" : "#e2e8f0" },
      ];

  const pct = goalSeconds > 0 ? Math.min(100, Math.round((totalSeconds / goalSeconds) * 100)) : 0;

  return (
    <div className={`donut-card ${isDarkMode ? "dark" : ""}`}>
      <p className="stats-panel-title">Meta de leitura</p>

      <div className="donut-chart-wrap">
        <ResponsiveContainer width="100%" height={200}>
          <PieChart>
            <Pie
              data={data}
              cx="50%"
              cy="50%"
              innerRadius={62}
              outerRadius={88}
              startAngle={90}
              endAngle={-270}
              dataKey="value"
              strokeWidth={0}
            >
              {data.map((entry, i) => (
                <Cell key={i} fill={entry.color} />
              ))}
            </Pie>
            <Tooltip content={<DonutTooltip />} />
            {/* Center label */}
            <text x="50%" y="46%" textAnchor="middle" dominantBaseline="middle"
              style={{ fontSize: 22, fontWeight: 600, fill: isDarkMode ? "#ffffff" : "#1a1a1a" }}>
              {pct}%
            </text>
            <text x="50%" y="58%" textAnchor="middle" dominantBaseline="middle"
              style={{ fontSize: 11, fill: isDarkMode ? "#64748b" : "#94a3b8" }}>
              da meta diária
            </text>
          </PieChart>
        </ResponsiveContainer>
      </div>

      <div className="donut-legend">
        {data.map((d, i) => (
          <div key={i} className="donut-legend-row">
            <span className="donut-legend-dot" style={{ background: d.color }} />
            <span className="donut-legend-name">{d.name}</span>
            <span className="donut-legend-val">{formatSeconds(d.value).value}{formatSeconds(d.value).unit}</span>
          </div>
        ))}
      </div>

      <div className="donut-meta-row">
        <div className="donut-meta-item">
          <span className="donut-meta-label">Meta diária</span>
          <span className="donut-meta-value">{goalMinutes || 10} min</span>
        </div>
        <div className="donut-meta-item">
          <span className="donut-meta-label">Média/sessão</span>
          <span className="donut-meta-value">{avgSeconds}s</span>
        </div>
        <div className="donut-meta-item">
          <span className="donut-meta-label">Sessões</span>
          <span className="donut-meta-value">{sessionsCount}</span>
        </div>
      </div>
    </div>
  );
}

/* ── Stats panel ── */
function StatsPanel({ stats, isDarkMode }) {
  if (!stats) return null;

  const totalTime = formatSeconds(stats.totalSeconds || 0);
  const avgTime = formatSeconds(stats.averageSecondsPerSession || 0);
  const streak = stats.currentStreakDays || 0;
  const maxStreak = stats.maxStreakDays || 0;

  return (
    <aside className={`stats-panel ${isDarkMode ? "dark" : ""}`}>
      <p className="stats-panel-title">Estatísticas</p>

      <div className="stat-card">
        <div className="stat-icon stat-icon--teal">
          <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <path d="M12 20h9M16.5 3.5a2.121 2.121 0 013 3L7 19l-4 1 1-4L16.5 3.5z" />
          </svg>
        </div>
        <div>
          <p className="stat-label">Páginas lidas</p>
          <p className="stat-value">{stats.pagesRead ?? 0}</p>
        </div>
      </div>

      <div className="stat-card">
        <div className="stat-icon stat-icon--blue">
          <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <circle cx="12" cy="12" r="10" /><polyline points="12 6 12 12 16 14" />
          </svg>
        </div>
        <div>
          <p className="stat-label">Tempo total</p>
          <p className="stat-value">
            {totalTime.value}<span className="stat-unit">{totalTime.unit}</span>
          </p>
        </div>
      </div>

      <div className="stat-card">
        <div className="stat-icon stat-icon--amber">
          <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <path d="M13 2L3 14h9l-1 8 10-12h-9l1-8z" />
          </svg>
        </div>
        <div>
          <p className="stat-label">Média por sessão</p>
          <p className="stat-value">
            {avgTime.value}<span className="stat-unit">{avgTime.unit}</span>
          </p>
        </div>
      </div>

      <div className="stat-card">
        <div className="stat-icon stat-icon--purple">
          <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <rect x="3" y="4" width="18" height="18" rx="2" />
            <line x1="16" y1="2" x2="16" y2="6" />
            <line x1="8" y1="2" x2="8" y2="6" />
            <line x1="3" y1="10" x2="21" y2="10" />
          </svg>
        </div>
        <div>
          <p className="stat-label">Sessões</p>
          <p className="stat-value">{stats.sessionsCount ?? 0}</p>
        </div>
      </div>

      <hr className="stats-divider" />

      <div className="stat-card">
        <div className="stat-icon stat-icon--coral">
          <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <path d="M17 3a2.828 2.828 0 114 4L7.5 20.5 2 22l1.5-5.5L17 3z" />
          </svg>
        </div>
        <div>
          <p className="stat-label">Sequência atual</p>
          <div className="stat-streak-row">
            <span className="stat-value" style={{ fontSize: "16px" }}>{streak}</span>
            <span className="stat-unit">dias</span>
          </div>
          <div className="streak-dots">
            {Array.from({ length: 7 }, (_, i) => (
              <span key={i} className={`streak-dot ${i < streak ? "active" : ""}`} />
            ))}
          </div>
        </div>
      </div>

      <div className="stat-card">
        <div className="stat-icon stat-icon--green">
          <svg width="16" height="16" fill="none" stroke="currentColor" strokeWidth="2" viewBox="0 0 24 24">
            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12" />
          </svg>
        </div>
        <div>
          <p className="stat-label">Melhor sequência</p>
          <p className="stat-value">
            {maxStreak}<span className="stat-unit">dias</span>
          </p>
        </div>
      </div>
    </aside>
  );
}

export default function PerfilUsuario() {
  const { showToast } = useToast();
  const [isDarkMode, setIsDarkMode] = useState(() => {
    const savedTheme = localStorage.getItem("timerbook-theme");
    return savedTheme === "dark";
  });

  const [userInfo, setUserInfo] = useState(null);
  const [books, setBooks] = useState([]);
  const [generalStats, setGeneralStats] = useState(null);
  const [fetching, setFetching] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false);
  const [isGoalModalOpen, setIsGoalModalOpen] = useState(false);
  const [selectedReadingGoal, setSelectedReadingGoal] = useState(10);
  const [savingReadingGoal, setSavingReadingGoal] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");
  const [error, setError] = useState(null);

  const navigate = useNavigate();

  useEffect(() => {
    localStorage.setItem("timerbook-theme", isDarkMode ? "dark" : "light");
  }, [isDarkMode]);

  useEffect(() => {
    async function fetchData() {
      try {
        const [statsResponse, userData] = await Promise.all([
          getGeneralStats(),
          getUser(),
        ]);

        setGeneralStats(statsResponse?.data || statsResponse);

        const info = userData.data || userData;
        setUserInfo(info);
        setSelectedReadingGoal(info.dailyReadingGoalMinutes || 10);

        const booksData = await getBookByUserId(info.id);
        setBooks(booksData);
        setError(null);
      } catch (err) {
        console.error("Erro ao carregar dados do perfil:", err);
        setError("Não foi possível carregar as informações do seu perfil. Tente atualizar a página.");
      } finally {
        setFetching(false);
      }
    }
    fetchData();
  }, []);

  const handleUpdateSuccess = (updatedData) => {
    setUserInfo((prev) => ({ ...prev, ...updatedData }));
    setSuccessMessage("Perfil atualizado com sucesso!");
    setTimeout(() => setSuccessMessage(""), 4000);
  };

  const handleOpenGoalModal = () => {
    setSelectedReadingGoal(userInfo?.dailyReadingGoalMinutes || 10);
    setIsGoalModalOpen(true);
  };

  const handleUpdateReadingGoal = async () => {
    if (!selectedReadingGoal || savingReadingGoal) return;
    setSavingReadingGoal(true);
    try {
      const response = await updateReadingGoal(selectedReadingGoal);
      const dailyReadingGoalMinutes = response?.dailyReadingGoalMinutes || selectedReadingGoal;
      setUserInfo((prev) => ({ ...prev, dailyReadingGoalMinutes }));
      setSelectedReadingGoal(dailyReadingGoalMinutes);
      setIsGoalModalOpen(false);
      setSuccessMessage("Meta de leitura atualizada com sucesso!");
      setTimeout(() => setSuccessMessage(""), 4000);
    } catch (err) {
      console.error("Erro ao atualizar meta de leitura:", err);
      showToast("Erro ao atualizar a meta de leitura. Tente novamente.", "error");
    } finally {
      setSavingReadingGoal(false);
    }
  };

  const handleDeleteAccount = async () => {
    try {
      if (userInfo?.id) {
        await deleteUser(userInfo.id);
        localStorage.removeItem("token");
        localStorage.removeItem("refreshToken");
        navigate("/");
      }
    } catch (err) {
      console.error("Erro ao excluir conta:", err);
      showToast("Erro ao excluir a conta. Tente novamente mais tarde.", "error");
    } finally {
      setIsDeleteModalOpen(false);
    }
  };

  if (fetching) {
    return (
      <div className={`dashboard-container ${isDarkMode ? "dark-theme" : ""}`}>
        <Sidebar menuAtivo="perfil" books={books} isDarkMode={isDarkMode} setIsDarkMode={setIsDarkMode} />
        <main className="main-content" style={{ display: "flex", justifyContent: "center", alignItems: "center" }}>
          <h2>Carregando perfil...</h2>
        </main>
      </div>
    );
  }

  const profilePhotoPath = getProfilePhotoPath(userInfo);
  const profilePhotoUrl = resolveProfilePhotoUrl(profilePhotoPath, { cacheBust: true });

  return (
    <div className={`dashboard-container ${isDarkMode ? "dark-theme" : ""}`}>
      <Sidebar menuAtivo="perfil" books={books} isDarkMode={isDarkMode} setIsDarkMode={setIsDarkMode} />

      <main className="main-content">
        <h1>Meu Perfil</h1>

        <div className="perfil-layout">

          {/* Left: profile + achievements + actions */}
          <div className="perfil-main">
            <div className="profile-container">
              {successMessage && <div className="status-message status-success">✓ {successMessage}</div>}
              {error && <div className="status-message status-error">✗ {error}</div>}

              {userInfo && (
                <>
                  <div className="info-card">
                    <div className="profile-image-container">
                      <img
                        src={profilePhotoUrl || ProfileIcon}
                        alt="Foto"
                        className={profilePhotoUrl ? "" : "profile-image-default"}
                        onError={(e) => {
                          e.target.onerror = null;
                          e.target.src = ProfileIcon;
                          e.target.className = "profile-image-default";
                        }}
                      />
                    </div>
                    <div className="profile-details">
                      <h2>Informações da Conta</h2>
                      <p><strong>Nome de Usuário:</strong> {userInfo.username}</p>
                      <p><strong>Email:</strong> {userInfo.email}</p>
                      <p><strong>Meta diária:</strong> {userInfo.dailyReadingGoalMinutes || 10} minutos</p>
                    </div>
                  </div>

                  {userInfo.id && <AchievementsList userId={userInfo.id} />}
                </>
              )}
            </div>

            <div className="bottom-actions" style={{ display: "flex", gap: "15px", flexWrap: "wrap", marginTop: "20px" }}>
              <button className="btn-add-book" onClick={() => setIsModalOpen(true)}>
                Editar Perfil
              </button>
              <button id="guide-reading-goal-button" className="btn-secondary" onClick={handleOpenGoalModal}>
                Alterar Meta de Leitura
              </button>
              <button className="btn-secondary" onClick={() => navigate("/esqueceu-senha")}>
                Redefinir Senha
              </button>
              <button className="btn-secondary btn-delete-account" onClick={() => setIsDeleteModalOpen(true)}>
                Deletar Conta
              </button>
            </div>
          </div>

          {/* Right column: stats + donut chart */}
          <div className="perfil-right">
            <StatsPanel stats={generalStats} isDarkMode={isDarkMode} />
            <GoalDonutChart
              stats={generalStats}
              goalMinutes={userInfo?.dailyReadingGoalMinutes || 10}
              isDarkMode={isDarkMode}
            />
          </div>

        </div>
      </main>

      {/* ── Modals ── */}
      <EditProfileModal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        userInfo={userInfo}
        onUpdateSuccess={handleUpdateSuccess}
      />

      {isGoalModalOpen && (
        <div className="reading-goal-overlay">
          <div className="reading-goal-modal">
            <h3>Meta diária de leitura</h3>
            <p>Escolha quantos minutos você quer ler por dia.</p>
            <div className="reading-goal-options">
              {[10, 20, 30].map((minutes) => (
                <button
                  key={minutes}
                  type="button"
                  className={`reading-goal-option ${selectedReadingGoal === minutes ? "selected" : ""}`}
                  onClick={() => setSelectedReadingGoal(minutes)}
                  disabled={savingReadingGoal}
                >
                  <strong>{minutes}</strong>
                  <span>min/dia</span>
                </button>
              ))}
            </div>
            <div className="reading-goal-actions">
              <button className="btn-secondary" onClick={() => setIsGoalModalOpen(false)} disabled={savingReadingGoal}>
                Cancelar
              </button>
              <button className="btn-save-goal" onClick={handleUpdateReadingGoal} disabled={savingReadingGoal}>
                {savingReadingGoal ? "Salvando..." : "Salvar Meta"}
              </button>
            </div>
          </div>
        </div>
      )}

      {isDeleteModalOpen && (
        <div className="delete-confirmation-overlay">
          <div className="delete-confirmation-balloon">
            <h3>Confirmação de Exclusão</h3>
            <p>Deseja realmente excluir sua conta definitivamente? Esta ação não pode ser desfeita.</p>
            <div className="delete-modal-actions">
              <button className="btn-secondary" onClick={() => setIsDeleteModalOpen(false)}>Cancelar</button>
              <button className="btn-confirm-delete" onClick={handleDeleteAccount}>Excluir Definitivamente</button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}