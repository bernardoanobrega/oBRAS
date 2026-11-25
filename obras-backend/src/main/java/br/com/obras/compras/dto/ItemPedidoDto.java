package br.com.obras.compras.dto;

import java.math.BigDecimal;

public record ItemPedidoDto(Long id, Long materialId, String materialNome, String unidadeMedida,
                            BigDecimal quantidade, BigDecimal valorUnitario, BigDecimal subtotal,
                            Long itemOrcadoId) {}
