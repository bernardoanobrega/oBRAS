package br.com.obras.equipamento.controller;

import static br.com.obras.equipamento.controller.PermissoesEquipamento.CRIAR_NA_OBRA;
import static br.com.obras.equipamento.controller.PermissoesEquipamento.EDITAR_NA_OBRA;
import static br.com.obras.equipamento.controller.PermissoesEquipamento.VISUALIZAR_GLOBAL;
import static br.com.obras.equipamento.controller.PermissoesEquipamento.VISUALIZAR_NA_OBRA;

import br.com.obras.equipamento.dto.AlocacaoEquipamentoDto;
import br.com.obras.equipamento.dto.AlocarEquipamentoDto;
import br.com.obras.equipamento.dto.DevolverEquipamentoDto;
import br.com.obras.equipamento.dto.ProrrogarAlocacaoDto;
import br.com.obras.equipamento.dto.TransferirEquipamentoDto;
import br.com.obras.equipamento.service.AlocacaoEquipamentoService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Alocação de equipamentos nos canteiros de uma obra. A obra da rota define a autorização. */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Alocações de equipamentos")
public class AlocacaoEquipamentoController {

    private final AlocacaoEquipamentoService alocacaoService;

    @GetMapping("/obras/{obraId}/equipamentos")
    @PreAuthorize(VISUALIZAR_NA_OBRA)
    public List<AlocacaoEquipamentoDto> listarAtivasDaObra(@PathVariable Long obraId) {
        return alocacaoService.listarAtivasDaObra(obraId);
    }

    @PostMapping("/obras/{obraId}/equipamentos/{equipamentoId}/alocacoes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(CRIAR_NA_OBRA)
    public AlocacaoEquipamentoDto alocar(@PathVariable Long obraId, @PathVariable Long equipamentoId,
            @Valid @RequestBody AlocarEquipamentoDto dto) {
        return alocacaoService.alocar(obraId, equipamentoId, dto);
    }

    @PatchMapping("/obras/{obraId}/equipamentos/{equipamentoId}/devolver")
    @PreAuthorize(EDITAR_NA_OBRA)
    public AlocacaoEquipamentoDto devolver(@PathVariable Long obraId, @PathVariable Long equipamentoId,
            @Valid @RequestBody DevolverEquipamentoDto dto) {
        return alocacaoService.devolver(obraId, equipamentoId, dto);
    }

    @PatchMapping("/obras/{obraId}/equipamentos/{equipamentoId}/transferir")
    @PreAuthorize(EDITAR_NA_OBRA)
    public AlocacaoEquipamentoDto transferir(@PathVariable Long obraId, @PathVariable Long equipamentoId,
            @Valid @RequestBody TransferirEquipamentoDto dto) {
        return alocacaoService.transferir(obraId, equipamentoId, dto);
    }

    @PatchMapping("/obras/{obraId}/alocacoes/{alocacaoId}/prorrogar")
    @PreAuthorize(EDITAR_NA_OBRA)
    public AlocacaoEquipamentoDto prorrogar(@PathVariable Long obraId, @PathVariable Long alocacaoId,
            @Valid @RequestBody ProrrogarAlocacaoDto dto) {
        return alocacaoService.prorrogar(obraId, alocacaoId, dto);
    }

    @GetMapping("/equipamentos/{equipamentoId}/alocacoes")
    @PreAuthorize(VISUALIZAR_GLOBAL)
    public List<AlocacaoEquipamentoDto> listarHistorico(@PathVariable Long equipamentoId) {
        return alocacaoService.listarHistorico(equipamentoId);
    }
}