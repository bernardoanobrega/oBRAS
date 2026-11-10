import EmConstrucao from '../../../shared/components/feedback/EmConstrucao.jsx';

export default function ComprasPage() {
  return (
    <EmConstrucao
      titulo="Compras"
      casosDeUso="UC11, UC12"
      requisitos="RF-15, RF-16, RF-31"
      descricao="Pedidos de compra por status (solicitado, em cotação, comprado, em trânsito, entregue), cotações e histórico."
    />
  );
}
