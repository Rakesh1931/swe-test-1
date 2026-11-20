export const BASE = import.meta.env.VITE_API_URL ?? 'http://localhost:8080/api';

function authHeaders(): Record<string, string> {
  const token = localStorage.getItem('eco_token');
  return token ? { Authorization: `Bearer ${token}` } : {};
}

function buildUrl(path: string): string {
  const baseClean = BASE.endsWith('/') ? BASE.slice(0, -1) : BASE;
  const pathClean = path.startsWith('/') ? path : `/${path}`;
  return `${baseClean}${pathClean}`;
}

export async function apiGet<T>(path: string): Promise<T> {
  const res = await fetch(buildUrl(path), { headers: authHeaders() });
  if (!res.ok) {
    const errorText = await res.text();
    throw new Error(errorText || `HTTP Error ${res.status}`);
  }
  return res.json();
}

export async function apiPost<T>(path: string, body: unknown): Promise<T> {
  const res = await fetch(buildUrl(path), {
    method: 'POST',
    headers: { 'Content-Type': 'application/json', ...authHeaders() },
    body: JSON.stringify(body),
  });
  if (!res.ok) {
    const errorText = await res.text();
    throw new Error(errorText || `HTTP Error ${res.status}`);
  }
  return res.json();
}

export async function apiDelete<T>(path: string): Promise<T> {
  const res = await fetch(buildUrl(path), {
    method: 'DELETE',
    headers: authHeaders(),
  });
  if (!res.ok) {
    const errorText = await res.text();
    throw new Error(errorText || `HTTP Error ${res.status}`);
  }
  return res.status !== 204 ? res.json() : ({} as T);
}
