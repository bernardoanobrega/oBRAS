import { useEffect, useState } from 'react';
import Modal from '../../../shared/components/ui/Modal.jsx';
import Input from '../../../shared/components/ui/Input.jsx';
import InputNumero from '../../../shared/components/ui/InputNumero.jsx';
import { useEnvioFormulario } from '../../../shared/hooks/useEnvioFormulario.js';
import { elaborarOrcamento } from '../api/orcamentoApi.js';
import { LIMITE_ALERTA_PADRAO, paraDecimal } from '../utils/orcamento.js';
import RodapeFormulario from './RodapeFormulario.jsx';

/** obraIdFixo preenchido (aba da obra) esconde o campo de obra. */
export default function NovoOrcamentoModal({ aberto, obraIdFixo = null, onFechar, onCriado }) {
  const [obraId, setObraId] = useState('');
  const [limiteAlerta, setLimiteAlerta] = useState(LIMITE_ALERTA_PADRAO);
  const { erros, erroGeral, enviando, enviar, limpar } = useEnvioFormulario();

  useEffect(() => {
    if (aberto) {
      setObraId(obraIdFixo ?? '');
      setLimiteAlerta(LIMITE_ALERTA_PADRAO);
      limpar();
    }
  }, [aberto, obraIdFixo, limpar]);

  async function aoEnviar(e) {
    e.preventDefault();
    const dto = { percentualLimiteAlerta: paraDecimal(limiteAlerta) };
    const sucesso = await enviar(() => elaborarOrcamento(obraId, dto));
    if (sucesso) onCriado(Number(obraId));
  }

  return (
    <Modal aberto={aberto} onFechar={onFechar} titulo="Novo orçamento">
      <form onSubmit={aoEnviar} className="space-y-3">
        {obraIdFixo == null && (
          <Input
            label="Número da obra"
            name="obraId"
            type="number"
            min="1"
            value={obraId}
            onChange={(e) => setObraId(e.target.value)}
            required
          />
        )}
        <InputNumero
          label="Limite de alerta (% do orçado já gasto)"
          name="percentualLimiteAlerta"
          valor={limiteAlerta}
          onChange={setLimiteAlerta}
          erro={erros.percentualLimiteAlerta}
          required
        />
        <RodapeFormulario
          erroGeral={erroGeral}
          enviando={enviando}
          onCancelar={onFechar}
          rotuloEnviar="Criar orçamento"
        />
      </form>
    </Modal>
  );
}
