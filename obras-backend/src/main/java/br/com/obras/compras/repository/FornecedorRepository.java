package br.com.obras.compras.repository;

import br.com.obras.compras.domain.Fornecedor;
import br.com.obras.compras.domain.SituacaoFornecedor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FornecedorRepository extends JpaRepository<Fornecedor, Long> {
    boolean existsByCpfCnpj(String cpfCnpj);

    boolean existsByCpfCnpjAndIdNot(String cpfCnpj, Long id);

    List<Fornecedor> findAllByOrderByNomeRazaoSocialAsc();

    List<Fornecedor> findBySituacaoOrderByNomeRazaoSocialAsc(SituacaoFornecedor situacao);
}
