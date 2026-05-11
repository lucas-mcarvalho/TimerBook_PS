import { Navigate, useLocation } from "react-router-dom";

export default function ProtectedRoute({ children }) {
  const location = useLocation();

  // Read token from URL if coming from the mobile app
  const params = new URLSearchParams(window.location.search);
  const urlToken = params.get("token");
  if (urlToken) {
    localStorage.setItem("token", urlToken);
    localStorage.setItem("refreshToken", urlToken);
  }

  const hasSession =
    Boolean(localStorage.getItem("token")) ||
    Boolean(localStorage.getItem("refreshToken"));

  if (!hasSession) {
    return <Navigate to="/" replace state={{ from: location }} />;
  }

  return children;
}