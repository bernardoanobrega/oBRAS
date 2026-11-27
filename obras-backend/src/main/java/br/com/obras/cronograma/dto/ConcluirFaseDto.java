package br.com.obras.cronograma.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;

public record ConcluirFaseDto(
    @NotNull(message = "Informe a data de conclusão")
    LocalDate dataConclusao) {
}
