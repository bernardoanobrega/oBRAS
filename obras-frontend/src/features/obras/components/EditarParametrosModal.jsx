import { useEffect, useState } from 'react';
import Modal from '../../../shared/components/ui/Modal.jsx';
import { estadoDoErro } from '../adapters/estadoDoErro.js';
import { editarObra } from '../api/obrasApi.js';
import {
  ObraFormulario,
  formularioInicial,
  paraCorpoObra,
  validarObra,
} from './ObraFormModal.jsx';

export function estadoAposErro(form, erro) {
  const resultado = estadoDoErro(erro);
  return { form, ...resultado, manterAberto: true };
}

export async function salvarParametros(obraId, form, salvar = editarObra) {
  const corpo = paraCorpoObra(form);
  await salvar(obraId, corpo);
  return corpo;
}

export default function EditarParametrosModal({
  aberto,
  obra,
  obraId = null,
  salvar = editarObra,
  onFechar,
  onSalvo,
}) {
  const [form, setForm] = useState(formularioInicial(null));
  const [erros, setErros] = useState({});
  const [erroGeral, setErroGeral] = useState(null);
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    if (aberto && obra) {
      setForm(formularioInicial(obra));
      setErros({});
      setErroGeral(null);
    }
  }, [aberto, obra]);

  const mudar = (campo) => (evento) => setForm((atual) => ({ ...atual, [campo]: evento.target.value }));

  async function enviar(evento) {
    evento.preventDefault();
    const errosLocais = validarObra(form);
    setErros(errosLocais);
    setErroGeral(null);
    if (Object.keys(errosLocais).length > 0) return;

    setEnviando(true);
    try {
      await salvarParametros(obra?.id ?? obraId, form, salvar);
      await onSalvo?.();
    } catch (erro) {
      const estado = estadoAposErro(form, erro);
      setForm(estado.form);
      setErros(estado.erros);
      setErroGeral(estado.erroGeral);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <Modal titulo="Editar Parâmetros" aberto={aberto} onFechar={onFechar}>
      <ObraFormulario
        form={form}
        erros={erros}
        erroGeral={erroGeral}
        enviando={enviando}
        onMudar={mudar}
        onEnviar={enviar}
        onCancelar={onFechar}
      />
    </Modal>
  );
}
