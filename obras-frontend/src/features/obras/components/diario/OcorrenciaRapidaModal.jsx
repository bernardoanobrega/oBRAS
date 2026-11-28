import { useEffect, useState } from 'react';
import Button from '../../../../shared/components/ui/Button.jsx';
import Input from '../../../../shared/components/ui/Input.jsx';
import Modal from '../../../../shared/components/ui/Modal.jsx';
import TextArea from '../../../../shared/components/ui/TextArea.jsx';
import { estadoDoErro } from '../../adapters/estadoDoErro.js';
import { CLIMAS, dataIsoDeHoje, temDiarioParaData } from '../../adapters/diarioF3.js';
import { payloadOcorrenciaRapida, validarOcorrenciaRapida } from '../../adapters/validarOcorrencia.js';
import ErroFormulario from './ErroFormulario.jsx';

const CAMPO = 'min-h-[44px] w-full rounded-lg border bg-fundo px-3 text-sm text-texto outline-none md:min-h-[36px]';

export default function OcorrenciaRapidaModal({ aberto = false, data, resumos = [], onFechar, onSalvar, onSalvo }) {
  const [form, setForm] = useState(() => ({
    data: data || dataIsoDeHoje(),
    descricao: '',
    clima: '',
    dataHora: '',
  }));
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState(null);
  const [enviando, setEnviando] = useState(false);
  const requerClima = !temDiarioParaData(resumos, form.data);

  useEffect(() => {
    if (!aberto) return;
    setForm({ data: data || dataIsoDeHoje(), descricao: '', clima: '', dataHora: '' });
    setErros({});
    setErroGeral(null);
  }, [aberto, data]);

  async function enviar(evento) {
    evento.preventDefault();
    const validacao = validarOcorrenciaRapida(form, { requerClima });
    if (!validacao.valido) {
      setErros(validacao.erros);
      setErroGeral(null);
      return;
    }
    setEnviando(true);
    setErros({});
    setErroGeral(null);
    try {
      await onSalvar(payloadOcorrenciaRapida(form, { requerClima }));
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
    <Modal aberto={aberto} titulo="Ocorrência rápida" onFechar={onFechar}>
      <form onSubmit={enviar} noValidate className="grid grid-cols-1 gap-3">
        <Input
          label="Data"
          id="rapida-data"
          name="data"
          type="date"
          value={form.data}
          erro={erros.data}
          onChange={(e) => setForm((atual) => ({ ...atual, data: e.target.value }))}
        />
        <div>
          <p className="mb-1 text-sm font-medium text-texto">Categoria</p>
          <p className="text-sm text-texto-suave">Outro</p>
          <input type="hidden" name="categoria" value="OUTRO" readOnly />
        </div>
        {requerClima && (
          <div>
            <label htmlFor="rapida-clima" className="mb-1 block text-sm font-medium text-texto">Clima *</label>
            <select
              id="rapida-clima"
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
        )}
        <Input
          label="Data e hora (opcional)"
          id="rapida-data-hora"
          name="dataHora"
          type="datetime-local"
          value={form.dataHora}
          onChange={(e) => setForm((atual) => ({ ...atual, dataHora: e.target.value }))}
        />
        <TextArea
          label="Descrição *"
          id="rapida-descricao"
          name="descricao"
          value={form.descricao}
          maxLength={2000}
          rows={3}
          erro={erros.descricao}
          onChange={(e) => setForm((atual) => ({ ...atual, descricao: e.target.value }))}
        />
        <ErroFormulario erroGeral={erroGeral} />
        <footer className="flex flex-wrap justify-end gap-2">
          <Button type="button" variante="secundario" onClick={onFechar}>Cancelar</Button>
          <Button type="submit" disabled={enviando}>{enviando ? 'Salvando...' : 'Registrar ocorrência'}</Button>
        </footer>
      </form>
    </Modal>
  );
}
