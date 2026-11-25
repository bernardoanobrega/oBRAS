package br.com.obras.estoque.service;

import br.com.obras.compras.domain.PrioridadePedido;
import br.com.obras.compras.domain.StatusPedido;
import br.com.obras.compras.dto.PedidoCompraDto;
import br.com.obras.compras.dto.PedidoRecebimento;
import br.com.obras.compras.service.PedidoCompraService;
import br.com.obras.estoque.domain.Material;
import br.com.obras.estoque.domain.MovimentacaoEstoque;
import br.com.obras.estoque.domain.SaldoEstoque;
import br.com.obras.estoque.domain.SituacaoSaldo;
import br.com.obras.estoque.domain.TipoMovimentacao;
import br.com.obras.estoque.dto.ConfigurarSaldoDto;
import br.com.obras.estoque.dto.MovimentacaoDto;
import br.com.obras.estoque.dto.SaidaResultadoDto;
import br.com.obras.estoque.dto.SaldoConsolidadoDto;
import br.com.obras.estoque.dto.SaldoDto;
import br.com.obras.estoque.dto.TransferenciaResultadoDto;
import br.com.obras.estoque.repository.ConsumoAgregado;
import br.com.obras.estoque.repository.MaterialRepository;
import br.com.obras.estoque.repository.MovimentacaoEstoqueRepository;
import br.com.obras.estoque.repository.SaldoEstoqueRepository;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.shared.port.ObraConsultaPort;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * UC13 e UC14 | RF-17, RF-18, RF-35.
 * Cada operação é UMA transação: saldo, movimentação e (na entrada) o status do pedido
 * são confirmados juntos ou revertidos juntos.
 */
@Service
public class EstoqueService {
    private static final int SEMANAS_MEDIA_CONSUMO = 4;
    private static final int DIAS_PADRAO_HISTORICO = 30;
    private static final int LIMITE_MOVIMENTACOES = 200;

    private final SaldoEstoqueRepository saldos;
    private final MovimentacaoEstoqueRepository movimentacoes;
    private final MaterialRepository materiais;
    private final ObraConsultaPort obras;
    private final PedidoCompraService pedidos;

    public EstoqueService(SaldoEstoqueRepository saldos, MovimentacaoEstoqueRepository movimentacoes,
                          MaterialRepository materiais, ObraConsultaPort obras, PedidoCompraService pedidos) {
        this.saldos = saldos;
        this.movimentacoes = movimentacoes;
        this.materiais = materiais;
        this.obras = obras;
        this.pedidos = pedidos;
    }

    // ---------- operações ----------

    /** Entrada do pedido inteiro, uma única vez, e só se estiver EM_TRANSITO. */
    @Transactional
    public List<MovimentacaoDto> registrarEntradaEstoque(Long obraId, Long pedidoId, Long canteiroId, Long autorId) {
        validarCanteiro(obraId, canteiroId);
        PedidoRecebimento pedido = pedidos.bloquearParaRecebimento(pedidoId);
        if (!pedido.obraId().equals(obraId)) {
            throw new RecursoNaoEncontradoException("Pedido de compra", pedidoId);
        }
        if (pedido.status() != StatusPedido.EM_TRANSITO) {
            throw new RegraNegocioException("O pedido não está em trânsito");
        }

        LocalDateTime agora = LocalDateTime.now();
        List<MovimentacaoEstoque> registradas = new ArrayList<>();
        for (PedidoRecebimento.Item item : pedido.itens()) {
            Material material = obterMaterial(item.materialId());
            SaldoEstoque saldo = obterOuCriarSaldoTravado(material, canteiroId);
            saldo.adicionarSaldo(item.quantidade());
            registradas.add(movimentacoes.save(MovimentacaoEstoque.entradaCompra(
                    material, item.quantidade(), canteiroId, pedidoId, autorId, agora)));
        }
        pedidos.registrarEntrega(pedidoId, autorId);
        return registradas.stream().map(MovimentacaoDto::de).toList();
    }

