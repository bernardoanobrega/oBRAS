import { useCallback, useEffect, useState } from 'react';
import { Pencil, Plus, Trash2 } from 'lucide-react';
import Modal from '../../../shared/components/ui/Modal.jsx';
import Button from '../../../shared/components/ui/Button.jsx';
import Spinner from '../../../shared/components/ui/Spinner.jsx';
import { mensagemErro, requisicaoCancelada } from '../../../shared/api/http.js';
import { formatarMoeda } from '../../../shared/utils/formatadores.js';
import { listarCustos, removerCusto } from '../api/orcamentoApi.js';
import { ORIGENS_CUSTO, formatarDataLocal } from '../utils/orcamento.js';
import CustoRealizadoForm from './CustoRealizadoForm.jsx';

const SEM_FORMULARIO = { aberto: false, custo: null };

/**
 * Custos lançados em um item: listar, lançar, editar e remover.
 * iniciarLancando abre direto no formulário de novo lançamento.
 */
export default function CustosItemModal({
  aberto,
  obraId,
  item,
  pode,
  iniciarLancando,
  onFechar,
  onAlterado,
}) {
  const [custos, setCustos] = useState(null);
  const [erro, setErro] = useState(null);
  const [formulario, setFormulario] = useState(SEM_FORMULARIO);
  const itemId = item?.id;

  const carregar = useCallback(
    async (signal) => {
      setErro(null);
      try {
        setCustos(await listarCustos(obraId, itemId, signal));
      } catch (e) {
        if (!requisicaoCancelada(e)) setErro(mensagemErro(e));
      }
    },
    [obraId, itemId],
  );

  useEffect(() => {
    if (!aberto || itemId == null) return undefined;
    setCustos(null);
    setFormulario(iniciarLancando ? { aberto: true, custo: null } : SEM_FORMULARIO);
    const controle = new AbortController();
    carregar(controle.signal);
    return () => controle.abort();
  }, [aberto, itemId, iniciarLancando, carregar]);

  function aposSalvar() {
    setFormulario(SEM_FORMULARIO);
    carregar();
    onAlterado();
  }

  async function remover(custo) {
    const mensagem = `Remover o lançamento "${custo.descricao}" de ${formatarMoeda(custo.valor)}?`;
    if (!window.confirm(mensagem)) return;
    try {
      await removerCusto(obraId, itemId, custo.id);
      aposSalvar();
    } catch (e) {
      setErro(mensagemErro(e));
    }
  }

  return (
    <Modal aberto={aberto} onFechar={onFechar} titulo="Custos do item" largura="max-w-2xl">
      {item && (
        <div className="mb-4 flex flex-wrap items-center justify-between gap-2 rounded-lg bg-superficie-alta p-3 text-sm text-texto-suave">
          <div>
            <p className="font-medium text-texto">
              {item.codigo} · {item.descricao}
            </p>
            <p>
              Orçado {formatarMoeda(item.valorOrcado)} · realizado{' '}
              {formatarMoeda(item.totalRealizado)}
            </p>
          </div>
          {pode('CRIAR') && !formulario.aberto && (
            <Button onClick={() => setFormulario({ aberto: true, custo: null })}>
              <Plus size={16} aria-hidden="true" /> Lançar custo
            </Button>
          )}
        </div>
      )}

      {formulario.aberto && (
        <div className="mb-4 rounded-lg border border-borda p-4">
          <CustoRealizadoForm
            obraId={obraId}
            itemId={itemId}
            custo={formulario.custo}
            onCancelar={() => setFormulario(SEM_FORMULARIO)}
            onSalvo={aposSalvar}
          />
        </div>
      )}

      {erro && (
        <p role="alert" className="mb-3 rounded bg-perigo-fundo p-2 text-sm text-perigo">
          {erro}
        </p>
      )}
      {!custos && !erro && <Spinner className="my-6" />}
      {custos?.length === 0 && (
        <p className="py-6 text-center text-sm text-texto-suave">
          Nenhum custo lançado neste item.
        </p>
      )}

      {custos?.length > 0 && (
        <ul className="divide-y divide-borda">
          {custos.map((custo) => (
            <li key={custo.id} className="flex flex-wrap items-center gap-x-4 gap-y-1 py-3">
              <div className="min-w-0 flex-1">
                <p className="truncate text-sm font-medium text-texto">{custo.descricao}</p>
                <p className="text-xs text-texto-suave">
                  {formatarDataLocal(custo.dataLancamento)} · {ORIGENS_CUSTO[custo.origem]}
                  {custo.vinculadoAOutroModulo && ' · lançado por outro módulo'}
                </p>
              </div>
              <p className="text-sm font-semibold text-texto">{formatarMoeda(custo.valor)}</p>
              {!custo.vinculadoAOutroModulo && (
                <div className="flex gap-1">
                  {pode('EDITAR') && (
                    <button
                      type="button"
                      aria-label={`Editar lançamento ${custo.descricao}`}
                      onClick={() => setFormulario({ aberto: true, custo })}
                      className="flex h-11 w-11 items-center justify-center rounded-lg text-texto-suave hover:bg-superficie-alta md:h-9 md:w-9"
                    >
                      <Pencil size={16} />
                    </button>
                  )}
                  {pode('EXCLUIR') && (
                    <button
                      type="button"
                      aria-label={`Remover lançamento ${custo.descricao}`}
                      onClick={() => remover(custo)}
                      className="flex h-11 w-11 items-center justify-center rounded-lg text-perigo hover:bg-perigo-fundo md:h-9 md:w-9"
                    >
                      <Trash2 size={16} />
                    </button>
                  )}
                </div>
              )}
            </li>
          ))}
        </ul>
      )}
    </Modal>
  );
}
