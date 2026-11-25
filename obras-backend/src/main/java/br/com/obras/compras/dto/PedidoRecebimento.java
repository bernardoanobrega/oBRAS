package br.com.obras.compras.dto;

import br.com.obras.compras.domain.StatusPedido;

import java.math.BigDecimal;
import java.util.List;

/** Visão do pedido para o módulo estoque dar entrada dos materiais. */
public record PedidoRecebimento(Long pedidoId, Long obraId, StatusPedido status, List<Item> itens) {
    public record Item(Long materialId, BigDecimal quantidade) {}
}
