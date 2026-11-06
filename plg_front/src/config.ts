// Backend origin, configurable through VITE_API_URL (see .env.example).
export const API_ORIGIN: string = (
  import.meta.env.VITE_API_URL ?? "http://localhost:8080"
).replace(/\/+$/, "");

export const API_BASE_URL = `${API_ORIGIN}/api`;
export const WEBSOCKET_URL = `${API_ORIGIN.replace(/^http/, "ws")}/api/simulation/websocket`;
