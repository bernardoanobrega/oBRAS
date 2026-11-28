import { expect, test } from '@playwright/test';
import {
  anexarFotoApi,
  credenciaisAdmin,
  criarDiario,
  criarOcorrencia,
  instalarSessao,
  iniciarObra,
  login,
  prepararObra,
} from './helpers.js';

async function prepararCenario(page, request) {
  const credenciais = credenciaisAdmin();
  test.skip(!credenciais, 'Defina E2E_LOGIN e E2E_SENHA para rodar o fluxo E2E');
  const admin = await login(request, credenciais.login, credenciais.senha);
  await instalarSessao(page, admin);

  const contexto = await prepararObra(request, admin.token, admin);
  await iniciarObra(request, admin.token, contexto.obra.id);
  const diario = await criarDiario(request, admin.token, contexto.obra.id, contexto.alocacaoAdmin.id);
  const ocorrencia = await criarOcorrencia(request, admin.token, contexto.obra.id, diario.id);
  await anexarFotoApi(request, admin.token, contexto.obra.id, ocorrencia.id);

  await page.goto(`/obras/${contexto.obra.id}/resumo`);
  await expect(page.getByText(contexto.obra.nome).first()).toBeVisible();
  await expect(page.getByText('Ocorrência E2E').first()).toBeVisible();
  await expect(page.locator('figcaption').filter({ hasText: 'Foto E2E' })).toBeVisible();

  return { admin, contexto };
}

async function metricasLayout(page) {
  return page.evaluate(() => {
    const html = document.documentElement;
    const main = document.querySelector('main');
    const aside = document.querySelector('aside');
    const nav = document.querySelector('nav[aria-label="Navegação principal"]');
    const estiloMain = main ? getComputedStyle(main) : null;
    const estiloAside = aside ? getComputedStyle(aside) : null;
    const estiloNav = nav ? getComputedStyle(nav) : null;
    const filhosFora = [];

    if (main) {
      for (const elemento of main.querySelectorAll('*')) {
        const retangulo = elemento.getBoundingClientRect();
        if (retangulo.width === 0 || retangulo.right <= window.innerWidth + 1) continue;

        let ancestral = elemento.parentElement;
        let emContainerRolavel = ['auto', 'scroll'].includes(getComputedStyle(elemento).overflowX);
        while (ancestral && ancestral !== main && !emContainerRolavel) {
          emContainerRolavel = ['auto', 'scroll'].includes(getComputedStyle(ancestral).overflowX);
          ancestral = ancestral.parentElement;
        }
        if (emContainerRolavel) continue;

        filhosFora.push({
          tag: elemento.tagName,
          classe: String(elemento.className ?? ''),
          direita: Math.round(retangulo.right),
        });
      }
    }

    return {
      htmlScrollWidth: html.scrollWidth,
      htmlClientWidth: html.clientWidth,
      mainScrollWidth: main?.scrollWidth ?? null,
      mainClientWidth: main?.clientWidth ?? null,
      mainOverflowX: estiloMain?.overflowX ?? null,
      filhosFora: filhosFora.slice(0, 10),
      sidebarDisplay: estiloAside?.display ?? null,
      sidebarPosition: estiloAside?.position ?? null,
      navDisplay: estiloNav?.display ?? null,
      navPosition: estiloNav?.position ?? null,
    };
  });
}

function expectSemOverflow(metricas) {
  expect.soft(metricas.htmlScrollWidth, JSON.stringify(metricas)).toBeLessThanOrEqual(metricas.htmlClientWidth);
  expect.soft(metricas.mainScrollWidth, JSON.stringify(metricas)).toBeLessThanOrEqual(metricas.mainClientWidth);
  expect.soft(metricas.filhosFora, JSON.stringify(metricas)).toEqual([]);
}

test.describe('L50 responsividade mobile', () => {
  test.use({ viewport: { width: 375, height: 812 } });

  test('layoutMobileSemOverflow', async ({ page, request }) => {
    await prepararCenario(page, request);
    const metricas = await metricasLayout(page);

    expectSemOverflow(metricas);
    expect(metricas.sidebarDisplay).toBe('none');
    expect(metricas.navDisplay).not.toBe('none');
    expect(metricas.navPosition).toBe('fixed');

    await page.screenshot({ path: '../docs/evidencias/l50/mobile-resumo.png', fullPage: true });
  });
});

test.describe('L50 responsividade tablet', () => {
  test.use({ viewport: { width: 820, height: 1180 } });

  test('layoutTabletSemSidebarFixa', async ({ page, request }) => {
    await prepararCenario(page, request);
    const metricas = await metricasLayout(page);

    expectSemOverflow(metricas);
    expect(metricas.sidebarDisplay).not.toBe('none');
    expect(metricas.sidebarPosition).not.toBe('fixed');
    expect(metricas.navDisplay).toBe('none');

    await page.screenshot({ path: '../docs/evidencias/l50/tablet-resumo.png', fullPage: true });
  });
});
