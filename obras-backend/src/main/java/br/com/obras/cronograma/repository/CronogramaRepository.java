package br.com.obras.cronograma.repository;

import br.com.obras.cronograma.domain.Cronograma;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CronogramaRepository extends JpaRepository<Cronograma, Long> {
    Optional<Cronograma> findByObraId(Long obraId);

    boolean existsByObraId(Long obraId);

    List<Cronograma> findAllByOrderByObraIdAsc();
}
