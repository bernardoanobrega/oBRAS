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
import java.math.RoundingMode;

@Entity
@Table(name = "item_pedido")
@Getter
@NoArgsConstructor
public class ItemPedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_compra_id", updatable = false)
    private PedidoCompra pedido;

    /** Material pertence ao módulo estoque: guardamos só o id (acesso via MaterialPort). */
    @Column(name = "material_id", nullable = false, updatable = false)
    private Long materialId;

    /** Item do orçamento (0..1). Módulo orçamento ainda não existe: guardamos só o id. */
    @Column(name = "item_orcado_id", updatable = false)
    private Long itemOrcadoId;

    @Column(nullable = false, precision = 15, scale = 3, updatable = false)
    private BigDecimal quantidade;

    @Column(name = "valor_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorUnitario;

    ItemPedido(PedidoCompra pedido, Long materialId, BigDecimal quantidade, BigDecimal valorUnitario,
               Long itemOrcadoId) {
        if (materialId == null) throw new RegraNegocioException("Material é obrigatório");
        exigirQuantidade(quantidade);
        exigirValor(valorUnitario);
        this.pedido = pedido;
        this.materialId = materialId;
        this.quantidade = quantidade;
        this.valorUnitario = valorUnitario;
        this.itemOrcadoId = itemOrcadoId;
    }

    /** Permitido em SOLICITADO e EM_COTACAO (a regra de estado fica no PedidoCompra). */
    void ajustarValorUnitario(BigDecimal valorUnitario) {
        exigirValor(valorUnitario);
        this.valorUnitario = valorUnitario;
    }

    public BigDecimal calcularValorSubTotal() {
        return quantidade.multiply(valorUnitario).setScale(2, RoundingMode.HALF_UP);
    }

    private static void exigirQuantidade(BigDecimal quantidade) {
        if (quantidade == null || quantidade.signum() <= 0) {
            throw new RegraNegocioException("Quantidade deve ser maior que zero");
        }
    }

    private static void exigirValor(BigDecimal valor) {
        if (valor == null || valor.signum() < 0) {
            throw new RegraNegocioException("Valor unitário não pode ser negativo");
        }
    }
}
