import { useState } from 'react';
import { Link } from 'react-router-dom';
import { mensagemErro } from '../../../shared/api/http.js';
import Can from '../../../shared/auth/Can.jsx';
import Button from '../../../shared/components/ui/Button.jsx';
import { ACOES_OBRA } from '../api/obrasApi.js';
import { BOTAO_PRIMARIO, BOTAO_SECUNDARIO } from './detalhe/tokens.js';

export const CONFIRMACOES = {
  concluir: 'Concluir obra? A equipe será encerrada ao concluir.',
  cancelar: 'Cancelar obra? A equipe será encerrada ao cancelar.',
};

export const ROTULOS_ACAO = {
  iniciar: 'Iniciar obra',
  paralisar: 'Paralisar obra',
  retomar: 'Retomar obra',
  concluir: 'Concluir obra',
  cancelar: 'Cancelar obra',
};

export function confirmarAcao(acao, confirmar) {
  const pergunta = CONFIRMACOES[acao];
  if (!pergunta) return true;
  const fn = typeof confirmar === 'function' ? confirmar : globalThis.confirm;
  return Boolean(fn?.(pergunta));
}

export function mensagemErroAcao(acao, erro) {
  const status = erro?.response?.status ?? erro?.status ?? null;
  const detalhe = erro?.response?.data?.mensagem
    ?? erro?.data?.mensagem
    ?? erro?.mensagem
    ?? mensagemErro(erro);
  if (status === 422) return detalhe;
  return `${ROTULOS_ACAO[acao] ?? 'Ação'} não pôde ser concluída. ${detalhe}`;
}

export function acoesDisponiveis(status) {
  const atual = String(status ?? '').toUpperCase();
  if (atual === 'PLANEJADA') return ['iniciar', 'cancelar'];
  if (atual === 'EM_ANDAMENTO') return ['paralisar', 'concluir', 'cancelar'];
  if (atual === 'PARALISADA') return ['retomar', 'cancelar'];
  return [];
}

export default function ObraAcoesMenu({ obra, obraId, confirmar, executarAcao, onAcao, onEditar }) {
  const [erro, setErro] = useState(null);
  const id = obra?.id ?? obraId;
  const acoes = acoesDisponiveis(obra?.status);

  async function agir(acao) {
    if (!confirmarAcao(acao, confirmar)) return;
    setErro(null);
    try {
      const executar = executarAcao ?? ACOES_OBRA[acao];
      await executar(id);
      await onAcao?.();
    } catch (falha) {
      setErro(mensagemErroAcao(acao, falha));
    }
  }

  return (
    <>
      <Can modulo="DETALHES_OBRA" acao="EDITAR" obraId={id}>
        <button type="button" onClick={onEditar} className={`${BOTAO_SECUNDARIO} min-h-[44px] md:min-h-[36px]`}>
          Editar Parâmetros
        </button>
        {acoes.map((acao) => (
          <Button
            key={acao}
            variante={acao === 'concluir' || acao === 'cancelar' ? 'perigo' : 'secundario'}
            onClick={() => agir(acao)}
          >
            {ROTULOS_ACAO[acao]}
          </Button>
        ))}
      </Can>
      <Can modulo="DETALHES_OBRA" acao="CRIAR" obraId={id}>
        <Link to={`/obras/${id}/diario`} className={`${BOTAO_PRIMARIO} min-h-[44px] md:min-h-[36px]`}>
          Nova Ocorrência
        </Link>
      </Can>
      {erro && (
        <p role="alert" className="w-full text-xs text-perigo">
          {erro}
        </p>
      )}
    </>
  );
}
