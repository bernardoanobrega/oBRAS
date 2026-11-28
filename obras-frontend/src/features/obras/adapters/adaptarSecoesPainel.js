import { paraClimaDia, paraOcorrencia } from '../api/adaptadoresDiario.js';
import { sigla, tomEquipe } from '../api/adaptadores.js';
import { fotosDeOcorrencias, fotoParaExibir } from './adaptadoresFoto.js';

export function adaptarClimaPainel(clima) {
  if (clima?.data == null && clima?.condicao == null) {
    return {
      codigo: null,
      condicao: null,
      data: null,
      badge: 'Sem diário registrado',
      tom: 'neutral',
      icone: 'calendar',
      semDiario: true,
    };
  }

  const adaptado = paraClimaDia({ data: clima.data, clima: clima.condicao });
  return {
    ...adaptado,
    condicao: clima.condicao == null ? null : adaptado.condicao,
    data: clima.data == null ? null : adaptado.data,
    semDiario: clima.data == null || clima.condicao == null,
  };
}

export function adaptarEquipePainel(equipe = []) {
  if (!Array.isArray(equipe)) return [];
  return equipe.filter(Boolean).map((membro) => {
    const cargos = Array.isArray(membro.cargos)
      ? membro.cargos.filter(Boolean)
      : membro.cargo ? [membro.cargo] : [];
    return {
      usuarioId: membro.usuarioId ?? null,
      sigla: membro.sigla ?? sigla(membro.nome),
      nome: membro.nome ?? '—',
      cargos,
      cargo: cargos.length ? cargos.join(' / ') : '—',
      dataInicio: membro.dataInicio ?? null,
      tom: membro.tom ?? tomEquipe(membro.usuarioId),
    };
  });
}

export function adaptarOcorrenciasPainel(ocorrencias = []) {
  if (!Array.isArray(ocorrencias)) return [];
  return ocorrencias
    .filter(Boolean)
    .map((item) => {
      const dto = item.categoria != null || item.descricao != null || item.dataHora != null;
      return dto ? paraOcorrencia(item) : item;
    })
    .filter(Boolean)
    .sort((a, b) => String(b.data ?? '').localeCompare(String(a.data ?? '')));
}

export function adaptarFotosPainel(painel) {
  const fotosPainel = Array.isArray(painel?.fotos) ? painel.fotos : [];
  const fotosOcorrencia = fotosPainel.length ? fotosPainel : fotosDeOcorrencias(painel?.ocorrencias ?? []);
  return fotosOcorrencia.filter(fotoParaExibir);
}
