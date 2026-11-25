import { useEffect, useState } from 'react';
import Modal from '../../../shared/components/ui/Modal.jsx';
import Input from '../../../shared/components/ui/Input.jsx';
import InputNumero from '../../../shared/components/ui/InputNumero.jsx';
import { useEnvioFormulario } from '../../../shared/hooks/useEnvioFormulario.js';
import { adicionarItem, atualizarItem } from '../api/orcamentoApi.js';
import { paraDecimal } from '../utils/orcamento.js';
import RodapeFormulario from './RodapeFormulario.jsx';

const FORMULARIO_VAZIO = {
  codigo: '',
  descricao: '',
  unidade: '',
  quantidade: '',
  custoUnitarioOrcado: '',
};

/** item nulo => cadastro (RF-30); item preenchido => edição de valores e quantitativos (RF-31). */
export default function ItemOrcadoFormModal({ aberto, obraId, item, onFechar, onSalvo }) {
  const edicao = item != null;
  const [form, setForm] = useState(FORMULARIO_VAZIO);
  const { erros, erroGeral, enviando, enviar, limpar } = useEnvioFormulario();

  useEffect(() => {
    if (aberto) {
      setForm(item ? { ...FORMULARIO_VAZIO, ...item } : FORMULARIO_VAZIO);
      limpar();
    }
  }, [aberto, item, limpar]);

  const mudar = (campo) => (e) => setForm((f) => ({ ...f, [campo]: e.target.value }));
  const mudarNumero = (campo) => (valor) => setForm((f) => ({ ...f, [campo]: valor }));

  async function aoEnviar(e) {
    e.preventDefault();
    const valores = {
      quantidade: paraDecimal(form.quantidade),
      custoUnitarioOrcado: paraDecimal(form.custoUnitarioOrcado),
    };
    const sucesso = await enviar(() =>
      edicao
        ? atualizarItem(obraId, item.id, valores)
        : adicionarItem(obraId, {
            codigo: form.codigo,
            descricao: form.descricao,
            unidade: form.unidade,
            ...valores,
          }),
    );
    if (sucesso) onSalvo();
  }

  return (
    <Modal
      aberto={aberto}
      onFechar={onFechar}
      titulo={edicao ? 'Editar valores do item' : 'Novo item'}
    >
      <form onSubmit={aoEnviar} className="space-y-3">
        <div className="grid gap-3 md:grid-cols-3">
          <Input
            label="Código"
            name="codigo"
            value={form.codigo}
            onChange={mudar('codigo')}
            erro={erros.codigo}
            disabled={edicao}
            required
          />
          <Input
            label="Unidade"
            name="unidade"
            placeholder="m², m³, un"
            value={form.unidade}
            onChange={mudar('unidade')}
            erro={erros.unidade}
            disabled={edicao}
            required
            className="md:col-span-2"
          />
        </div>
        <Input
          label="Descrição"
          name="descricao"
          value={form.descricao}
          onChange={mudar('descricao')}
          erro={erros.descricao}
          disabled={edicao}
          required
        />
        <div className="grid gap-3 md:grid-cols-2">
          <InputNumero
            label="Quantidade"
            name="quantidade"
            casasDecimais={4}
            valor={form.quantidade}
            onChange={mudarNumero('quantidade')}
            erro={erros.quantidade}
            required
          />
          <InputNumero
            label="Custo unitário orçado (R$)"
            name="custoUnitarioOrcado"
            placeholder="0,00"
            valor={form.custoUnitarioOrcado}
            onChange={mudarNumero('custoUnitarioOrcado')}
            erro={erros.custoUnitarioOrcado}
            required
          />
        </div>
        <RodapeFormulario erroGeral={erroGeral} enviando={enviando} onCancelar={onFechar} />
      </form>
    </Modal>
  );
}
