package br.com.obras.orcamento.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Decisão que altera o total orçado (aditivo, remanejamento ou contenção de custos).
 * valorImpacto positivo amplia o orçado; negativo reduz.
 */
@Entity
@Table(name = "decisao_financeira")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DecisaoFinanceira {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orcamento_id", nullable = false, updatable = false)
    private Orcamento orcamento;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;

    @Column(name = "data_decisao", nullable = false)
    private LocalDate dataDecisao;

    @Column(nullable = false, length = 500)
    private String descricao;

    @Column(nullable = false, length = 2000)
    private String justificativa;

    @Column(name = "valor_impacto", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorImpacto;

    DecisaoFinanceira(Orcamento orcamento, String descricao, String justificativa, BigDecimal valorImpacto,
                      Long usuarioId) {
        this.orcamento = orcamento;
        this.descricao = descricao;
        this.justificativa = justificativa;
        this.valorImpacto = CalculoFinanceiro.monetario(valorImpacto);
        this.usuarioId = usuarioId;
        this.dataDecisao = LocalDate.now();
    }

    /** Correção feita por Orcamento#editarDecisao, que valida o impacto no total orçado. Data e autor ficam. */
    void atualizar(String novaDescricao, String novaJustificativa, BigDecimal novoValorImpacto) {
        this.descricao = novaDescricao;
        this.justificativa = novaJustificativa;
        this.valorImpacto = CalculoFinanceiro.monetario(novoValorImpacto);
    }
}
