package br.com.obras.compras.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** itemOrcadoId é opcional: vincula o item ao orçamento para alerta de estouro e custo realizado. */
public record NovoItemPedidoDto(
        @NotNull Long materialId,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal quantidade,
        @NotNull @DecimalMin("0") BigDecimal valorUnitario,
        Long itemOrcadoId) {}
