import { useEffect, useState } from 'react';
import Button from '../../../../shared/components/ui/Button.jsx';
import Input from '../../../../shared/components/ui/Input.jsx';
import Modal from '../../../../shared/components/ui/Modal.jsx';
import { enviarFoto } from '../../api/diarioApi.js';
import { estadoDoErro } from '../../adapters/estadoDoErro.js';
import { validarFoto } from '../../adapters/validarFoto.js';
import ErroFormulario from './ErroFormulario.jsx';

export default function FotoUpload({ aberto = false, obraId, ocorrenciaId, onFechar, onSalvo }) {
  const [arquivo, setArquivo] = useState(null);
  const [legenda, setLegenda] = useState('');
  const [erroGeral, setErroGeral] = useState(null);
  const [enviando, setEnviando] = useState(false);

  useEffect(() => {
    if (!aberto) return;
    setArquivo(null);
    setLegenda('');
    setErroGeral(null);
  }, [aberto]);

  async function enviar(evento) {
    evento.preventDefault();
    const validacao = await validarFoto(arquivo);
    if (!validacao.valido) {
      setErroGeral(validacao.mensagem);
      return;
    }
    setEnviando(true);
    setErroGeral(null);
    try {
      await enviarFoto(obraId, ocorrenciaId, arquivo, legenda);
      onSalvo?.();
    } catch (err) {
      setErroGeral(estadoDoErro(err).erroGeral);
    } finally {
      setEnviando(false);
    }
  }

  return (
    <Modal aberto={aberto} titulo="Anexar foto da ocorrência" onFechar={onFechar}>
      <form onSubmit={enviar} noValidate className="grid grid-cols-1 gap-3">
        <Input
          label="Imagem JPG ou PNG (até 10 MB)"
          id="foto-arquivo"
          name="arquivo"
          type="file"
          accept="image/jpeg,image/png"
          onChange={(e) => setArquivo(e.target.files?.[0] ?? null)}
        />
        <Input
          label="Legenda (opcional)"
          id="foto-legenda"
          name="legenda"
          value={legenda}
          maxLength={300}
          onChange={(e) => setLegenda(e.target.value)}
        />
        <ErroFormulario erroGeral={erroGeral} />
        <footer className="flex flex-wrap justify-end gap-2">
          <Button type="button" variante="secundario" onClick={onFechar}>Cancelar</Button>
          <Button type="submit" disabled={enviando}>{enviando ? 'Enviando...' : 'Anexar foto'}</Button>
        </footer>
      </form>
    </Modal>
  );
}
