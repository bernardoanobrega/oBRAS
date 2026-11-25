package br.com.obras.compras.controller;

import br.com.obras.compras.dto.FornecedorDto;
import br.com.obras.compras.dto.FornecedorRequest;
import br.com.obras.compras.dto.PedidoResumoDto;
import br.com.obras.compras.service.FornecedorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Cadastro central: a permissão vale se o usuário a tem individualmente OU via cargo em alguma obra. */
@RestController
@RequestMapping("/api/fornecedores")
public class FornecedorController {
    private final FornecedorService service;

    public FornecedorController(FornecedorService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.podeEmAlgumaObra('COMPRAS_FORNECEDORES', 'CRIAR')")
    public FornecedorDto cadastrar(@Valid @RequestBody FornecedorRequest dto) { return service.cadastrar(dto); }

    @GetMapping
    @PreAuthorize("@perm.podeEmAlgumaObra('COMPRAS_FORNECEDORES', 'VISUALIZAR')")
    public List<FornecedorDto> listar(@RequestParam(defaultValue = "false") boolean apenasAtivos) {
        return service.listar(apenasAtivos);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.podeEmAlgumaObra('COMPRAS_FORNECEDORES', 'VISUALIZAR')")
    public FornecedorDto buscar(@PathVariable Long id) { return service.buscar(id); }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.podeEmAlgumaObra('COMPRAS_FORNECEDORES', 'EDITAR')")
    public FornecedorDto atualizar(@PathVariable Long id, @Valid @RequestBody FornecedorRequest dto) {
        return service.atualizar(id, dto);
    }

    /** Sem DELETE: fornecedor só é desativado. */
    @PatchMapping("/{id}/desativar")
    @PreAuthorize("@perm.podeEmAlgumaObra('COMPRAS_FORNECEDORES', 'EXCLUIR')")
    public FornecedorDto desativar(@PathVariable Long id) { return service.desativar(id); }

    @GetMapping("/{id}/historico-compras")
    @PreAuthorize("@perm.podeEmAlgumaObra('COMPRAS_FORNECEDORES', 'VISUALIZAR')")
    public List<PedidoResumoDto> historicoCompras(@PathVariable Long id) {
        return service.listarHistoricoCompras(id);
    }
}
