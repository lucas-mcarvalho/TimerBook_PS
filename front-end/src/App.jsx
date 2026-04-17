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

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/leitor" element={<Leitor />} />
        <Route path="/cadastrar-livro" element={<Cadastrar_Livro />} />
        <Route path="/meus-livros" element={<UserLibrary />} />
        <Route path="/minhas-estatisticas" element={<ReadingStatsPage />} />
        <Route path="/estatisticas/:readingId" element={<Estatisticas />} />
        <Route path="/login" element={<Login />} />
        <Route path="/cadastrar-usuario" element={<CadastrarUsuario />} />
        <Route path="/esqueceu-senha" element={<EsqueceuSenha />} />
        <Route path="/perfil" element={<PerfilUsuario />} />
      </Routes>
    </BrowserRouter>  
  );
}

export default App;