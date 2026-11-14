package br.com.obras.obra.repository;

import br.com.obras.obra.domain.AlocacaoObra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

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
}