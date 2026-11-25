package br.com.obras.orcamento.domain;

import br.com.obras.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Custo efetivamente gasto em um item orçado. Criado somente por {@link ItemOrcado#registrarCusto}. */
@Entity
@Table(name = "custo_realizado")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CustoRealizado {
    private static final int TAMANHO_MAXIMO_DESCRICAO = 300;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "item_orcado_id", nullable = false, updatable = false)
    private ItemOrcado item;

    @Column(name = "usuario_id", nullable = false, updatable = false)
    private Long usuarioId;

    @Column(name = "data_lancamento", nullable = false)
    private LocalDate dataLancamento;

    @Column(nullable = false, length = TAMANHO_MAXIMO_DESCRICAO)
    private String descricao;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private OrigemCusto origem;

    @Column(name = "id_origem")
    private Long idOrigem;

    CustoRealizado(ItemOrcado item, String descricao, BigDecimal valor, OrigemCusto origem, Long idOrigem,
                   Long usuarioId) {
        this.item = item;
        this.descricao = descricao;
        this.valor = valor == null ? null : CalculoFinanceiro.monetario(valor);
        this.origem = origem;
        this.idOrigem = idOrigem;
        this.usuarioId = usuarioId;
        this.dataLancamento = LocalDate.now();
    }

    /** Valor positivo, descrição preenchida, origem e autor informados; origem OUTRO não tem idOrigem. */
    public boolean validar() {
        return dadosValidos(descricao, valor, origem, idOrigem) && usuarioId != null;
    }

    /** Custo com idOrigem veio de outro módulo (ex.: pedido de compra) e só pode ser corrigido lá. */
    public boolean estaVinculadoAOutroModulo() {
        return idOrigem != null;
    }

    /** Corrige um lançamento manual. A data e o autor originais são mantidos. */
    void atualizar(String novaDescricao, BigDecimal novoValor, OrigemCusto novaOrigem) {
        BigDecimal valorArredondado = novoValor == null ? null : CalculoFinanceiro.monetario(novoValor);
        if (!dadosValidos(novaDescricao, valorArredondado, novaOrigem, idOrigem)) {
            throw new RegraNegocioException("Dados do custo inválidos: informe descrição e valor maior que zero");
        }
        this.descricao = novaDescricao.trim();
        this.valor = valorArredondado;
        this.origem = novaOrigem;
    }

    private static boolean dadosValidos(String descricao, BigDecimal valor, OrigemCusto origem, Long idOrigem) {
        boolean possuiDescricao = descricao != null && !descricao.isBlank()
                && descricao.length() <= TAMANHO_MAXIMO_DESCRICAO;
        boolean possuiValorPositivo = valor != null && valor.signum() > 0;
        boolean possuiOrigemCoerente = origem != null && (origem != OrigemCusto.OUTRO || idOrigem == null);
        return possuiDescricao && possuiValorPositivo && possuiOrigemCoerente;
    }
}
