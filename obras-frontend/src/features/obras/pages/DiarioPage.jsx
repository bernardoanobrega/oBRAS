import EmConstrucao from '../../../shared/components/feedback/EmConstrucao.jsx';

export default function DiarioPage() {
  return (
    <EmConstrucao
      titulo="Diário de ocorrências"
      casosDeUso="UC21"
      requisitos="RF-25, RF-26"
      descricao="Registro diário com clima, equipe presente, ocorrências, paralisações e fotos. Tela pensada primeiro para celular."
    />
  );
}
