package br.com.obras.cronograma.domain;

import br.com.obras.cronograma.domain.enums.SituacaoPrazo;
import br.com.obras.cronograma.domain.enums.StatusFase;
import br.com.obras.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity 
@Table(name = "fase")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Fase {
    private static final int TAMANHO_LOTE_AVANCOS = 50;
    private static final int TAMANHO_MAXIMO_NOME = 150;
    private static final int TAMANHO_MAXIMO_DESCRICAO = 500;
    
    // ===== ATTRIBUTES =====

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) 
    @JoinColumn(name = "id_cronograma", nullable = false, updatable = false)
    private Cronograma cronograma;

    @Column(nullable = false, length = TAMANHO_MAXIMO_NOME)
    private String nome;

    @Column(length = TAMANHO_MAXIMO_DESCRICAO)
    private String descricao;

    @Column(nullable = false)
    private int ordem;

    @Column(name = "inicio_previsto", nullable = false)
    private LocalDate inicioPrevisto;

    @Column(name = "fim_previsto", nullable = false)
    private LocalDate fimPrevisto;

    @Column(name = "inicio_real")
    private LocalDate inicioReal;

    @Column(name = "fim_real")
    private LocalDate fimReal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusFase status;

    @OneToMany(mappedBy = "fase", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataRegistro ASC, id ASC")
    @BatchSize(size = TAMANHO_LOTE_AVANCOS)
    private List<AvancoFisico> avancos = new ArrayList<>();

    // ===== CONSTRUCTORS =====

    public Fase(Cronograma cronograma, String nome, String descricao, int ordem, LocalDate inicioPrevisto, LocalDate fimPrevisto) {
        if (nome != null && nome.trim().length() > TAMANHO_MAXIMO_NOME) {
            throw new RegraNegocioException("O nome da fase deve ter no máximo " + TAMANHO_MAXIMO_NOME + " caracteres");
        }
        if (descricao != null && descricao.length() > TAMANHO_MAXIMO_DESCRICAO) {
            throw new RegraNegocioException(
                    "A descrição da fase deve ter no máximo " + TAMANHO_MAXIMO_DESCRICAO + " caracteres");
        }
        this.cronograma = cronograma;
        this.nome = nome == null ? null : nome.trim();
        this.descricao = descricao == null || descricao.isBlank() ? null : descricao.trim();
        this.ordem = ordem;
        this.inicioPrevisto = inicioPrevisto;
        this.fimPrevisto = fimPrevisto;
        this.status = StatusFase.NAO_INICIADA;
    }

    // ===== METHODS =====

    /**
     * Método para validar o início e o fim de uma fase
     * @return Retorna um valor booleano representando a corretude do período
     */
    public boolean validarPeriodo() {
        return inicioPrevisto != null && fimPrevisto != null && !inicioPrevisto.isAfter(fimPrevisto);
    }

    /**
     * Método para calcular a quantidade de dias de duração da fase
     * @return Retorna o número de dias calculado
     */
    public long calcularDuracaoDias() {
        return Math.max(1, ChronoUnit.DAYS.between(inicioPrevisto, fimPrevisto) + 1);
    }

    /**
     * Método para adicionar um avanço físico a uma fase
     * @param percentualAcumulado
     * @param observacao
     * @param autor
     * @return Insere o novo avanço na lista de avanços da fase, e retorna o avanço adicionado
     */
    public AvancoFisico registrarAvanco(double percentualAcumulado, String observacao, Long autorId) {
        if(status == StatusFase.CONCLUIDA || status == StatusFase.CANCELADA) {
            throw new RegraNegocioException("Não é possível registrar avanço em uma fase " + status + ".");
        }
        AvancoFisico avanco = new AvancoFisico(this, percentualAcumulado, observacao, autorId);
        double percentualAtual = calcularPercentualConcluido();
        if(!avanco.validar(percentualAtual)) {
            throw new RegraNegocioException("Percentual inválido: deve estar entre 0 e 100 ou igual ao último avanço (" + percentualAtual + "%).");
        }
        avancos.add(avanco);
        if(status == StatusFase.NAO_INICIADA && percentualAcumulado > 0) {
            status = StatusFase.EM_ANDAMENTO;
            inicioReal = avanco.getDataRegistro();
        }
        
        cronograma.atualizarStatus();
        return avanco;
    }

    /**
     * Método para calcular o percentual de conclusão da fase
     * @return Retorna um valor entre 0 e 100 representando o percentual de conclusão
     */
    public double calcularPercentualConcluido() {
        if(status == StatusFase.NAO_INICIADA || avancos.isEmpty()) {
            return 0.0;
        }
        if(status == StatusFase.CONCLUIDA) {
            return 100.0;
        }
        return avancos.get(avancos.size() - 1).getPercentualAcumulado();
    }

    /**
     * Método para finalizar uma fase
     * @param dataReal
     */
    public void concluir(LocalDate dataReal) {
        if(status != StatusFase.EM_ANDAMENTO) {
            throw new RegraNegocioException("Só é possível concluir uma fase que já está em andamento (status atual: " + status + ").");
        }
        if(calcularPercentualConcluido() < 100.0) {
            throw new RegraNegocioException("A fase só pode ser concluída com 100% de avanço registrado.");
        }
        if(dataReal == null) {
            throw new RegraNegocioException("A data de conclusão é obrigatória.");
        }
        if(inicioReal != null && dataReal.isBefore(inicioReal)) {
            throw new RegraNegocioException("A data de conclusão não pode ser anterior ao início real.");
        }

        fimReal = dataReal;
        status = StatusFase.CONCLUIDA;
        cronograma.atualizarStatus();
    }

    /**
     * Método para determinar se uma fase está atrasada em relação ao fimPrevisto
     * @param dataReferencia
     * @return Retorna um valor booleano representando o atraso da fase
     */
    public boolean estaAtrasada(LocalDate dataReferencia) {
        if(dataReferencia == null) {
            throw new IllegalArgumentException("A data de referência é obrigatória.");
        }
        if(status == StatusFase.CONCLUIDA || status == StatusFase.CANCELADA) {
            return false;
        }
        if(status == StatusFase.NAO_INICIADA && dataReferencia.isAfter(inicioPrevisto)) {
            return true;
        }
        return dataReferencia.isAfter(fimPrevisto);
    }

    /**
     * Método para calcular a quantidade de dias de desvio em relação ao prazo estimado
     * @param dataReferencia
     * @return Retorna a quantidade de dias 
     * positivo = atrasada, negativo = adiantada, 0 = em dia
     */
    public int calcularDesvioDias(LocalDate dataReferencia) {
        if(dataReferencia == null) {
            throw new IllegalArgumentException("A data de referência é obrigatória.");
        }
        switch (status) {
            case CONCLUIDA:
                return (int) ChronoUnit.DAYS.between(fimPrevisto, fimReal);
            case NAO_INICIADA:
                return (int) Math.max(0, ChronoUnit.DAYS.between(inicioPrevisto, dataReferencia));
            case EM_ANDAMENTO:
                return (int) Math.max(0, ChronoUnit.DAYS.between(fimPrevisto, dataReferencia));
            default:
                return 0;
        }
    }

    /**
     * Método para classificar uma fase em relação ao prazo
     * @param dataReferencia
     * @return Altera a situação da fase
     */
    public SituacaoPrazo classificarSituacao(LocalDate dataReferencia) {
        return switch (status) {
            case CONCLUIDA -> SituacaoPrazo.CONCLUIDA;
            case CANCELADA -> SituacaoPrazo.CANCELADA;
            case NAO_INICIADA -> estaAtrasada(dataReferencia) ? SituacaoPrazo.ATRASADA : SituacaoPrazo.NAO_INICIADA;
            case EM_ANDAMENTO -> estaAtrasada(dataReferencia) ? SituacaoPrazo.ATRASADA : SituacaoPrazo.EM_DIA;
        };
    }

    /**
     * Método para calcular o percentual do que deveria estar concluído na data de referência
     * @param dataReferencia 
     * @return Retorna um valor entre 0 e 100
     */
    public double calcularPercentualPlanejado(LocalDate dataReferencia) {
        if (dataReferencia == null) {
            throw new IllegalArgumentException("A data de referência é obrigatória");
        }
        if (dataReferencia.isBefore(inicioPrevisto)) {
            return 0.0;
        }
        if (dataReferencia.isAfter(fimPrevisto)) {
            return 100.0;
        }
        return (ChronoUnit.DAYS.between(inicioPrevisto, dataReferencia) + 1) * 100.0 / calcularDuracaoDias();
    }

    /**
     * Método para cancelar uma fase
     */
    public void cancelarFase() {
        if (status == StatusFase.CONCLUIDA) {
            throw new RegraNegocioException("Fase concluída não pode ser cancelada.");
        }
        if (status == StatusFase.CANCELADA) {
            return;
        }
        status = StatusFase.CANCELADA;
        cronograma.atualizarStatus();
    }

    /**
     * Método para alterar a descrição da fase
     * @param descricao
     */
    public void atualizarDescricao(String descricao) {
        if (descricao != null && descricao.length() > TAMANHO_MAXIMO_DESCRICAO) {
            throw new RegraNegocioException("A descrição da fase deve ter no máximo " + TAMANHO_MAXIMO_DESCRICAO + " caracteres");
        }
        this.descricao = descricao == null || descricao.isBlank() ? null : descricao.trim();
    }

    
    // ===== GETTERS AND SETTERS =====
    
    public Long getId() {
        return id;
    }

    public Cronograma getCronograma() {
        return cronograma;
    }

    public String getNome() {
        return nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public int getOrdem() {
        return ordem;
    }

    public LocalDate getInicioPrevisto() {
        return inicioPrevisto;
    }

    public LocalDate getFimPrevisto() {
        return fimPrevisto;
    }

    public LocalDate getInicioReal() {
        return inicioReal;
    }

    public LocalDate getFimReal() {
        return fimReal;
    }

    public StatusFase getStatus() {
        return status;
    }

    public List<AvancoFisico> getAvancos() {
        return Collections.unmodifiableList(avancos);
    }    
}
