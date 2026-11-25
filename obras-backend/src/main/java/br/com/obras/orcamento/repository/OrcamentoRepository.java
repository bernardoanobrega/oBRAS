package br.com.obras.orcamento.repository;

import br.com.obras.orcamento.domain.Orcamento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrcamentoRepository extends JpaRepository<Orcamento, Long> {
    Optional<Orcamento> findByObraId(Long obraId);

    /** Orçamento que contém o item; usado por outros módulos, que só conhecem o id do item. */
    Optional<Orcamento> findByItensId(Long itemId);

    boolean existsByObraId(Long obraId);

    List<Orcamento> findAllByOrderByObraIdAsc();
}
