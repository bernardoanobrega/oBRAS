package br.com.obras.cronograma.domain;

import br.com.obras.cronograma.domain.enums.StatusCronograma;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Cronograma {
    // ATTRIBUTES
    private Long id;
    private LocalDate dataUltimaAtualizacao;
    private StatusCronograma status;
    private final List<Fase> fases = new ArrayList<>();

    // CONSTRUCTORS
    public Cronograma(LocalDate dataCriacao) {
        this.dataUltimaAtualizacao = dataCriacao;
        this.status = StatusCronograma.VAZIO;
    }
}
