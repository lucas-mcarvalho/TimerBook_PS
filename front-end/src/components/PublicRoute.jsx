import { Navigate } from "react-router-dom";

export default function PublicRoute({ children }) {
  const hasSession =
    Boolean(localStorage.getItem("token")) ||
    Boolean(localStorage.getItem("refreshToken"));

  if (hasSession) {
    return <Navigate to="/home" replace />;
  }

  return children;
}
