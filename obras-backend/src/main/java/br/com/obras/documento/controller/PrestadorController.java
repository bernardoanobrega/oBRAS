package br.com.obras.documento.controller;

import br.com.obras.documento.dto.DocumentosDto.*;
import br.com.obras.documento.service.DocumentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/prestadores")
public class PrestadorController {
    private final DocumentoService service;
    public PrestadorController(DocumentoService service) { this.service=service; }
    @GetMapping
    @PreAuthorize("@perm.podeGlobal('CONTRATOS_DOCUMENTOS', 'VISUALIZAR') or " +
                  "@perm.podeEmAlgumaObra('CONTRATOS_DOCUMENTOS', 'VISUALIZAR')")
    public List<Prestador> listar() { return service.prestadores(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.podeGlobal('CONTRATOS_DOCUMENTOS', 'CRIAR') or " +
                  "@perm.podeEmAlgumaObra('CONTRATOS_DOCUMENTOS', 'CRIAR')")
    public Prestador criar(@Valid @RequestBody DadosPrestador dados) { return service.salvarPrestador(null,dados); }
    @PutMapping("/{id}")
    @PreAuthorize("@perm.podeGlobal('CONTRATOS_DOCUMENTOS', 'EDITAR') or " +
                  "@perm.podeEmAlgumaObra('CONTRATOS_DOCUMENTOS', 'EDITAR')")
    public Prestador editar(@PathVariable Long id,@Valid @RequestBody DadosPrestador dados) { return service.salvarPrestador(id,dados); }
    @PatchMapping("/{id}/desativar")
    @PreAuthorize("@perm.podeGlobal('CONTRATOS_DOCUMENTOS', 'EXCLUIR') or " +
                  "@perm.podeEmAlgumaObra('CONTRATOS_DOCUMENTOS', 'EXCLUIR')")
    public Prestador desativar(@PathVariable Long id) { return service.desativar(id); }
    @GetMapping("/{id}/historico")
    @PreAuthorize("@perm.podeGlobal('CONTRATOS_DOCUMENTOS', 'VISUALIZAR') or " +
                  "@perm.podeEmAlgumaObra('CONTRATOS_DOCUMENTOS', 'VISUALIZAR')")
    public Historico historico(@PathVariable Long id) { return service.historico(id); }
}
