package br.com.obras.estoque.dto;

import br.com.obras.estoque.domain.SaldoEstoque;
import br.com.obras.estoque.domain.SituacaoSaldo;

import java.math.BigDecimal;

public record SaldoDto(Long id, Long materialId, String materialNome, String unidadeMedida, Long canteiroId,
                       BigDecimal quantidadeAtual, BigDecimal quantidadeMinima, SituacaoSaldo situacao,
                       BigDecimal mediaConsumoSemanal) {
    public static SaldoDto de(SaldoEstoque s, BigDecimal mediaConsumoSemanal) {
        return new SaldoDto(s.getId(), s.getMaterial().getId(), s.getMaterial().getNome(),
                s.getMaterial().getUnidadeMedida(), s.getCanteiroId(), s.getQuantidadeAtual(),
                s.getQuantidadeMinima(), s.situacao(), mediaConsumoSemanal);
    }
}
