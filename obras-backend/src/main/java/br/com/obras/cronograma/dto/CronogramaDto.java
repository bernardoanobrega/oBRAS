package br.com.obras.cronograma.dto;

import br.com.obras.cronograma.domain.Cronograma;
import br.com.obras.cronograma.domain.enums.StatusCronograma;

import java.time.LocalDate;
import java.util.List;

public record CronogramaDto(Long id, Long obraId, StatusCronograma status, LocalDate dataUltimaAtualizacao,
                            IndicadoresCronogramaDto indicadores, List<FaseDto> fases) {
    public static CronogramaDto de(Cronograma cronograma, LocalDate dataReferencia) {
        return new CronogramaDto(cronograma.getId(), cronograma.getObraId(), cronograma.getStatus(),
                cronograma.getDataUltimaAtualizacao(), IndicadoresCronogramaDto.de(cronograma, dataReferencia),
                cronograma.getFases().stream().map(fase -> FaseDto.de(fase, dataReferencia)).toList());
    }
}
