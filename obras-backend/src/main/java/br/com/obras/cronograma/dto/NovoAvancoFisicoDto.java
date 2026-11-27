package br.com.obras.cronograma.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record NovoAvancoFisicoDto(
        @NotNull(message = "Informe o percentual acumulado")
        @DecimalMin(value = "0", message = "O percentual não pode ser negativo")
        @DecimalMax(value = "100", message = "O percentual deve ser no máximo 100")
        @Digits(integer = 3, fraction = 2, message = "Use no máximo duas casas decimais")
        BigDecimal percentualAcumulado,
        @Size(max = 500, message = "Observação com até 500 caracteres")
        String observacao) {
}
