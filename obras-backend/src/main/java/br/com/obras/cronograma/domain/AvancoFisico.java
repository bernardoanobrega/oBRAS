package br.com.obras.cronograma.domain;

import java.time.LocalDate;
import br.com.obras.usuario.domain.Usuario;
import jakarta.persistence.*;

@Entity 
public class AvancoFisico {
    // ===== ATTRIBUTES =====

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) 
    private Long id;
    
    @ManyToOne(optional = false)
    private Fase fase;

    @ManyToOne(optional = false)
    private Usuario autor;

    private LocalDate dataRegistro;
    private double percentualAcumulado;
    private String observacao;

    // ===== CONSTRUCTORS =====

    public AvancoFisico(Fase fase, double percentualAcumulado, String observacao, Usuario autor) {
        this.fase = fase;
        this.dataRegistro = LocalDate.now();
        this.percentualAcumulado = percentualAcumulado;
        this.observacao = observacao;
        this.autor = autor;
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

    public Usuario getAutor() {
        return autor;
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
