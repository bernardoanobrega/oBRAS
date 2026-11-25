package br.com.obras.estoque.repository;

import java.math.BigDecimal;

/** Resultado de consulta agregada de consumo (usado em constructor expression do JPQL). */
public record ConsumoAgregado(Long materialId, Long canteiroId, BigDecimal total) {}
