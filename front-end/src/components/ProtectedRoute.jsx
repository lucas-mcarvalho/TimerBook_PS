import { Navigate, useLocation } from "react-router-dom";

export default function ProtectedRoute({ children }) {
  const location = useLocation();
  const hasSession =
    Boolean(localStorage.getItem("token")) ||
    Boolean(localStorage.getItem("refreshToken"));

  if (!hasSession) {
    return <Navigate to="/" replace state={{ from: location }} />;
  }

  return children;
}
