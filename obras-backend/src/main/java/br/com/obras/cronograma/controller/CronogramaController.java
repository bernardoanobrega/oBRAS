package br.com.obras.cronograma.controller;

import br.com.obras.cronograma.dto.AvancoFisicoDto;
import br.com.obras.cronograma.dto.ConcluirFaseDto;
import br.com.obras.cronograma.dto.CronogramaDto;
import br.com.obras.cronograma.dto.FaseDto;
import br.com.obras.cronograma.dto.NovaFaseDto;
import br.com.obras.cronograma.dto.NovoAvancoFisicoDto;
import br.com.obras.cronograma.service.CronogramaService;
import br.com.obras.shared.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/obras/{obraId}/cronograma")
public class CronogramaController {
    private final CronogramaService service;

    public CronogramaController(CronogramaService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.pode(#obraId, 'CRONOGRAMA', 'CRIAR')")
    public CronogramaDto criar(@PathVariable Long obraId) {
        return service.criar(obraId);
    }

    @GetMapping
    @PreAuthorize("@perm.pode(#obraId, 'CRONOGRAMA', 'VISUALIZAR')")
    public CronogramaDto buscar(@PathVariable Long obraId) {
        return service.buscarPorObra(obraId);
    }

    @PatchMapping("/cancelar")
    @PreAuthorize("@perm.pode(#obraId, 'CRONOGRAMA', 'EXCLUIR')")
    public CronogramaDto cancelar(@PathVariable Long obraId) {
        return service.cancelar(obraId);
    }

    @PostMapping("/fases")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.pode(#obraId, 'CRONOGRAMA', 'CRIAR')")
    public FaseDto adicionarFase(@PathVariable Long obraId, @Valid @RequestBody NovaFaseDto dto) {
        return service.adicionarFase(obraId, dto);
    }

    @GetMapping("/fases/atrasadas")
    @PreAuthorize("@perm.pode(#obraId, 'CRONOGRAMA', 'VISUALIZAR')")
    public List<FaseDto> listarAtrasadas(@PathVariable Long obraId,
                                         @RequestParam(required = false)
                                         @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataReferencia) {
        return service.listarFasesAtrasadas(obraId, dataReferencia);
    }

    @DeleteMapping("/fases/{faseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@perm.pode(#obraId, 'CRONOGRAMA', 'EXCLUIR')")
    public void removerFase(@PathVariable Long obraId, @PathVariable Long faseId) {
        service.removerFase(obraId, faseId);
    }

    @PatchMapping("/fases/{faseId}/cancelar")
    @PreAuthorize("@perm.pode(#obraId, 'CRONOGRAMA', 'EXCLUIR')")
    public FaseDto cancelarFase(@PathVariable Long obraId, @PathVariable Long faseId) {
        return service.cancelarFase(obraId, faseId);
    }

    @PatchMapping("/fases/{faseId}/concluir")
    @PreAuthorize("@perm.pode(#obraId, 'CRONOGRAMA', 'EDITAR')")
    public FaseDto concluirFase(@PathVariable Long obraId, @PathVariable Long faseId,
                                @Valid @RequestBody ConcluirFaseDto dto) {
        return service.concluirFase(obraId, faseId, dto);
    }

    @GetMapping("/fases/{faseId}/avancos")
    @PreAuthorize("@perm.pode(#obraId, 'CRONOGRAMA', 'VISUALIZAR')")
    public List<AvancoFisicoDto> listarAvancos(@PathVariable Long obraId, @PathVariable Long faseId) {
        return service.listarAvancos(obraId, faseId);
    }

    @PostMapping("/fases/{faseId}/avancos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.pode(#obraId, 'CRONOGRAMA', 'EDITAR')")
    public AvancoFisicoDto registrarAvanco(@PathVariable Long obraId, @PathVariable Long faseId,
                                           @Valid @RequestBody NovoAvancoFisicoDto dto) {
        return service.registrarAvanco(obraId, faseId, dto, CurrentUser.id());
    }
}
