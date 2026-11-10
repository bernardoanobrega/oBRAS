import EmConstrucao from '../../../shared/components/feedback/EmConstrucao.jsx';

export default function EquipamentosObraPage() {
  return (
    <EmConstrucao
      titulo="Equipamentos da obra"
      casosDeUso="UC15, UC16"
      requisitos="RF-19, RF-20, RF-34"
      descricao="Alocar e devolver equipamentos (condição BOA ou DANIFICADO), disponibilidade e alertas de atraso."
    />
  );
}
