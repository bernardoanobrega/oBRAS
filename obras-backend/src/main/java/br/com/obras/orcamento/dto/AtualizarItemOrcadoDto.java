package br.com.obras.orcamento.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** RF-31: só valores e quantitativos são editáveis. */
public record AtualizarItemOrcadoDto(
        @NotNull(message = "Informe a quantidade")
        @DecimalMin(value = "0", inclusive = false, message = "A quantidade deve ser maior que zero")
        @Digits(integer = 11, fraction = 4, message = "Quantidade com até 4 casas decimais")
        BigDecimal quantidade,
        @NotNull(message = "Informe o custo unitário")
        @DecimalMin(value = "0", message = "O custo unitário não pode ser negativo")
        @Digits(integer = 13, fraction = 2, message = "Custo unitário com até 2 casas decimais")
        BigDecimal custoUnitarioOrcado) {
}
