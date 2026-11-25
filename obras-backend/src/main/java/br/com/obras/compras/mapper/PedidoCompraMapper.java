package br.com.obras.compras.mapper;

import br.com.obras.compras.domain.Cotacao;
import br.com.obras.compras.domain.ItemPedido;
import br.com.obras.compras.domain.PedidoCompra;
import br.com.obras.compras.dto.CotacaoDto;
import br.com.obras.compras.dto.HistoricoStatusDto;
import br.com.obras.compras.dto.ItemPedidoDto;
import br.com.obras.compras.dto.PedidoCompraDto;
import br.com.obras.compras.dto.PedidoResumoDto;
import br.com.obras.shared.port.MaterialPort;
import br.com.obras.shared.port.MaterialPort.MaterialResumo;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/** Usar dentro de transação: acessa coleções lazy. */
@Component
public class PedidoCompraMapper {
    private final MaterialPort materiais;

    public PedidoCompraMapper(MaterialPort materiais) { this.materiais = materiais; }

    public PedidoCompraDto paraDto(PedidoCompra p) {
        Set<Long> materialIds = p.getItens().stream().map(ItemPedido::getMaterialId).collect(Collectors.toSet());
        Map<Long, MaterialResumo> catalogo = materiais.buscarVarios(materialIds);

        var itens = p.getItens().stream().map(i -> {
            MaterialResumo m = catalogo.get(i.getMaterialId());
            return new ItemPedidoDto(i.getId(), i.getMaterialId(), m == null ? null : m.nome(),
                    m == null ? null : m.unidadeMedida(), i.getQuantidade(), i.getValorUnitario(),
                    i.calcularValorSubTotal(), i.getItemOrcadoId());
        }).toList();

        var cotacoes = p.getCotacoes().stream().map(this::paraDto).toList();
        var historico = p.getHistorico().stream()
                .map(h -> new HistoricoStatusDto(h.getStatus(), h.getDataHora(), h.getAutorId())).toList();

        return new PedidoCompraDto(p.getId(), p.getObraId(), p.getSolicitanteId(),
                p.getFornecedor() == null ? null : p.getFornecedor().getId(), nomeFornecedor(p),
                p.getDataSolicitacao(), p.getDataPrevista(), p.getValorTotal(), p.getStatus(),
                p.getPrioridade(), p.estaAtrasado(LocalDate.now()), itens, cotacoes, historico);
    }

    public PedidoResumoDto paraResumo(PedidoCompra p) {
        return new PedidoResumoDto(p.getId(), p.getDataSolicitacao(), p.getDataPrevista(), p.getValorTotal(),
                p.getStatus(), p.getPrioridade(), nomeFornecedor(p), p.estaAtrasado(LocalDate.now()));
    }

    private CotacaoDto paraDto(Cotacao c) {
        return new CotacaoDto(c.getId(), c.getFornecedor().getId(), c.getFornecedor().getNomeRazaoSocial(),
                c.getDataCotacao(), c.getValorTotalProposto(), c.getPrazoEntregaDias(), c.isSelecionada());
    }

    private String nomeFornecedor(PedidoCompra p) {
        return p.getFornecedor() == null ? null : p.getFornecedor().getNomeRazaoSocial();
    }
}
