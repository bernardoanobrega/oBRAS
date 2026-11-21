package br.com.obras.documento.controller;

import br.com.obras.documento.dto.DocumentosDto.*;
import br.com.obras.documento.service.DocumentoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/prestadores")
public class PrestadorController {
    private final DocumentoService service;
    public PrestadorController(DocumentoService service) { this.service=service; }
    @GetMapping public List<Prestador> listar() { return service.prestadores(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED)
    public Prestador criar(@Valid @RequestBody DadosPrestador dados) { return service.salvarPrestador(null,dados); }
    @PutMapping("/{id}")
    public Prestador editar(@PathVariable Long id,@Valid @RequestBody DadosPrestador dados) { return service.salvarPrestador(id,dados); }
    @PatchMapping("/{id}/desativar")
    public Prestador desativar(@PathVariable Long id) { return service.desativar(id); }
    @GetMapping("/{id}/historico")
    public Historico historico(@PathVariable Long id) { return service.historico(id); }
}
