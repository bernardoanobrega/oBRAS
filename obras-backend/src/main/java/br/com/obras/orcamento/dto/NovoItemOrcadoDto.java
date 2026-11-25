package br.com.obras.orcamento.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** RF-30. */
public record NovoItemOrcadoDto(
        @NotBlank(message = "Informe o código")
        @Size(max = 50, message = "Código com até 50 caracteres")
        String codigo,
        @NotBlank(message = "Informe a descrição")
        @Size(max = 300, message = "Descrição com até 300 caracteres")
        String descricao,
        @NotBlank(message = "Informe a unidade")
        @Size(max = 20, message = "Unidade com até 20 caracteres")
        String unidade,
        @NotNull(message = "Informe a quantidade")
        @DecimalMin(value = "0", inclusive = false, message = "A quantidade deve ser maior que zero")
        @Digits(integer = 11, fraction = 4, message = "Quantidade com até 4 casas decimais")
        BigDecimal quantidade,
        @NotNull(message = "Informe o custo unitário")
        @DecimalMin(value = "0", message = "O custo unitário não pode ser negativo")
        @Digits(integer = 13, fraction = 2, message = "Custo unitário com até 2 casas decimais")
        BigDecimal custoUnitarioOrcado) {
}
