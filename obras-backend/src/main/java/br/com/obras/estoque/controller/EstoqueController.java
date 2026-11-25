package br.com.obras.estoque.controller;

import br.com.obras.compras.dto.PedidoCompraDto;
import br.com.obras.estoque.domain.TipoMovimentacao;
import br.com.obras.estoque.dto.ConfigurarSaldoDto;
import br.com.obras.estoque.dto.EntradaEstoqueDto;
import br.com.obras.estoque.dto.MovimentacaoDto;
import br.com.obras.estoque.dto.ReposicaoDto;
import br.com.obras.estoque.dto.SaidaEstoqueDto;
import br.com.obras.estoque.dto.SaidaResultadoDto;
import br.com.obras.estoque.dto.SaldoConsolidadoDto;
import br.com.obras.estoque.dto.SaldoDto;
import br.com.obras.estoque.dto.TransferenciaEstoqueDto;
import br.com.obras.estoque.dto.TransferenciaResultadoDto;
import br.com.obras.estoque.service.EstoqueService;
import br.com.obras.shared.security.CurrentUser;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
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

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/obras/{obraId}/estoque")
public class EstoqueController {
    private final EstoqueService service;

    public EstoqueController(EstoqueService service) { this.service = service; }

    @GetMapping("/saldos")
    @PreAuthorize("@perm.pode(#obraId, 'ESTOQUE', 'VISUALIZAR')")
    public List<SaldoDto> saldos(@PathVariable Long obraId, @RequestParam(required = false) Long canteiroId) {
        return service.consultarSaldos(obraId, canteiroId);
    }

    @GetMapping("/saldos/consolidado")
    @PreAuthorize("@perm.pode(#obraId, 'ESTOQUE', 'VISUALIZAR')")
    public List<SaldoConsolidadoDto> saldoConsolidado(@PathVariable Long obraId) {
        return service.consultarSaldoConsolidado(obraId);
    }

    @PutMapping("/saldos")
    @PreAuthorize("@perm.pode(#obraId, 'ESTOQUE', 'EDITAR')")
    public SaldoDto configurarSaldo(@PathVariable Long obraId, @Valid @RequestBody ConfigurarSaldoDto dto) {
        return service.configurarSaldo(obraId, dto);
    }

    @GetMapping("/movimentacoes")
    @PreAuthorize("@perm.pode(#obraId, 'ESTOQUE', 'VISUALIZAR')")
    public List<MovimentacaoDto> movimentacoes(
            @PathVariable Long obraId,
            @RequestParam(required = false) Long canteiroId,
            @RequestParam(required = false) TipoMovimentacao tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim) {
        return service.listarMovimentacoes(obraId, canteiroId, tipo, inicio, fim);
    }

    @PostMapping("/entradas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.pode(#obraId, 'ESTOQUE', 'CRIAR')")
    public List<MovimentacaoDto> entrada(@PathVariable Long obraId, @Valid @RequestBody EntradaEstoqueDto dto) {
        return service.registrarEntradaEstoque(obraId, dto.pedidoId(), dto.canteiroId(), CurrentUser.id());
    }

    @PostMapping("/saidas")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.pode(#obraId, 'ESTOQUE', 'CRIAR')")
    public SaidaResultadoDto saida(@PathVariable Long obraId, @Valid @RequestBody SaidaEstoqueDto dto) {
        return service.registrarSaidaEstoque(obraId, dto.materialId(), dto.canteiroId(), dto.quantidade(),
                CurrentUser.id());
    }

    @PostMapping("/transferencias")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.pode(#obraId, 'ESTOQUE', 'CRIAR')")
    public TransferenciaResultadoDto transferencia(@PathVariable Long obraId,
                                                   @Valid @RequestBody TransferenciaEstoqueDto dto) {
        return service.registrarTransferenciaEstoque(obraId, dto.materialId(), dto.canteiroOrigemId(),
                dto.canteiroDestinoId(), dto.quantidade(), CurrentUser.id());
    }

    /** Gera o PedidoCompra de reposição. Exige ESTOQUE/CRIAR (o fluxo é do Estoquista). */
    @PostMapping("/reposicoes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.pode(#obraId, 'ESTOQUE', 'CRIAR')")
    public PedidoCompraDto reposicao(@PathVariable Long obraId, @Valid @RequestBody ReposicaoDto dto) {
        return service.solicitarReposicao(obraId, dto.materialId(), dto.canteiroId(), dto.quantidade(),
                CurrentUser.id());
    }
}
