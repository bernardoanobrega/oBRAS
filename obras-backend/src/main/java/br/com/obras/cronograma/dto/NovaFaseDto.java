package br.com.obras.cronograma.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record NovaFaseDto(
        @NotBlank(message = "Informe o nome da fase")
        @Size(max = 150, message = "Nome com até 150 caracteres")
        String nome,
        @Size(max = 500, message = "Descrição com até 500 caracteres")
        String descricao,
        @NotNull(message = "Informe a ordem da fase")
        @Min(value = 1, message = "A ordem deve ser maior ou igual a 1")
        Integer ordem,
        @NotNull(message = "Informe o início previsto")
        LocalDate inicioPrevisto,
        @NotNull(message = "Informe o fim previsto")
        LocalDate fimPrevisto) {
}
