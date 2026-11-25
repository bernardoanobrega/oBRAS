package br.com.obras.orcamento.controller;

import br.com.obras.orcamento.dto.OrcamentoResumoDto;
import br.com.obras.orcamento.service.OrcamentoService;
import br.com.obras.shared.security.CurrentUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Lista de orçamentos do menu lateral. A permissão é por obra, então o filtro
 * (ORCAMENTO:VISUALIZAR em cada obra) é aplicado no service, linha a linha.
 */
@RestController
public class OrcamentoListagemController {
    private final OrcamentoService service;

    public OrcamentoListagemController(OrcamentoService service) {
        this.service = service;
    }

    @GetMapping("/api/orcamentos")
    @PreAuthorize("isAuthenticated()")
    public List<OrcamentoResumoDto> listar() {
        return service.listarVisiveis(CurrentUser.id());
    }
}
