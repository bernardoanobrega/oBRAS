package br.com.obras.equipamento.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ProrrogarAlocacaoDto(
        @NotNull(message = "Informe a nova data prevista de devolução.")
        LocalDate novaDataPrevista) {
}