    @Transactional
    public SaidaResultadoDto registrarSaidaEstoque(Long obraId, Long materialId, Long canteiroId,
                                                   BigDecimal quantidade, Long autorId) {
        validarCanteiro(obraId, canteiroId);
        Material material = obterMaterial(materialId);
        SaldoEstoque saldo = saldos.buscarParaAtualizar(materialId, canteiroId)
                .orElseThrow(() -> new RegraNegocioException("Quantidade insuficiente em estoque"));

        saldo.subtrairSaldo(quantidade);
        MovimentacaoEstoque movimentacao = movimentacoes.save(MovimentacaoEstoque.saidaConsumo(
                material, quantidade, canteiroId, autorId, LocalDateTime.now()));
        return new SaidaResultadoDto(MovimentacaoDto.de(movimentacao), SaldoDto.de(saldo, null),
                saldo.verificarAlertaMinimo());
    }

    /** Transferência entre canteiros da MESMA obra (premissa: o custo fica na obra de origem). */
    @Transactional
    public TransferenciaResultadoDto registrarTransferenciaEstoque(Long obraId, Long materialId,
                                                                   Long canteiroOrigemId, Long canteiroDestinoId,
                                                                   BigDecimal quantidade, Long autorId) {
        validarCanteiro(obraId, canteiroOrigemId);
        validarCanteiro(obraId, canteiroDestinoId);
        if (canteiroOrigemId.equals(canteiroDestinoId)) {
            throw new RegraNegocioException("Origem e destino da transferência devem ser canteiros diferentes");
        }
        Material material = obterMaterial(materialId);

        // trava sempre em ordem crescente de canteiro: transferências opostas não geram deadlock
        SaldoEstoque origem;
        SaldoEstoque destino;
        if (canteiroOrigemId < canteiroDestinoId) {
            origem = obterSaldoOrigem(materialId, canteiroOrigemId);
            destino = obterOuCriarSaldoTravado(material, canteiroDestinoId);
        } else {
            destino = obterOuCriarSaldoTravado(material, canteiroDestinoId);
            origem = obterSaldoOrigem(materialId, canteiroOrigemId);
        }

        origem.subtrairSaldo(quantidade);
        destino.adicionarSaldo(quantidade);
        MovimentacaoEstoque movimentacao = movimentacoes.save(MovimentacaoEstoque.transferencia(
                material, quantidade, canteiroOrigemId, canteiroDestinoId, autorId, LocalDateTime.now()));
        return new TransferenciaResultadoDto(MovimentacaoDto.de(movimentacao), SaldoDto.de(origem, null),
                SaldoDto.de(destino, null), origem.verificarAlertaMinimo());
    }

    /** Cria o saldo (zerado) se não existir e define a quantidade mínima. */
    @Transactional
    public SaldoDto configurarSaldo(Long obraId, ConfigurarSaldoDto dto) {
        validarCanteiro(obraId, dto.canteiroId());
        Material material = obterMaterial(dto.materialId());
        SaldoEstoque saldo = obterOuCriarSaldoTravado(material, dto.canteiroId());
        saldo.definirQuantidadeMinima(dto.quantidadeMinima());
        return SaldoDto.de(saldo, null);
    }

    /** Gera um PedidoCompra SOLICITADO a partir do alerta de estoque baixo. */
    @Transactional
    public PedidoCompraDto solicitarReposicao(Long obraId, Long materialId, Long canteiroId,
                                              BigDecimal quantidade, Long solicitanteId) {
        validarCanteiro(obraId, canteiroId);
        obterMaterial(materialId);
        boolean esgotado = saldos.findByMaterialIdAndCanteiroId(materialId, canteiroId)
                .map(s -> s.situacao() == SituacaoSaldo.ESGOTADO)
                .orElse(true);
        PrioridadePedido prioridade = esgotado ? PrioridadePedido.ALTA : PrioridadePedido.MEDIA;
        return pedidos.criarReposicao(obraId, solicitanteId, materialId, quantidade, prioridade);
    }

    // ---------- consultas ----------

