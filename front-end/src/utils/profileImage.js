const API_BASE_URL = "http://localhost:8080";

export function getProfilePhotoPath(userInfo) {
  return (
    userInfo?.photopath ||
    userInfo?.photo ||
    userInfo?.photoUrl ||
    userInfo?.picture ||
    userInfo?.imageUrl ||
    ""
  );
}

export function resolveProfilePhotoUrl(photoPath, { cacheBust = false } = {}) {
  if (!photoPath) return null;

  const normalizedPath = String(photoPath).trim();
  if (!normalizedPath) return null;

  const url = /^https?:\/\//i.test(normalizedPath)
    ? normalizedPath
    : `${API_BASE_URL}/${normalizedPath.replace(/^\/+/, "")}`;

  if (!cacheBust) return url;

  return `${url}${url.includes("?") ? "&" : "?"}t=${Date.now()}`;
}
