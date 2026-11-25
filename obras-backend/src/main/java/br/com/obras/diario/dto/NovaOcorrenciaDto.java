package br.com.obras.diario.dto;

import br.com.obras.diario.domain.CategoriaOcorrencia;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record NovaOcorrenciaDto(
    @NotNull CategoriaOcorrencia categoria,
    @NotBlank @Size(max = 2000) String descricao,
    LocalDateTime dataHora,
    @Valid ParalisacaoDto paralisacao
) {}
