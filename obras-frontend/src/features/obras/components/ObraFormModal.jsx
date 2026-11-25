import { useEffect, useState } from 'react';
import Modal from '../../../shared/components/ui/Modal.jsx';
import { errosPorCampo, mensagemErro } from '../../../shared/api/http.js';
import { criarObra, editarObra } from '../api/obrasApi.js';
import { BOTAO_PRIMARIO, BOTAO_SECUNDARIO, FOCO } from './detalhe/tokens.js';

export const TIPOLOGIAS = [
  ['RESIDENCIAL', 'Residencial'],
  ['COMERCIAL', 'Comercial'],
  ['INDUSTRIAL', 'Industrial'],
  ['INFRAESTRUTURA', 'Infraestrutura'],
  ['REFORMA', 'Reforma'],
  ['RESTAURO', 'Restauro'],
];

// Mesmos limites das colunas da tabela obra (V2).
const LIMITES = { nome: 200, endereco: 300, descricao: 1000 };

const CLASSE_CAMPO = `w-full rounded-lg border bg-[#111111] px-3 text-sm text-zinc-200 outline-none ${FOCO}`;

/** Formulário vazio (cadastro) ou preenchido com o ObraDto (edição). */
export function formularioInicial(obra) {
  return {
    nome: obra?.nome ?? '',
    tipologia: obra?.tipologia ?? '',
    endereco: obra?.endereco ?? '',
    descricao: obra?.descricao ?? '',
    inicioPrevisto: obra?.inicioPrevisto ?? '',
    fimPrevisto: obra?.fimPrevisto ?? '',
  };
}

/**
 * Validação de conforto, antes de enviar. A validação que vale é a do backend (L11/L12).
 * Devolve { campo: mensagem }; objeto vazio = formulário válido.
 * Responsável técnico não é campo daqui: a obra nasce sem ele (ADR-05) e ele é definido no L13.
 */
export function validarObra(form) {
  const erros = {};
  const preenchido = (v) => typeof v === 'string' && v.trim() !== '';

  if (!preenchido(form.nome)) erros.nome = 'Informe o nome da obra.';
  else if (form.nome.trim().length > LIMITES.nome) erros.nome = `Máximo de ${LIMITES.nome} caracteres.`;

  if (!preenchido(form.tipologia)) erros.tipologia = 'Escolha a tipologia.';

  if (!preenchido(form.endereco)) erros.endereco = 'Informe o endereço.';
  else if (form.endereco.trim().length > LIMITES.endereco) erros.endereco = `Máximo de ${LIMITES.endereco} caracteres.`;

  if ((form.descricao ?? '').trim().length > LIMITES.descricao) {
    erros.descricao = `Máximo de ${LIMITES.descricao} caracteres.`;
  }

  if (!preenchido(form.inicioPrevisto)) erros.inicioPrevisto = 'Informe o início previsto.';
  if (!preenchido(form.fimPrevisto)) erros.fimPrevisto = 'Informe o fim previsto.';

  // Datas no formato AAAA-MM-DD: comparar o texto é o mesmo que comparar a data.
  if (!erros.inicioPrevisto && !erros.fimPrevisto && form.fimPrevisto < form.inicioPrevisto) {
    erros.fimPrevisto = 'O fim previsto não pode ser antes do início.';
  }

  return erros;
}

/** Corpo do POST/PUT conforme o L12: sem status e sem responsavelId. */
export function paraCorpoObra(form) {
  const descricao = form.descricao.trim();
  return {
    nome: form.nome.trim(),
    tipologia: form.tipologia,
    endereco: form.endereco.trim(),
    descricao: descricao === '' ? null : descricao,
    inicioPrevisto: form.inicioPrevisto,
    fimPrevisto: form.fimPrevisto,
  };
}

/** Converte o erro da API (ApiError) no que o formulário mostra: erros por campo + mensagem geral. */
export function estadoDoErro(err) {
  return { erros: errosPorCampo(err), erroGeral: mensagemErro(err) };
}

/** Campo escuro (padrão Figma). Mostra a mensagem de erro do campo logo abaixo. */
function Campo({ rotulo, nome, erro, children }) {
  return (
    <div>
      <label htmlFor={nome} className="mb-1 block text-xs font-medium text-zinc-400">{rotulo}</label>
      {children}
      {erro && <p className="mt-1 text-xs text-red-400">{erro}</p>}
    </div>
  );
}

