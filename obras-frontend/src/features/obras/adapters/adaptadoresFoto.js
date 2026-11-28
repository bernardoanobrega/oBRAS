export function legendaEfetiva(legenda, descricaoOcorrencia) {
  const texto = String(legenda ?? '').trim();
  return texto || String(descricaoOcorrencia ?? '').trim();
}

export function fotosDeOcorrencias(ocorrencias = []) {
  if (!Array.isArray(ocorrencias)) return [];
  return ocorrencias.flatMap((ocorrencia) => {
    const ocorrenciaId = ocorrencia?.id ?? ocorrencia?.ocorrenciaId;
    if (ocorrenciaId == null || !Array.isArray(ocorrencia?.fotos)) return [];
    return ocorrencia.fotos
      .filter((foto) => foto?.id != null)
      .map((foto) => ({
        ...foto,
        ocorrenciaId,
        descricaoOcorrencia: String(ocorrencia.descricao ?? '').trim(),
      }));
  });
}

export function fotoParaExibir(foto) {
  return !!foto && foto.id != null && foto.ocorrenciaId != null;
}
