package br.com.obras.compras.dto;

/** excedeOrcamento = alerta (não bloqueia): o item ultrapassa o valor orçado. */
public record ItemAdicionadoDto(PedidoCompraDto pedido, boolean excedeOrcamento) {}
