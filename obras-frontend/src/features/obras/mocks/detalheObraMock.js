// Mock da fatia visual L43+L44. Shapes estáveis: L45 substitui por chamadas reais ao endpoint do painel.
// Sem componentes, sem URLs externas, sem /img/... (não existe public/img).

export const OBRA = {
  nome: 'Edifício Horizon Prime',
  status: 'Em Andamento',
  art: '2026/8941',
  tipologia: 'Residencial Multifamiliar (18 Pavimentos)',
  localizacao: 'Av. Beira Mar, 1040',
  previsao: 'Fev/2026 a Nov/2027',
};

export const CLIMA_DIA = {
  condicao: 'Ensolarado',
  data: '22/09/2026',
  badge: 'Canteiro Praticável',
};

export const KPIS = {
  avancoRealizado: 42,
  avancoPrevisto: 38,
  spi: 1.08,
  orcamentoTotal: 12500000,
  executado: 5200000,
  saldo: 7300000,
  limite: 41.6,
  maoDeObraTotal: 38,
  vsOntem: 3,
  fonte: 'SIMULADO',
};

export const EQUIPE = [
  { sigla: 'CM', nome: 'Carlos Mendes', cargo: 'Responsável Técnico Principal', tom: 'amber' },
  { sigla: 'MS', nome: 'Michel Santos', cargo: 'Engenheiro Residente', tom: 'blue' },
  { sigla: 'MC', nome: 'Mariana Costa', cargo: 'Mestre de Obras Geral', tom: 'neutral' },
  { sigla: 'JA', nome: 'Josué Andrade', cargo: 'Encarregado de Armação e Estrutura', tom: 'neutral' },
];

export const OCORRENCIAS = [
  { data: '25/09 · 14:00', tag: 'Clima / Paralisação', tom: 'red', texto: 'Chuva torrencial suspendeu concretagem da laje do 4º pavimento por 3h30' },
  { data: '24/09 · 10:15', tag: 'Logística', tom: 'amber', texto: 'Atraso de 1h na entrega de aço CA-50 da usina parceira' },
  { data: '22/09 · 08:30', tag: 'Segurança', tom: 'green', texto: 'Inspeção semanal de segurança (EPIs e PCMAT) concluída com 100% de conformidade' },
];

export const FOTOS = [
  { id: 1, legenda: 'Pilares P-10 e P-12 - 4º Pavimento · 25/09', gradiente: 'from-amber-500/30 to-zinc-800' },
  { id: 2, legenda: 'Subsolo 01 · 23/09', gradiente: 'from-blue-500/30 to-zinc-800' },
  { id: 3, legenda: 'Fachada Leste · 20/09', gradiente: 'from-green-500/30 to-zinc-800' },
];
