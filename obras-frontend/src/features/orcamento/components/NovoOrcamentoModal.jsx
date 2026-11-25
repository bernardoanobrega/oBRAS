import { useEffect, useState } from 'react';
import Modal from '../../../shared/components/ui/Modal.jsx';
import Select from '../../../shared/components/ui/Select.jsx';
import InputNumero from '../../../shared/components/ui/InputNumero.jsx';
import { useEnvioFormulario } from '../../../shared/hooks/useEnvioFormulario.js';
import { elaborarOrcamento } from '../api/orcamentoApi.js';
import { LIMITE_ALERTA_PADRAO, paraDecimal } from '../utils/orcamento.js';
import RodapeFormulario from './RodapeFormulario.jsx';

/**
 * obraIdFixo preenchido (aba da obra) esconde o campo de obra.
 * obras: obras do usuário que ainda não têm orçamento, para o seletor.
 */
export default function NovoOrcamentoModal({
  aberto,
  obraIdFixo = null,
  obras = [],
  onFechar,
  onCriado,
}) {
  const [obraId, setObraId] = useState('');
  const [limiteAlerta, setLimiteAlerta] = useState(LIMITE_ALERTA_PADRAO);
  const { erros, erroGeral, enviando, enviar, limpar } = useEnvioFormulario();
  // Valor primitivo: a lista de obras é recriada a cada render da página e reiniciaria o formulário.
  const primeiraObraId = obras[0]?.id ?? '';

  useEffect(() => {
    if (aberto) {
      setObraId(obraIdFixo ?? primeiraObraId);
      setLimiteAlerta(LIMITE_ALERTA_PADRAO);
      limpar();
    }
  }, [aberto, obraIdFixo, primeiraObraId, limpar]);

  const semObraDisponivel = obraIdFixo == null && obras.length === 0;

  async function aoEnviar(e) {
    e.preventDefault();
    const dto = { percentualLimiteAlerta: paraDecimal(limiteAlerta) };
    const sucesso = await enviar(() => elaborarOrcamento(obraId, dto));
    if (sucesso) onCriado(Number(obraId));
  }

  return (
    <Modal aberto={aberto} onFechar={onFechar} titulo="Novo orçamento">
      <form onSubmit={aoEnviar} className="space-y-3">
        {obraIdFixo == null &&
          (semObraDisponivel ? (
            <p className="rounded-lg bg-superficie-alta p-3 text-sm text-texto-suave">
              Todas as suas obras já têm orçamento.
            </p>
          ) : (
            <Select
              label="Obra"
              name="obraId"
              opcoes={Object.fromEntries(obras.map((obra) => [obra.id, obra.nome]))}
              value={obraId}
              onChange={(e) => setObraId(e.target.value)}
              required
            />
          ))}
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
          bloqueado={semObraDisponivel}
        />
      </form>
    </Modal>
  );
}
