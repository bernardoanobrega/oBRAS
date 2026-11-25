import { useParams } from 'react-router-dom';
import { mensagemErro } from '../../../shared/api/http.js';
import { usePermissao } from '../../../shared/auth/usePermissao.js';
import ErrorState from '../../../shared/components/feedback/ErrorState.jsx';
import Spinner from '../../../shared/components/ui/Spinner.jsx';
import { useDiario } from '../api/useDiario.js';
import DiarioDetalhe from '../components/diario/DiarioDetalhe.jsx';
import DiarioLista from '../components/diario/DiarioLista.jsx';
import { Badge } from '../components/detalhe/primitivos.jsx';

function mensagemFalha(erro) {
  if (erro?.mensagem) return erro.mensagem;
  const status = erro?.response?.status;
  if (status === 403) return 'Você não tem permissão para visualizar o diário desta obra.';
  if (status === 404) return 'Obra não encontrada.';
  return mensagemErro(erro);
}

export default function DiarioPage() {
  const { obraId } = useParams();
  const { pode, carregando: carregandoPermissao } = usePermissao(obraId);
  const { resumos, diario, selecionadoId, carregando, erro, recarregar, selecionarDiario } = useDiario(obraId);
  const podeEditar = !carregandoPermissao && pode('DETALHES_OBRA', 'CRIAR');

  const casca = 'space-y-6 rounded-2xl bg-[#111111] p-4 font-sans text-zinc-200 md:p-6';

  if (carregando) {
    return (
      <div className={casca}>
        <h1 className="sr-only">Diário de ocorrências</h1>
        <Spinner className="py-16" />
      </div>
    );
  }

  if (erro) {
    const status = erro?.response?.status;
    const podeRepetir = !erro?.mensagem && ![400, 403, 404].includes(status);
    return (
      <div className={casca}>
        <h1 className="sr-only">Diário de ocorrências</h1>
        <ErrorState mensagem={mensagemFalha(erro)} onTentarNovamente={podeRepetir ? recarregar : undefined} />
      </div>
    );
  }

  return (
    <div className={casca}>
      <header className="flex items-start justify-between gap-3">
        <div>
          <h1 className="text-lg font-semibold text-white">Diário de ocorrências</h1>
          <p className="text-xs text-zinc-500">UC21 · RF-25, RF-26</p>
        </div>
        <Badge tom={podeEditar ? 'green' : 'neutral'}>{podeEditar ? 'Permissão de edição' : 'Somente leitura'}</Badge>
      </header>

      <DiarioLista resumos={resumos} selecionadoId={selecionadoId} onSelecionar={selecionarDiario} />
      <DiarioDetalhe obraId={obraId} diario={diario} />
    </div>
  );
}
