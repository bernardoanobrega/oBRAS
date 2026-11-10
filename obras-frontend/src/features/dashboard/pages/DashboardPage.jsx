import EmConstrucao from '../../../shared/components/feedback/EmConstrucao.jsx';

export default function DashboardPage() {
  return (
    <EmConstrucao
      titulo="Dashboard multi-obras"
      casosDeUso="UC03"
      requisitos="RF-03, RF-04"
      descricao="Painel consolidado com indicadores de saúde financeira, desvios de prazo e alertas críticos de várias obras. Depende dos módulos Orçamento e Cronograma."
    />
  );
}
