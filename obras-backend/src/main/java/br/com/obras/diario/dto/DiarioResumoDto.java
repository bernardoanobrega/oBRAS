package br.com.obras.diario.dto;

import br.com.obras.diario.domain.RegistroDiario;

import java.time.LocalDate;

public record DiarioResumoDto(
    Long id,
    LocalDate data,
    String clima,
    String descricao,
    long totalOcorrencias
) {
    public static DiarioResumoDto de(RegistroDiario d, long totalOcorrencias) {
        return new DiarioResumoDto(d.getId(), d.getData(), d.getClima().name(), d.getDescricao(), totalOcorrencias);
    }
}
