import { useCallback, useEffect, useState } from 'react';
import Button from '../../../shared/components/ui/Button.jsx';
import Spinner from '../../../shared/components/ui/Spinner.jsx';
import EmptyState from '../../../shared/components/feedback/EmptyState.jsx';
import ErrorState from '../../../shared/components/feedback/ErrorState.jsx';
import { mensagemErro, requisicaoCancelada } from '../../../shared/api/http.js';
import { usePermissao } from '../../../shared/auth/usePermissao.js';
import { buscarOrcamento, cancelarOrcamento, removerItem } from '../api/orcamentoApi.js';
import IndicadoresOrcamento from './IndicadoresOrcamento.jsx';
import PlanilhaOrcamentaria from './PlanilhaOrcamentaria.jsx';
import HistoricoOrcamento from './HistoricoOrcamento.jsx';
import NovoOrcamentoModal from './NovoOrcamentoModal.jsx';
import ItemOrcadoFormModal from './ItemOrcadoFormModal.jsx';
import CustosItemModal from './CustosItemModal.jsx';
import DecisaoFinanceiraFormModal from './DecisaoFinanceiraFormModal.jsx';

const HTTP_NAO_ENCONTRADO = 404;
const MODULO = 'ORCAMENTO';
const SEM_MODAL = { tipo: null, item: null };
const NENHUMA_PERMISSAO = () => false;

/** Painel orçamentário de uma obra (RF-74): indicadores, planilha e histórico de decisões. */
export default function PainelOrcamento({ obraId }) {
  const { pode: podeNoSistema, carregando: carregandoPermissoes } = usePermissao(obraId);
  const [orcamento, setOrcamento] = useState(null);
  const [semOrcamento, setSemOrcamento] = useState(false);
  const [erro, setErro] = useState(null);
  const [modal, setModal] = useState(SEM_MODAL);

  const pode = useCallback((acao) => podeNoSistema(MODULO, acao), [podeNoSistema]);

  const carregar = useCallback(
    async (signal) => {
      setErro(null);
      try {
        setOrcamento(await buscarOrcamento(obraId, signal));
        setSemOrcamento(false);
      } catch (e) {
        if (requisicaoCancelada(e)) return;
        if (e.response?.status === HTTP_NAO_ENCONTRADO) {
          setOrcamento(null);
          setSemOrcamento(true);
        } else {
          setErro(mensagemErro(e));
        }
      }
    },
    [obraId],
  );

  useEffect(() => {
    const controle = new AbortController();
    carregar(controle.signal);
    return () => controle.abort();
  }, [carregar]);

  const fecharModal = useCallback(() => setModal(SEM_MODAL), []);
  const aposSalvar = () => {
    fecharModal();
    carregar();
  };

  async function executar(acao) {
    try {
      await acao();
      carregar();
    } catch (e) {
      setErro(mensagemErro(e));
    }
  }

  function aoAcionarItem(tipo, item) {
    if (tipo !== 'remover') {
      setModal({ tipo, item });
      return;
    }
    if (window.confirm(`Remover o item ${item.codigo} - ${item.descricao}?`)) {
      executar(() => removerItem(obraId, item.id));
    }
  }

  function cancelar() {
    const mensagem =
      'Cancelar este orçamento? Depois de cancelado ele não poderá mais ser alterado.';
    if (window.confirm(mensagem)) executar(() => cancelarOrcamento(obraId));
  }

  if (erro) return <ErrorState mensagem={erro} onTentarNovamente={() => carregar()} />;
  if (carregandoPermissoes || (!orcamento && !semOrcamento)) return <Spinner className="mt-10" />;

  if (semOrcamento) {
    return (
      <div className="space-y-3">
        <EmptyState mensagem="Esta obra ainda não possui orçamento." />
        {pode('CRIAR') && (
          <div className="flex justify-center">
            <Button onClick={() => setModal({ tipo: 'novo', item: null })}>Criar orçamento</Button>
          </div>
        )}
        <NovoOrcamentoModal
          aberto={modal.tipo === 'novo'}
          obraIdFixo={obraId}
          onFechar={fecharModal}
          onCriado={aposSalvar}
        />
      </div>
    );
  }

  const editavel = orcamento.status !== 'CANCELADO';
  // Item relido do painel: totais do cabeçalho do modal acompanham cada lançamento.
  const itemAtualizado = orcamento.itens.find((item) => item.id === modal.item?.id) ?? modal.item;

  return (
    <div className="space-y-4">
      {editavel && pode('EXCLUIR') && (
        <div className="flex justify-end">
          <Button variante="secundario" onClick={cancelar}>
            Cancelar orçamento
          </Button>
        </div>
      )}

      <IndicadoresOrcamento orcamento={orcamento} />

      <div className="grid gap-4 lg:grid-cols-3">
        <PlanilhaOrcamentaria
          orcamento={orcamento}
          pode={pode}
          editavel={editavel}
          onNovoItem={() => setModal({ tipo: 'editar', item: null })}
          onAcao={aoAcionarItem}
        />
        <HistoricoOrcamento
          orcamento={orcamento}
          podeRegistrar={editavel && pode('APROVAR')}
          onRegistrarDecisao={() => setModal({ tipo: 'decisao', item: null })}
          onEditarDecisao={(decisao) => setModal({ tipo: 'decisao', item: null, decisao })}
        />
      </div>

      <ItemOrcadoFormModal
        aberto={modal.tipo === 'editar'}
        obraId={obraId}
        item={modal.item}
        onFechar={fecharModal}
        onSalvo={aposSalvar}
      />
      <CustosItemModal
        aberto={modal.tipo === 'custos' || modal.tipo === 'lancarCusto'}
        obraId={obraId}
        item={itemAtualizado}
        pode={editavel ? pode : NENHUMA_PERMISSAO}
        iniciarLancando={modal.tipo === 'lancarCusto'}
        onFechar={fecharModal}
        onAlterado={() => carregar()}
      />
      <DecisaoFinanceiraFormModal
        aberto={modal.tipo === 'decisao'}
        obraId={obraId}
        decisao={modal.decisao ?? null}
        onFechar={fecharModal}
        onSalvo={aposSalvar}
      />
    </div>
  );
}
