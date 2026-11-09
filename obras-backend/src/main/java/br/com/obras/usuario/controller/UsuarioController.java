package br.com.obras.usuario.controller;

import br.com.obras.usuario.dto.*;
import br.com.obras.usuario.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {
    private final UsuarioService service;

    public UsuarioController(UsuarioService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.podeGlobal('USUARIOS', 'CRIAR')")
    public UsuarioDto criar(@Valid @RequestBody NovoUsuarioDto dto) { return service.criar(dto); }

    @GetMapping
    @PreAuthorize("@perm.podeGlobal('USUARIOS', 'VISUALIZAR')")
    public List<UsuarioDto> listar() { return service.listar(); }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.podeGlobal('USUARIOS', 'VISUALIZAR')")
    public UsuarioDto buscar(@PathVariable Long id) { return service.buscar(id); }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.podeGlobal('USUARIOS', 'EDITAR')")
    public UsuarioDto atualizar(@PathVariable Long id, @Valid @RequestBody AtualizarUsuarioDto dto) {
        return service.atualizar(id, dto);
    }

    /** Não existe DELETE: usuário é só desativado. */
    @PatchMapping("/{id}/desativar")
    @PreAuthorize("@perm.podeGlobal('USUARIOS', 'EXCLUIR')")
    public UsuarioDto desativar(@PathVariable Long id) { return service.desativar(id); }

    @PutMapping("/{id}/permissoes")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@perm.podeGlobal('USUARIOS', 'EDITAR')")
    public void definirPermissao(@PathVariable Long id, @Valid @RequestBody DefinirPermissaoDto dto) {
        service.definirPermissao(id, dto);
    }
}
