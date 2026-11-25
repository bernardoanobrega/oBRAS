package br.com.obras.estoque.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Cria o saldo (zerado) se ainda não existir e define a quantidade mínima. */
public record ConfigurarSaldoDto(@NotNull Long materialId, @NotNull Long canteiroId,
                                 @NotNull @DecimalMin("0") BigDecimal quantidadeMinima) {}
