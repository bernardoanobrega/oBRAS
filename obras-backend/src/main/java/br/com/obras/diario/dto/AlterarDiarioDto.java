package br.com.obras.diario.dto;

import br.com.obras.diario.domain.Clima;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.Set;

public record AlterarDiarioDto(
    @NotNull Clima clima,
    @Size(max = 2000) String descricao,
    Set<Long> equipePresenteIds
) {
    public AlterarDiarioDto {
        if (equipePresenteIds == null) equipePresenteIds = Set.of();
    }
}
