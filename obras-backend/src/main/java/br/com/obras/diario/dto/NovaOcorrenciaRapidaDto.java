package br.com.obras.diario.dto;

import br.com.obras.diario.domain.CategoriaOcorrencia;
import br.com.obras.diario.domain.Clima;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Ocorrência rápida (L33). Sem legenda (vai no upload da foto, L35) e sem
 * paralisação (formulário completo, L38).
 *
 * @param clima    obrigatório só quando ainda não existe diário no dia; senão é ignorado
 * @param categoria opcional; ausente vira {@code OUTRO} (ADR-14)
 * @param dataHora opcional; ausente vira "agora" e precisa cair no dia {@code data} (ADR-18)
 */
public record NovaOcorrenciaRapidaDto(
    @NotNull LocalDate data,
    Clima clima,
    CategoriaOcorrencia categoria,
    @NotBlank @Size(max = 2000) String descricao,
    LocalDateTime dataHora
) {}
