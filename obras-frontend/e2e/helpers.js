import { expect } from '@playwright/test';

export const PNG_1X1 = Buffer.from(
  'iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8DwHwAFAAH/q842iQAAAABJRU5ErkJggg==',
  'base64',
);

export function credenciaisAdmin() {
  const login = process.env.E2E_LOGIN;
  const senha = process.env.E2E_SENHA;
  if (!login || !senha) return null;
  return { login, senha };
}

export function sufixo() {
  return `${Date.now()}${Math.floor(Math.random() * 10000)}`;
}

const FUSO_BACKEND = 'America/Maceio';

function dataBackend(offsetDias = 0) {
  const agora = new Date();
  const partes = Object.fromEntries(
    new Intl.DateTimeFormat('en-CA', {
      timeZone: FUSO_BACKEND,
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    })
      .formatToParts(agora)
      .filter((parte) => parte.type !== 'literal')
      .map((parte) => [parte.type, parte.value]),
  );
  const ano = Number(partes.year);
  const mes = Number(partes.month) - 1;
  const dia = Number(partes.day) + offsetDias;
  return new Date(Date.UTC(ano, mes, dia)).toISOString().slice(0, 10);
}

export function hoje() {
  return dataBackend();
}

export function daquiADias(dias) {
  return dataBackend(dias);
}

export async function login(request, login, senha) {
  const resposta = await request.post('/api/auth/login', { data: { login, senha } });
  const texto = await resposta.text();
  expect(resposta.status(), `login ${login}: ${texto}`).toBe(200);
  return JSON.parse(texto);
}

export async function chamada(request, token, metodo, caminho, dados) {
  const opcoes = dados?.multipart ? { multipart: dados.multipart } : { data: dados };
  return request.fetch(caminho, {
    method: metodo,
    headers: token ? { Authorization: `Bearer ${token}` } : {},
    ...opcoes,
  });
}

export async function chamadaJson(request, token, metodo, caminho, dados) {
  const resposta = await chamada(request, token, metodo, caminho, dados);
  const texto = await resposta.text();
  expect(resposta.status(), `${metodo} ${caminho}: ${texto}`).toBeLessThan(400);
  return texto ? JSON.parse(texto) : null;
}

export async function instalarSessao(page, sessao) {
  await page.addInitScript(({ token, usuario }) => {
    window.localStorage.setItem('obras.token', token);
    window.localStorage.setItem('obras.usuario', JSON.stringify(usuario));
    window.localStorage.setItem('obras.expiraEm', String(Date.now() + 60 * 60 * 1000));
  }, { token: sessao.token, usuario: sessao.usuario });
}

export async function criarUsuario(request, token, perfil = 'E2E') {
  const id = sufixo();
  const login = `e2e${id}`;
  const senha = `E2e@${id}a`;
  const usuario = await chamadaJson(request, token, 'POST', '/api/usuarios', {
    nome: `${perfil} ${id}`,
    login,
    senha,
  });
  return { usuario, login, senha };
}

export async function criarObra(request, token) {
  const id = sufixo();
  return chamadaJson(request, token, 'POST', '/api/obras', {
    nome: `Obra E2E ${id}`,
    descricao: 'Obra determinística de E2E',
    tipologia: 'RESIDENCIAL',
    endereco: 'Rua E2E, 100',
    inicioPrevisto: hoje(),
    fimPrevisto: daquiADias(30),
    responsavelId: null,
  });
}

export async function listarCargos(request, token) {
  const cargos = await chamadaJson(request, token, 'GET', '/api/cargos');
  expect(cargos.length, 'seed de cargos não encontrado').toBeGreaterThan(0);
  return cargos;
}

export async function alocar(request, token, obraId, usuarioId, cargoId) {
  return chamadaJson(request, token, 'POST', `/api/obras/${obraId}/alocacoes`, {
    usuarioId,
    cargoIds: [cargoId],
  });
}

export async function definirResponsavel(request, token, obraId, usuarioId) {
  return chamadaJson(request, token, 'PUT', `/api/obras/${obraId}/responsavel`, { usuarioId });
}

export async function prepararObra(request, token, admin, comResponsavel = true) {
  const obra = await criarObra(request, token);
  const [cargo] = await listarCargos(request, token);
  const alocacaoAdmin = await alocar(request, token, obra.id, admin.usuario.id, cargo.id);
  let responsavel = null;
  if (comResponsavel) {
    const criado = await criarUsuario(request, token, 'Responsável E2E');
    const alocacao = await alocar(request, token, obra.id, criado.usuario.id, cargo.id);
    await definirResponsavel(request, token, obra.id, criado.usuario.id);
    responsavel = { ...criado, alocacao };
  }
  return { obra, cargo, alocacaoAdmin, responsavel };
}

export async function iniciarObra(request, token, obraId) {
  return chamadaJson(request, token, 'POST', `/api/obras/${obraId}/iniciar`);
}

export async function criarDiario(request, token, obraId, alocacaoId) {
  return chamadaJson(request, token, 'POST', `/api/obras/${obraId}/diarios`, {
    data: hoje(),
    clima: 'ENSOLARADO',
    descricao: 'Diário E2E',
    equipePresenteIds: [alocacaoId],
  });
}

export async function criarOcorrencia(request, token, obraId, diarioId) {
  return chamadaJson(request, token, 'POST', `/api/obras/${obraId}/diarios/${diarioId}/ocorrencias`, {
    categoria: 'SEGURANCA',
    descricao: 'Ocorrência E2E',
    dataHora: null,
    paralisacao: null,
  });
}

export async function anexarFotoApi(request, token, obraId, ocorrenciaId, legenda = 'Foto E2E') {
  return chamadaJson(request, token, 'POST', `/api/obras/${obraId}/ocorrencias/${ocorrenciaId}/fotos`, {
    multipart: {
      arquivo: { name: 'e2e.png', mimeType: 'image/png', buffer: PNG_1X1 },
      legenda,
    },
  });
}

export async function buscarPainel(request, token, obraId) {
  return chamadaJson(request, token, 'GET', `/api/obras/${obraId}/painel`);
}
