package br.com.obras.orcamento.domain;

import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Orçamento executivo de uma obra (UC10). Máquina de estados do diagrama orcamento-states:
 * ELABORACAO até o primeiro item; depois o status acompanha a saúde financeira; CANCELADO é final.
 */
@Entity
@Table(name = "orcamento")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Orcamento {
    private static final BigDecimal LIMITE_ALERTA_MAXIMO = new BigDecimal("100");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "obra_id", nullable = false, updatable = false)
    private Long obraId;

    @Column(name = "data_elaboracao", nullable = false, updatable = false)
    private LocalDate dataElaboracao;

    @Column(name = "data_ultima_atualizacao", nullable = false)
    private LocalDate dataUltimaAtualizacao;

    @Column(name = "percentual_limite_alerta", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualLimiteAlerta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusOrcamento status;

    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("codigo")
    private List<ItemOrcado> itens = new ArrayList<>();

    @OneToMany(mappedBy = "orcamento", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataDecisao DESC, id DESC")
    private List<DecisaoFinanceira> decisoes = new ArrayList<>();

    /** Transição [*] -> ELABORACAO. */
    public static Orcamento elaborar(Long obraId, BigDecimal percentualLimiteAlerta) {
        if (obraId == null) {
            throw new RegraNegocioException("A obra do orçamento é obrigatória");
        }
        boolean limiteValido = percentualLimiteAlerta != null && percentualLimiteAlerta.signum() > 0
                && percentualLimiteAlerta.compareTo(LIMITE_ALERTA_MAXIMO) <= 0;
        if (!limiteValido) {
            throw new RegraNegocioException("O percentual limite de alerta deve ser maior que 0 e no máximo 100");
        }
        Orcamento orcamento = new Orcamento();
        orcamento.obraId = obraId;
        orcamento.percentualLimiteAlerta = percentualLimiteAlerta;
        orcamento.dataElaboracao = LocalDate.now();
        orcamento.dataUltimaAtualizacao = orcamento.dataElaboracao;
        orcamento.status = StatusOrcamento.ELABORACAO;
        return orcamento;
    }

    public List<ItemOrcado> getItens() {
        return Collections.unmodifiableList(itens);
    }

    public List<DecisaoFinanceira> getDecisoes() {
        return Collections.unmodifiableList(decisoes);
    }

    /** RF-30. O primeiro item leva ELABORACAO -> SAUDAVEL (início do acompanhamento). */
    public ItemOrcado adicionarItem(String codigo, String descricao, String unidade, BigDecimal quantidade,
                                    BigDecimal custoUnitarioOrcado) {
        exigirAtivo();
        boolean codigoRepetido = codigo != null && itens.stream()
                .anyMatch(item -> item.getCodigo().equalsIgnoreCase(codigo.trim()));
        if (codigoRepetido) {
            throw new RegraNegocioException("Já existe um item com o código " + codigo.trim() + " neste orçamento");
        }
        ItemOrcado item = new ItemOrcado(this, codigo, descricao, unidade, quantidade, custoUnitarioOrcado);
        itens.add(item);
        if (status == StatusOrcamento.ELABORACAO) {
            status = StatusOrcamento.SAUDAVEL;
        }
        atualizarAcompanhamento();
        return item;
    }

    /** Item com custo lançado não pode ser removido: apagaria histórico financeiro. */
    public void removerItem(Long idItem) {
        exigirAtivo();
        ItemOrcado item = buscarItem(idItem);
        if (item.possuiCustos()) {
            throw new RegraNegocioException("Não é possível remover um item que já possui custos lançados");
        }
        itens.remove(item);
        atualizarAcompanhamento();
    }

    public ItemOrcado buscarItem(Long idItem) {
        return itens.stream()
                .filter(item -> Objects.equals(item.getId(), idItem))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item orçado", idItem));
    }

    /** O valorImpacto entra no total orçado e pode reduzir o desvio (ATENCAO -> SAUDAVEL, CRITICA -> ATENCAO). */
    public DecisaoFinanceira registrarDecisao(String descricao, String justificativa, BigDecimal valorImpacto,
                                              Long usuarioId) {
        exigirAtivo();
        if (usuarioId == null) {
            throw new RegraNegocioException("O autor da decisão é obrigatório");
        }
        validarDecisao(descricao, justificativa, valorImpacto, calcularTotalOrcado());
        DecisaoFinanceira decisao = new DecisaoFinanceira(this, descricao.trim(), justificativa.trim(),
                valorImpacto, usuarioId);
        decisoes.add(decisao);
        atualizarAcompanhamento();
        return decisao;
    }

    /** Corrige uma decisão registrada; o novo impacto substitui o anterior no total orçado. */
    public DecisaoFinanceira editarDecisao(Long idDecisao, String descricao, String justificativa,
                                           BigDecimal valorImpacto) {
        exigirAtivo();
        DecisaoFinanceira decisao = buscarDecisao(idDecisao);
        BigDecimal totalSemEstaDecisao = calcularTotalOrcado().subtract(decisao.getValorImpacto());
        validarDecisao(descricao, justificativa, valorImpacto, totalSemEstaDecisao);
        decisao.atualizar(descricao.trim(), justificativa.trim(), valorImpacto);
        atualizarAcompanhamento();
        return decisao;
    }

    public DecisaoFinanceira buscarDecisao(Long idDecisao) {
        return decisoes.stream()
                .filter(decisao -> Objects.equals(decisao.getId(), idDecisao))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Decisão financeira", idDecisao));
    }

    /** Soma dos itens mais o impacto das decisões financeiras. */
    public BigDecimal calcularTotalOrcado() {
        BigDecimal totalItens = itens.stream()
                .map(ItemOrcado::calcularValorOrcado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalDecisoes = decisoes.stream()
                .map(DecisaoFinanceira::getValorImpacto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return CalculoFinanceiro.monetario(totalItens.add(totalDecisoes));
    }

    public BigDecimal calcularTotalRealizado() {
        BigDecimal total = itens.stream()
                .map(ItemOrcado::calcularTotalRealizado)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return CalculoFinanceiro.monetario(total);
    }

    public BigDecimal calcularDesvioFinanceiro() {
        return calcularTotalRealizado().subtract(calcularTotalOrcado());
    }

    public BigDecimal calcularPercentualDesvio() {
        return CalculoFinanceiro.percentual(calcularDesvioFinanceiro(), calcularTotalOrcado());
    }

    /** Diagrama orcamento-estouro-desvios-activity. */
    public List<ItemOrcado> listarItensEmDesvio() {
        return itens.stream().filter(ItemOrcado::estaEmEstouro).toList();
    }

    /** RF-33 e RF-34 (diagrama orcamento-geracaoindicadores-saude-sequence). */
    public IndicadorSaudeFinanceira gerarIndicadoresSaude() {
        return new IndicadorSaudeFinanceira(calcularTotalOrcado(), calcularTotalRealizado(),
                listarItensEmDesvio().size(), percentualLimiteAlerta);
    }

    /** Qualquer estado não final -> CANCELADO. */
    public void cancelar() {
        exigirAtivo();
        status = StatusOrcamento.CANCELADO;
        dataUltimaAtualizacao = LocalDate.now();
    }

    public boolean estaCancelado() {
        return status == StatusOrcamento.CANCELADO;
    }

    void exigirAtivo() {
        if (estaCancelado()) {
            throw new RegraNegocioException("Orçamento cancelado não pode ser alterado");
        }
    }

    /** Recalcula a saúde após qualquer mudança de valores. Em ELABORACAO o status não muda. */
    void atualizarAcompanhamento() {
        dataUltimaAtualizacao = LocalDate.now();
        if (status != StatusOrcamento.ELABORACAO) {
            status = StatusOrcamento.de(gerarIndicadoresSaude().getSaude());
        }
    }

    private static void validarDecisao(String descricao, String justificativa, BigDecimal valorImpacto,
                                       BigDecimal totalOrcadoSemDecisao) {
        if (descricao == null || descricao.isBlank() || justificativa == null || justificativa.isBlank()) {
            throw new RegraNegocioException("Descrição e justificativa da decisão são obrigatórias");
        }
        if (valorImpacto == null) {
            throw new RegraNegocioException("O valor de impacto da decisão é obrigatório");
        }
        if (totalOrcadoSemDecisao.add(valorImpacto).signum() < 0) {
            throw new RegraNegocioException("A decisão deixaria o total orçado negativo");
        }
    }
}
