package br.com.obras.equipamento.dto;

import br.com.obras.equipamento.domain.TipoEquipamento;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record EquipamentoCriacaoDto(
        @NotBlank(message = "Informe o código de patrimônio.")
        @Size(max = 50, message = "O código de patrimônio deve ter no máximo 50 caracteres.")
        String codigoPatrimonio,

        @NotBlank(message = "Informe a descrição.")
        @Size(max = 300, message = "A descrição deve ter no máximo 300 caracteres.")
        String descricao,

        @NotNull(message = "Informe o tipo.")
        TipoEquipamento tipo) {
}