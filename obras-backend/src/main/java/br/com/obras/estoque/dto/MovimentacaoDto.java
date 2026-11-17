package br.com.obras.estoque.dto;

import br.com.obras.estoque.domain.MovimentacaoEstoque;
import br.com.obras.estoque.domain.TipoMovimentacao;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MovimentacaoDto(Long id, TipoMovimentacao tipo, Long materialId, String materialNome,
                              BigDecimal quantidade, Long canteiroOrigemId, Long canteiroDestinoId,
                              Long pedidoCompraId, Long autorId, LocalDateTime dataMovimentacao) {
    public static MovimentacaoDto de(MovimentacaoEstoque m) {
        return new MovimentacaoDto(m.getId(), m.getTipo(), m.getMaterial().getId(), m.getMaterial().getNome(),
                m.getQuantidade(), m.getCanteiroOrigemId(), m.getCanteiroDestinoId(), m.getPedidoCompraId(),
                m.getAutorId(), m.getDataMovimentacao());
    }
}
