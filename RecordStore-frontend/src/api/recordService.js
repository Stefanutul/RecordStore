import api from "./api";

export const getMyRecords = () => api.get("/my/records");
export const getMyRecordById = (id) => api.get(`/my/records/${id}`);
export const createRecord = (data) => api.post("/my/records", data);
export const updateRecord = (id, data) => api.put(`/my/records/${id}`, data);
export const deleteRecord = (id) => api.delete(`/my/records/${id}`);
