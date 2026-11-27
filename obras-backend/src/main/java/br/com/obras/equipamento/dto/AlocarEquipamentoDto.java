package br.com.obras.equipamento.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record AlocarEquipamentoDto(
        @NotNull(message = "Informe o canteiro.")
        Long canteiroId,

        @NotNull(message = "Informe o responsável.")
        Long responsavelId,

        @NotNull(message = "Informe a data prevista de devolução.")
        LocalDate dataPrevistaDevolucao,

        @Size(max = 1000, message = "A observação deve ter no máximo 1000 caracteres.")
        String observacao) {
}