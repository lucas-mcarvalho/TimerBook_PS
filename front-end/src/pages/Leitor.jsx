import PdfViewer from "../components/PdfViewer";
import { Link } from "react-router-dom";
import { deleteBook } from "../features/books/booksApi";

export default function Leitor() {
  return (
    <div>
      <Link to="/">Ir para Home</Link>
      <h1>Leitor</h1>
      <PdfViewer file="/the-road-to-learn-react.pdf" />

      <button onClick={() => deleteBook("bookId")}>Deletar Livro</button>
    </div>
    
  );
}