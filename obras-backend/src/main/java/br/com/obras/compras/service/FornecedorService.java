package br.com.obras.compras.service;

import br.com.obras.compras.domain.Fornecedor;
import br.com.obras.compras.domain.SituacaoFornecedor;
import br.com.obras.compras.dto.FornecedorDto;
import br.com.obras.compras.dto.FornecedorRequest;
import br.com.obras.compras.dto.PedidoResumoDto;
import br.com.obras.compras.mapper.PedidoCompraMapper;
import br.com.obras.compras.repository.FornecedorRepository;
import br.com.obras.compras.repository.PedidoCompraRepository;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** RF-07 e RF-08. */
@Service
public class FornecedorService {
    private final FornecedorRepository fornecedores;
    private final PedidoCompraRepository pedidos;
    private final PedidoCompraMapper mapper;

    public FornecedorService(FornecedorRepository fornecedores, PedidoCompraRepository pedidos,
                             PedidoCompraMapper mapper) {
        this.fornecedores = fornecedores;
        this.pedidos = pedidos;
        this.mapper = mapper;
    }

    @Transactional
    public FornecedorDto cadastrar(FornecedorRequest dto) {
        String documento = Fornecedor.normalizarDocumento(dto.cpfCnpj());
        if (fornecedores.existsByCpfCnpj(documento)) {
            throw new RegraNegocioException("CPF/CNPJ já cadastrado");
        }
        Fornecedor f = Fornecedor.cadastrar(dto.nomeRazaoSocial(), dto.cpfCnpj(), dto.email(), dto.telefone());
        return FornecedorDto.de(fornecedores.save(f));
    }

    @Transactional
    public FornecedorDto atualizar(Long id, FornecedorRequest dto) {
        Fornecedor f = obter(id);
        String documento = Fornecedor.normalizarDocumento(dto.cpfCnpj());
        if (fornecedores.existsByCpfCnpjAndIdNot(documento, id)) {
            throw new RegraNegocioException("CPF/CNPJ utilizado por outro fornecedor");
        }
        f.alterarDados(dto.nomeRazaoSocial(), dto.cpfCnpj(), dto.email(), dto.telefone());
        return FornecedorDto.de(f);
    }

    @Transactional
    public FornecedorDto desativar(Long id) {
        Fornecedor f = obter(id);
        f.desativar();
        return FornecedorDto.de(f);
    }

    @Transactional(readOnly = true)
    public List<FornecedorDto> listar(boolean apenasAtivos) {
        List<Fornecedor> lista = apenasAtivos
                ? fornecedores.findBySituacaoOrderByNomeRazaoSocialAsc(SituacaoFornecedor.ATIVO)
                : fornecedores.findAllByOrderByNomeRazaoSocialAsc();
        return lista.stream().map(FornecedorDto::de).toList();
    }

    @Transactional(readOnly = true)
    public FornecedorDto buscar(Long id) { return FornecedorDto.de(obter(id)); }

    /** Pedidos em que o fornecedor foi o escolhido (cotação selecionada). */
    @Transactional(readOnly = true)
    public List<PedidoResumoDto> listarHistoricoCompras(Long id) {
        obter(id);
        return pedidos.findByFornecedorIdOrderByIdDesc(id).stream().map(mapper::paraResumo).toList();
    }

    private Fornecedor obter(Long id) {
        return fornecedores.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Fornecedor", id));
    }
}
