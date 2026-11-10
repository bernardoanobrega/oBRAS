import EmConstrucao from '../../../shared/components/feedback/EmConstrucao.jsx';

export default function OrcamentoPage() {
  return (
    <EmConstrucao
      titulo="Orçamento"
      casosDeUso="UC10"
      requisitos="RF-13, RF-14, RF-33"
      descricao="Planilha orçamentária, lançamento de custo realizado, comparativo orçado x realizado e saúde financeira (saudável, atenção, crítica)."
    />
  );
}
