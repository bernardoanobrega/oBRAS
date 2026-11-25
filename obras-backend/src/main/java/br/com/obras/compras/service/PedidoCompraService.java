package br.com.obras.compras.service;

import br.com.obras.compras.domain.Fornecedor;
import br.com.obras.compras.domain.ItemPedido;
import br.com.obras.compras.domain.PedidoCompra;
import br.com.obras.compras.domain.PrioridadePedido;
import br.com.obras.compras.domain.StatusPedido;
import br.com.obras.compras.dto.AjusteValorUnitarioDto;
import br.com.obras.compras.dto.ItemAdicionadoDto;
import br.com.obras.compras.dto.NovaCotacaoDto;
import br.com.obras.compras.dto.NovoItemPedidoDto;
import br.com.obras.compras.dto.NovoPedidoDto;
import br.com.obras.compras.dto.PedidoCompraDto;
import br.com.obras.compras.dto.PedidoRecebimento;
import br.com.obras.compras.dto.PedidoResumoDto;
import br.com.obras.compras.mapper.PedidoCompraMapper;
import br.com.obras.compras.repository.FornecedorRepository;
import br.com.obras.compras.repository.PedidoCompraRepository;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.shared.port.MaterialPort;
import br.com.obras.shared.port.ObraConsultaPort;
import br.com.obras.shared.port.OrcamentoPort;
import br.com.obras.shared.port.OrcamentoPort.ItemOrcadoResumo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** UC11 | RF-15, RF-16, RF-31. Regras de estado ficam em PedidoCompra. */
@Service
public class PedidoCompraService {
    private final PedidoCompraRepository pedidos;
    private final FornecedorRepository fornecedores;
    private final MaterialPort materiais;
    private final ObraConsultaPort obras;
    private final OrcamentoPort orcamento;
    private final PedidoCompraMapper mapper;

    public PedidoCompraService(PedidoCompraRepository pedidos, FornecedorRepository fornecedores,
                               MaterialPort materiais, ObraConsultaPort obras, OrcamentoPort orcamento,
                               PedidoCompraMapper mapper) {
        this.pedidos = pedidos;
        this.fornecedores = fornecedores;
        this.materiais = materiais;
        this.obras = obras;
        this.orcamento = orcamento;
        this.mapper = mapper;
    }

    // ---------- fluxo do Estoquista / Gestor ----------

    @Transactional
    public PedidoCompraDto criar(Long obraId, NovoPedidoDto dto, Long solicitanteId) {
        return criarPedido(obraId, dto.prioridade(), dto.dataPrevista(), solicitanteId);
    }

    @Transactional(readOnly = true)
    public List<PedidoResumoDto> listar(Long obraId, StatusPedido status) {
        List<PedidoCompra> lista = status == null
                ? pedidos.findByObraIdOrderByIdDesc(obraId)
                : pedidos.findByObraIdAndStatusOrderByIdDesc(obraId, status);
        return lista.stream().map(mapper::paraResumo).toList();
    }

    @Transactional(readOnly = true)
    public PedidoCompraDto buscar(Long obraId, Long pedidoId) {
        return mapper.paraDto(obter(obraId, pedidoId));
    }

