import { useParams } from 'react-router-dom';
import { mensagemErro } from '../../../shared/api/http.js';
import ErrorState from '../../../shared/components/feedback/ErrorState.jsx';
import Spinner from '../../../shared/components/ui/Spinner.jsx';
import { useDetalheObra } from '../api/useDetalheObra.js';
import EquipeResumo from '../components/detalhe/EquipeResumo.jsx';
import { Badge } from '../components/detalhe/primitivos.jsx';
import { CARD } from '../components/detalhe/tokens.js';

function mensagemFalha(erro) {
  if (erro?.mensagem) return erro.mensagem;
  const status = erro?.response?.status;
  if (status === 403) return 'Você não tem permissão para visualizar esta obra.';
  if (status === 404) return 'Obra não encontrada.';
  return mensagemErro(erro);
}

export default function EquipeObraPage() {
  const { obraId } = useParams();
  const { obra, equipe, carregando, erro, recarregar } = useDetalheObra(obraId);
  const casca = 'space-y-6 rounded-2xl bg-fundo p-4 font-sans text-texto md:p-6';

  if (carregando) {
    return (
      <div className={casca}>
        <h2 className="sr-only">Equipe da Obra</h2>
        <Spinner className="py-16" />
      </div>
    );
  }

  if (erro) {
    const status = erro?.response?.status;
    const podeRepetir = !erro?.mensagem && ![400, 403, 404].includes(status);
    return (
      <div className={casca}>
        <h2 className="sr-only">Equipe da Obra</h2>
        <ErrorState mensagem={mensagemFalha(erro)} onTentarNovamente={podeRepetir ? recarregar : undefined} />
      </div>
    );
  }

  const campos = [
    ['ART', obra.art],
    ['Tipologia', obra.tipologia],
    ['Localização', obra.localizacao],
    ['Previsão Contratual', obra.previsao],
  ];

  return (
    <div className={casca}>
      <h2 className="sr-only">Equipe da Obra</h2>
      <header className="flex flex-wrap items-center gap-3">
        <h3 className="text-xl font-semibold text-texto">Equipe · {obra.nome}</h3>
        <Badge tom={obra.statusTom ?? 'green'} dot>{obra.status}</Badge>
      </header>
      <dl className={`${CARD} grid grid-cols-2 gap-4 p-5 text-xs md:grid-cols-4`}>
        {campos.map(([rotulo, valor]) => (
          <div key={rotulo} className="flex flex-col gap-1">
            <dt className="text-texto-fraco">{rotulo}</dt>
            <dd className="font-medium text-texto">{valor}</dd>
          </div>
        ))}
      </dl>
      <EquipeResumo equipe={equipe} />
    </div>
  );
}
