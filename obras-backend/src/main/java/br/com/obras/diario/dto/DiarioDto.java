package br.com.obras.diario.dto;

import java.time.LocalDate;
import java.util.List;

public record DiarioDto(
    Long id,
    Long obraId,
    LocalDate data,
    String clima,
    String descricao,
    List<MembroDiarioDto> equipePresente,
    List<OcorrenciaDto> ocorrencias
) {}
