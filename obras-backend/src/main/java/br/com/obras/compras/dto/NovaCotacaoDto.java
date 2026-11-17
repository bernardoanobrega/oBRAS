package br.com.obras.compras.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record NovaCotacaoDto(
        @NotNull Long fornecedorId,
        @NotNull @DecimalMin(value = "0", inclusive = false) BigDecimal valorTotalProposto,
        @NotNull @Min(0) Integer prazoEntregaDias) {}
