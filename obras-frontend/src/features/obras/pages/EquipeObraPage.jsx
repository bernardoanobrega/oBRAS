import { useState } from 'react';
import { useParams } from 'react-router-dom';
import { Pencil, UserMinus, UserPlus } from 'lucide-react';
import { mensagemErro } from '../../../shared/api/http.js';
import Can from '../../../shared/auth/Can.jsx';
import EmptyState from '../../../shared/components/feedback/EmptyState.jsx';
import ErrorState from '../../../shared/components/feedback/ErrorState.jsx';
import Spinner from '../../../shared/components/ui/Spinner.jsx';
import { encerrarAlocacao } from '../api/equipeApi.js';
import { useEquipe } from '../api/useEquipe.js';
import { Avatar, Badge, IconButton } from '../components/detalhe/primitivos.jsx';
import { BOTAO_PRIMARIO, CARD } from '../components/detalhe/tokens.js';
import AlocarUsuarioModal from '../components/equipe/AlocarUsuarioModal.jsx';

function mensagemFalha(erro) {
  if (erro?.mensagem) return erro.mensagem;
  const status = erro?.response?.status;
  if (status === 403) return 'Você não tem permissão para visualizar esta obra.';
  if (status === 404) return 'Obra não encontrada.';
  return mensagemErro(erro);
}

/**
 * Pergunta antes de encerrar; só chama a API se a pessoa confirmar.
 * confirmar e encerrar são parâmetros para poder testar sem navegador.
 * Devolve true se encerrou, false se a pessoa cancelou.
 */
export async function encerrarComConfirmacao(alocacao, { confirmar = window.confirm, encerrar }) {
  const ok = confirmar(
    `Encerrar a alocação de ${alocacao.nome}? A pessoa deixa a equipe desta obra, mas o histórico é mantido.`,
  );
  if (!ok) return false;
  await encerrar(alocacao.id);
  return true;
}

function ResponsavelTecnico({ responsavel }) {
  return (
    <section className={`${CARD} flex flex-wrap items-center gap-x-8 gap-y-3 p-5`}>
      <div>
        <p className="text-xs text-texto-fraco">Responsável técnico</p>
        {responsavel?.definido ? (
          <p className="flex flex-wrap items-center gap-2 text-sm font-medium text-texto">
            {responsavel.nome ?? 'Nome indisponível'}
            {!responsavel.naEquipe && <Badge tom="neutral">fora da equipe</Badge>}
          </p>
        ) : (
          <p className="text-sm text-texto-suave">Não definido</p>
        )}
      </div>
      <div>
        <p className="text-xs text-texto-fraco">ART da obra</p>
        <p className="text-sm font-medium text-texto">{responsavel?.art ?? '—'}</p>
      </div>
    </section>
  );
}

export default function EquipeObraPage() {
  const { obraId } = useParams();
  const { obra, alocacoes, responsavel, carregando, erro, recarregar } = useEquipe(obraId);
  // alocacao nula + aberto = alocar; alocacao preenchida = alterar cargos
  const [modal, setModal] = useState({ aberto: false, alocacao: null });
  const [erroAcao, setErroAcao] = useState(null);
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

  async function encerrar(alocacao) {
    setErroAcao(null);
    try {
      const encerrou = await encerrarComConfirmacao(alocacao, { encerrar: (id) => encerrarAlocacao(obraId, id) });
      if (encerrou) recarregar();
    } catch (e) {
      setErroAcao(mensagemErro(e));
    }
  }

  return (
    <div className={casca}>
      <h2 className="sr-only">Equipe da Obra</h2>
      <header className="flex flex-wrap items-center justify-between gap-3">
        <div className="flex flex-wrap items-center gap-3">
          <h3 className="text-xl font-semibold text-texto">Equipe · {obra.nome}</h3>
          <Badge tom={obra.statusTom ?? 'green'} dot>{obra.status}</Badge>
        </div>
        <Can modulo="DETALHES_OBRA" acao="CRIAR" obraId={obraId}>
          <button type="button" className={`${BOTAO_PRIMARIO} min-h-[44px] md:min-h-[36px]`}
                  onClick={() => setModal({ aberto: true, alocacao: null })}>
            <UserPlus size={14} /> Alocar profissional
          </button>
        </Can>
      </header>

      <ResponsavelTecnico responsavel={responsavel} />

      {erroAcao && (
        <p role="alert" className="rounded-lg border border-perigo/40 bg-perigo-fundo p-2 text-sm text-perigo">{erroAcao}</p>
      )}

      <section className={`${CARD} p-5`}>
        <h3 className="text-sm font-semibold text-texto">Profissionais alocados</h3>
        <p className="text-xs text-texto-fraco">
          {alocacoes.length} {alocacoes.length === 1 ? 'profissional' : 'profissionais'}
        </p>

        {alocacoes.length === 0 ? (
          <div className="mt-4">
            <EmptyState mensagem="Nenhum profissional alocado nesta obra." />
          </div>
        ) : (
          <div className="mt-4 overflow-x-auto">
            <table className="w-full text-left text-xs">
              <thead className="text-[10px] tracking-wider text-texto-fraco">
                <tr>
                  <th className="py-2 font-normal">PROFISSIONAL</th>
                  <th className="font-normal">CARGOS</th>
                  <th className="font-normal">DESDE</th>
                  <th />
                </tr>
              </thead>
              <tbody>
                {alocacoes.map((a) => (
                  <tr key={a.id} className="h-14 border-t border-borda">
                    <td>
                      <div className="flex items-center gap-3">
                        <Avatar tom={a.tom}>{a.sigla}</Avatar>
                        <p className="whitespace-nowrap text-texto">{a.nome}</p>
                      </div>
                    </td>
                    <td>
                      <div className="flex flex-wrap gap-1">
                        {a.cargos.map((c) => <Badge key={c.id} tom={a.tom}>{c.nome}</Badge>)}
                      </div>
                    </td>
                    <td className="whitespace-nowrap text-texto-suave">{a.desde}</td>
                    <td>
                      <Can modulo="DETALHES_OBRA" acao="EDITAR" obraId={obraId}>
                        <div className="flex justify-end gap-2">
                          <IconButton icon={Pencil} label={`Alterar cargos de ${a.nome}`}
                                      onClick={() => setModal({ aberto: true, alocacao: a })} />
                          <IconButton icon={UserMinus} label={`Encerrar alocação de ${a.nome}`}
                                      onClick={() => encerrar(a)} />
                        </div>
                      </Can>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </section>

      <AlocarUsuarioModal
        aberto={modal.aberto}
        obraId={obraId}
        alocacao={modal.alocacao}
        onFechar={() => setModal({ aberto: false, alocacao: null })}
        onSalvo={() => { setModal({ aberto: false, alocacao: null }); recarregar(); }}
      />
    </div>
  );
}
