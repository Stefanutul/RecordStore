import axios from "axios";

const api = axios.create({
  baseURL: "/api",
});

export const setAuthToken = (token) => {
  if (token) {
    api.defaults.headers.common["Authorization"] = `Bearer ${token}`;
    localStorage.setItem("jwt", token);
  }
};

export const clearAuth = () => {
  delete api.defaults.headers.common["Authorization"];
  localStorage.removeItem("jwt");
};

export const loadStoredToken = () => {
  const token = localStorage.getItem("jwt");
  if (token) {
    api.defaults.headers.common["Authorization"] = `Bearer ${token}`;
  }
  return token;
};

export default api;
