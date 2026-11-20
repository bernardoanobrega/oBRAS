package br.com.obras.diario.dto;

import br.com.obras.diario.domain.Ocorrencia;
import br.com.obras.diario.domain.RegistroFotografico;

import java.time.LocalDateTime;
import java.util.List;

public record OcorrenciaDto(
    Long id,
    Long diarioId,
    String categoria,
    String descricao,
    LocalDateTime dataHora,
    Long autorUsuarioId,
    ParalisacaoDto paralisacao,
    List<FotoDto> fotos
) {
    public static OcorrenciaDto de(Ocorrencia o) {
        ParalisacaoDto paralisacao = o.getParalisacao() == null
            ? null
            : new ParalisacaoDto(o.getParalisacao().getInicio(), o.getParalisacao().getFim());

        List<FotoDto> fotos = o.getFotos().stream()
            .filter(f -> !f.getAuditavel().isExcluido())
            .map(FotoDto::de)
            .toList();

        return new OcorrenciaDto(
            o.getId(),
            o.getRegistroDiario().getId(),
            o.getCategoria().name(),
            o.getDescricao(),
            o.getDataHora(),
            o.getAlocacaoObra().getUsuario().getId(),
            paralisacao,
            fotos
        );
    }
}
