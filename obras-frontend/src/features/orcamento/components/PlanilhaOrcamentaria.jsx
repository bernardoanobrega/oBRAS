import Button from '../../../shared/components/ui/Button.jsx';
import EmptyState from '../../../shared/components/feedback/EmptyState.jsx';
import { formatarMoeda, formatarPercentualComSinal } from '../../../shared/utils/formatadores.js';
import { formatarDataLocal } from '../utils/orcamento.js';
import ItemAcoesMenu from './ItemAcoesMenu.jsx';
import SituacaoBadge from './SituacaoBadge.jsx';

const quantidade = new Intl.NumberFormat('pt-BR', { maximumFractionDigits: 4 });

const COR_DESVIO_POR_SAUDE = {
  SAUDAVEL: 'text-green-700',
  ATENCAO: 'text-yellow-700',
  CRITICA: 'text-red-700',
};

/** Planilha orçamentária executiva (RF-32) com orçado x realizado por item (RF-33). */
export default function PlanilhaOrcamentaria({ orcamento, pode, editavel, onNovoItem, onAcao }) {
  const { itens, dataUltimaAtualizacao } = orcamento;

  const acoesDoItem = (item) => {
    const verCustos = { rotulo: 'Custos lançados', aoClicar: () => onAcao('custos', item) };
    if (!editavel) return [verCustos];
    const acoes = [];
    if (pode('CRIAR')) {
      acoes.push({ rotulo: 'Lançar custo', aoClicar: () => onAcao('lancarCusto', item) });
    }
    acoes.push(verCustos);
    if (pode('EDITAR')) {
      acoes.push({ rotulo: 'Editar valores', aoClicar: () => onAcao('editar', item) });
    }
    if (pode('EXCLUIR')) {
      acoes.push({
        rotulo: 'Remover item',
        aoClicar: () => onAcao('remover', item),
        desabilitada: item.quantidadeCustos > 0,
        dica:
          item.quantidadeCustos > 0 ? 'Item com custos lançados não pode ser removido' : undefined,
      });
    }
    return acoes;
  };

  return (
    <section className="rounded-xl border border-slate-200 bg-white shadow-sm lg:col-span-2">
      <header className="flex flex-wrap items-center justify-between gap-2 p-4">
        <div>
          <h2 className="font-semibold text-slate-800">Planilha orçamentária</h2>
          <p className="text-xs text-slate-500">{itens.length} itens · valores em reais</p>
        </div>
        {editavel && pode('CRIAR') && <Button onClick={onNovoItem}>Novo item</Button>}
      </header>

      {itens.length === 0 ? (
        <div className="p-4 pt-0">
          <EmptyState mensagem="Nenhum item cadastrado. Adicione o primeiro item para iniciar o acompanhamento." />
        </div>
      ) : (
        <div className="overflow-x-auto">
          <table className="w-full min-w-[760px] text-sm">
            <thead className="text-left text-xs uppercase tracking-wide text-slate-500">
              <tr>
                <th className="px-4 py-2 font-medium">Código</th>
                <th className="px-4 py-2 font-medium">Descrição</th>
                <th className="px-2 py-2 font-medium">Un</th>
                <th className="px-2 py-2 text-right font-medium">Qtd</th>
                <th className="px-4 py-2 text-right font-medium">Orçado</th>
                <th className="px-4 py-2 text-right font-medium">Realizado</th>
                <th className="px-4 py-2 font-medium">Situação</th>
                <th className="px-2 py-2 text-right font-medium">Desvio</th>
                <th className="px-2 py-2">
                  <span className="sr-only">Ações</span>
                </th>
              </tr>
            </thead>
            <tbody>
              {itens.map((item) => (
                <tr key={item.id} className="border-t border-slate-100">
                  <td className="px-4 py-3 font-mono text-xs text-slate-600">{item.codigo}</td>
                  <td className="px-4 py-3 font-medium text-slate-800">{item.descricao}</td>
                  <td className="px-2 py-3 text-slate-500">{item.unidade}</td>
                  <td className="px-2 py-3 text-right">{quantidade.format(item.quantidade)}</td>
                  <td className="px-4 py-3 text-right">{formatarMoeda(item.valorOrcado)}</td>
                  <td className="px-4 py-3 text-right">{formatarMoeda(item.totalRealizado)}</td>
                  <td className="px-4 py-3">
                    <SituacaoBadge codigo={item.saude} compacto />
                  </td>
                  <td
                    className={`px-2 py-3 text-right font-medium ${COR_DESVIO_POR_SAUDE[item.saude]}`}
                  >
                    {formatarPercentualComSinal(item.percentualDesvio)}
                  </td>
                  <td className="px-2 py-1 text-right">
                    <ItemAcoesMenu descricaoItem={item.descricao} acoes={acoesDoItem(item)} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}

      <footer className="border-t border-slate-100 px-4 py-3 text-xs text-slate-500">
        {itens.length} itens · atualizado em {formatarDataLocal(dataUltimaAtualizacao)}
      </footer>
    </section>
  );
}