    @Transactional
    public ItemAdicionadoDto adicionarItem(Long obraId, Long pedidoId, NovoItemPedidoDto dto) {
        PedidoCompra pedido = obter(obraId, pedidoId);
        materiais.buscar(dto.materialId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Material", dto.materialId()));
        ItemOrcadoResumo itemOrcado = dto.itemOrcadoId() == null ? null : validarItemOrcado(dto.itemOrcadoId(), obraId);

        ItemPedido item = pedido.adicionarItem(dto.materialId(), dto.quantidade(), dto.valorUnitario(),
                dto.itemOrcadoId());
        boolean excede = itemOrcado != null && itemOrcado.totalRealizado()
                .add(item.calcularValorSubTotal()).compareTo(itemOrcado.valorOrcado()) > 0;

        // O pedido já está gerenciado: save() faria merge e o item novo ficaria transiente (TransientObjectException).
        pedidos.flush();
        return new ItemAdicionadoDto(mapper.paraDto(pedido), excede);
    }

    @Transactional
    public PedidoCompraDto removerItem(Long obraId, Long pedidoId, Long itemId) {
        PedidoCompra pedido = obter(obraId, pedidoId);
        pedido.removerItem(itemId);
        return mapper.paraDto(pedidos.saveAndFlush(pedido));
    }

    @Transactional
    public PedidoCompraDto ajustarValorUnitario(Long obraId, Long pedidoId, Long itemId,
                                                AjusteValorUnitarioDto dto) {
        PedidoCompra pedido = obter(obraId, pedidoId);
        pedido.ajustarValorUnitario(itemId, dto.valorUnitario());
        return mapper.paraDto(pedidos.saveAndFlush(pedido));
    }

    @Transactional
    public PedidoCompraDto registrarCotacao(Long obraId, Long pedidoId, NovaCotacaoDto dto) {
        PedidoCompra pedido = obter(obraId, pedidoId);
        Fornecedor fornecedor = fornecedores.findById(dto.fornecedorId())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor", dto.fornecedorId()));
        pedido.registrarCotacao(fornecedor, dto.valorTotalProposto(), dto.prazoEntregaDias(), LocalDateTime.now());
        return mapper.paraDto(pedidos.saveAndFlush(pedido));
    }

    @Transactional
    public PedidoCompraDto selecionarCotacao(Long obraId, Long pedidoId, Long cotacaoId) {
        PedidoCompra pedido = obter(obraId, pedidoId);
        pedido.selecionarCotacao(cotacaoId);
        return mapper.paraDto(pedidos.saveAndFlush(pedido));
    }

    /** ENTREGUE não é manual: só a entrada no estoque leva o pedido a esse estado. */
    @Transactional
    public PedidoCompraDto alterarStatus(Long obraId, Long pedidoId, StatusPedido novoStatus, Long autorId) {
        if (novoStatus == StatusPedido.ENTREGUE) {
            throw new RegraNegocioException("A entrega é registrada pela entrada do material no estoque");
        }
        PedidoCompra pedido = obter(obraId, pedidoId);
        pedido.alterarStatus(novoStatus, autorId, LocalDateTime.now());
        if (novoStatus == StatusPedido.COMPRADO) {
            lancarCustosRealizados(pedido, autorId);
        }
        return mapper.paraDto(pedidos.saveAndFlush(pedido));
    }

    @Transactional
    public PedidoCompraDto cancelar(Long obraId, Long pedidoId, Long autorId) {
        PedidoCompra pedido = obter(obraId, pedidoId);
        pedido.cancelar(autorId, LocalDateTime.now());
        return mapper.paraDto(pedidos.saveAndFlush(pedido));
    }

    // ---------- API usada pelo módulo estoque (mesma transação do chamador) ----------

    /** Reposição gerada a partir do alerta de estoque mínimo. Nasce em SOLICITADO com um item. */
    @Transactional
    public PedidoCompraDto criarReposicao(Long obraId, Long solicitanteId, Long materialId,
                                          BigDecimal quantidade, PrioridadePedido prioridade) {
        materiais.buscar(materialId).orElseThrow(() -> new RecursoNaoEncontradoException("Material", materialId));
        PedidoCompra pedido = PedidoCompra.criar(obraId, solicitanteId, prioridade, null, LocalDateTime.now());
        pedido.adicionarItem(materialId, quantidade, BigDecimal.ZERO, null);
        return mapper.paraDto(pedidos.saveAndFlush(pedido));
    }

    /** Trava o pedido (lock pessimista) e devolve o que deve entrar no estoque. */
    @Transactional
    public PedidoRecebimento bloquearParaRecebimento(Long pedidoId) {
        PedidoCompra pedido = pedidos.buscarParaAtualizar(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido de compra", pedidoId));
        var itens = pedido.getItens().stream()
                .map(i -> new PedidoRecebimento.Item(i.getMaterialId(), i.getQuantidade())).toList();
        return new PedidoRecebimento(pedido.getId(), pedido.getObraId(), pedido.getStatus(), itens);
    }

    @Transactional
    public void registrarEntrega(Long pedidoId, Long autorId) {
        PedidoCompra pedido = pedidos.findById(pedidoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido de compra", pedidoId));
        pedido.alterarStatus(StatusPedido.ENTREGUE, autorId, LocalDateTime.now());
    }

    // ---------- privados ----------

    private PedidoCompraDto criarPedido(Long obraId, PrioridadePedido prioridade, LocalDate dataPrevista,
                                        Long solicitanteId) {
        if (!obras.obraExiste(obraId)) throw new RecursoNaoEncontradoException("Obra", obraId);
        PedidoCompra pedido = PedidoCompra.criar(obraId, solicitanteId, prioridade, dataPrevista,
                LocalDateTime.now());
        return mapper.paraDto(pedidos.saveAndFlush(pedido));
    }

    /** O pedido precisa pertencer à obra da rota: impede acessar pedido de outra obra. */
    private PedidoCompra obter(Long obraId, Long pedidoId) {
        return pedidos.findByIdAndObraId(pedidoId, obraId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Pedido de compra", pedidoId));
    }

    private ItemOrcadoResumo validarItemOrcado(Long itemOrcadoId, Long obraId) {
        ItemOrcadoResumo resumo = orcamento.buscarItem(itemOrcadoId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Item do orçamento", itemOrcadoId));
        if (!resumo.obraId().equals(obraId)) {
            throw new RegraNegocioException("O item do orçamento não pertence a esta obra");
        }
        return resumo;
    }

    /** Ao comprar, o custo entra como realizado (origem MATERIAL) nos itens ligados ao orçamento. */
    private void lancarCustosRealizados(PedidoCompra pedido, Long autorId) {
        for (ItemPedido item : pedido.getItens()) {
            if (item.getItemOrcadoId() != null) {
                orcamento.registrarCustoMaterial(item.getItemOrcadoId(),
                        "Pedido de compra #" + pedido.getId(), item.calcularValorSubTotal(),
                        pedido.getId(), autorId);
            }
        }
    }
}
