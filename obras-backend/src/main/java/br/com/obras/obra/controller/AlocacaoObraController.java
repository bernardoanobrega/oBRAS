package br.com.obras.obra.controller;

import br.com.obras.obra.dto.AlocacaoObraDto;
import br.com.obras.obra.dto.AtualizarCargosDto;
import br.com.obras.obra.dto.NovaAlocacaoDto;
import br.com.obras.obra.dto.UsuarioCandidatoDto;
import br.com.obras.obra.service.AlocacaoObraService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/obras/{obraId}/alocacoes")
public class AlocacaoObraController {
    private final AlocacaoObraService service;

    public AlocacaoObraController(AlocacaoObraService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'VISUALIZAR')")
    public List<AlocacaoObraDto> listar(@PathVariable Long obraId) {
        return service.listar(obraId);
    }

    @GetMapping("/candidatos")
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'CRIAR')")
    public List<UsuarioCandidatoDto> candidatos(@PathVariable Long obraId) {
        return service.candidatos(obraId);
    }

    @PostMapping
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'CRIAR')")
    public ResponseEntity<AlocacaoObraDto> alocar(@PathVariable Long obraId,
                                                  @Valid @RequestBody NovaAlocacaoDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.alocar(obraId, dto));
    }

    @PatchMapping("/{alocacaoId}/encerrar")
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'EDITAR')")
    public ResponseEntity<Void> encerrar(@PathVariable Long obraId, @PathVariable Long alocacaoId) {
        service.encerrar(obraId, alocacaoId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{alocacaoId}/cargos")
    @PreAuthorize("@perm.pode(#obraId, 'DETALHES_OBRA', 'EDITAR')")
    public AlocacaoObraDto atualizarCargos(@PathVariable Long obraId, @PathVariable Long alocacaoId,
                                           @Valid @RequestBody AtualizarCargosDto dto) {
        return service.atualizarCargos(obraId, alocacaoId, new java.util.HashSet<>(dto.cargoIds()));
    }
}
