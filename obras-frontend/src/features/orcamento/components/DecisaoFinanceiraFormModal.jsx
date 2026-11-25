import { useEffect, useState } from 'react';
import Modal from '../../../shared/components/ui/Modal.jsx';
import Input from '../../../shared/components/ui/Input.jsx';
import InputNumero from '../../../shared/components/ui/InputNumero.jsx';
import TextArea from '../../../shared/components/ui/TextArea.jsx';
import { useEnvioFormulario } from '../../../shared/hooks/useEnvioFormulario.js';
import { editarDecisao, registrarDecisao } from '../api/orcamentoApi.js';
import { paraDecimal } from '../utils/orcamento.js';
import RodapeFormulario from './RodapeFormulario.jsx';

const FORMULARIO_VAZIO = { descricao: '', justificativa: '', valorImpacto: '' };

/** decisao nula => nova decisão; decisao preenchida => correção (data e autor originais ficam). */
export default function DecisaoFinanceiraFormModal({ aberto, obraId, decisao, onFechar, onSalvo }) {
  const edicao = decisao != null;
  const [form, setForm] = useState(FORMULARIO_VAZIO);
  const { erros, erroGeral, enviando, enviar, limpar } = useEnvioFormulario();

  useEffect(() => {
    if (aberto) {
      setForm(
        decisao
          ? {
              descricao: decisao.descricao,
              justificativa: decisao.justificativa,
              valorImpacto: String(decisao.valorImpacto),
            }
          : FORMULARIO_VAZIO,
      );
      limpar();
    }
  }, [aberto, decisao, limpar]);

  const mudar = (campo) => (e) => setForm((f) => ({ ...f, [campo]: e.target.value }));

  async function aoEnviar(e) {
    e.preventDefault();
    const dto = { ...form, valorImpacto: paraDecimal(form.valorImpacto) };
    const sucesso = await enviar(() =>
      edicao ? editarDecisao(obraId, decisao.id, dto) : registrarDecisao(obraId, dto),
    );
    if (sucesso) onSalvo();
  }

  return (
    <Modal
      aberto={aberto}
      onFechar={onFechar}
      titulo={edicao ? 'Editar decisão financeira' : 'Registrar decisão financeira'}
    >
      <form onSubmit={aoEnviar} className="space-y-3">
        <Input
          label="Descrição"
          name="descricao"
          placeholder="Ex.: aditivo de reajuste do aço"
          value={form.descricao}
          onChange={mudar('descricao')}
          erro={erros.descricao}
          required
        />
        <TextArea
          label="Justificativa"
          name="justificativa"
          value={form.justificativa}
          onChange={mudar('justificativa')}
          erro={erros.justificativa}
          required
        />
        <InputNumero
          label="Impacto no orçado (R$)"
          name="valorImpacto"
          placeholder="0,00"
          permitirNegativo
          valor={form.valorImpacto}
          onChange={(valor) => setForm((f) => ({ ...f, valorImpacto: valor }))}
          erro={erros.valorImpacto}
          required
        />
        <p className="text-xs text-texto-suave">
          Valor positivo amplia o orçamento (aditivo); negativo reduz (contenção), por exemplo
          -50.000,00. Use 0 para só registrar a decisão.
        </p>
        <RodapeFormulario
          erroGeral={erroGeral}
          enviando={enviando}
          onCancelar={onFechar}
          rotuloEnviar={edicao ? 'Salvar alteração' : 'Registrar'}
        />
      </form>
    </Modal>
  );
}
