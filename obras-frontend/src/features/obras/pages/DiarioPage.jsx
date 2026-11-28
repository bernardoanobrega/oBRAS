import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { mensagemErro } from '../../../shared/api/http.js';
import Can from '../../../shared/auth/Can.jsx';
import { usePermissao } from '../../../shared/auth/usePermissao.js';
import ErrorState from '../../../shared/components/feedback/ErrorState.jsx';
import Button from '../../../shared/components/ui/Button.jsx';
import Spinner from '../../../shared/components/ui/Spinner.jsx';
import { buscarDiario } from '../api/diarioApi.js';
import { dataIsoDeHoje } from '../adapters/diarioF3.js';
import DiarioDetalhe from '../components/diario/DiarioDetalhe.jsx';
import DiarioFormModal from '../components/diario/DiarioFormModal.jsx';
import DiarioLista from '../components/diario/DiarioLista.jsx';
import OcorrenciaFormModal from '../components/diario/OcorrenciaFormModal.jsx';
import OcorrenciaRapidaModal from '../components/diario/OcorrenciaRapidaModal.jsx';
import { useDiarioF3 } from '../hooks/useDiarioF3.js';
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
  const {
    resumos,
    diario,
    selecionadoId,
    carregando,
    erro,
    recarregar,
    selecionarDiario,
    salvarDiario,
    salvarOcorrencia,
    removerOcorrencia,
    salvarOcorrenciaRapida,
  } = useDiarioF3(obraId);
  const [modal, setModal] = useState({ tipo: null, alvo: null });
  const [erroAcao, setErroAcao] = useState(null);
  const podeCriar = !carregandoPermissao && pode('DETALHES_OBRA', 'CRIAR');
  const podeEditar = !carregandoPermissao && pode('DETALHES_OBRA', 'EDITAR');

  const casca = 'space-y-6 rounded-2xl bg-fundo p-4 font-sans text-texto md:p-6';
  const fechar = () => setModal({ tipo: null, alvo: null });

  async function abrirEdicaoDiario(resumo) {
    setErroAcao(null);
    try {
      const completo = await buscarDiario(obraId, resumo.id);
      setModal({ tipo: 'diario', alvo: completo });
    } catch (err) {
      setErroAcao(mensagemErro(err));
    }
  }

  async function excluirOcorrencia(ocorrencia) {
    setErroAcao(null);
    try {
      await removerOcorrencia(ocorrencia);
    } catch (err) {
      setErroAcao(mensagemErro(err));
    }
  }

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
      <header className="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
        <div>
          <h1 className="text-lg font-semibold text-texto">Diário de ocorrências</h1>
          <p className="text-xs text-texto-fraco">UC21 · RF-25, RF-26</p>
        </div>
        <div className="flex flex-wrap items-center gap-2">
          <Badge tom={podeCriar ? 'green' : 'neutral'}>{podeCriar ? 'Permissão de edição' : 'Somente leitura'}</Badge>
          {podeCriar && (
            <Can modulo="DETALHES_OBRA" acao="CRIAR" obraId={obraId}>
              <Button type="button" onClick={() => setModal({ tipo: 'diario', alvo: null })}>Novo diário</Button>
            </Can>
          )}
          {podeCriar && (
            <Can modulo="DETALHES_OBRA" acao="CRIAR" obraId={obraId}>
              <Button type="button" variante="secundario" onClick={() => setModal({ tipo: 'rapida', alvo: null })}>
                Ocorrência rápida
              </Button>
            </Can>
          )}
        </div>
      </header>

      <DiarioLista
        obraId={obraId}
        resumos={resumos}
        selecionadoId={selecionadoId}
        onSelecionar={selecionarDiario}
        onEditar={podeEditar ? abrirEdicaoDiario : undefined}
      />
      <DiarioDetalhe
        obraId={obraId}
        diario={diario}
        erroAcao={erroAcao}
        onEditar={(alvo) => setModal({ tipo: 'diario', alvo })}
        onNovaOcorrencia={() => setModal({ tipo: 'ocorrencia', alvo: null })}
        onOcorrenciaRapida={() => setModal({ tipo: 'rapida', alvo: null })}
        onEditarOcorrencia={(alvo) => setModal({ tipo: 'ocorrencia', alvo })}
        onExcluirOcorrencia={excluirOcorrencia}
        onRecarregar={recarregar}
      />

      <DiarioFormModal
        aberto={modal.tipo === 'diario'}
        obraId={obraId}
        diario={modal.tipo === 'diario' ? modal.alvo : null}
        onFechar={fechar}
        onSalvar={salvarDiario}
        onSalvo={fechar}
      />
      <OcorrenciaFormModal
        aberto={modal.tipo === 'ocorrencia'}
        ocorrencia={modal.tipo === 'ocorrencia' ? modal.alvo : null}
        onFechar={fechar}
        onSalvar={(payload) => salvarOcorrencia(payload, modal.alvo)}
        onSalvo={fechar}
      />
      <OcorrenciaRapidaModal
        aberto={modal.tipo === 'rapida'}
        data={dataIsoDeHoje()}
        resumos={resumos}
        onFechar={fechar}
        onSalvar={salvarOcorrenciaRapida}
        onSalvo={fechar}
      />
    </div>
  );
}
