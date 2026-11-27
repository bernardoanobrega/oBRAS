package br.com.obras.obra.dto;

import br.com.obras.obra.domain.Obra;

import java.time.LocalDate;

public record ObraDto(
    Long id,
    String nome,
    String descricao,
    String tipologia,
    String endereco,
    LocalDate inicioPrevisto,
    LocalDate fimPrevisto,
    String status,
    String art,
    Long responsavelId,
    Long auditavelId
) {
    public ObraDto(
        Long id,
        String nome,
        String descricao,
        String tipologia,
        String endereco,
        LocalDate inicioPrevisto,
        LocalDate fimPrevisto,
        String status,
        String art,
        Long responsavelId
    ) {
        this(id, nome, descricao, tipologia, endereco, inicioPrevisto, fimPrevisto,
            status, art, responsavelId, null);
    }

    public static ObraDto de(Obra o) {
        return new ObraDto(
            o.getId(),
            o.getNome(),
            o.getDescricao(),
            o.getTipologia().name(),
            o.getEndereco(),
            o.getInicioPrevisto(),
            o.getFimPrevisto(),
            o.getStatus().name(),
            o.getArt(),
            o.getResponsavelId(),
            o.getAuditavelId()
        );
    }
}
