package br.com.obras.orcamento.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record NovoOrcamentoDto(
        @NotNull(message = "Informe o percentual limite de alerta")
        @DecimalMin(value = "0", inclusive = false, message = "O limite de alerta deve ser maior que zero")
        @DecimalMax(value = "100", message = "O limite de alerta deve ser no máximo 100")
        @Digits(integer = 3, fraction = 2, message = "Use no máximo duas casas decimais")
        BigDecimal percentualLimiteAlerta) {
}
