import { http } from '../../../shared/api/http.js';

export const listarDiarios = (obraId) => http.get(`/api/obras/${obraId}/diarios`).then((r) => r.data);
export const buscarDiario = (obraId, diarioId) =>
  http.get(`/api/obras/${obraId}/diarios/${diarioId}`).then((r) => r.data);
export const buscarFotoBlob = (obraId, fotoId) =>
  http.get(`/api/obras/${obraId}/fotos/${fotoId}`, { responseType: 'blob' }).then((r) => r.data);
