package br.com.obras.diario.dto;

import java.time.LocalDateTime;

public record FotoDto(
    Long id,
    Long ocorrenciaId,
    String nomeArquivo,
    String legenda,
    LocalDateTime dataEnvio
) {}
