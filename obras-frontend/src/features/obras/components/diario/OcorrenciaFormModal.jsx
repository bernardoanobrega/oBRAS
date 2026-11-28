import { useEffect, useState } from 'react';
import Button from '../../../../shared/components/ui/Button.jsx';
import Input from '../../../../shared/components/ui/Input.jsx';
import Modal from '../../../../shared/components/ui/Modal.jsx';
import TextArea from '../../../../shared/components/ui/TextArea.jsx';
import { estadoDoErro } from '../../adapters/estadoDoErro.js';
import { paraFormularioOcorrencia } from '../../adapters/diarioF3.js';
import { CATEGORIAS, payloadOcorrencia, validarOcorrencia } from '../../adapters/validarOcorrencia.js';
import ErroFormulario from './ErroFormulario.jsx';

const CAMPO = 'min-h-[44px] w-full rounded-lg border bg-fundo px-3 text-sm text-texto outline-none md:min-h-[36px]';

export default function OcorrenciaFormModal({ aberto = false, ocorrencia = null, onFechar, onSalvar, onSalvo }) {
  const [form, setForm] = useState(() => paraFormularioOcorrencia(ocorrencia));
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState(null);
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    if (!aberto) return;
    setForm(paraFormularioOcorrencia(ocorrencia));
    setErros({});
    setErroGeral(null);
  }, [aberto, ocorrencia]);

  async function enviar(evento) {
    evento.preventDefault();
    const validacao = validarOcorrencia(form);
    if (!validacao.valido) {
      setErros(validacao.erros);
      setErroGeral(null);
      return;
    }
    setEnviando(true);
    setErros({});
    setErroGeral(null);
    try {
      await onSalvar(payloadOcorrencia(form));
      onSalvo?.();
    } catch (err) {
      const estado = estadoDoErro(err);
      setErros(estado.erros);
      setErroGeral(estado.erroGeral);
    } finally {
      setEnviando(false);
    }
  }

  const paralisacao = form.paralisacao ?? { inicio: '', fim: '' };

  return (
    <Modal aberto={aberto} titulo={ocorrencia?.id ? 'Editar ocorrência' : 'Nova ocorrência'} onFechar={onFechar}>
      <form onSubmit={enviar} noValidate className="grid grid-cols-1 gap-3">
        <div>
          <label htmlFor="ocorrencia-categoria" className="mb-1 block text-sm font-medium text-texto">Categoria *</label>
          <select
            id="ocorrencia-categoria"
            name="categoria"
            value={form.categoria}
            onChange={(e) => setForm((atual) => ({ ...atual, categoria: e.target.value }))}
            className={`${CAMPO} ${erros.categoria ? 'border-perigo' : 'border-borda-forte'}`}
          >
            <option value="">Selecione...</option>
            {Object.entries(CATEGORIAS).map(([valor, rotulo]) => (
              <option key={valor} value={valor}>{rotulo}</option>
            ))}
          </select>
          {erros.categoria && <p className="mt-1 text-xs text-perigo">{erros.categoria}</p>}
        </div>

        <TextArea
          label="Descrição *"
          id="ocorrencia-descricao"
          name="descricao"
          value={form.descricao}
          maxLength={2000}
          rows={3}
          erro={erros.descricao}
          onChange={(e) => setForm((atual) => ({ ...atual, descricao: e.target.value }))}
        />

        <Input
          label="Data e hora (opcional)"
          id="ocorrencia-data-hora"
          name="dataHora"
          type="datetime-local"
          value={form.dataHora}
          onChange={(e) => setForm((atual) => ({ ...atual, dataHora: e.target.value }))}
        />

        <fieldset className="rounded-lg border border-borda p-3">
          <legend className="px-1 text-sm font-medium text-texto">Paralisação</legend>
          <label className="flex min-h-[44px] items-center gap-2 text-sm text-texto md:min-h-[36px]">
            <input
              type="checkbox"
              id="ocorrencia-com-paralisacao"
              checked={!!form.paralisacao}
              onChange={(e) =>
                setForm((atual) => ({
                  ...atual,
                  paralisacao: e.target.checked ? { inicio: '', fim: '' } : null,
                }))
              }
            />
            Esta ocorrência paralisou a obra
          </label>
          {form.paralisacao && (
            <div className="mt-2 grid grid-cols-1 gap-3 md:grid-cols-2">
              <Input
                label="Início *"
                id="paralisacao-inicio"
                name="paralisacaoInicio"
                type="datetime-local"
                value={paralisacao.inicio}
                erro={erros['paralisacao.inicio']}
                onChange={(e) =>
                  setForm((atual) => ({
                    ...atual,
                    paralisacao: { ...(atual.paralisacao ?? {}), inicio: e.target.value },
                  }))
                }
              />
              <Input
                label="Fim (opcional)"
                id="paralisacao-fim"
                name="paralisacaoFim"
                type="datetime-local"
                value={paralisacao.fim}
                erro={erros['paralisacao.fim']}
                onChange={(e) =>
                  setForm((atual) => ({
                    ...atual,
                    paralisacao: { ...(atual.paralisacao ?? {}), fim: e.target.value },
                  }))
                }
              />
            </div>
          )}
        </fieldset>

        <ErroFormulario erroGeral={erroGeral} />
        <footer className="flex flex-wrap justify-end gap-2">
          <Button type="button" variante="secundario" onClick={onFechar}>Cancelar</Button>
          <Button type="submit" disabled={enviando}>{enviando ? 'Salvando...' : 'Salvar ocorrência'}</Button>
        </footer>
      </form>
    </Modal>
  );
}
