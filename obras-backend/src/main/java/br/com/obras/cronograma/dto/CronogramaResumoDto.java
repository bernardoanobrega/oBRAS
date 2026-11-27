package br.com.obras.cronograma.dto;

import br.com.obras.cronograma.domain.Cronograma;
import br.com.obras.cronograma.domain.enums.StatusCronograma;

import java.time.LocalDate;

public record CronogramaResumoDto(Long id, Long obraId, StatusCronograma status, LocalDate dataUltimaAtualizacao) {
    public static CronogramaResumoDto de(Cronograma cronograma) {
        return new CronogramaResumoDto(cronograma.getId(), cronograma.getObraId(), cronograma.getStatus(), cronograma.getDataUltimaAtualizacao());
    }
}
