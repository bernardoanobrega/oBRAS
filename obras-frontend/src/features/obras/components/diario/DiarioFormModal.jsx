import { useEffect, useState } from 'react';
import Button from '../../../../shared/components/ui/Button.jsx';
import Input from '../../../../shared/components/ui/Input.jsx';
import Modal from '../../../../shared/components/ui/Modal.jsx';
import TextArea from '../../../../shared/components/ui/TextArea.jsx';
import { CLIMAS, paraFormularioDiario } from '../../adapters/diarioF3.js';
import { estadoDoErro } from '../../adapters/estadoDoErro.js';
import { validarDiario } from '../../adapters/validarDiario.js';
import { useAlocacoesObra } from '../../hooks/useAlocacoesObra.js';
import ErroFormulario from './ErroFormulario.jsx';

const CAMPO = 'min-h-[44px] w-full rounded-lg border bg-fundo px-3 text-sm text-texto outline-none md:min-h-[36px]';

export default function DiarioFormModal({ aberto = false, obraId, diario = null, onFechar, onSalvar, onSalvo }) {
  const [form, setForm] = useState(() => paraFormularioDiario(diario));
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState(null);
  const [enviando, setEnviando] = useState(false);
  const { alocacoes, erro: erroEquipe } = useAlocacoesObra(obraId, aberto);

  useEffect(() => {
    if (!aberto) return;
    setForm(paraFormularioDiario(diario));
    setErros({});
    setErroGeral(null);
  }, [aberto, diario]);

  async function enviar(evento) {
    evento.preventDefault();
    const validacao = validarDiario(form);
    if (!validacao.valido) {
      setErros(validacao.erros);
      setErroGeral(null);
      return;
    }
    setEnviando(true);
    setErros({});
    setErroGeral(null);
    try {
      await onSalvar(form);
      onSalvo?.();
    } catch (err) {
      const estado = estadoDoErro(err);
      setErros(estado.erros);
      setErroGeral(estado.erroGeral);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <Modal aberto={aberto} titulo={diario?.id ? 'Editar diário' : 'Novo diário'} onFechar={onFechar}>
      <form onSubmit={enviar} noValidate className="grid grid-cols-1 gap-3 md:grid-cols-2">
        <Input
          label="Data"
          id="diario-data"
          name="data"
          type="date"
          value={form.data}
          readOnly={!!diario?.id}
          erro={erros.data}
          onChange={(e) => setForm((atual) => ({ ...atual, data: e.target.value }))}
        />
        <div>
          <label htmlFor="diario-clima" className="mb-1 block text-sm font-medium text-texto">Clima *</label>
          <select
            id="diario-clima"
            name="clima"
            value={form.clima}
            onChange={(e) => setForm((atual) => ({ ...atual, clima: e.target.value }))}
            className={`${CAMPO} ${erros.clima ? 'border-perigo' : 'border-borda-forte'}`}
          >
            <option value="">Selecione...</option>
            {Object.entries(CLIMAS).map(([valor, rotulo]) => (
              <option key={valor} value={valor}>{rotulo}</option>
            ))}
          </select>
          {erros.clima && <p className="mt-1 text-xs text-perigo">{erros.clima}</p>}
        </div>
        <TextArea
          label="Descrição do dia"
          id="diario-descricao"
          name="descricao"
          value={form.descricao}
          maxLength={2000}
          rows={3}
          className="md:col-span-2"
          onChange={(e) => setForm((atual) => ({ ...atual, descricao: e.target.value }))}
        />
        <fieldset className="md:col-span-2">
          <legend className="mb-1 text-sm font-medium text-texto">Equipe presente</legend>
          <label htmlFor="diario-equipe" className="sr-only">Selecionar equipe presente</label>
          <select
            id="diario-equipe"
            name="equipePresenteIds"
            multiple
            size={Math.min(Math.max(alocacoes.length, 2), 5)}
            value={form.equipePresenteIds}
            onChange={(e) =>
              setForm((atual) => ({
                ...atual,
                equipePresenteIds: Array.from(e.target.selectedOptions, (opcao) => opcao.value),
              }))
            }
            className={`${CAMPO} h-auto py-1 ${erros.equipePresenteIds ? 'border-perigo' : 'border-borda-forte'}`}
          >
            {alocacoes.map((alocacao) => (
              <option key={alocacao.id} value={alocacao.id}>{alocacao.nome}</option>
            ))}
          </select>
          {alocacoes.length === 0 && <p className="mt-1 text-xs text-texto-fraco">Sem profissionais alocados.</p>}
        </fieldset>
        <div className="md:col-span-2">
          <ErroFormulario erroGeral={erroGeral ?? erroEquipe} />
        </div>
        <footer className="flex flex-wrap justify-end gap-2 md:col-span-2">
          <Button type="button" variante="secundario" onClick={onFechar}>Cancelar</Button>
          <Button type="submit" disabled={enviando}>{enviando ? 'Salvando...' : 'Salvar diário'}</Button>
        </footer>
      </form>
    </Modal>
  );
}
