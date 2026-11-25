package br.com.obras.diario.repository;

import br.com.obras.diario.domain.RegistroDiario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RegistroDiarioRepository extends JpaRepository<RegistroDiario, Long> {
    @Query("""
        select d from RegistroDiario d
        where d.obra.id = :obraId
          and d.excluido = false
        order by d.data desc
    """)
    List<RegistroDiario> listarPorObra(@Param("obraId") Long obraId);

    @Query("""
        select d from RegistroDiario d
        where d.id = :diarioId
          and d.obra.id = :obraId
          and d.excluido = false
    """)
    Optional<RegistroDiario> buscar(@Param("obraId") Long obraId, @Param("diarioId") Long diarioId);

    /**
     * Igual a {@link #buscar}, mas já traz a equipe presente com os usuários
     * (nomes) na mesma consulta, para montar o detalhe sem N+1.
     */
    @Query("""
        select distinct d from RegistroDiario d
        left join fetch d.equipePresente e
        left join fetch e.usuario
        where d.id = :diarioId
          and d.obra.id = :obraId
          and d.excluido = false
    """)
    Optional<RegistroDiario> buscarComEquipe(@Param("obraId") Long obraId, @Param("diarioId") Long diarioId);

    @Query("""
        select d from RegistroDiario d
        where d.obra.id = :obraId
          and d.data = :data
          and d.excluido = false
    """)
    Optional<RegistroDiario> buscarAtivoPorData(@Param("obraId") Long obraId, @Param("data") LocalDate data);
}
