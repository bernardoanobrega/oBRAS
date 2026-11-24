package br.com.obras.diario.dto;

import br.com.obras.diario.domain.Clima;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.Set;

public record NovoDiarioDto(
    @NotNull LocalDate data,
    @NotNull Clima clima,
    @Size(max = 2000) String descricao,
    Set<Long> equipePresenteIds
) {
    public NovoDiarioDto {
        if (equipePresenteIds == null) equipePresenteIds = Set.of();
    }
}