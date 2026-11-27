package br.com.obras.obra.controller;

import br.com.obras.obra.dto.AtualizarObraDto;
import br.com.obras.obra.dto.EquipeMembroDto;
import br.com.obras.obra.dto.NovaObraDto;
import br.com.obras.obra.dto.ObraDto;
import br.com.obras.obra.service.ObraService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.parameters.P;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/obras")
public class ObraController {
    public record ResponsavelRequest(@NotNull Long usuarioId) {}

    private final ObraService service;
    
    public ObraController(ObraService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.podeGlobal('DETALHES_OBRA', 'CRIAR')")
    public ObraDto criar(@Valid @RequestBody NovaObraDto dto) {
        return service.criar(dto);
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

    @PutMapping("/{obraId}")
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'EDITAR')")
    public ObraDto atualizar(@PathVariable @P("obraId") Long obraId,
                             @Valid @RequestBody AtualizarObraDto dto) {
        return service.atualizar(obraId, dto);
    }

    @PutMapping("/{obraId}/responsavel")
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'EDITAR')")
    public ObraDto definirResponsavel(@PathVariable @P("obraId") Long obraId,
                                      @Valid @RequestBody ResponsavelRequest request) {
        return service.definirResponsavel(obraId, request.usuarioId());
    }

    @PostMapping("/{obraId}/iniciar")
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'EDITAR')")
    public ObraDto iniciar(@PathVariable @P("obraId") Long obraId) {
        return service.iniciar(obraId);
    }

    @PostMapping("/{obraId}/paralisar")
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'EDITAR')")
    public ObraDto paralisar(@PathVariable @P("obraId") Long obraId) {
        return service.paralisar(obraId);
    }

    @PostMapping("/{obraId}/retomar")
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'EDITAR')")
    public ObraDto retomar(@PathVariable @P("obraId") Long obraId) {
        return service.retomar(obraId);
    }

    @PostMapping("/{obraId}/concluir")
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'EDITAR')")
    public ObraDto concluir(@PathVariable @P("obraId") Long obraId) {
        return service.concluir(obraId);
    }

    @PostMapping("/{obraId}/cancelar")
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'EDITAR')")
    public ObraDto cancelar(@PathVariable @P("obraId") Long obraId) {
        return service.cancelar(obraId);
    }

    @GetMapping("/{obraId}/equipe")
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'VISUALIZAR')")
    public List<EquipeMembroDto> equipe(@PathVariable @P("obraId") Long obraId) {
        return service.equipe(obraId);
    }
}