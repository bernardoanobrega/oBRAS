import { useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { Pencil, Plus, Search } from 'lucide-react';
import { mensagemErro } from '../../../shared/api/http.js';
import Can from '../../../shared/auth/Can.jsx';
import EmptyState from '../../../shared/components/feedback/EmptyState.jsx';
import ErrorState from '../../../shared/components/feedback/ErrorState.jsx';
import Spinner from '../../../shared/components/ui/Spinner.jsx';
import { paraHeader } from '../api/adaptadores.js';
import { useObras } from '../api/useObras.js';
import { Badge, IconButton } from '../components/detalhe/primitivos.jsx';
import { BOTAO_PRIMARIO, CARD, FOCO, LINK } from '../components/detalhe/tokens.js';
import ObraFormModal from '../components/ObraFormModal.jsx';

const STATUS = [
  ['PLANEJADA', 'Planejada'],
  ['EM_ANDAMENTO', 'Em Andamento'],
  ['PARALISADA', 'Paralisada'],
  ['CANCELADA', 'Cancelada'],
  ['CONCLUIDA', 'Concluída'],
];

// Tema escuro do Figma (mesma casca do Diário e do Detalhe da obra).
const CASCA = 'space-y-4 rounded-2xl bg-[#111111] p-4 font-sans text-zinc-200 md:p-6';
const CAMPO_ESCURO =
  'min-h-[44px] w-full rounded-lg border border-zinc-800 bg-[#121215] px-3 text-sm text-zinc-200 ' +
  `outline-none placeholder:text-zinc-500 ${FOCO} md:min-h-[36px]`;

const normalizar = (v) => v.normalize('NFD').replace(/[̀-ͯ]/g, '').toLocaleLowerCase('pt-BR');

export function filtrarObras(linhas, busca, status) {
  const termo = normalizar(busca.trim());
  return linhas.filter((linha) =>
    (!termo || normalizar(linha.nome).includes(termo)) &&
    (status === 'TODOS' || linha.statusId === status),
  );
}

export default function ObrasPage() {
  const { obras, carregando, erro, recarregar } = useObras();
  const [busca, setBusca] = useState('');
  const [status, setStatus] = useState('TODOS');
  // obra nula + aberto = cadastro; obra preenchida = edição
  const [form, setForm] = useState({ aberto: false, obra: null });

  const linhas = useMemo(
    () => (obras ?? []).map((dto) => ({ ...paraHeader(dto), id: dto.id, statusId: dto.status, dto })),
    [obras],
  );
  const visiveis = useMemo(() => filtrarObras(linhas, busca, status), [linhas, busca, status]);

  return (
    <div className={CASCA}>
      <header className="flex items-start justify-between gap-3">
        <div>
          <h1 className="text-lg font-semibold text-white">Obras</h1>
          <p className="text-xs text-zinc-500">Obras que você pode visualizar</p>
        </div>
        {/* sem obraId => permissão global (ADR-11): ainda não existe obra para perguntar */}
        <Can modulo="DETALHES_OBRA" acao="CRIAR">
          <button type="button" className={`${BOTAO_PRIMARIO} min-h-[44px] md:min-h-[36px]`}
                  onClick={() => setForm({ aberto: true, obra: null })}>
            <Plus size={14} /> Nova obra
          </button>
        </Can>
      </header>

      {erro && <ErrorState mensagem={mensagemErro(erro)} onTentarNovamente={recarregar} />}
      {carregando && <Spinner className="py-16" />}
      {obras && obras.length === 0 && <EmptyState mensagem="Nenhuma obra disponível para visualização." />}

      {obras && obras.length > 0 && (
        <>
          <div className="flex flex-col gap-3 sm:flex-row sm:items-center">
            <label className="relative sm:max-w-xs sm:flex-1">
              <span className="sr-only">Buscar obra</span>
              <Search size={14} className="pointer-events-none absolute left-3 top-1/2 -translate-y-1/2 text-zinc-500" />
              <input name="busca" placeholder="Buscar por nome da obra" value={busca}
                     onChange={(e) => setBusca(e.target.value)} className={`${CAMPO_ESCURO} pl-9`} />
            </label>
            <label className="sm:w-48">
              <span className="sr-only">Status</span>
              <select id="status" className={CAMPO_ESCURO} value={status} onChange={(e) => setStatus(e.target.value)}>
                <option value="TODOS">Todos os status</option>
                {STATUS.map(([valor, rotulo]) => <option key={valor} value={valor}>{rotulo}</option>)}
              </select>
            </label>
            <p className="text-xs text-zinc-500 sm:ml-auto">{visiveis.length} de {linhas.length} obras</p>
          </div>

          {visiveis.length === 0 ? (
            <EmptyState mensagem="Nenhuma obra corresponde aos filtros." />
          ) : (
            <div className={`${CARD} overflow-x-auto`}>
              <table className="w-full text-sm">
                <thead className="text-left text-xs text-zinc-500">
                  <tr>
                    <th className="px-4 py-3 font-medium">Obra</th>
                    <th className="px-4 py-3 font-medium">Tipologia</th>
                    <th className="px-4 py-3 font-medium">Localização</th>
                    <th className="px-4 py-3 font-medium">Previsão</th>
                    <th className="px-4 py-3 font-medium">Status</th>
                    <th className="px-4 py-3 text-right font-medium">Ações</th>
                  </tr>
                </thead>
                <tbody>
                  {visiveis.map((obra) => (
                    <tr key={obra.id} className="border-t border-zinc-800">
                      <td className="px-4 py-3 font-medium text-white">{obra.nome}</td>
                      <td className="px-4 py-3 text-zinc-400">{obra.tipologia}</td>
                      <td className="px-4 py-3 text-zinc-400">{obra.localizacao}</td>
                      <td className="px-4 py-3 text-zinc-400">{obra.previsao}</td>
                      <td className="px-4 py-3">
                        <Badge tom={obra.statusTom} dot>{obra.status}</Badge>
                      </td>
                      <td className="px-4 py-3">
                        <div className="flex items-center justify-end gap-2">
                          {/* com obraId => permissão daquela obra (cargo da alocação + exceções) */}
                          <Can modulo="DETALHES_OBRA" acao="EDITAR" obraId={obra.id}>
                            <IconButton icon={Pencil} label={`Editar obra ${obra.nome}`}
                                        onClick={() => setForm({ aberto: true, obra: obra.dto })} />
                          </Can>
                          <Link className={`px-2 ${LINK}`}
                                to={`/obras/${obra.id}/resumo`}>
                            Abrir
                          </Link>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}

      <ObraFormModal
        aberto={form.aberto}
        obra={form.obra}
        onFechar={() => setForm({ aberto: false, obra: null })}
        onSalvo={() => { setForm({ aberto: false, obra: null }); recarregar(); }}
      />
    </div>
  );
}
