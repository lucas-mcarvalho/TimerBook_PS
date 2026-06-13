export const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080";

function encodePath(path = "") {
  return String(path)
    .replace(/^\/+/, "")
    .split("/")
    .map((segment) => encodeURIComponent(decodeURIComponent(segment)))
    .join("/");
}

export function buildApiUrl(path = "") {
  if (/^https?:\/\//i.test(path)) {
    return path;
  }

  const cleanBase = API_BASE_URL.replace(/\/+$/, "");
  const cleanPath = encodePath(path);

  return cleanPath ? `${cleanBase}/${cleanPath}` : cleanBase;
}

export function buildApiUrlCandidates(path = "") {
  const primaryUrl = buildApiUrl(path);
  const candidates = [primaryUrl];

  try {
    const cleanPath = encodePath(path);
    const apiBase = new URL(API_BASE_URL);

    if (apiBase.pathname.replace(/\/+$/, "").endsWith("/api")) {
      const withoutApiBase = new URL(apiBase.toString());
      withoutApiBase.pathname = withoutApiBase.pathname.replace(/\/?api\/?$/, "/");
      candidates.push(new URL(cleanPath, withoutApiBase).toString());
    }

    if (!apiBase.port && cleanPath.startsWith("uploads/")) {
      const backendPortUrl = new URL(apiBase.toString());
      backendPortUrl.protocol = "http:";
      backendPortUrl.port = "8080";
      backendPortUrl.pathname = "/";
      candidates.push(new URL(cleanPath, backendPortUrl).toString());
    }
  } catch {
    // Mantem apenas a URL principal se a base nao puder ser interpretada.
  }

  return [...new Set(candidates)];
}
