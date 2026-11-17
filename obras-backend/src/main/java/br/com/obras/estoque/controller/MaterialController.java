package br.com.obras.estoque.controller;

import br.com.obras.estoque.dto.MaterialDto;
import br.com.obras.estoque.dto.MaterialRequest;
import br.com.obras.estoque.service.MaterialService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Catálogo global: vale a permissão individual OU a de cargo em alguma obra. Sem DELETE. */
@RestController
@RequestMapping("/api/materiais")
public class MaterialController {
    private final MaterialService service;

    public MaterialController(MaterialService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.podeEmAlgumaObra('ESTOQUE', 'CRIAR')")
    public MaterialDto criar(@Valid @RequestBody MaterialRequest dto) { return service.criar(dto); }

    @GetMapping
    @PreAuthorize("@perm.podeEmAlgumaObra('ESTOQUE', 'VISUALIZAR')")
    public List<MaterialDto> listar(@RequestParam(required = false) String busca) { return service.listar(busca); }

    @GetMapping("/{id}")
    @PreAuthorize("@perm.podeEmAlgumaObra('ESTOQUE', 'VISUALIZAR')")
    public MaterialDto buscar(@PathVariable Long id) { return service.buscarDto(id); }

    @PutMapping("/{id}")
    @PreAuthorize("@perm.podeEmAlgumaObra('ESTOQUE', 'EDITAR')")
    public MaterialDto atualizar(@PathVariable Long id, @Valid @RequestBody MaterialRequest dto) {
        return service.atualizar(id, dto);
    }
}
