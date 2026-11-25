package br.com.obras.compras.dto;

import br.com.obras.compras.domain.PrioridadePedido;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record NovoPedidoDto(@NotNull PrioridadePedido prioridade, LocalDate dataPrevista) {}
