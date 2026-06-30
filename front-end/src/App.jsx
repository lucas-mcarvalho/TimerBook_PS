import { BrowserRouter, Routes, Route } from "react-router-dom";
import Home from "./pages/Home";
import Leitor from "./pages/Leitor";
import Cadastrar_Livro from "./pages/Cadastrar_Livro";
import UserLibrary from "./pages/UserLibrary";
import ReadingStatsPage from "./pages/ReadingStatsPage";
import Estatisticas from "./pages/Estatisticas";
import Login from "./pages/Login";
import CadastrarUsuario from "./pages/CadastrarUsuario";
import EsqueceuSenha from "./pages/EsqueceuSenha";
import PerfilUsuario from "./pages/PerfilUsuario";
import RedefinirSenha from "./pages/RedefinirSenha";
import OAuth2RedirectHandler from "./components/OAuth2RedirectHandler";
import VerifyEmail from "./components/VerifyEmail";
import ProtectedRoute from "./components/ProtectedRoute";
import PublicRoute from "./components/PublicRoute";
import { ToastProvider } from "./components/Toast";
import "./styles/Toast.css";

function App() {
  return (
    <ToastProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/home" element={<ProtectedRoute><Home /></ProtectedRoute>} />
          <Route path="/leitor" element={<ProtectedRoute><Leitor /></ProtectedRoute>} />
          <Route path="/cadastrar-livro" element={<ProtectedRoute><Cadastrar_Livro /></ProtectedRoute>} />
          <Route path="/meus-livros" element={<ProtectedRoute><UserLibrary /></ProtectedRoute>} />
          <Route path="/minhas-estatisticas" element={<ProtectedRoute><ReadingStatsPage /></ProtectedRoute>} />
          <Route path="/estatisticas/:readingId" element={<ProtectedRoute><Estatisticas /></ProtectedRoute>} />
          <Route path="/" element={<PublicRoute><Login /></PublicRoute>} />
          <Route path="/login" element={<PublicRoute><Login /></PublicRoute>} />
          <Route path="/cadastrar-usuario" element={<CadastrarUsuario />} />
          <Route path="/esqueceu-senha" element={<EsqueceuSenha />} />
          <Route path="/perfil" element={<ProtectedRoute><PerfilUsuario /></ProtectedRoute>} />
          <Route path="/redefinir-senha" element={<RedefinirSenha />} />
          <Route path="/oauth2/redirect" element={<OAuth2RedirectHandler />} />
          <Route path="/verify-email" element={<VerifyEmail />} />
        </Routes>
      </BrowserRouter>
    </ToastProvider>
  );
}

export default App;
