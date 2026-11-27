package br.com.obras.cronograma.controller;

import br.com.obras.cronograma.dto.CronogramaResumoDto;
import br.com.obras.cronograma.service.CronogramaService;
import br.com.obras.shared.security.CurrentUser;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CronogramaListagemController {
    private final CronogramaService service;

    public CronogramaListagemController(CronogramaService service) {
        this.service = service;
    }

    @GetMapping("/api/cronogramas")
    @PreAuthorize("isAuthenticated()")
    public List<CronogramaResumoDto> listar() {
        return service.listarVisiveis(CurrentUser.id());
    }
}
