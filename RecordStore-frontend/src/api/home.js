import api from "./api";

export const getHome = () => {
  return api.get("/home");
};