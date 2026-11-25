package br.com.obras.orcamento.controller;

import br.com.obras.orcamento.dto.AtualizarCustoRealizadoDto;
import br.com.obras.orcamento.dto.AtualizarDecisaoFinanceiraDto;
import br.com.obras.orcamento.dto.AtualizarItemOrcadoDto;
import br.com.obras.orcamento.dto.CustoRealizadoDto;
import br.com.obras.orcamento.dto.DecisaoFinanceiraDto;
import br.com.obras.orcamento.dto.ItemOrcadoDto;
import br.com.obras.orcamento.dto.NovaDecisaoFinanceiraDto;
import br.com.obras.orcamento.dto.NovoCustoRealizadoDto;
import br.com.obras.orcamento.dto.NovoItemOrcadoDto;
import br.com.obras.orcamento.dto.NovoOrcamentoDto;
import br.com.obras.orcamento.dto.OrcamentoDto;
import br.com.obras.orcamento.service.OrcamentoService;
import br.com.obras.shared.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/** Orçamento da obra (UC10). Recurso único por obra: /api/obras/{obraId}/orcamento. */
@RestController
@RequestMapping("/api/obras/{obraId}/orcamento")
public class OrcamentoController {
    private final OrcamentoService service;

    public OrcamentoController(OrcamentoService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.pode(#obraId, 'ORCAMENTO', 'CRIAR')")
    public OrcamentoDto elaborar(@PathVariable Long obraId, @Valid @RequestBody NovoOrcamentoDto dto) {
        return service.elaborar(obraId, dto);
    }

    @GetMapping
    @PreAuthorize("@perm.pode(#obraId, 'ORCAMENTO', 'VISUALIZAR')")
    public OrcamentoDto buscar(@PathVariable Long obraId) {
        return service.buscarPorObra(obraId);
    }

    /** Orçamento não é excluído: só cancelado. */
    @PatchMapping("/cancelar")
    @PreAuthorize("@perm.pode(#obraId, 'ORCAMENTO', 'EXCLUIR')")
    public OrcamentoDto cancelar(@PathVariable Long obraId) {
        return service.cancelar(obraId);
    }

    @PostMapping("/itens")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.pode(#obraId, 'ORCAMENTO', 'CRIAR')")
    public ItemOrcadoDto adicionarItem(@PathVariable Long obraId, @Valid @RequestBody NovoItemOrcadoDto dto) {
        return service.adicionarItem(obraId, dto);
    }

    @PutMapping("/itens/{itemId}")
    @PreAuthorize("@perm.pode(#obraId, 'ORCAMENTO', 'EDITAR')")
    public ItemOrcadoDto atualizarItem(@PathVariable Long obraId, @PathVariable Long itemId,
                                       @Valid @RequestBody AtualizarItemOrcadoDto dto) {
        return service.atualizarItem(obraId, itemId, dto);
    }

    @DeleteMapping("/itens/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@perm.pode(#obraId, 'ORCAMENTO', 'EXCLUIR')")
    public void removerItem(@PathVariable Long obraId, @PathVariable Long itemId) {
        service.removerItem(obraId, itemId);
    }

    @GetMapping("/itens/desvios")
    @PreAuthorize("@perm.pode(#obraId, 'ORCAMENTO', 'VISUALIZAR')")
    public List<ItemOrcadoDto> listarItensEmDesvio(@PathVariable Long obraId) {
        return service.listarItensEmDesvio(obraId);
    }

    @GetMapping("/itens/{itemId}/custos")
    @PreAuthorize("@perm.pode(#obraId, 'ORCAMENTO', 'VISUALIZAR')")
    public List<CustoRealizadoDto> listarCustos(@PathVariable Long obraId, @PathVariable Long itemId) {
        return service.listarCustos(obraId, itemId);
    }

    @PostMapping("/itens/{itemId}/custos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.pode(#obraId, 'ORCAMENTO', 'CRIAR')")
    public CustoRealizadoDto registrarCusto(@PathVariable Long obraId, @PathVariable Long itemId,
                                            @Valid @RequestBody NovoCustoRealizadoDto dto) {
        return service.registrarCusto(obraId, itemId, dto, CurrentUser.id());
    }

    @PutMapping("/itens/{itemId}/custos/{custoId}")
    @PreAuthorize("@perm.pode(#obraId, 'ORCAMENTO', 'EDITAR')")
    public CustoRealizadoDto editarCusto(@PathVariable Long obraId, @PathVariable Long itemId,
                                         @PathVariable Long custoId,
                                         @Valid @RequestBody AtualizarCustoRealizadoDto dto) {
        return service.editarCusto(obraId, itemId, custoId, dto);
    }

    @DeleteMapping("/itens/{itemId}/custos/{custoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("@perm.pode(#obraId, 'ORCAMENTO', 'EXCLUIR')")
    public void removerCusto(@PathVariable Long obraId, @PathVariable Long itemId, @PathVariable Long custoId) {
        service.removerCusto(obraId, itemId, custoId);
    }

    /** Decisão altera o total orçado, por isso exige APROVAR. */
    @PostMapping("/decisoes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.pode(#obraId, 'ORCAMENTO', 'APROVAR')")
    public DecisaoFinanceiraDto registrarDecisao(@PathVariable Long obraId,
                                                 @Valid @RequestBody NovaDecisaoFinanceiraDto dto) {
        return service.registrarDecisao(obraId, dto, CurrentUser.id());
    }

    @PutMapping("/decisoes/{decisaoId}")
    @PreAuthorize("@perm.pode(#obraId, 'ORCAMENTO', 'APROVAR')")
    public DecisaoFinanceiraDto editarDecisao(@PathVariable Long obraId, @PathVariable Long decisaoId,
                                              @Valid @RequestBody AtualizarDecisaoFinanceiraDto dto) {
        return service.editarDecisao(obraId, decisaoId, dto);
    }
}
