package br.com.obras.estoque.repository;

import br.com.obras.estoque.domain.Material;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MaterialRepository extends JpaRepository<Material, Long> {
    boolean existsByNomeIgnoreCaseAndUnidadeMedidaIgnoreCase(String nome, String unidadeMedida);

    boolean existsByNomeIgnoreCaseAndUnidadeMedidaIgnoreCaseAndIdNot(String nome, String unidadeMedida, Long id);

    List<Material> findAllByOrderByNomeAsc();

    List<Material> findByNomeContainingIgnoreCaseOrderByNomeAsc(String nome);
}
