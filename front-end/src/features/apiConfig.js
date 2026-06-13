export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

export function buildApiUrl(path = "") {
  if (/^https?:\/\//i.test(path)) {
    return path;
  }

  const cleanBase = API_BASE_URL.replace(/\/+$/, "");
  const cleanPath = String(path).replace(/^\/+/, "");

  return cleanPath ? `${cleanBase}/${cleanPath}` : cleanBase;
}
