import { useCallback, useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { mensagemErro } from '../../../shared/api/http.js';
import EmptyState from '../../../shared/components/feedback/EmptyState.jsx';
import ErrorState from '../../../shared/components/feedback/ErrorState.jsx';
import Badge from '../../../shared/components/ui/Badge.jsx';
import Input from '../../../shared/components/ui/Input.jsx';
import Spinner from '../../../shared/components/ui/Spinner.jsx';
import { paraHeader } from '../api/adaptadores.js';
import { listarObras } from '../api/obrasApi.js';

const TOM_CLARO = {
  green: 'verde',
  blue: 'azul',
  amber: 'amarelo',
  red: 'vermelho',
  neutral: 'cinza',
};

const STATUS = [
  ['PLANEJADA', 'Planejada'],
  ['EM_ANDAMENTO', 'Em Andamento'],
  ['PARALISADA', 'Paralisada'],
  ['CANCELADA', 'Cancelada'],
  ['CONCLUIDA', 'Concluída'],
];

const normalizar = (v) => v.normalize('NFD').replace(/[\u0300-\u036f]/g, '').toLocaleLowerCase('pt-BR');

export function filtrarObras(linhas, busca, status) {
  const termo = normalizar(busca.trim());
  return linhas.filter((linha) =>
    (!termo || normalizar(linha.nome).includes(termo)) &&
    (status === 'TODOS' || linha.statusId === status),
  );
}

export default function ObrasPage() {
  const [obras, setObras] = useState(null);
  const [erro, setErro] = useState(null);
  const [busca, setBusca] = useState('');
  const [status, setStatus] = useState('TODOS');

  const carregar = useCallback(async () => {
    setErro(null);
    try {
      setObras(await listarObras());
    } catch (e) {
      setErro(mensagemErro(e));
    }
  }, []);

  useEffect(() => {
    carregar();
  }, [carregar]);

  const linhas = useMemo(
    () => (obras ?? []).map((dto) => ({ ...paraHeader(dto), id: dto.id, statusId: dto.status })),
    [obras],
  );
  const visiveis = useMemo(() => filtrarObras(linhas, busca, status), [linhas, busca, status]);

  return (
    <div>
      <h1 className="mb-4 text-xl font-semibold">Obras</h1>

      {erro && <ErrorState mensagem={erro} onTentarNovamente={carregar} />}
      {!erro && !obras && <Spinner className="mt-10" />}
      {obras && obras.length === 0 && <EmptyState mensagem="Nenhuma obra disponível para visualização." />}

      {obras && obras.length > 0 && (
        <>
          <div className="mb-4 flex flex-col gap-3 sm:flex-row sm:items-end">
            <Input
              label="Buscar obra"
              name="busca"
              placeholder="Nome da obra"
              value={busca}
              onChange={(e) => setBusca(e.target.value)}
              className="sm:max-w-xs sm:flex-1"
            />
            <div className="sm:w-56">
              <label htmlFor="status" className="mb-1 block text-sm font-medium text-texto">Status</label>
              <select
                id="status"
                className="min-h-[44px] w-full rounded-lg border border-borda-forte px-3 text-sm outline-none focus:ring-2 focus:ring-brand md:min-h-[36px]"
                value={status}
                onChange={(e) => setStatus(e.target.value)}
              >
                <option value="TODOS">Todos</option>
                {STATUS.map(([valor, rotulo]) => <option key={valor} value={valor}>{rotulo}</option>)}
              </select>
            </div>
            <p className="text-sm text-texto-suave sm:ml-auto">{visiveis.length} de {linhas.length} obras</p>
          </div>

          {visiveis.length === 0 ? (
            <EmptyState mensagem="Nenhuma obra corresponde aos filtros." />
          ) : (
            <div className="overflow-x-auto rounded-xl border border-borda bg-superficie">
              <table className="w-full text-sm">
                <thead className="bg-superficie-alta text-left text-texto-suave">
                  <tr>
                    <th className="px-4 py-3">Obra</th>
                    <th className="px-4 py-3">Tipologia</th>
                    <th className="px-4 py-3">Localização</th>
                    <th className="px-4 py-3">Previsão</th>
                    <th className="px-4 py-3">Status</th>
                    <th className="px-4 py-3 text-right">Ações</th>
                  </tr>
                </thead>
                <tbody>
                  {visiveis.map((obra) => (
                    <tr key={obra.id} className="border-t">
                      <td className="px-4 py-3 font-medium">{obra.nome}</td>
                      <td className="px-4 py-3">{obra.tipologia}</td>
                      <td className="px-4 py-3">{obra.localizacao}</td>
                      <td className="px-4 py-3">{obra.previsao}</td>
                      <td className="px-4 py-3">
                        <Badge tom={TOM_CLARO[obra.statusTom] ?? 'cinza'}>{obra.status}</Badge>
                      </td>
                      <td className="px-4 py-3 text-right">
                        <Link className="font-medium text-brand-claro hover:underline" to={`/obras/${obra.id}/resumo`}>
                          Abrir
                        </Link>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
    </div>
  );
}
