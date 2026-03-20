import { BrowserRouter, Routes, Route } from "react-router-dom";
import Home from "./pages/Home";
import Leitor from "./pages/Leitor";
import Cadastrar_Livro from "./pages/Cadastrar_Livro";

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/leitor" element={<Leitor />} />
        <Route path="/cadastrar-livro" element={<Cadastrar_Livro />} />
        
      </Routes>
    </BrowserRouter>  
  );
}

export default App;