const classeCampo = (erro, extra = 'min-h-[44px] md:min-h-[36px]') =>
  `${CLASSE_CAMPO} ${extra} ${erro ? 'border-red-500' : 'border-zinc-800'}`;

/** Só desenha os campos; não guarda estado. Separado para poder ser testado sem navegador. */
export function ObraFormulario({ form, erros = {}, erroGeral = null, enviando = false, onMudar, onEnviar, onCancelar }) {
  const props = (nome) => ({ id: nome, name: nome, value: form[nome], onChange: onMudar?.(nome) });
  return (
    <form onSubmit={onEnviar} noValidate className="space-y-3">
      <Campo rotulo="Nome *" nome="nome" erro={erros.nome}>
        <input {...props('nome')} maxLength={LIMITES.nome} className={classeCampo(erros.nome)} />
      </Campo>

      <Campo rotulo="Tipologia *" nome="tipologia" erro={erros.tipologia}>
        <select {...props('tipologia')} className={classeCampo(erros.tipologia)}>
          <option value="">Selecione...</option>
          {TIPOLOGIAS.map(([valor, rotulo]) => <option key={valor} value={valor}>{rotulo}</option>)}
        </select>
      </Campo>

      <Campo rotulo="Endereço *" nome="endereco" erro={erros.endereco}>
        <input {...props('endereco')} maxLength={LIMITES.endereco} className={classeCampo(erros.endereco)} />
      </Campo>

      <div className="grid gap-3 sm:grid-cols-2">
        <Campo rotulo="Início previsto *" nome="inicioPrevisto" erro={erros.inicioPrevisto}>
          <input type="date" {...props('inicioPrevisto')} className={`${classeCampo(erros.inicioPrevisto)} [color-scheme:dark]`} />
        </Campo>
        <Campo rotulo="Fim previsto *" nome="fimPrevisto" erro={erros.fimPrevisto}>
          <input type="date" {...props('fimPrevisto')} className={`${classeCampo(erros.fimPrevisto)} [color-scheme:dark]`} />
        </Campo>
      </div>

      <Campo rotulo="Descrição" nome="descricao" erro={erros.descricao}>
        <textarea {...props('descricao')} rows={3} maxLength={LIMITES.descricao}
                  className={classeCampo(erros.descricao, 'py-2')} />
      </Campo>

      {erroGeral && (
        <p role="alert" className="rounded-lg border border-red-500/40 bg-red-500/10 p-2 text-sm text-red-400">{erroGeral}</p>
      )}

      <div className="flex justify-end gap-2 pt-2">
        <button type="button" onClick={onCancelar} className={`${BOTAO_SECUNDARIO} min-h-[44px] md:min-h-[36px]`}>
          Cancelar
        </button>
        <button type="submit" disabled={enviando}
                className={`${BOTAO_PRIMARIO} min-h-[44px] disabled:cursor-not-allowed disabled:opacity-60 md:min-h-[36px]`}>
          {enviando ? 'Salvando...' : 'Salvar'}
        </button>
      </div>
    </form>
  );
}

/** obra nula => cadastro (POST); obra preenchida (ObraDto) => edição (PUT). */
export default function ObraFormModal({ aberto, obra, onFechar, onSalvo }) {
  const edicao = !!obra;
  const [form, setForm] = useState(formularioInicial(null));
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState(null);
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    if (aberto) {
      setForm(formularioInicial(obra));
      setErros({});
      setErroGeral(null);
    }
  }, [aberto, obra]);

  const mudar = (campo) => (e) => setForm((f) => ({ ...f, [campo]: e.target.value }));

  async function enviar(e) {
    e.preventDefault();
    const errosLocais = validarObra(form);
    setErros(errosLocais);
    setErroGeral(null);
    if (Object.keys(errosLocais).length > 0) return;

    setEnviando(true);
    try {
      const corpo = paraCorpoObra(form);
      if (edicao) await editarObra(obra.id, corpo);
      else await criarObra(corpo);
      onSalvo();
    } catch (err) {
      const r = estadoDoErro(err);
      setErros(r.erros);
      setErroGeral(r.erroGeral);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <Modal aberto={aberto} onFechar={onFechar} titulo={edicao ? 'Editar obra' : 'Nova obra'} tema="escuro">
      <ObraFormulario form={form} erros={erros} erroGeral={erroGeral} enviando={enviando}
                      onMudar={mudar} onEnviar={enviar} onCancelar={onFechar} />
    </Modal>
  );
}
