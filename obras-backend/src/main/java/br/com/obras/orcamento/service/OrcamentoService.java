package br.com.obras.orcamento.service;

import br.com.obras.orcamento.domain.CustoRealizado;
import br.com.obras.orcamento.domain.DecisaoFinanceira;
import br.com.obras.orcamento.domain.ItemOrcado;
import br.com.obras.orcamento.domain.Orcamento;
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
import br.com.obras.orcamento.dto.OrcamentoResumoDto;
import br.com.obras.orcamento.repository.OrcamentoRepository;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.shared.security.AcaoPermissao;
import br.com.obras.shared.security.AutorizacaoPort;
import br.com.obras.shared.security.ModuloSistema;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serviço público do módulo Orçamento (UC10). Outros módulos, como Compras, lançam custos
 * por {@link #registrarCusto}, nunca pelo repository.
 */
@Service
public class OrcamentoService {
    private final OrcamentoRepository orcamentos;
    private final AutorizacaoPort autorizacao;

    public OrcamentoService(OrcamentoRepository orcamentos, AutorizacaoPort autorizacao) {
        this.orcamentos = orcamentos;
        this.autorizacao = autorizacao;
    }

    /** Cria o orçamento da obra (1:1). Estado inicial ELABORACAO. */
    @Transactional
    public OrcamentoDto elaborar(Long obraId, NovoOrcamentoDto dto) {
        if (orcamentos.existsByObraId(obraId)) {
            throw new RegraNegocioException("Esta obra já possui orçamento");
        }
        Orcamento orcamento = Orcamento.elaborar(obraId, dto.percentualLimiteAlerta());
        return OrcamentoDto.de(orcamentos.save(orcamento));
    }

    /** RF-32, RF-33 e RF-74: planilha, decisões e indicadores de saúde. */
    @Transactional(readOnly = true)
    public OrcamentoDto buscarPorObra(Long obraId) {
        return OrcamentoDto.de(obterPorObra(obraId));
    }

    /** Orçamentos das obras em que o usuário pode visualizar o módulo Orçamento. */
    @Transactional(readOnly = true)
    public List<OrcamentoResumoDto> listarVisiveis(Long usuarioId) {
        return orcamentos.findAllByOrderByObraIdAsc().stream()
                .filter(orcamento -> autorizacao.possuiPermissao(usuarioId, orcamento.getObraId(),
                        ModuloSistema.ORCAMENTO, AcaoPermissao.VISUALIZAR))
                .map(OrcamentoResumoDto::de)
                .toList();
    }

    /** RF-30. */
    @Transactional
    public ItemOrcadoDto adicionarItem(Long obraId, NovoItemOrcadoDto dto) {
        Orcamento orcamento = obterPorObra(obraId);
        ItemOrcado item = orcamento.adicionarItem(dto.codigo(), dto.descricao(), dto.unidade(), dto.quantidade(),
                dto.custoUnitarioOrcado());
        orcamentos.flush();
        return ItemOrcadoDto.de(item);
    }

    /** RF-31. */
    @Transactional
    public ItemOrcadoDto atualizarItem(Long obraId, Long itemId, AtualizarItemOrcadoDto dto) {
        ItemOrcado item = obterPorObra(obraId).buscarItem(itemId);
        item.atualizarValores(dto.quantidade(), dto.custoUnitarioOrcado());
        return ItemOrcadoDto.de(item);
    }

    @Transactional
    public void removerItem(Long obraId, Long itemId) {
        obterPorObra(obraId).removerItem(itemId);
    }

    /** RF-33 e RF-34: lança o custo e recalcula a saúde financeira do orçamento. */
    @Transactional
    public CustoRealizadoDto registrarCusto(Long obraId, Long itemId, NovoCustoRealizadoDto dto, Long usuarioId) {
        ItemOrcado item = obterPorObra(obraId).buscarItem(itemId);
        CustoRealizado custo = item.registrarCusto(dto.descricao(), dto.valor(), dto.origem(), dto.idOrigem(),
                usuarioId);
        orcamentos.flush();
        return CustoRealizadoDto.de(custo);
    }

    @Transactional
    public CustoRealizadoDto editarCusto(Long obraId, Long itemId, Long custoId, AtualizarCustoRealizadoDto dto) {
        ItemOrcado item = obterPorObra(obraId).buscarItem(itemId);
        CustoRealizado custo = item.editarCusto(custoId, dto.descricao(), dto.valor(), dto.origem());
        return CustoRealizadoDto.de(custo);
    }

    @Transactional
    public void removerCusto(Long obraId, Long itemId, Long custoId) {
        obterPorObra(obraId).buscarItem(itemId).removerCusto(custoId);
    }

    @Transactional(readOnly = true)
    public List<CustoRealizadoDto> listarCustos(Long obraId, Long itemId) {
        return obterPorObra(obraId).buscarItem(itemId).getCustos().stream()
                .map(CustoRealizadoDto::de)
                .toList();
    }

    @Transactional
    public DecisaoFinanceiraDto registrarDecisao(Long obraId, NovaDecisaoFinanceiraDto dto, Long usuarioId) {
        Orcamento orcamento = obterPorObra(obraId);
        DecisaoFinanceira decisao = orcamento.registrarDecisao(dto.descricao(), dto.justificativa(),
                dto.valorImpacto(), usuarioId);
        orcamentos.flush();
        return DecisaoFinanceiraDto.de(decisao);
    }

    @Transactional
    public DecisaoFinanceiraDto editarDecisao(Long obraId, Long decisaoId, AtualizarDecisaoFinanceiraDto dto) {
        DecisaoFinanceira decisao = obterPorObra(obraId).editarDecisao(decisaoId, dto.descricao(),
                dto.justificativa(), dto.valorImpacto());
        return DecisaoFinanceiraDto.de(decisao);
    }

    /** RF-34: itens cujo realizado passou do orçado. */
    @Transactional(readOnly = true)
    public List<ItemOrcadoDto> listarItensEmDesvio(Long obraId) {
        return obterPorObra(obraId).listarItensEmDesvio().stream()
                .map(ItemOrcadoDto::de)
                .toList();
    }

    @Transactional
    public OrcamentoDto cancelar(Long obraId) {
        Orcamento orcamento = obterPorObra(obraId);
        orcamento.cancelar();
        return OrcamentoDto.de(orcamento);
    }

    private Orcamento obterPorObra(Long obraId) {
        return orcamentos.findByObraId(obraId)
                .orElseThrow(() -> new RecursoNaoEncontradoException("Orçamento da obra", obraId));
    }
}
