package br.com.obras.obra.dto;

import br.com.obras.obra.domain.Tipologia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record AtualizarObraDto(
    @NotBlank(message = "O nome da obra é obrigatório")
    @Size(max = 200, message = "O nome deve ter no máximo 200 caracteres")
    String nome,

    @Size(max = 1000, message = "A descrição deve ter no máximo 1000 caracteres")
    String descricao,

    @NotNull(message = "A tipologia da obra é obrigatória")
    Tipologia tipologia,

    @NotBlank(message = "O endereço da obra é obrigatório")
    @Size(max = 300, message = "O endereço deve ter no máximo 300 caracteres")
    String endereco,

    @NotNull(message = "O início previsto é obrigatório")
    LocalDate inicioPrevisto,

    @NotNull(message = "O fim previsto é obrigatório")
    LocalDate fimPrevisto,

    @Size(max = 50, message = "A ART deve ter no máximo 50 caracteres")
    String art,

    Long responsavelId,

    @Null(message = "status só muda por transição")
    String status
) {}
