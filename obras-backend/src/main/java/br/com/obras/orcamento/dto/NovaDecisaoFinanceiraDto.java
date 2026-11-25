package br.com.obras.orcamento.dto;

import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record NovaDecisaoFinanceiraDto(
        @NotBlank(message = "Informe a descrição")
        @Size(max = 500, message = "Descrição com até 500 caracteres")
        String descricao,
        @NotBlank(message = "Informe a justificativa")
        @Size(max = 2000, message = "Justificativa com até 2000 caracteres")
        String justificativa,
        @NotNull(message = "Informe o valor de impacto (use 0 quando não altera o orçado)")
        @Digits(integer = 13, fraction = 2, message = "Valor com até 2 casas decimais")
        BigDecimal valorImpacto) {
}
