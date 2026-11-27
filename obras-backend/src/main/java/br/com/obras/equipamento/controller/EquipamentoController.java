package br.com.obras.equipamento.controller;

import static br.com.obras.equipamento.controller.PermissoesEquipamento.CRIAR_GLOBAL;
import static br.com.obras.equipamento.controller.PermissoesEquipamento.EDITAR_GLOBAL;
import static br.com.obras.equipamento.controller.PermissoesEquipamento.EXCLUIR_GLOBAL;
import static br.com.obras.equipamento.controller.PermissoesEquipamento.VISUALIZAR_GLOBAL;

import br.com.obras.equipamento.domain.StatusEquipamento;
import br.com.obras.equipamento.domain.TipoEquipamento;
import br.com.obras.equipamento.dto.EquipamentoCriacaoDto;
import br.com.obras.equipamento.dto.EquipamentoDto;
import br.com.obras.equipamento.dto.EquipamentoEdicaoDto;
import br.com.obras.equipamento.service.EquipamentoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

/** Cadastro global de equipamentos. Equipamento nunca é excluído: usa-se desativar. */
@RestController
@RequestMapping("/api/equipamentos")
@RequiredArgsConstructor
@Tag(name = "Equipamentos")
public class EquipamentoController {

    private final EquipamentoService equipamentoService;

    @GetMapping
    @PreAuthorize(VISUALIZAR_GLOBAL)
    public Page<EquipamentoDto> listar(
            @RequestParam(required = false) StatusEquipamento status,
            @RequestParam(required = false) TipoEquipamento tipo,
            @RequestParam(required = false) String busca,
            @PageableDefault(size = 20, sort = "codigoPatrimonio") Pageable pageable) {
        return equipamentoService.listar(status, tipo, busca, pageable);
    }

    @GetMapping("/{equipamentoId}")
    @PreAuthorize(VISUALIZAR_GLOBAL)
    public EquipamentoDto buscar(@PathVariable Long equipamentoId) {
        return equipamentoService.buscar(equipamentoId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(CRIAR_GLOBAL)
    public EquipamentoDto cadastrar(@Valid @RequestBody EquipamentoCriacaoDto dto) {
        return equipamentoService.cadastrar(dto);
    }

    @PutMapping("/{equipamentoId}")
    @PreAuthorize(EDITAR_GLOBAL)
    public EquipamentoDto editar(@PathVariable Long equipamentoId, @Valid @RequestBody EquipamentoEdicaoDto dto) {
        return equipamentoService.editar(equipamentoId, dto);
    }

    @PatchMapping("/{equipamentoId}/desativar")
    @PreAuthorize(EXCLUIR_GLOBAL)
    public EquipamentoDto desativar(@PathVariable Long equipamentoId) {
        return equipamentoService.desativar(equipamentoId);
    }

    @PatchMapping("/{equipamentoId}/reativar")
    @PreAuthorize(EDITAR_GLOBAL)
    public EquipamentoDto reativar(@PathVariable Long equipamentoId) {
        return equipamentoService.reativar(equipamentoId);
    }
}