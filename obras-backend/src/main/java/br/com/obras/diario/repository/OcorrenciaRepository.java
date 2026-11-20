package br.com.obras.diario.repository;

import br.com.obras.diario.domain.Ocorrencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OcorrenciaRepository extends JpaRepository<Ocorrencia, Long> {
    @Query("""
        select o from Ocorrencia o
        join o.auditavel a
        where o.id = :ocorrenciaId
          and o.registroDiario.obra.id = :obraId
          and a.excluido = false
    """)
    Optional<Ocorrencia> buscar(@Param("obraId") Long obraId, @Param("ocorrenciaId") Long ocorrenciaId);

    @Query("""
        select o from Ocorrencia o
        join o.auditavel a
        where o.registroDiario.id = :diarioId
          and a.excluido = false
        order by o.dataHora desc
    """)
    List<Ocorrencia> listarPorDiario(@Param("diarioId") Long diarioId);

    @Query("""
        select count(o) from Ocorrencia o
        join o.auditavel a
        where o.registroDiario.id = :diarioId
          and a.excluido = false
    """)
    long contarPorDiario(@Param("diarioId") Long diarioId);
}
