import { useCallback, useEffect, useState } from 'react';
import Button from '../../../shared/components/ui/Button.jsx';
import Select from '../../../shared/components/ui/Select.jsx';
import Spinner from '../../../shared/components/ui/Spinner.jsx';
import EmptyState from '../../../shared/components/feedback/EmptyState.jsx';
import ErrorState from '../../../shared/components/feedback/ErrorState.jsx';
import { mensagemErro, requisicaoCancelada } from '../../../shared/api/http.js';
import { listarObras, listarOrcamentos } from '../api/orcamentoApi.js';
import { rotuloObra, situacao } from '../utils/orcamento.js';
import PainelOrcamento from '../components/PainelOrcamento.jsx';
import NovoOrcamentoModal from '../components/NovoOrcamentoModal.jsx';

/** Item "Orçamento" do menu lateral: escolhe a obra e mostra o painel orçamentário dela. */
export default function OrcamentosPage() {
  const [orcamentos, setOrcamentos] = useState(null);
  const [obras, setObras] = useState([]);
  const [obraSelecionada, setObraSelecionada] = useState(null);
  const [erro, setErro] = useState(null);
  const [criando, setCriando] = useState(false);

  const carregar = useCallback(async (signal) => {
    setErro(null);
    try {
      const [lista, obrasDoUsuario] = await Promise.all([
        listarOrcamentos(signal),
        listarObras(signal),
      ]);
      setObras(obrasDoUsuario);
      setOrcamentos(lista);
      setObraSelecionada((atual) => atual ?? lista[0]?.obraId ?? null);
    } catch (e) {
      if (!requisicaoCancelada(e)) setErro(mensagemErro(e));
    }
  }, []);

  useEffect(() => {
    const controle = new AbortController();
    carregar(controle.signal);
    return () => controle.abort();
  }, [carregar]);

  function aoCriar(obraId) {
    setCriando(false);
    setObraSelecionada(obraId);
    carregar();
  }

  const opcoesObra = Object.fromEntries(
    (orcamentos ?? []).map((o) => [
      o.obraId,
      `${rotuloObra(o.obraId, obras)} · ${situacao(o.status).rotulo}`,
    ]),
  );
  const obrasSemOrcamento = obras.filter(
    (obra) => !(orcamentos ?? []).some((o) => o.obraId === obra.id),
  );

  return (
    <div className="space-y-4">
      <div className="flex flex-wrap items-end justify-between gap-3">
        <div className="flex flex-wrap items-end gap-2">
          {orcamentos?.length > 0 && (
            <Select
              label="Obra"
              name="obra"
              className="w-64"
              opcoes={opcoesObra}
              value={obraSelecionada ?? ''}
              onChange={(e) => setObraSelecionada(Number(e.target.value))}
            />
          )}
          <Button variante="secundario" onClick={() => setCriando(true)}>
            Novo orçamento
          </Button>
        </div>
      </div>

      {erro && <ErrorState mensagem={erro} onTentarNovamente={() => carregar()} />}
      {!erro && !orcamentos && <Spinner className="mt-10" />}
      {orcamentos?.length === 0 && (
        <EmptyState mensagem="Nenhum orçamento disponível para você. Crie um orçamento para uma obra." />
      )}
      {obraSelecionada != null && (
        <PainelOrcamento key={obraSelecionada} obraId={obraSelecionada} />
      )}

      <NovoOrcamentoModal
        aberto={criando}
        obras={obrasSemOrcamento}
        onFechar={() => setCriando(false)}
        onCriado={aoCriar}
      />
    </div>
  );
}
