package br.com.obras.estoque.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record SaidaEstoqueDto(@NotNull Long materialId, @NotNull Long canteiroId,
                              @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal quantidade) {}
