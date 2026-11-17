package br.com.obras.compras.domain;

import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Máquina de estados: SOLICITADO -> EM_COTACAO -> COMPRADO -> EM_TRANSITO -> ENTREGUE
 * (CANCELADO só a partir de SOLICITADO ou EM_COTACAO). Todas as regras ficam aqui.
 */
@Entity
@Table(name = "pedido_compra")
@Getter
@NoArgsConstructor
public class PedidoCompra {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "obra_id", nullable = false, updatable = false)
    private Long obraId;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long solicitanteId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fornecedor_id")
    private Fornecedor fornecedor;

    @Column(name = "data_solicitacao", nullable = false, updatable = false)
    private LocalDate dataSolicitacao;

    @Column(name = "data_prevista")
    private LocalDate dataPrevista;

    @Column(name = "valor_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal valorTotal = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusPedido status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private PrioridadePedido prioridade;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id asc")
    private List<ItemPedido> itens = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id asc")
    private List<Cotacao> cotacoes = new ArrayList<>();

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id asc")
    private List<HistoricoStatusPedido> historico = new ArrayList<>();

    public static PedidoCompra criar(Long obraId, Long solicitanteId, PrioridadePedido prioridade,
                                     LocalDate dataPrevista, LocalDateTime instante) {
        if (prioridade == null) throw new RegraNegocioException("Prioridade é obrigatória");
        LocalDate hoje = instante.toLocalDate();
        if (dataPrevista != null && dataPrevista.isBefore(hoje)) {
            throw new RegraNegocioException("A data prevista não pode ser anterior à data da solicitação");
        }
        PedidoCompra pedido = new PedidoCompra();
        pedido.obraId = obraId;
        pedido.solicitanteId = solicitanteId;
        pedido.prioridade = prioridade;
        pedido.dataPrevista = dataPrevista;
        pedido.dataSolicitacao = hoje;
        pedido.status = StatusPedido.SOLICITADO;
        pedido.historico.add(new HistoricoStatusPedido(pedido, StatusPedido.SOLICITADO, solicitanteId, instante));
        return pedido;
    }

    // ---------- itens ----------

    public ItemPedido adicionarItem(Long materialId, BigDecimal quantidade, BigDecimal valorUnitario,
                                    Long itemOrcadoId) {
        exigirStatus("adicionar itens", StatusPedido.SOLICITADO);
        ItemPedido item = new ItemPedido(this, materialId, quantidade, valorUnitario, itemOrcadoId);
        itens.add(item);
        calcularValorTotal();
        return item;
    }

    public void removerItem(Long itemId) {
        exigirStatus("remover itens", StatusPedido.SOLICITADO);
        boolean removido = itens.removeIf(i -> itemId.equals(i.getId()));
        if (!removido) throw new RecursoNaoEncontradoException("Item do pedido", itemId);
        calcularValorTotal();
    }

    public void ajustarValorUnitario(Long itemId, BigDecimal valorUnitario) {
        exigirStatus("ajustar valores", StatusPedido.SOLICITADO, StatusPedido.EM_COTACAO);
        buscarItem(itemId).ajustarValorUnitario(valorUnitario);
        calcularValorTotal();
    }

    public BigDecimal calcularValorTotal() {
        valorTotal = itens.stream()
                .map(ItemPedido::calcularValorSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return valorTotal;
    }

    // ---------- cotações ----------

    public Cotacao registrarCotacao(Fornecedor fornecedor, BigDecimal valorTotalProposto,
                                    Integer prazoEntregaDias, LocalDateTime instante) {
        exigirStatus("registrar cotações", StatusPedido.EM_COTACAO);
        if (fornecedor == null || !fornecedor.estaAtivo()) {
            throw new RegraNegocioException("Fornecedor inativo ou inválido");
        }
        Cotacao cotacao = new Cotacao(this, fornecedor, valorTotalProposto, prazoEntregaDias, instante);
        cotacoes.add(cotacao);
        return cotacao;
    }

    /** Escolhe a cotação vencedora e define o fornecedor do pedido. */
    public Cotacao selecionarCotacao(Long cotacaoId) {
        exigirStatus("selecionar cotação", StatusPedido.EM_COTACAO);
        Cotacao escolhida = cotacoes.stream()
                .filter(c -> cotacaoId.equals(c.getId()))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Cotação", cotacaoId));
        cotacoes.forEach(Cotacao::desmarcar);
        escolhida.marcarComoSelecionada();
        this.fornecedor = escolhida.getFornecedor();
        return escolhida;
    }

    // ---------- estados ----------

    public HistoricoStatusPedido alterarStatus(StatusPedido novoStatus, Long autorId, LocalDateTime instante) {
        if (novoStatus == null || !status.podeIrPara(novoStatus)) {
            if (novoStatus == StatusPedido.CANCELADO) {
                throw new RegraNegocioException("O cancelamento só é permitido até a fase de cotação");
            }
            throw new RegraNegocioException("Transição de status inválida: " + status + " para " + novoStatus);
        }
        if (novoStatus == StatusPedido.EM_COTACAO && itens.isEmpty()) {
            throw new RegraNegocioException("Adicione ao menos um item antes de enviar para cotação");
        }
        if (novoStatus == StatusPedido.COMPRADO) {
            if (fornecedor == null || !fornecedor.estaAtivo()) {
                throw new RegraNegocioException("Selecione uma cotação com fornecedor ativo antes de comprar");
            }
            if (valorTotal.signum() <= 0) {
                throw new RegraNegocioException("O valor total deve ser maior que zero");
            }
        }
        this.status = novoStatus;
        HistoricoStatusPedido registro = new HistoricoStatusPedido(this, novoStatus, autorId, instante);
        historico.add(registro);
        return registro;
    }

    public HistoricoStatusPedido cancelar(Long autorId, LocalDateTime instante) {
        return alterarStatus(StatusPedido.CANCELADO, autorId, instante);
    }

    /** Atraso é derivado da data, não é um estado (mesma ideia do RN-EQP-08). */
    public boolean estaAtrasado(LocalDate dataReferencia) {
        return dataPrevista != null
                && dataPrevista.isBefore(dataReferencia)
                && status != StatusPedido.ENTREGUE
                && status != StatusPedido.CANCELADO;
    }

    // ---------- privados ----------

    private ItemPedido buscarItem(Long itemId) {
        return itens.stream()
                .filter(i -> itemId.equals(i.getId()))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item do pedido", itemId));
    }

    private void exigirStatus(String acao, StatusPedido... permitidos) {
        if (!Arrays.asList(permitidos).contains(status)) {
            throw new RegraNegocioException("Não é possível " + acao + " com o pedido em " + status);
        }
    }
}
