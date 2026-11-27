package br.com.obras.equipamento.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record TransferirEquipamentoDto(
        @NotNull(message = "Informe o canteiro de destino.")
        Long canteiroDestinoId,

        @NotNull(message = "Informe o responsável.")
        Long responsavelId,

        @NotNull(message = "Informe a data prevista de devolução.")
        LocalDate dataPrevistaDevolucao,

        @Size(max = 1000, message = "A observação deve ter no máximo 1000 caracteres.")
        String observacao) {
}