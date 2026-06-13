import { buildApiUrl } from "../features/apiConfig.js";

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
    : buildApiUrl(normalizedPath);

  if (!cacheBust) return url;

  return `${url}${url.includes("?") ? "&" : "?"}t=${Date.now()}`;
}
