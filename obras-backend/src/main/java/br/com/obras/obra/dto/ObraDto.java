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
    String responsavelNome,
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
            status, art, responsavelId, null, null);
    }

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
        Long responsavelId,
        Long auditavelId
    ) {
        this(id, nome, descricao, tipologia, endereco, inicioPrevisto, fimPrevisto,
            status, art, responsavelId, null, auditavelId);
    }

    public static ObraDto de(Obra obra) {
        return de(obra, null);
    }

    public static ObraDto de(Obra obra, String responsavelNome) {
        return new ObraDto(
            obra.getId(),
            obra.getNome(),
            obra.getDescricao(),
            obra.getTipologia().name(),
            obra.getEndereco(),
            obra.getInicioPrevisto(),
            obra.getFimPrevisto(),
            obra.getStatus().name(),
            obra.getArt(),
            obra.getResponsavelId(),
            responsavelNome,
            obra.getAuditavelId()
        );
    }
}
