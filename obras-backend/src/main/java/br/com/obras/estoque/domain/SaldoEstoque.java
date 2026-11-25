package br.com.obras.estoque.domain;

import br.com.obras.shared.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/** Um saldo por (material, canteiro). O saldo da obra é a soma dos saldos dos seus canteiros. */
@Entity
@Table(name = "saldo_estoque")
@Getter
@NoArgsConstructor
public class SaldoEstoque {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", updatable = false)
    private Material material;

    /** Canteiro pertence ao módulo obra: guardamos só o id. */
    @Column(name = "canteiro_id", nullable = false, updatable = false)
    private Long canteiroId;

    @Column(name = "quantidade_atual", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidadeAtual = BigDecimal.ZERO;

    @Column(name = "quantidade_minima", nullable = false, precision = 15, scale = 3)
    private BigDecimal quantidadeMinima = BigDecimal.ZERO;

    /** Estado inicial: ESGOTADO (quantidadeAtual = 0). */
    public static SaldoEstoque criar(Material material, Long canteiroId, BigDecimal quantidadeMinima) {
        SaldoEstoque s = new SaldoEstoque();
        s.material = material;
        s.canteiroId = canteiroId;
        s.definirQuantidadeMinima(quantidadeMinima);
        return s;
    }

    public void adicionarSaldo(BigDecimal quantidade) {
        exigirPositiva(quantidade);
        quantidadeAtual = quantidadeAtual.add(quantidade);
    }

    /** Exige 0 < quantidade <= quantidadeAtual; fora disso a operação é rejeitada e nada muda. */
    public void subtrairSaldo(BigDecimal quantidade) {
        exigirPositiva(quantidade);
        if (quantidade.compareTo(quantidadeAtual) > 0) {
            throw new RegraNegocioException("Quantidade insuficiente em estoque");
        }
        quantidadeAtual = quantidadeAtual.subtract(quantidade);
    }

    public void definirQuantidadeMinima(BigDecimal quantidadeMinima) {
        if (quantidadeMinima == null || quantidadeMinima.signum() < 0) {
            throw new RegraNegocioException("A quantidade mínima não pode ser negativa");
        }
        this.quantidadeMinima = quantidadeMinima;
    }

    public SituacaoSaldo situacao() {
        if (quantidadeAtual.signum() == 0) return SituacaoSaldo.ESGOTADO;
        if (quantidadeAtual.compareTo(quantidadeMinima) <= 0) return SituacaoSaldo.ALERTA_MINIMO;
        return SituacaoSaldo.REGULAR;
    }

    /** Verdadeiro em ESGOTADO e ALERTA_MINIMO. */
    public boolean verificarAlertaMinimo() { return situacao() != SituacaoSaldo.REGULAR; }

    private static void exigirPositiva(BigDecimal quantidade) {
        if (quantidade == null || quantidade.signum() <= 0) {
            throw new RegraNegocioException("A quantidade deve ser maior que zero");
        }
    }
}
