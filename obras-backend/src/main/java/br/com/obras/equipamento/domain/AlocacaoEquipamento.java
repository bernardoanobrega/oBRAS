package br.com.obras.equipamento.domain;

import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Período em que um equipamento esteve (ou está) alocado em uma obra. Está ativa enquanto {@code dataFim} é vazia.
 * Criada apenas por {@link Equipamento}, que garante a invariante de uma única alocação ativa.
 */
@Entity
@Table(name = "alocacao_equipamento")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AlocacaoEquipamento {

    private static final int TAMANHO_MAXIMO_OBSERVACAO = 1000;

    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "id")
    private Auditavel auditavel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "equipamento_id", nullable = false, updatable = false)
    private Equipamento equipamento;

    @Column(name = "obra_id", nullable = false, updatable = false)
    private Long obraId;

    @Column(name = "responsavel_id", nullable = false, updatable = false)
    private Long responsavelId;

    @Column(name = "data_inicio", nullable = false, updatable = false)
    private LocalDate dataInicio;

    @Column(name = "data_prevista_devolucao", nullable = false)
    private LocalDate dataPrevistaDevolucao;

    @Column(name = "data_fim")
    private LocalDate dataFim;

    @Column(length = TAMANHO_MAXIMO_OBSERVACAO)
    private String observacao;

    @Column(name = "observacao_devolucao", length = TAMANHO_MAXIMO_OBSERVACAO)
    private String observacaoDevolucao;

    AlocacaoEquipamento(Auditavel auditavel, Equipamento equipamento, Long obraId, Long responsavelId,
            LocalDate dataInicio, LocalDate dataPrevistaDevolucao, String observacao) {
        if (auditavel == null || auditavel.isExcluido()) {
            throw new RegraNegocioException("A alocação exige um registro auditável ativo.");
        }
        if (obraId == null) {
            throw new RegraNegocioException("A obra é obrigatória.");
        }
        if (responsavelId == null) {
            throw new RegraNegocioException("O responsável é obrigatório.");
        }
        exigirPrevisaoValida(dataInicio, dataPrevistaDevolucao);
        this.auditavel = auditavel;
        this.equipamento = equipamento;
        this.obraId = obraId;
        this.responsavelId = responsavelId;
        this.dataInicio = dataInicio;
        this.dataPrevistaDevolucao = dataPrevistaDevolucao;
        this.observacao = limitarObservacao(observacao);
    }

    public void prorrogar(LocalDate novaDataPrevista) {
        exigirAtiva("Apenas alocações ativas podem ser prorrogadas.");
        if (novaDataPrevista == null || !novaDataPrevista.isAfter(dataPrevistaDevolucao)) {
            throw new RegraNegocioException("A nova data prevista deve ser posterior à data prevista atual.");
        }
        dataPrevistaDevolucao = novaDataPrevista;
    }

    public void encerrar(LocalDate dataFim, String observacao) {
        exigirAtiva("A alocação já foi encerrada.");
        if (dataFim == null || dataFim.isBefore(dataInicio)) {
            throw new RegraNegocioException("A data de fim não pode ser anterior à data de início.");
        }
        this.dataFim = dataFim;
        this.observacaoDevolucao = limitarObservacao(observacao);
    }

    public boolean estaAtiva() {
        return dataFim == null;
    }

    public boolean estaAtrasada() {
        return estaAtrasada(LocalDate.now());
    }

    public boolean estaAtrasada(LocalDate referencia) {
        return estaAtiva() && referencia.isAfter(dataPrevistaDevolucao);
    }

    private void exigirAtiva(String mensagem) {
        if (!estaAtiva()) {
            throw new RegraNegocioException(mensagem);
        }
    }

    private static void exigirPrevisaoValida(LocalDate dataInicio, LocalDate dataPrevistaDevolucao) {
        if (dataPrevistaDevolucao == null) {
            throw new RegraNegocioException("A data prevista de devolução é obrigatória.");
        }
        if (dataPrevistaDevolucao.isBefore(dataInicio)) {
            throw new RegraNegocioException("A data prevista de devolução não pode ser anterior ao início.");
        }
    }

    private static String limitarObservacao(String observacao) {
        if (observacao == null || observacao.isBlank()) {
            return null;
        }
        String observacaoLimpa = observacao.strip();
        if (observacaoLimpa.length() > TAMANHO_MAXIMO_OBSERVACAO) {
            throw new RegraNegocioException(
                    "A observação deve ter no máximo " + TAMANHO_MAXIMO_OBSERVACAO + " caracteres.");
        }
        return observacaoLimpa;
    }
}