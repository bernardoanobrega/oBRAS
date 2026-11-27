package br.com.obras.obra.controller;

import br.com.obras.obra.dto.PainelObraDto;
import br.com.obras.obra.service.PainelObraService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/obras/{obraId}/painel")
@Tag(name = "Painel da obra", description = "Dados agregados da obra para o painel (somente leitura)")
public class PainelObraController {
    private final PainelObraService service;

    public PainelObraController(PainelObraService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'VISUALIZAR')")
    @Operation(summary = "Painel agregado da obra",
        description = "Obra, clima do dia, KPIs (simulados), equipe, mão de obra, ocorrências e fotos do diário de hoje")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Painel da obra",
            content = @Content(schema = @Schema(implementation = PainelObraDto.class))),
        @ApiResponse(responseCode = "401", description = "Não autenticado", content = @Content),
        @ApiResponse(responseCode = "403", description = "Sem permissão DETALHES_OBRA/VISUALIZAR na obra", content = @Content),
        @ApiResponse(responseCode = "404", description = "Obra não encontrada", content = @Content)
    })
    public PainelObraDto painel(@PathVariable @P("obraId") Long obraId) {
        return service.painel(obraId);
    }
}