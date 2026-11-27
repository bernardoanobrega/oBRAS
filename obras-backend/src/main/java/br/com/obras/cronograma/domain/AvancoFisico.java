package br.com.obras.cronograma.domain;

import br.com.obras.shared.exception.RegraNegocioException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity 
@Table(name = "avanco_fisico")
@Getter 
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AvancoFisico {
    private static final int TAMANHO_MAXIMO_OBSERVACAO = 500;

    // ===== ATTRIBUTES =====

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fase_id", nullable = false, updatable = false)
    private Fase fase;

    @Column(name = "autor_id", nullable = false, updatable = false)
    private Long autorId;

    @Column(name = "data_registro", nullable = false, updatable = false)
    private LocalDate dataRegistro;

    @Column(name = "percentual_acumulado", nullable = false)
    private double percentualAcumulado;

    @Column(length = TAMANHO_MAXIMO_OBSERVACAO)
    private String observacao;

    // ===== CONSTRUCTORS =====

    public AvancoFisico(Fase fase, double percentualAcumulado, String observacao, Long autorId) {
        if (autorId == null) {
            throw new RegraNegocioException("O autor do avanço físico é obrigatório");
        }
        if (observacao != null && observacao.length() > TAMANHO_MAXIMO_OBSERVACAO) {
            throw new RegraNegocioException(
                    "A observação deve ter no máximo " + TAMANHO_MAXIMO_OBSERVACAO + " caracteres");
        }
        this.fase = fase;
        this.dataRegistro = LocalDate.now();
        this.percentualAcumulado = percentualAcumulado;
        this.observacao = observacao == null || observacao.isBlank() ? null : observacao.trim();
        this.autorId = autorId;
    }

    // ===== METHODS =====

    /**
     * Método para validar o percentual de avanço registrado
     * @param percentualAnterior
     * @return Retorna um valor booleano representando a corretude do percentual de avanço
     */
    public boolean validar(double percentualAnterior) {
        return percentualAcumulado >= 0 && percentualAcumulado <= 100 && percentualAcumulado >= percentualAnterior;
    }
    
    // ===== GETTERS =====
    
    public Long getId() {
        return id;
    }

    public Fase getFase() {
        return fase;
    }

    public Long getAutorId() {
        return autorId;
    }

    public LocalDate getDataRegistro() {
        return dataRegistro;
    }

    public double getPercentualAcumulado() {
        return percentualAcumulado;
    }

    public String getObservacao() {
        return observacao;
    }
}
