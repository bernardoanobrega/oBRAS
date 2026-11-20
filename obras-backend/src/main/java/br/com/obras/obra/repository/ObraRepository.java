package br.com.obras.obra.repository;

import br.com.obras.obra.domain.Obra;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ObraRepository extends JpaRepository<Obra, Long> {
    List<Obra> findAllByOrderByNomeAsc();
}