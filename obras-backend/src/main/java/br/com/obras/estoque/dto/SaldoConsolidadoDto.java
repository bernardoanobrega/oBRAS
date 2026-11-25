package br.com.obras.estoque.dto;

import java.math.BigDecimal;

/** Saldo da obra inteira (soma dos canteiros) por material. */
public record SaldoConsolidadoDto(Long materialId, String materialNome, String unidadeMedida,
                                  BigDecimal quantidadeTotal) {}
