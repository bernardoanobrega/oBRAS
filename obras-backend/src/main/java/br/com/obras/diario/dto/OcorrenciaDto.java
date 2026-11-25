package br.com.obras.diario.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OcorrenciaDto(
    Long id,
    Long diarioId,
    String categoria,
    String descricao,
    LocalDateTime dataHora,
    MembroDiarioDto autor,
    ParalisacaoDto paralisacao,
    List<FotoDto> fotos
) {}
