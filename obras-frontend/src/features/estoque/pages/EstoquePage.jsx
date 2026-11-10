import EmConstrucao from '../../../shared/components/feedback/EmConstrucao.jsx';

export default function EstoquePage() {
  return (
    <EmConstrucao
      titulo="Estoque"
      casosDeUso="UC13, UC14"
      requisitos="RF-17, RF-18, RF-35"
      descricao="Saldo por canteiro e por obra, entrada por pedido, saída, transferência e alerta de estoque mínimo."
    />
  );
}
