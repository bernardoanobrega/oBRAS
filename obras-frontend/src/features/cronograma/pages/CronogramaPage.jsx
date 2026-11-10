import EmConstrucao from '../../../shared/components/feedback/EmConstrucao.jsx';

export default function CronogramaPage() {
  return (
    <EmConstrucao
      titulo="Cronograma"
      casosDeUso="UC06, UC07"
      requisitos="RF-09, RF-10, RF-28"
      descricao="Linha do tempo das fases, atrasos e desvio em dias, e registro de avanço físico (% acumulado)."
    />
  );
}
