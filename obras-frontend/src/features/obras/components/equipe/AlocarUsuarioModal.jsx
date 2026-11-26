import { useEffect, useState } from 'react';
import Modal from '../../../../shared/components/ui/Modal.jsx';
import Spinner from '../../../../shared/components/ui/Spinner.jsx';
import { errosPorCampo, mensagemErro } from '../../../../shared/api/http.js';
import { alocar, alterarCargos, listarCandidatos, listarCargos } from '../../api/equipeApi.js';
import { BOTAO_PRIMARIO, BOTAO_SECUNDARIO, FOCO } from '../detalhe/tokens.js';
import CargosSelect, { alternarCargo } from './CargosSelect.jsx';

/**
 * Validação de conforto. A unicidade (usuário já alocado) é do backend: aqui só exibimos o erro dele.
 * Na edição o usuário não muda, então só os cargos são conferidos.
 */
export function validarAlocacao(form, edicao = false) {
  const erros = {};
  if (!edicao && !form.usuarioId) erros.usuarioId = 'Escolha quem será alocado.';
  if (!form.cargoIds?.length) erros.cargoIds = 'Escolha ao menos um cargo.';
  return erros;
}

/** Corpo do POST (alocar) ou do PUT de cargos, conforme o L22. */
export function paraCorpoAlocacao(form, edicao = false) {
  return edicao
    ? { cargoIds: form.cargoIds }
    : { usuarioId: Number(form.usuarioId), cargoIds: form.cargoIds };
}

/** Erro da API (ApiError) → erros por campo + mensagem geral (ex.: 409/422 de duplicidade). */
export function estadoDoErroAlocacao(err) {
  return { erros: errosPorCampo(err), erroGeral: mensagemErro(err) };
}

const CAMPO = `min-h-[44px] w-full rounded-lg border bg-fundo px-3 text-sm text-texto outline-none md:min-h-[36px] ${FOCO}`;

/** Só desenha; não guarda estado. Separado para ser testado sem navegador. */
export function AlocacaoFormulario({
  edicao = false,
  nomeUsuario = null,
  candidatos = [],
  cargos = [],
  form,
  erros = {},
  erroGeral = null,
  enviando = false,
  onMudarUsuario,
  onAlternarCargo,
  onEnviar,
  onCancelar,
}) {
  return (
    <form onSubmit={onEnviar} noValidate className="space-y-3">
      {edicao ? (
        <p className="text-sm text-texto-suave">
          Profissional: <span className="font-medium text-texto">{nomeUsuario}</span>
        </p>
      ) : (
        <div>
          <label htmlFor="usuarioId" className="mb-1 block text-xs font-medium text-texto-suave">Profissional *</label>
          <select id="usuarioId" name="usuarioId" value={form.usuarioId} onChange={onMudarUsuario}
                  className={`${CAMPO} ${erros.usuarioId ? 'border-perigo' : 'border-borda'}`}>
            <option value="">Selecione...</option>
            {candidatos.map((u) => <option key={u.id} value={u.id}>{u.nome}</option>)}
          </select>
          {erros.usuarioId && <p className="mt-1 text-xs text-perigo">{erros.usuarioId}</p>}
        </div>
      )}

      <CargosSelect cargos={cargos} selecionados={form.cargoIds} onAlternar={onAlternarCargo} erro={erros.cargoIds} />

      {erroGeral && (
        <p role="alert" className="rounded-lg border border-perigo/40 bg-perigo-fundo p-2 text-sm text-perigo">{erroGeral}</p>
      )}

      <div className="flex justify-end gap-2 pt-2">
        <button type="button" onClick={onCancelar} className={`${BOTAO_SECUNDARIO} min-h-[44px] md:min-h-[36px]`}>
          Cancelar
        </button>
        <button type="submit" disabled={enviando}
                className={`${BOTAO_PRIMARIO} min-h-[44px] disabled:cursor-not-allowed disabled:opacity-60 md:min-h-[36px]`}>
          {enviando ? 'Salvando...' : edicao ? 'Salvar cargos' : 'Alocar'}
        </button>
      </div>
    </form>
  );
}

/** alocacao nula => alocar alguém (POST); alocacao preenchida => alterar os cargos dela (PUT). */
export default function AlocarUsuarioModal({ aberto, obraId, alocacao, onFechar, onSalvo }) {
  const edicao = !!alocacao;
  const [form, setForm] = useState({ usuarioId: '', cargoIds: [] });
  const [opcoes, setOpcoes] = useState({ carregando: true, erro: null, cargos: [], candidatos: [] });
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState(null);
  const [enviando, setEnviando] = useState(false);

  // Ao abrir: preenche o formulário e busca cargos (e candidatos, se for alocar).
  useEffect(() => {
    if (!aberto) return undefined;
    setForm({ usuarioId: '', cargoIds: alocacao ? alocacao.cargos.map((c) => c.id) : [] });
    setErros({});
    setErroGeral(null);
    setOpcoes({ carregando: true, erro: null, cargos: [], candidatos: [] });

    let vivo = true;
    Promise.all([listarCargos(), alocacao ? Promise.resolve([]) : listarCandidatos(obraId)])
      .then(([cargos, candidatos]) => vivo && setOpcoes({ carregando: false, erro: null, cargos, candidatos }))
      .catch((e) => vivo && setOpcoes({ carregando: false, erro: mensagemErro(e), cargos: [], candidatos: [] }));
    return () => {
      vivo = false;
    };
  }, [aberto, alocacao, obraId]);

  async function enviar(e) {
    e.preventDefault();
    const errosLocais = validarAlocacao(form, edicao);
    setErros(errosLocais);
    setErroGeral(null);
    if (Object.keys(errosLocais).length > 0) return;

    setEnviando(true);
    try {
      const corpo = paraCorpoAlocacao(form, edicao);
      if (edicao) await alterarCargos(obraId, alocacao.id, corpo);
      else await alocar(obraId, corpo);
      onSalvo();
    } catch (err) {
      const r = estadoDoErroAlocacao(err);
      setErros(r.erros);
      setErroGeral(r.erroGeral);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <Modal aberto={aberto} onFechar={onFechar} titulo={edicao ? 'Alterar cargos' : 'Alocar profissional'}>
      {opcoes.carregando && <Spinner className="py-8" />}
      {!opcoes.carregando && opcoes.erro && (
        <p role="alert" className="rounded-lg border border-perigo/40 bg-perigo-fundo p-2 text-sm text-perigo">{opcoes.erro}</p>
      )}
      {!opcoes.carregando && !opcoes.erro && (
        <AlocacaoFormulario
          edicao={edicao}
          nomeUsuario={alocacao?.nome}
          candidatos={opcoes.candidatos}
          cargos={opcoes.cargos}
          form={form}
          erros={erros}
          erroGeral={erroGeral}
          enviando={enviando}
          onMudarUsuario={(e) => setForm((f) => ({ ...f, usuarioId: e.target.value }))}
          onAlternarCargo={(id) => setForm((f) => ({ ...f, cargoIds: alternarCargo(f.cargoIds, id) }))}
          onEnviar={enviar}
          onCancelar={onFechar}
        />
      )}
    </Modal>
  );
}
