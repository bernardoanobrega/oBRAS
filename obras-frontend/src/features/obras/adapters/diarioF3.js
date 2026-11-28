export const CLIMAS = {
  ENSOLARADO: 'Ensolarado',
  NUBLADO: 'Nublado',
  CHUVA_FRACA: 'Chuva fraca',
  CHUVA_FORTE: 'Chuva forte',
};

function exigirArray(valor, campo) {
  if (valor == null) return [];
  if (!Array.isArray(valor)) throw new TypeError(`Campo ${campo} deve ser uma lista`);
  return valor;
}

function identificadorMembro(membro) {
  if (membro == null) return null;
  if (typeof membro === 'number' || typeof membro === 'string') return membro;
  if (membro.alocacaoId != null) return membro.alocacaoId;
  if (membro.usuarioId != null) return membro.usuarioId;
  if (membro.id != null) return membro.id;
  return null;
}

export function normalizarOcorrenciaDto(dto) {
  if (!dto || typeof dto !== 'object') return dto;
  return {
    ...dto,
    autorUsuarioId: dto.autorUsuarioId ?? dto.autor?.usuarioId ?? dto.autor?.id ?? null,
  };
}

export function normalizarDiarioDto(dto) {
  if (!dto || typeof dto !== 'object') return dto;
  const equipe = exigirArray(dto.equipePresente, 'equipePresente');
  const ids = equipe.map(identificadorMembro).filter((id) => id != null);
  const usuarioIds = equipe
    .map((membro) => (membro && typeof membro === 'object' ? (membro.usuarioId ?? identificadorMembro(membro)) : membro))
    .filter((id) => id != null);
  return {
    ...dto,
    equipePresenteIds: Array.isArray(dto.equipePresenteIds) ? dto.equipePresenteIds : ids,
    equipePresenteUsuarioIds: usuarioIds,
    ocorrencias: exigirArray(dto.ocorrencias, 'ocorrencias').map(normalizarOcorrenciaDto),
  };
}

export function normalizarListaDiarios(dtos) {
  if (!Array.isArray(dtos)) throw new TypeError('Lista de diários inválida');
  return dtos.map(normalizarDiarioDto);
}

export function dataIsoDeHoje() {
  const agora = new Date();
  const ano = agora.getFullYear();
  const mes = String(agora.getMonth() + 1).padStart(2, '0');
  const dia = String(agora.getDate()).padStart(2, '0');
  return `${ano}-${mes}-${dia}`;
}

export function paraDateTimeLocal(valor) {
  if (valor == null || valor === '') return '';
  const texto = String(valor);
  const parte = texto.includes('T') ? texto.split('T')[1] : '';
  if (!parte) return texto;
  return `${texto.split('T')[0]}T${parte.slice(0, 5)}`;
}

export function paraFormularioDiario(dto) {
  const normalizado = normalizarDiarioDto(dto);
  return {
    data: normalizado?.data ? String(normalizado.data).slice(0, 10) : dataIsoDeHoje(),
    clima: normalizado?.clima ?? '',
    descricao: normalizado?.descricao ?? '',
    equipePresenteIds: (normalizado?.equipePresenteIds ?? []).map(String),
  };
}

export function paraFormularioOcorrencia(dto) {
  if (!dto || typeof dto !== 'object') {
    return { categoria: 'OUTRO', descricao: '', dataHora: '', paralisacao: null };
  }
  const paralisacao = dto.paralisacao;
  return {
    categoria: dto.categoria ?? 'OUTRO',
    descricao: dto.descricao ?? '',
    dataHora: paraDateTimeLocal(dto.dataHora),
    paralisacao: paralisacao
      ? { inicio: paraDateTimeLocal(paralisacao.inicio), fim: paraDateTimeLocal(paralisacao.fim) }
      : null,
  };
}

export function temDiarioParaData(resumos, dataIso) {
  if (!Array.isArray(resumos) || !dataIso) return false;
  return resumos.some((resumo) => String(resumo?.data ?? resumo?.dataIso ?? '').slice(0, 10) === dataIso);
}
