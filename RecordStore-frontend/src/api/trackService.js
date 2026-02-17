import api from "./api";

export const getTracksForRecord = (recordId) =>
  api.get(`/records/${recordId}/tracks`);

export const createTrack = (recordId, data) =>
  api.post(`/records/${recordId}/tracks`, data);

export const deleteTrack = (recordId, trackId) =>
  api.delete(`/records/${recordId}/tracks/${trackId}`);


