package br.com.obras.diario.repository;

import br.com.obras.diario.domain.Ocorrencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
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

    /**
     * Ocorrências ativas do diário, já com autor (alocação + usuário) e paralisação
     * carregados na mesma consulta, para o mapper não disparar uma consulta por item (N+1).
     */
    @Query("""
        select o from Ocorrencia o
        join o.auditavel a
        join fetch o.alocacaoObra al
        join fetch al.usuario
        left join fetch o.paralisacao
        where o.registroDiario.id = :diarioId
          and a.excluido = false
        order by o.dataHora desc
    """)
    List<Ocorrencia> listarPorDiario(@Param("diarioId") Long diarioId);

    /** Uma única consulta conta as ocorrências ativas de vários diários (evita N+1 na lista). */
    @Query("""
        select new br.com.obras.diario.repository.ContagemOcorrencias(o.registroDiario  .id, count(o))
        from Ocorrencia o
        join o.auditavel a
        where o.registroDiario.id in :diarioIds
          and a.excluido = false
        group by o.registroDiario.id
    """)
    List<ContagemOcorrencias> contarPorDiarios(@Param("diarioIds") Collection<Long> diarioIds);
}
