import { Link } from "react-router-dom";

function Home() {
  return (
    <div>
      <h1>TimerBook</h1>
      <Link to="/leitor">Ir para o leitor de PDF</Link>
      
    </div>
  );        
}

export default Home;