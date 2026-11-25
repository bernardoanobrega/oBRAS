package br.com.obras.orcamento.dto;

import br.com.obras.orcamento.domain.OrigemCusto;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/** Correção de um custo lançado manualmente. Data, autor e idOrigem não mudam. */
public record AtualizarCustoRealizadoDto(
        @NotBlank(message = "Informe a descrição")
        @Size(max = 300, message = "Descrição com até 300 caracteres")
        String descricao,
        @NotNull(message = "Informe o valor")
        @DecimalMin(value = "0", inclusive = false, message = "O valor deve ser maior que zero")
        @Digits(integer = 13, fraction = 2, message = "Valor com até 2 casas decimais")
        BigDecimal valor,
        @NotNull(message = "Informe a origem do custo")
        OrigemCusto origem) {
}
