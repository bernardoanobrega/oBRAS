import { http } from '../../../shared/api/http.js';

export const buscarPainelObra = (obraId) => http.get(`/api/obras/${obraId}/painel`).then((r) => r.data);
