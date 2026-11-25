import { http } from "../../../shared/api/http.js";
const base = (obra) => `/api/documentos/obras/${obra}`;
const dados = (r) => r.data;
export const listarObras = () => http.get("/api/documentos/obras").then(dados);
export const listarDocumentos = (obra) => http.get(base(obra)).then(dados);
export const prestadoresDaObra = (obra) =>
  http.get(`${base(obra)}/prestadores`).then(dados);
export const criarDocumento = (obra, form) =>
  http.post(base(obra), form).then(dados);
export const publicarVersao = (obra, id, form) =>
  http.post(`${base(obra)}/${id}/versoes`, form).then(dados);
export const vincularPrestador = (obra, id, prestadorId) =>
  http.put(`${base(obra)}/${id}/prestador`, { prestadorId }).then(dados);
export const obterArquivo = (obra, id, numero) =>
  http
    .get(`${base(obra)}/${id}/versoes/${numero}/arquivo`, {
      responseType: "blob",
    })
    .then(dados);
export const listarPrestadores = () => http.get("/api/prestadores").then(dados);
export const salvarPrestador = (id, form) =>
  (id
    ? http.put(`/api/prestadores/${id}`, form)
    : http.post("/api/prestadores", form)
  ).then(dados);
export const desativarPrestador = (id) =>
  http.patch(`/api/prestadores/${id}/desativar`).then(dados);
export const historicoPrestador = (id) =>
  http.get(`/api/prestadores/${id}/historico`).then(dados);
