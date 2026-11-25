package br.com.obras.compras.domain;

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

import java.time.LocalDateTime;

/** Imutável: toda mudança de estado do pedido gera uma linha. */
@Entity
@Table(name = "historico_status_pedido")
@Getter
@NoArgsConstructor
public class HistoricoStatusPedido {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pedido_compra_id", updatable = false)
    private PedidoCompra pedido;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15, updatable = false)
    private StatusPedido status;

    @Column(name = "data_hora", nullable = false, updatable = false)
    private LocalDateTime dataHora;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long autorId;

    HistoricoStatusPedido(PedidoCompra pedido, StatusPedido status, Long autorId, LocalDateTime dataHora) {
        this.pedido = pedido;
        this.status = status;
        this.autorId = autorId;
        this.dataHora = dataHora;
    }
}
