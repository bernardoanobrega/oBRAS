package br.com.obras.compras.dto;

import br.com.obras.compras.domain.StatusPedido;

import java.time.LocalDateTime;

public record HistoricoStatusDto(StatusPedido status, LocalDateTime dataHora, Long autorId) {}
