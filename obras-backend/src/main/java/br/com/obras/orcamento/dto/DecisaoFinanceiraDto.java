package br.com.obras.orcamento.dto;

import br.com.obras.orcamento.domain.DecisaoFinanceira;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DecisaoFinanceiraDto(Long id, LocalDate dataDecisao, String descricao, String justificativa,
                                   BigDecimal valorImpacto, Long usuarioId) {
    public static DecisaoFinanceiraDto de(DecisaoFinanceira decisao) {
        return new DecisaoFinanceiraDto(decisao.getId(), decisao.getDataDecisao(), decisao.getDescricao(),
                decisao.getJustificativa(), decisao.getValorImpacto(), decisao.getUsuarioId());
    }
}
