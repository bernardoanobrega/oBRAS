package br.com.obras.compras.dto;

import br.com.obras.compras.domain.PrioridadePedido;
import br.com.obras.compras.domain.StatusPedido;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PedidoResumoDto(Long id, LocalDate dataSolicitacao, LocalDate dataPrevista, BigDecimal valorTotal,
                              StatusPedido status, PrioridadePedido prioridade, String fornecedorNome,
                              boolean atrasado) {}
