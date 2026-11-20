package br.com.obras.diario.dto;

import br.com.obras.diario.domain.CategoriaOcorrencia;
import br.com.obras.diario.domain.Clima;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record NovaOcorrenciaRapidaDto(
    @NotNull LocalDate data,
    Clima clima,
    @NotNull CategoriaOcorrencia categoria,
    @NotBlank @Size(max = 2000) String descricao,
    @Size(max = 300) String legenda,
    LocalDateTime dataHora,
    @Valid ParalisacaoDto paralisacao
) {}
