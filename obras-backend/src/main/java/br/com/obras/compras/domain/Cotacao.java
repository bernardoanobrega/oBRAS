package br.com.obras.compras.domain;

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
import java.time.LocalDateTime;

@Entity
@Table(name = "cotacao")
@Getter
@NoArgsConstructor
public class Cotacao {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_compra_id", updatable = false)
    private PedidoCompra pedido;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fornecedor_id", updatable = false)
    private Fornecedor fornecedor;

    @Column(name = "data_cotacao", nullable = false, updatable = false)
    private LocalDateTime dataCotacao;

    @Column(name = "valor_total_proposto", nullable = false, precision = 15, scale = 2, updatable = false)
    private BigDecimal valorTotalProposto;

    @Column(name = "prazo_entrega_dias", nullable = false, updatable = false)
    private Integer prazoEntregaDias;

    @Column(nullable = false)
    private boolean selecionada;

    Cotacao(PedidoCompra pedido, Fornecedor fornecedor, BigDecimal valorTotalProposto,
            Integer prazoEntregaDias, LocalDateTime dataCotacao) {
        if (valorTotalProposto == null || valorTotalProposto.signum() <= 0) {
            throw new RegraNegocioException("Valor proposto deve ser maior que zero");
        }
        if (prazoEntregaDias == null || prazoEntregaDias < 0) {
            throw new RegraNegocioException("Prazo de entrega não pode ser negativo");
        }
        this.pedido = pedido;
        this.fornecedor = fornecedor;
        this.valorTotalProposto = valorTotalProposto;
        this.prazoEntregaDias = prazoEntregaDias;
        this.dataCotacao = dataCotacao;
    }

    void marcarComoSelecionada() { this.selecionada = true; }

    void desmarcar() { this.selecionada = false; }
}
