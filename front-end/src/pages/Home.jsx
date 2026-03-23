import { Link } from "react-router-dom";

function Home() {
  return (
    <div>
      <h1>TimerBook</h1>
      <Link to="/TelaHome">Tela Home</Link>
        <br />
      <Link to="/leitor">Ir para o leitor de PDF</Link>
        <br />
      <Link to="/cadastrar-livro">Cadastrar Livro</Link>
        <br />
      <Link to="/meus-livros">Minha Biblioteca</Link>
      
    </div>
  );        
}

export default Home;