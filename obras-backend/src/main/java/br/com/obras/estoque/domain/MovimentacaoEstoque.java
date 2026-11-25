package br.com.obras.estoque.domain;

import br.com.obras.shared.exception.RegraNegocioException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/** Imutável (histórico). Criada só pelas fábricas, que garantem as regras por tipo. */
@Entity
@Table(name = "movimentacao_estoque")
@Getter
@NoArgsConstructor
public class MovimentacaoEstoque {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20, updatable = false)
    private TipoMovimentacao tipo;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "material_id", updatable = false)
    private Material material;

    @Column(nullable = false, precision = 15, scale = 3, updatable = false)
    private BigDecimal quantidade;

    @Column(name = "canteiro_origem_id", updatable = false)
    private Long canteiroOrigemId;

    @Column(name = "canteiro_destino_id", updatable = false)
    private Long canteiroDestinoId;

    @Column(name = "pedido_compra_id", updatable = false)
    private Long pedidoCompraId;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long autorId;

    @Column(name = "data_movimentacao", nullable = false, updatable = false)
    private LocalDateTime dataMovimentacao;

    public static MovimentacaoEstoque entradaCompra(Material material, BigDecimal quantidade,
                                                    Long canteiroDestinoId, Long pedidoCompraId,
                                                    Long autorId, LocalDateTime instante) {
        if (pedidoCompraId == null) throw new RegraNegocioException("Entrada por compra exige o pedido");
        return nova(TipoMovimentacao.ENTRADA_COMPRA, material, quantidade, null, canteiroDestinoId,
                pedidoCompraId, autorId, instante);
    }

    public static MovimentacaoEstoque saidaConsumo(Material material, BigDecimal quantidade,
                                                   Long canteiroOrigemId, Long autorId, LocalDateTime instante) {
        return nova(TipoMovimentacao.SAIDA_CONSUMO, material, quantidade, canteiroOrigemId, null,
                null, autorId, instante);
    }

    public static MovimentacaoEstoque transferencia(Material material, BigDecimal quantidade,
                                                    Long canteiroOrigemId, Long canteiroDestinoId,
                                                    Long autorId, LocalDateTime instante) {
        if (canteiroOrigemId != null && canteiroOrigemId.equals(canteiroDestinoId)) {
            throw new RegraNegocioException("Origem e destino da transferência devem ser canteiros diferentes");
        }
        return nova(TipoMovimentacao.TRANSFERENCIA, material, quantidade, canteiroOrigemId,
                canteiroDestinoId, null, autorId, instante);
    }

    private static MovimentacaoEstoque nova(TipoMovimentacao tipo, Material material, BigDecimal quantidade,
                                            Long origem, Long destino, Long pedido, Long autorId,
                                            LocalDateTime instante) {
        if (quantidade == null || quantidade.signum() <= 0) {
            throw new RegraNegocioException("A quantidade deve ser maior que zero");
        }
        boolean precisaOrigem = tipo != TipoMovimentacao.ENTRADA_COMPRA;
        boolean precisaDestino = tipo != TipoMovimentacao.SAIDA_CONSUMO;
        if ((precisaOrigem && origem == null) || (precisaDestino && destino == null)) {
            throw new RegraNegocioException("Canteiro de origem/destino não informado");
        }
        MovimentacaoEstoque m = new MovimentacaoEstoque();
        m.tipo = tipo;
        m.material = material;
        m.quantidade = quantidade;
        m.canteiroOrigemId = origem;
        m.canteiroDestinoId = destino;
        m.pedidoCompraId = pedido;
        m.autorId = autorId;
        m.dataMovimentacao = instante;
        return m;
    }
}
