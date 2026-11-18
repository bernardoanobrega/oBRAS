package br.com.obras.diario.dto;

import br.com.obras.diario.domain.RegistroDiario;
import br.com.obras.obra.domain.AlocacaoObra;

import java.time.LocalDate;
import java.util.List;

public record DiarioDto(
    Long id,
    Long obraId,
    LocalDate data,
    String clima,
    String descricao,
    List<Long> equipePresenteIds,
    List<OcorrenciaDto> ocorrencias
) {
    public static DiarioDto de(RegistroDiario d, List<OcorrenciaDto> ocorrencias) {
        List<Long> equipe = d.getEquipePresente().stream()
            .map(AlocacaoObra::getId)
            .sorted()
            .toList();
        return new DiarioDto(
            d.getId(),
            d.getObra().getId(),
            d.getData(),
            d.getClima().name(),
            d.getDescricao(),
            equipe,
            ocorrencias
        );
    }
}
