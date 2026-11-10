import EmConstrucao from '../../../shared/components/feedback/EmConstrucao.jsx';

export default function ObrasPage() {
  return (
    <EmConstrucao
      titulo="Obras"
      casosDeUso="UC04, UC05"
      requisitos="RF-05, RF-06, RF-32"
      descricao="Lista de obras com filtro por status e cadastro. Falta no backend um endpoint de obras do usuário."
    />
  );
}
