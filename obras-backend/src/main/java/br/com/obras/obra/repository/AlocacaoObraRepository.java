package br.com.obras.obra.repository;

import br.com.obras.obra.domain.AlocacaoObra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface AlocacaoObraRepository extends JpaRepository<AlocacaoObra, Long> {
    @Query("""
        select distinct a
            from AlocacaoObra a
            join fetch a.usuario
            left join fetch a.cargos
        where a.obra.id = :obraId
            and a.dataFim is null            
    """)
    List<AlocacaoObra> findEquipeAtiva(@Param("obraId") Long obraId);

    @Query("""
        select a from AlocacaoObra a
        where a.obra.id = :obraId
          and a.dataFim is null
          and a.usuario.id = :usuarioId
    """)
    Optional<AlocacaoObra> findAtivaPorUsuario(
        @Param("obraId") Long obraId,
        @Param("usuarioId") Long usuarioId
    );

    @Query("""
        select a from AlocacaoObra a
        where a.obra.id = :obraId
          and a.dataFim is null
          and a.id in :ids
    """)
    List<AlocacaoObra> findAtivasPorIds(@Param("obraId") Long obraId, @Param("ids") Set<Long> ids);
}
