package br.com.obras.cronograma.domain;

import java.time.LocalDate;

public class AvancoFisico {
    // ATTRIBUTES
    private Long id;
    private Long idFase;
    private LocalDate dataRegistro;
    private double percentualAcumulado;
    private String observacao;

    // CONSTRUCTORS
    public AvancoFisico(LocalDate dataRegistro, double percentualAcumulado, String observacao) {
        this.dataRegistro = dataRegistro;
        this.percentualAcumulado = percentualAcumulado;
        this.observacao = observacao;
    }
}
