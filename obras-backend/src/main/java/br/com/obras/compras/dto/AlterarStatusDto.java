package br.com.obras.compras.dto;

import br.com.obras.compras.domain.StatusPedido;
import jakarta.validation.constraints.NotNull;

public record AlterarStatusDto(@NotNull StatusPedido status) {}
