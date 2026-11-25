import EmConstrucao from '../../../shared/components/feedback/EmConstrucao.jsx';

/** Entrada do menu lateral; o cronograma de cada obra fica na aba Cronograma do Detalhe de Obra. */
export default function CronogramasPage() {
  return (
    <EmConstrucao
      titulo="Cronograma"
      casosDeUso="UC08, UC09"
      requisitos="RF-20 a RF-29"
      descricao="Lista os cronogramas das obras do usuário. Enquanto isso, abra a obra em Detalhe de Obra e use a aba Cronograma."
    />
  );
}
