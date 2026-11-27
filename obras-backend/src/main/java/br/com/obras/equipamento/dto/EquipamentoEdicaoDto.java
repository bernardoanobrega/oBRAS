package br.com.obras.equipamento.dto;

import br.com.obras.equipamento.domain.TipoEquipamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EquipamentoEdicaoDto(
        @NotBlank(message = "Informe a descrição.")
        @Size(max = 300, message = "A descrição deve ter no máximo 300 caracteres.")
        String descricao,

        @NotNull(message = "Informe o tipo.")
        TipoEquipamento tipo) {
}