    /** Saldo por canteiro (canteiroId informado) ou de todos os canteiros da obra. */
    @Transactional(readOnly = true)
    public List<SaldoDto> consultarSaldos(Long obraId, Long canteiroId) {
        List<Long> canteiroIds = resolverCanteiros(obraId, canteiroId);
        if (canteiroIds.isEmpty()) return List.of();

        Map<String, BigDecimal> consumo = movimentacoes
                .somarConsumo(canteiroIds, LocalDateTime.now().minusWeeks(SEMANAS_MEDIA_CONSUMO))
                .stream()
                .collect(Collectors.toMap(c -> chave(c.materialId(), c.canteiroId()), ConsumoAgregado::total));

        return saldos.listarPorCanteiros(canteiroIds).stream()
                .map(s -> SaldoDto.de(s, mediaSemanal(consumo.get(chave(s.getMaterial().getId(), s.getCanteiroId())))))
                .toList();
    }

    /** calcularSaldoObra: total por material somando todos os canteiros. */
    @Transactional(readOnly = true)
    public List<SaldoConsolidadoDto> consultarSaldoConsolidado(Long obraId) {
        List<Long> canteiroIds = obras.canteiroIdsDaObra(obraId);
        return canteiroIds.isEmpty() ? List.of() : saldos.consolidarPorMaterial(canteiroIds);
    }

    @Transactional(readOnly = true)
    public List<MovimentacaoDto> listarMovimentacoes(Long obraId, Long canteiroId, TipoMovimentacao tipo,
                                                     LocalDate inicio, LocalDate fim) {
        List<Long> canteiroIds = resolverCanteiros(obraId, canteiroId);
        if (canteiroIds.isEmpty()) return List.of();

        LocalDate hoje = LocalDate.now();
        LocalDate de = inicio != null ? inicio : hoje.minusDays(DIAS_PADRAO_HISTORICO);
        LocalDate ate = fim != null ? fim : hoje;
        if (ate.isBefore(de)) throw new RegraNegocioException("A data final não pode ser anterior à inicial");

        List<TipoMovimentacao> tipos = tipo == null ? List.of(TipoMovimentacao.values()) : List.of(tipo);
        return movimentacoes.listar(canteiroIds, tipos, de.atStartOfDay(), ate.plusDays(1).atStartOfDay(),
                        PageRequest.of(0, LIMITE_MOVIMENTACOES))
                .stream().map(MovimentacaoDto::de).toList();
    }

    // ---------- privados ----------

    /** O canteiro precisa pertencer à obra da rota; senão responde 404 (não revela que existe). */
    private void validarCanteiro(Long obraId, Long canteiroId) {
        Long dono = obras.obraIdDoCanteiro(canteiroId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Canteiro", canteiroId));
        if (!dono.equals(obraId)) throw new RecursoNaoEncontradoException("Canteiro", canteiroId);
    }

    private List<Long> resolverCanteiros(Long obraId, Long canteiroId) {
        if (canteiroId == null) return obras.canteiroIdsDaObra(obraId);
        validarCanteiro(obraId, canteiroId);
        return List.of(canteiroId);
    }

    private Material obterMaterial(Long materialId) {
        return materiais.findById(materialId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Material", materialId));
    }

    private SaldoEstoque obterSaldoOrigem(Long materialId, Long canteiroId) {
        return saldos.buscarParaAtualizar(materialId, canteiroId)
                .orElseThrow(() -> new RegraNegocioException("Quantidade insuficiente em estoque"));
    }

    private SaldoEstoque obterOuCriarSaldoTravado(Material material, Long canteiroId) {
        return saldos.buscarParaAtualizar(material.getId(), canteiroId)
                .orElseGet(() -> saldos.save(SaldoEstoque.criar(material, canteiroId, BigDecimal.ZERO)));
    }

    private static BigDecimal mediaSemanal(BigDecimal totalConsumido) {
        if (totalConsumido == null) return BigDecimal.ZERO.setScale(3);
        return totalConsumido.divide(BigDecimal.valueOf(SEMANAS_MEDIA_CONSUMO), 3, RoundingMode.HALF_UP);
    }

    private static String chave(Long materialId, Long canteiroId) {
        return materialId + ":" + canteiroId;
    }
}
