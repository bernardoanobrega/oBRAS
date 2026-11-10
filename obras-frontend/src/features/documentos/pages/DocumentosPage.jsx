import EmConstrucao from '../../../shared/components/feedback/EmConstrucao.jsx';

export default function DocumentosPage() {
  return (
    <EmConstrucao
      titulo="Documentos da obra"
      casosDeUso="UC17 a UC20"
      requisitos="RF-21 a RF-24, RF-30"
      descricao="Painel por categoria, upload (PDF, PNG ou JPG até 10 MB), revisão atual e histórico de versões."
    />
  );
}
