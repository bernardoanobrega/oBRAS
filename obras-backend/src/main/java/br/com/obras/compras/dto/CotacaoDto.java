package br.com.obras.compras.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CotacaoDto(Long id, Long fornecedorId, String fornecedorNome, LocalDateTime dataCotacao,
                         BigDecimal valorTotalProposto, Integer prazoEntregaDias, boolean selecionada) {}
