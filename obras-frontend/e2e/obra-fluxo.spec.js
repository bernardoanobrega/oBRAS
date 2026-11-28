import { expect, test } from '@playwright/test';
import {
  alocar,
  anexarFotoApi,
  chamada,
  chamadaJson,
  criarDiario,
  criarOcorrencia,
  criarUsuario,
  credenciaisAdmin,
  daquiADias,
  definirResponsavel,
  hoje,
  iniciarObra,
  instalarSessao,
  listarCargos,
  login,
  PNG_1X1,
  prepararObra,
  sufixo,
} from './helpers.js';

test.describe('fluxo do detalhe de obra', () => {
  let admin;
  let credenciais;

  test.beforeEach(async ({ page, request }) => {
    credenciais = credenciaisAdmin();
    test.skip(!credenciais, 'Defina E2E_LOGIN e E2E_SENHA para rodar o fluxo E2E');
    admin = await login(request, credenciais.login, credenciais.senha);
    await instalarSessao(page, admin);
  });

  test('e2eCriaObraSemResponsavel', async ({ page, request }) => {
    const nome = `Obra E2E UI ${sufixo()}`;
    await page.goto('/obras');
    await page.getByRole('button', { name: 'Nova obra' }).click();
    await page.getByLabel('Nome *').fill(nome);
    await page.getByLabel('Tipologia *').selectOption('RESIDENCIAL');
    await page.getByLabel('Endereço *').fill('Rua E2E, 200');
    await page.getByLabel('Início previsto *').fill(hoje());
    await page.getByLabel('Fim previsto *').fill(daquiADias(30));
    await page.getByRole('button', { name: 'Salvar' }).click();

    await expect(page.getByRole('dialog')).toHaveCount(0);
    await expect(page.getByText(nome)).toBeVisible();

    const obras = await chamadaJson(request, admin.token, 'GET', '/api/obras');
    const obra = obras.find((item) => item.nome === nome);
    expect(obra).toBeTruthy();
    expect(obra.responsavelId).toBeNull();
    expect(obra.status).toBe('PLANEJADA');
  });

  test('e2eIniciaSomenteComResponsavel', async ({ page, request }) => {
    const { obra } = await prepararObra(request, admin.token, admin, false);

    await page.goto(`/obras/${obra.id}/resumo`);
    await page.getByRole('button', { name: 'Iniciar obra' }).click();
    await expect(page.getByRole('alert')).toContainText('A obra exige um responsável para ser iniciada');

    const criado = await criarUsuario(request, admin.token, 'Responsável E2E');
    const [cargo] = await listarCargos(request, admin.token);
    await alocar(request, admin.token, obra.id, criado.usuario.id, cargo.id);
    await definirResponsavel(request, admin.token, obra.id, criado.usuario.id);

    await page.reload();
    await page.getByRole('button', { name: 'Iniciar obra' }).click();
    await expect
      .poll(async () => (await chamadaJson(request, admin.token, 'GET', `/api/obras/${obra.id}`)).status)
      .toBe('EM_ANDAMENTO');
  });

  test('e2eAlocaEquipe', async ({ page, request }) => {
    const { obra } = await prepararObra(request, admin.token, admin, false);
    const criado = await criarUsuario(request, admin.token, 'Profissional E2E');

    await page.goto(`/obras/${obra.id}/equipe`);
    await page.getByRole('button', { name: 'Alocar profissional' }).click();
    await page.getByLabel('Profissional *').selectOption({ label: criado.usuario.nome });
    await page.locator('input[name="cargoIds"]').first().check();
    await page.getByRole('button', { name: 'Alocar', exact: true }).click();

    await expect(page.getByText(criado.usuario.nome).first()).toBeVisible();
    const alocacoes = await chamadaJson(request, admin.token, 'GET', `/api/obras/${obra.id}/alocacoes`);
    expect(alocacoes.some((alocacao) => alocacao.usuarioId === criado.usuario.id)).toBe(true);
  });

  test('e2eRegistraDiarioEOcorrencia', async ({ page, request }) => {
    const contexto = await prepararObra(request, admin.token, admin);
    await iniciarObra(request, admin.token, contexto.obra.id);

    await page.goto(`/obras/${contexto.obra.id}/diario`);
    await page.getByRole('button', { name: 'Novo diário' }).click();
    await page.getByLabel('Data').fill(hoje());
    await page.getByLabel('Clima *').selectOption({ label: 'Ensolarado' });
    await page.getByLabel('Descrição do dia').fill('Diário E2E pela interface');
    await page.getByLabel('Selecionar equipe presente').selectOption({ label: admin.usuario.nome });
    await page.getByRole('button', { name: 'Salvar diário' }).click();

    await expect(page.getByText('Diário E2E pela interface')).toBeVisible();
    await page.getByRole('button', { name: 'Nova ocorrência', exact: true }).click();
    await page.getByLabel('Categoria *').selectOption({ label: 'Segurança' });
    await page.getByLabel('Descrição *').fill('Ocorrência E2E pela interface');
    await page.getByRole('button', { name: 'Salvar ocorrência' }).click();

    await expect(page.getByText('Ocorrência E2E pela interface')).toBeVisible();
  });

  test('e2eAnexaFoto', async ({ page, request }) => {
    const contexto = await prepararObra(request, admin.token, admin);
    await iniciarObra(request, admin.token, contexto.obra.id);
    const diario = await criarDiario(request, admin.token, contexto.obra.id, contexto.alocacaoAdmin.id);
    const ocorrencia = await criarOcorrencia(request, admin.token, contexto.obra.id, diario.id);

    await page.goto(`/obras/${contexto.obra.id}/diario`);
    await expect(page.getByText('Ocorrência E2E')).toBeVisible();
    await page.getByRole('button', { name: 'Anexar foto', exact: true }).first().click();
    await page.locator('#foto-arquivo').setInputFiles({
      name: 'e2e.png',
      mimeType: 'image/png',
      buffer: PNG_1X1,
    });
    await page.getByLabel('Legenda (opcional)').fill('Foto E2E pela interface');
    await page.getByRole('dialog').getByRole('button', { name: 'Anexar foto', exact: true }).click();

    await expect(page.locator('figcaption').filter({ hasText: 'Foto E2E pela interface' })).toBeVisible();
    const painel = await chamadaJson(request, admin.token, 'GET', `/api/obras/${contexto.obra.id}/painel`);
    expect(painel.fotos.some((foto) => foto.ocorrenciaId === ocorrencia.id)).toBe(true);
  });

  test('e2ePainelRefleteDados', async ({ page, request }) => {
    const contexto = await prepararObra(request, admin.token, admin);
    await iniciarObra(request, admin.token, contexto.obra.id);
    const diario = await criarDiario(request, admin.token, contexto.obra.id, contexto.alocacaoAdmin.id);
    const ocorrencia = await criarOcorrencia(request, admin.token, contexto.obra.id, diario.id);
    await anexarFotoApi(request, admin.token, contexto.obra.id, ocorrencia.id);

    await page.goto(`/obras/${contexto.obra.id}/resumo`);
    await expect(page.getByText(contexto.obra.nome)).toBeVisible();
    await expect(page.getByText(admin.usuario.nome).first()).toBeVisible();
    await expect(page.getByText('Ocorrência E2E').first()).toBeVisible();
    await expect(page.locator('figcaption').filter({ hasText: 'Foto E2E' })).toBeVisible();
    await expect(page.getByText('SIMULADO').first()).toBeVisible();
  });

  test('e2eConcluiEEncerraEquipe', async ({ page, request }) => {
    const contexto = await prepararObra(request, admin.token, admin);
    await iniciarObra(request, admin.token, contexto.obra.id);
    page.on('dialog', (dialog) => dialog.accept());

    await page.goto(`/obras/${contexto.obra.id}/resumo`);
    await page.getByRole('button', { name: 'Concluir obra' }).click();
    await expect
      .poll(async () => (await chamadaJson(request, admin.token, 'GET', `/api/obras/${contexto.obra.id}`)).status)
      .toBe('CONCLUIDA');

    const equipe = await chamadaJson(request, admin.token, 'GET', `/api/obras/${contexto.obra.id}/equipe`);
    expect(equipe).toEqual([]);
  });

  test('e2eUsuarioSemPermissaoNaoAcessa', async ({ page, request }) => {
    const { obra } = await prepararObra(request, admin.token, admin);
    const semPermissao = await criarUsuario(request, admin.token, 'Sem Permissão E2E');
    const sessao = await login(request, semPermissao.login, semPermissao.senha);
    await instalarSessao(page, sessao);

    await page.goto(`/obras/${obra.id}/resumo`);
    await expect(page.getByRole('heading', { name: 'Sem permissão' })).toBeVisible();

    const resposta = await chamada(request, sessao.token, 'GET', `/api/obras/${obra.id}/painel`);
    expect(resposta.status()).toBe(403);
  });
});
