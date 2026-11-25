package br.com.obras.orcamento.domain;

import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Linha da planilha orçamentária executiva (RF-30, RF-31). */
@Entity
@Table(name = "item_orcado")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ItemOrcado {
    private static final int TAMANHO_LOTE_CUSTOS = 50;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "orcamento_id", nullable = false, updatable = false)
    private Orcamento orcamento;

    @Column(nullable = false, length = 50, updatable = false)
    private String codigo;

    @Column(nullable = false, length = 300)
    private String descricao;

    @Column(nullable = false, length = 20)
    private String unidade;

    @Column(nullable = false, precision = 15, scale = 4)
    private BigDecimal quantidade;

    @Column(name = "custo_unitario_orcado", nullable = false, precision = 15, scale = 2)
    private BigDecimal custoUnitarioOrcado;

    @OneToMany(mappedBy = "item", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataLancamento DESC, id DESC")
    @BatchSize(size = TAMANHO_LOTE_CUSTOS)
    private List<CustoRealizado> custos = new ArrayList<>();

    ItemOrcado(Orcamento orcamento, String codigo, String descricao, String unidade, BigDecimal quantidade,
               BigDecimal custoUnitarioOrcado) {
        this.orcamento = orcamento;
        this.codigo = exigirTexto(codigo, "Código do item");
        this.descricao = exigirTexto(descricao, "Descrição do item");
        this.unidade = exigirTexto(unidade, "Unidade do item");
        definirValores(quantidade, custoUnitarioOrcado);
    }

    public List<CustoRealizado> getCustos() {
        return Collections.unmodifiableList(custos);
    }

    /**
     * Lança um custo real no item e recalcula a saúde do orçamento
     * (diagrama itemorcado-validacaodecusto-sequence).
     */
    public CustoRealizado registrarCusto(String descricao, BigDecimal valor, OrigemCusto origem, Long idOrigem,
                                         Long usuarioId) {
        orcamento.exigirAtivo();
        CustoRealizado custo = new CustoRealizado(this, descricao, valor, origem, idOrigem, usuarioId);
        if (!custo.validar()) {
            throw new RegraNegocioException("Dados do custo inválidos: informe descrição, valor maior que zero "
                    + "e origem. A origem OUTRO não aceita referência de origem.");
        }
        custos.add(custo);
        orcamento.atualizarAcompanhamento();
        return custo;
    }

    public CustoRealizado buscarCusto(Long idCusto) {
        return custos.stream()
                .filter(custo -> Objects.equals(custo.getId(), idCusto))
                .findFirst()
                .orElseThrow(() -> new RecursoNaoEncontradoException("Custo realizado", idCusto));
    }

    /** Corrige descrição, valor e origem de um custo lançado manualmente e recalcula a saúde. */
    public CustoRealizado editarCusto(Long idCusto, String descricao, BigDecimal valor, OrigemCusto origem) {
        orcamento.exigirAtivo();
        CustoRealizado custo = buscarCustoManual(idCusto);
        custo.atualizar(descricao, valor, origem);
        orcamento.atualizarAcompanhamento();
        return custo;
    }

    /** Exclui um custo lançado manualmente (ex.: lançamento em duplicidade) e recalcula a saúde. */
    public void removerCusto(Long idCusto) {
        orcamento.exigirAtivo();
        custos.remove(buscarCustoManual(idCusto));
        orcamento.atualizarAcompanhamento();
    }

    /** RF-31: editar quantitativo e custo unitário. */
    public void atualizarValores(BigDecimal quantidade, BigDecimal custoUnitarioOrcado) {
        orcamento.exigirAtivo();
        definirValores(quantidade, custoUnitarioOrcado);
        orcamento.atualizarAcompanhamento();
    }

    public BigDecimal calcularValorOrcado() {
        return CalculoFinanceiro.monetario(quantidade.multiply(custoUnitarioOrcado));
    }

    public BigDecimal calcularTotalRealizado() {
        BigDecimal total = custos.stream()
                .map(CustoRealizado::getValor)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return CalculoFinanceiro.monetario(total);
    }

    /** Realizado menos orçado: positivo indica gasto acima do previsto. */
    public BigDecimal calcularDesvioFinanceiro() {
        return calcularTotalRealizado().subtract(calcularValorOrcado());
    }

    public BigDecimal calcularPercentualDesvio() {
        return CalculoFinanceiro.percentual(calcularDesvioFinanceiro(), calcularValorOrcado());
    }

    public BigDecimal calcularPercentualExecutado() {
        return CalculoFinanceiro.percentual(calcularTotalRealizado(), calcularValorOrcado());
    }

    public boolean estaEmEstouro() {
        return calcularTotalRealizado().compareTo(calcularValorOrcado()) > 0;
    }

    /** CRITICA em estouro; ATENCAO ao atingir o limite de alerta do orçamento; SAUDAVEL nos demais casos. */
    public SaudeFinanceira classificarSaude(BigDecimal limiteAlerta) {
        if (estaEmEstouro()) {
            return SaudeFinanceira.CRITICA;
        }
        boolean atingiuLimite = CalculoFinanceiro.atingiuPercentual(calcularTotalRealizado(),
                calcularValorOrcado(), limiteAlerta);
        return atingiuLimite ? SaudeFinanceira.ATENCAO : SaudeFinanceira.SAUDAVEL;
    }

    public boolean possuiCustos() {
        return !custos.isEmpty();
    }

    private void definirValores(BigDecimal quantidade, BigDecimal custoUnitarioOrcado) {
        if (quantidade == null || quantidade.signum() <= 0) {
            throw new RegraNegocioException("A quantidade do item deve ser maior que zero");
        }
        if (custoUnitarioOrcado == null || custoUnitarioOrcado.signum() < 0) {
            throw new RegraNegocioException("O custo unitário orçado não pode ser negativo");
        }
        this.quantidade = quantidade;
        this.custoUnitarioOrcado = CalculoFinanceiro.monetario(custoUnitarioOrcado);
    }

    private CustoRealizado buscarCustoManual(Long idCusto) {
        CustoRealizado custo = buscarCusto(idCusto);
        if (custo.estaVinculadoAOutroModulo()) {
            throw new RegraNegocioException("Este custo foi lançado por outro módulo (ex.: pedido de compra) "
                    + "e só pode ser corrigido na origem");
        }
        return custo;
    }

    private static String exigirTexto(String valor, String campo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException(campo + " é obrigatório(a)");
        }
        return valor.trim();
    }
}
