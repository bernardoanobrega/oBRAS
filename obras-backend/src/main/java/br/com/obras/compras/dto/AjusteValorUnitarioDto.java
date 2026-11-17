package br.com.obras.compras.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AjusteValorUnitarioDto(@NotNull @DecimalMin("0") BigDecimal valorUnitario) {}
