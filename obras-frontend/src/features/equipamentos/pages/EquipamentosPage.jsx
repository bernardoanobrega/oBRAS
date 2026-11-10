import EmConstrucao from '../../../shared/components/feedback/EmConstrucao.jsx';

export default function EquipamentosPage() {
  return (
    <EmConstrucao
      titulo="Cadastro de equipamentos"
      casosDeUso="UC15, UC16"
      requisitos="RF-19, RF-20"
      descricao="Cadastro por código de patrimônio, tipo, origem (próprio ou locado) e manutenção."
    />
  );
}
