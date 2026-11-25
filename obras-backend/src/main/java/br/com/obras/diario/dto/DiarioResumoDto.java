package br.com.obras.diario.dto;

import java.time.LocalDate;

public record DiarioResumoDto(
    Long id,
    LocalDate data,
    String clima,
    String descricao,
    long totalOcorrencias
) {}
