package br.com.obras.equipamento.domain;

import br.com.obras.shared.domain.Auditavel;
import br.com.obras.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Máquina, ferramenta ou outro equipamento controlado por unidade.
 *
 * <p>Invariante: {@code status = ALOCADO} se e somente se existe uma {@link AlocacaoEquipamento} ativa,
 * e há no máximo uma alocação ativa por equipamento. Equipamento nunca é excluído: usa-se {@code INATIVO}.
 */
@Entity
@Table(name = "equipamento")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Equipamento extends Auditavel {

    private static final int TAMANHO_MAXIMO_CODIGO = 50;
    private static final int TAMANHO_MAXIMO_DESCRICAO = 300;
    private static final String OBSERVACAO_TRANSFERENCIA = "Transferido para outra obra.";

    @Column(name = "codigo_patrimonio", nullable = false, updatable = false, length = TAMANHO_MAXIMO_CODIGO)
    private String codigoPatrimonio;

    @Column(nullable = false, length = TAMANHO_MAXIMO_DESCRICAO)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private TipoEquipamento tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusEquipamento status;

    @Getter(AccessLevel.NONE)
    @OneToMany(mappedBy = "equipamento", fetch = FetchType.LAZY)
    private List<AlocacaoEquipamento> alocacoes = new ArrayList<>();

    public Equipamento(String codigoPatrimonio, String descricao, TipoEquipamento tipo) {
        this.codigoPatrimonio = exigirTexto(codigoPatrimonio, "Código de patrimônio", TAMANHO_MAXIMO_CODIGO);
        this.descricao = exigirTexto(descricao, "Descrição", TAMANHO_MAXIMO_DESCRICAO);
        this.tipo = exigirTipo(tipo);
        this.status = StatusEquipamento.DISPONIVEL;
    }

    public void editar(String descricao, TipoEquipamento tipo) {
        if (status == StatusEquipamento.INATIVO) {
            throw new RegraNegocioException("Equipamento inativo não pode ser editado.");
        }
        this.descricao = exigirTexto(descricao, "Descrição", TAMANHO_MAXIMO_DESCRICAO);
        this.tipo = exigirTipo(tipo);
    }

    public void desativar() {
        if (status == StatusEquipamento.ALOCADO) {
            throw new RegraNegocioException("Equipamento alocado deve ser devolvido antes de ser desativado.");
        }
        if (status == StatusEquipamento.INATIVO) {
            throw new RegraNegocioException("Equipamento já está inativo.");
        }
        status = StatusEquipamento.INATIVO;
    }

    public void reativar() {
        if (status != StatusEquipamento.INATIVO) {
            throw new RegraNegocioException("Apenas equipamentos inativos podem ser reativados.");
        }
        status = StatusEquipamento.DISPONIVEL;
    }

    public AlocacaoEquipamento alocar(Long obraId, LocalDate dataPrevistaDevolucao, Long responsavelId,
            String observacao) {
        if (status == StatusEquipamento.ALOCADO) {
            throw new RegraNegocioException("Equipamento já está alocado.");
        }
        if (status == StatusEquipamento.INATIVO) {
            throw new RegraNegocioException("Equipamento inativo não pode ser alocado.");
        }
        var alocacao = new AlocacaoEquipamento(this, obraId, responsavelId, LocalDate.now(),
                dataPrevistaDevolucao, observacao);
        alocacoes.add(alocacao);
        status = StatusEquipamento.ALOCADO;
        return alocacao;
    }

    public void devolver(String observacao) {
        AlocacaoEquipamento alocacaoAtual = exigirAlocacaoAtiva("Apenas equipamentos alocados podem ser devolvidos.");
        alocacaoAtual.encerrar(LocalDate.now(), observacao);
        status = StatusEquipamento.DISPONIVEL;
    }

    public AlocacaoEquipamento transferir(Long obraDestinoId, LocalDate dataPrevistaDevolucao,
            Long responsavelId, String observacao) {
        AlocacaoEquipamento alocacaoAtual = exigirAlocacaoAtiva("Apenas equipamentos alocados podem ser transferidos.");
        if (alocacaoAtual.getObraId().equals(obraDestinoId)) {
            throw new RegraNegocioException("O equipamento já está alocado na obra de destino.");
        }
        LocalDate hoje = LocalDate.now();
        var novaAlocacao = new AlocacaoEquipamento(this, obraDestinoId, responsavelId, hoje,
                dataPrevistaDevolucao, observacao);
        alocacaoAtual.encerrar(hoje, OBSERVACAO_TRANSFERENCIA);
        alocacoes.add(novaAlocacao);
        return novaAlocacao;
    }

    public Optional<AlocacaoEquipamento> alocacaoAtiva() {
        return alocacoes.stream().filter(AlocacaoEquipamento::estaAtiva).findFirst();
    }

    public Optional<Long> localizacaoAtual() {
        return alocacaoAtiva().map(AlocacaoEquipamento::getObraId);
    }

    public boolean estaAtrasado() {
        return estaAtrasado(LocalDate.now());
    }

    public boolean estaAtrasado(LocalDate referencia) {
        return alocacaoAtiva().map(alocacao -> alocacao.estaAtrasada(referencia)).orElse(false);
    }

    private AlocacaoEquipamento exigirAlocacaoAtiva(String mensagemQuandoNaoAlocado) {
        if (status != StatusEquipamento.ALOCADO) {
            throw new RegraNegocioException(mensagemQuandoNaoAlocado);
        }
        return alocacaoAtiva().orElseThrow(
                () -> new IllegalStateException("Equipamento alocado sem alocação ativa."));
    }

    private static TipoEquipamento exigirTipo(TipoEquipamento tipo) {
        if (tipo == null) {
            throw new RegraNegocioException("O campo Tipo é obrigatório.");
        }
        return tipo;
    }

    private static String exigirTexto(String valor, String campo, int tamanhoMaximo) {
        if (valor == null || valor.isBlank()) {
            throw new RegraNegocioException("O campo " + campo + " é obrigatório.");
        }
        String valorLimpo = valor.strip();
        if (valorLimpo.length() > tamanhoMaximo) {
            throw new RegraNegocioException(
                    "O campo " + campo + " deve ter no máximo " + tamanhoMaximo + " caracteres.");
        }
        return valorLimpo;
    }
}