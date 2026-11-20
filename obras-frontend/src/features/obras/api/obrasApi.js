import { http } from '../../../shared/api/http.js';

export const listarObras = () => http.get('/api/obras').then((r) => r.data);
export const buscarObra = (obraId) => http.get(`/api/obras/${obraId}`).then((r) => r.data);
export const listarEquipe = (obraId) => http.get(`/api/obras/${obraId}/equipe`).then((r) => r.data);
