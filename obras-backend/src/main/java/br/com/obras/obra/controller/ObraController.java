package br.com.obras.obra.controller;

import br.com.obras.obra.dto.EquipeMembroDto;
import br.com.obras.obra.dto.ObraDto;
import br.com.obras.obra.service.ObraService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/obras")
public class ObraController {
    private final ObraService service;
    
    public ObraController(ObraService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<ObraDto> listar() {
        return service.listar();
    }

    @GetMapping("/{obraId}")
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'VISUALIZAR')")
    public ObraDto buscar(@PathVariable @P("obraId") Long obraId) {
        return service.buscar(obraId);
    }

    @GetMapping("/{obraId}/equipe")
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'VISUALIZAR')")
    public List<EquipeMembroDto> equipe(@PathVariable @P("obraId") Long obraId) {
        return service.equipe(obraId);
    }
}