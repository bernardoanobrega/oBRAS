package br.com.obras.compras.controller;

import br.com.obras.compras.domain.StatusPedido;
import br.com.obras.compras.dto.AjusteValorUnitarioDto;
import br.com.obras.compras.dto.AlterarStatusDto;
import br.com.obras.compras.dto.ItemAdicionadoDto;
import br.com.obras.compras.dto.NovaCotacaoDto;
import br.com.obras.compras.dto.NovoItemPedidoDto;
import br.com.obras.compras.dto.NovoPedidoDto;
import br.com.obras.compras.dto.PedidoCompraDto;
import br.com.obras.compras.dto.PedidoResumoDto;
import br.com.obras.compras.service.PedidoCompraService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/obras/{obraId}/pedidos-compra")
public class PedidoCompraController {
    private final PedidoCompraService service;

    public PedidoCompraController(PedidoCompraService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.pode(#obraId, 'COMPRAS_FORNECEDORES', 'CRIAR')")
    public PedidoCompraDto criar(@PathVariable Long obraId, @Valid @RequestBody NovoPedidoDto dto) {
        return service.criar(obraId, dto, CurrentUser.id());
    }

    @GetMapping
    @PreAuthorize("@perm.pode(#obraId, 'COMPRAS_FORNECEDORES', 'VISUALIZAR')")
    public List<PedidoResumoDto> listar(@PathVariable Long obraId,
                                        @RequestParam(required = false) StatusPedido status) {
        return service.listar(obraId, status);
    }

    @GetMapping("/{pedidoId}")
    @PreAuthorize("@perm.pode(#obraId, 'COMPRAS_FORNECEDORES', 'VISUALIZAR')")
    public PedidoCompraDto buscar(@PathVariable Long obraId, @PathVariable Long pedidoId) {
        return service.buscar(obraId, pedidoId);
    }

    @PostMapping("/{pedidoId}/itens")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.pode(#obraId, 'COMPRAS_FORNECEDORES', 'EDITAR')")
    public ItemAdicionadoDto adicionarItem(@PathVariable Long obraId, @PathVariable Long pedidoId,
                                           @Valid @RequestBody NovoItemPedidoDto dto) {
        return service.adicionarItem(obraId, pedidoId, dto);
    }

    @DeleteMapping("/{pedidoId}/itens/{itemId}")
    @PreAuthorize("@perm.pode(#obraId, 'COMPRAS_FORNECEDORES', 'EDITAR')")
    public PedidoCompraDto removerItem(@PathVariable Long obraId, @PathVariable Long pedidoId,
                                       @PathVariable Long itemId) {
        return service.removerItem(obraId, pedidoId, itemId);
    }

    @PutMapping("/{pedidoId}/itens/{itemId}/valor-unitario")
    @PreAuthorize("@perm.pode(#obraId, 'COMPRAS_FORNECEDORES', 'EDITAR')")
    public PedidoCompraDto ajustarValorUnitario(@PathVariable Long obraId, @PathVariable Long pedidoId,
                                                @PathVariable Long itemId,
                                                @Valid @RequestBody AjusteValorUnitarioDto dto) {
        return service.ajustarValorUnitario(obraId, pedidoId, itemId, dto);
    }

    @PostMapping("/{pedidoId}/cotacoes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("@perm.pode(#obraId, 'COMPRAS_FORNECEDORES', 'EDITAR')")
    public PedidoCompraDto registrarCotacao(@PathVariable Long obraId, @PathVariable Long pedidoId,
                                            @Valid @RequestBody NovaCotacaoDto dto) {
        return service.registrarCotacao(obraId, pedidoId, dto);
    }

    /** Aprovação do gestor: escolher a cotação define o fornecedor do pedido. */
    @PutMapping("/{pedidoId}/cotacoes/{cotacaoId}/selecionar")
    @PreAuthorize("@perm.pode(#obraId, 'COMPRAS_FORNECEDORES', 'APROVAR')")
    public PedidoCompraDto selecionarCotacao(@PathVariable Long obraId, @PathVariable Long pedidoId,
                                             @PathVariable Long cotacaoId) {
        return service.selecionarCotacao(obraId, pedidoId, cotacaoId);
    }

    @PatchMapping("/{pedidoId}/status")
    @PreAuthorize("@perm.pode(#obraId, 'COMPRAS_FORNECEDORES', 'EDITAR')")
    public PedidoCompraDto alterarStatus(@PathVariable Long obraId, @PathVariable Long pedidoId,
                                         @Valid @RequestBody AlterarStatusDto dto) {
        return service.alterarStatus(obraId, pedidoId, dto.status(), CurrentUser.id());
    }

    @PostMapping("/{pedidoId}/cancelar")
    @PreAuthorize("@perm.pode(#obraId, 'COMPRAS_FORNECEDORES', 'EDITAR')")
    public PedidoCompraDto cancelar(@PathVariable Long obraId, @PathVariable Long pedidoId) {
        return service.cancelar(obraId, pedidoId, CurrentUser.id());
    }
}
