package br.com.obras.cronograma.domain;

import br.com.obras.cronograma.domain.enums.StatusFase;

import java.time.LocalDate;

public class Fase {
    // ATTRIBUTES
    private Long id;
    private Long idCronograma;
    private String nome;
    private String descricao;
    private int ordem;
    private LocalDate inicioPrevisto;
    private LocalDate fimPrevisto;
    private LocalDate inicioReal;
    private LocalDate fimReal;
    private StatusFase status;

    // CONSTRUCTORS
    public Fase(String nome, String descricao, int ordem, LocalDate inicioPrevisto, LocalDate fimPrevisto) {
        this.nome = nome;
        this.descricao = descricao;
        this.ordem = ordem;
        this.inicioPrevisto = inicioPrevisto;
        this.fimPrevisto = fimPrevisto;
        this.status = StatusFase.NAO_INICIADA;
    }
}
