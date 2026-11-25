package br.com.obras.estoque.dto;

import jakarta.validation.constraints.NotNull;

public record EntradaEstoqueDto(@NotNull Long pedidoId, @NotNull Long canteiroId) {}
