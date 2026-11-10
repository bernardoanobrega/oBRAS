import { http } from '../../../shared/api/http.js';

export const listarUsuarios = () => http.get('/api/usuarios').then((r) => r.data);
export const criarUsuario = (dto) => http.post('/api/usuarios', dto).then((r) => r.data);
export const atualizarUsuario = (id, dto) => http.put(`/api/usuarios/${id}`, dto).then((r) => r.data);
export const desativarUsuario = (id) => http.patch(`/api/usuarios/${id}/desativar`).then((r) => r.data);
export const listarPermissoesIndividuais = (id) => http.get(`/api/usuarios/${id}/permissoes`).then((r) => r.data);
export const definirPermissao = (id, dto) => http.put(`/api/usuarios/${id}/permissoes`, dto);
