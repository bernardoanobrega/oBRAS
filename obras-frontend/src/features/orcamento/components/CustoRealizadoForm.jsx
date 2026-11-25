import { useEffect, useState } from 'react';
import Input from '../../../shared/components/ui/Input.jsx';
import InputNumero from '../../../shared/components/ui/InputNumero.jsx';
import Select from '../../../shared/components/ui/Select.jsx';
import { useEnvioFormulario } from '../../../shared/hooks/useEnvioFormulario.js';
import { editarCusto, registrarCusto } from '../api/orcamentoApi.js';
import { ORIGENS_CUSTO, paraDecimal } from '../utils/orcamento.js';
import RodapeFormulario from './RodapeFormulario.jsx';

const FORMULARIO_VAZIO = { descricao: '', valor: '', origem: 'MATERIAL' };

/** custo nulo => novo lançamento; custo preenchido => correção do lançamento. */
export default function CustoRealizadoForm({ obraId, itemId, custo, onCancelar, onSalvo }) {
  const edicao = custo != null;
  const [form, setForm] = useState(FORMULARIO_VAZIO);
  const { erros, erroGeral, enviando, enviar, limpar } = useEnvioFormulario();

  useEffect(() => {
    setForm(
      custo
        ? { descricao: custo.descricao, valor: String(custo.valor), origem: custo.origem }
        : FORMULARIO_VAZIO,
    );
    limpar();
  }, [custo, limpar]);

  const mudar = (campo) => (e) => setForm((f) => ({ ...f, [campo]: e.target.value }));

  async function aoEnviar(e) {
    e.preventDefault();
    const dto = { descricao: form.descricao, valor: paraDecimal(form.valor), origem: form.origem };
    const sucesso = await enviar(() =>
      edicao ? editarCusto(obraId, itemId, custo.id, dto) : registrarCusto(obraId, itemId, dto),
    );
    if (sucesso) onSalvo();
  }

  return (
    <form onSubmit={aoEnviar} className="space-y-3">
      <h3 className="text-sm font-semibold text-texto">
        {edicao ? 'Editar lançamento' : 'Novo lançamento'}
      </h3>
      <Input
        label="Descrição"
        name="descricao"
        placeholder="Ex.: concreto usinado, NF 1234"
        value={form.descricao}
        onChange={mudar('descricao')}
        erro={erros.descricao}
        required
      />
      <div className="grid gap-3 md:grid-cols-2">
        <InputNumero
          label="Valor (R$)"
          name="valor"
          placeholder="0,00"
          valor={form.valor}
          onChange={(valor) => setForm((f) => ({ ...f, valor }))}
          erro={erros.valor}
          required
        />
        <Select
          label="Origem"
          name="origem"
          opcoes={ORIGENS_CUSTO}
          value={form.origem}
          onChange={mudar('origem')}
          erro={erros.origem}
        />
      </div>
      <RodapeFormulario
        erroGeral={erroGeral}
        enviando={enviando}
        onCancelar={onCancelar}
        rotuloEnviar={edicao ? 'Salvar alteração' : 'Lançar custo'}
      />
    </form>
  );
}
