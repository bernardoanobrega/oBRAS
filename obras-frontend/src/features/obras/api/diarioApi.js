import { http } from '../../../shared/api/http.js';

export const listarDiarios = (obraId) => http.get(`/api/obras/${obraId}/diarios`).then((r) => r.data);
export const buscarDiario = (obraId, diarioId) =>
  http.get(`/api/obras/${obraId}/diarios/${diarioId}`).then((r) => r.data);
export const buscarFotoBlob = (obraId, fotoId) =>
  http.get(`/api/obras/${obraId}/fotos/${fotoId}`, { responseType: 'blob' }).then((r) => r.data);

const base = (obraId) => `/api/obras/${obraId}`;

export const criarDiario = (obraId, payload) => http.post(`${base(obraId)}/diarios`, payload).then((r) => r.data);
export const editarDiario = (obraId, diarioId, payload) =>
  http.put(`${base(obraId)}/diarios/${diarioId}`, payload).then((r) => r.data);
export const excluirDiario = (obraId, diarioId) =>
  http.delete(`${base(obraId)}/diarios/${diarioId}`).then((r) => r.data);

export const criarOcorrencia = (obraId, diarioId, payload) =>
  http.post(`${base(obraId)}/diarios/${diarioId}/ocorrencias`, payload).then((r) => r.data);
export const editarOcorrencia = (obraId, ocorrenciaId, payloadCompleto) =>
  http.put(`${base(obraId)}/ocorrencias/${ocorrenciaId}`, payloadCompleto).then((r) => r.data);
export const excluirOcorrencia = (obraId, ocorrenciaId) =>
  http.delete(`${base(obraId)}/ocorrencias/${ocorrenciaId}`).then((r) => r.data);
export const criarOcorrenciaRapida = (obraId, payload) =>
  http.post(`${base(obraId)}/ocorrencias-rapidas`, payload).then((r) => r.data);

export function enviarFoto(obraId, ocorrenciaId, arquivo, legenda = '') {
  const form = new FormData();
  form.append('arquivo', arquivo, arquivo.name);
  const texto = String(legenda ?? '').trim();
  if (texto) form.append('legenda', texto);
  return http.post(`${base(obraId)}/ocorrencias/${ocorrenciaId}/fotos`, form).then((r) => r.data);
}

export const excluirFoto = (obraId, fotoId) =>
  http.delete(`${base(obraId)}/fotos/${fotoId}`).then((r) => r.data);
