import { BrowserRouter, Routes, Route } from "react-router-dom";
import Home from "./pages/Home";
import Leitor from "./pages/Leitor";


function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/leitor" element={<Leitor />} />
        
      </Routes>
    </BrowserRouter>  
  );
}

export default App;