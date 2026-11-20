package br.com.obras.cronograma.domain;

import br.com.obras.cronograma.domain.enums.StatusFase;
import br.com.obras.usuario.domain.Usuario;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity 
public class Fase {
    // ===== ATTRIBUTES =====

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;

    @ManyToOne(optional = false) 
    @JoinColumn(name = "id_cronograma")
    private Cronograma cronograma;

    private String nome;
    private String descricao;
    private int ordem;
    private LocalDate inicioPrevisto;
    private LocalDate fimPrevisto;
    private LocalDate inicioReal;
    private LocalDate fimReal;
    
    @Enumerated(EnumType.STRING)
    private StatusFase status;

    @OneToMany(mappedBy = "fase", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("dataRegistro ASC, id ASC")
    private List<AvancoFisico> avancos = new ArrayList<>();

    // ===== CONSTRUCTORS =====

    public Fase(Cronograma cronograma, String nome, int ordem, LocalDate inicioPrevisto, LocalDate fimPrevisto) {
        this.cronograma = cronograma;
        this.nome = nome;
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
     * Método para adicionar um avanço físico a uma fase
     * @param percentualAcumulado
     * @param observacao
     * @param autor
     * @return Insere o novo avanço na lista de avanços da fase, e retorna o avanço adicionado
     */
    public AvancoFisico registrarAvanco(double percentualAcumulado, String observacao, Usuario autor) {
        if(status == StatusFase.CONCLUIDA || status == StatusFase.CANCELADA) {
            throw new IllegalStateException("Não é possível registrar avanço em uma fase " + status + ".");
        }
        if(autor == null) {
            throw new IllegalArgumentException("O autor do avanço físico é obrigatório.");
        }
        AvancoFisico avanco = new AvancoFisico(this, percentualAcumulado, observacao, autor);
        if(!avanco.validar(calcularPercentualConcluido())) {
            throw new IllegalArgumentException("Percentual inválido: deve estar entre 0 e 100 ou igual ao último avanço (" + calcularPercentualConcluido() + "%).");
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
            throw new IllegalStateException("Só é possível concluir uma fase que já está em andamento (status atual: " + status + ").");
        }
        if(calcularPercentualConcluido() < 100.0) {
            throw new IllegalStateException("A fase só pode ser concluída com 100% de avanço registrado.");
        }
        if(dataReal == null) {
            throw new IllegalArgumentException("A data de conclusão é obrigatória.");
        }
        if(inicioReal != null && dataReal.isBefore(inicioReal)) {
            throw new IllegalArgumentException("A data de conclusão não pode ser anterior ao início real.");
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
     * Método para cancelar uma fase
     */
    public void cancelarFase() {
        if (status == StatusFase.CONCLUIDA) {
            throw new IllegalStateException("Fase concluída não pode ser cancelada.");
        }
        if (status == StatusFase.CANCELADA) {
            return;
        }
        status = StatusFase.CANCELADA;
        cronograma.atualizarStatus();
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

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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
