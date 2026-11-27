package br.com.obras.obra.repository;

import br.com.obras.obra.domain.Obra;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * A exclusão lógica da obra fica no {@code auditavel} (V3_1). A obra guarda só o
 * {@code auditavelId}, então as consultas juntam com {@code Auditavel} pela coluna.
 */
public interface ObraRepository extends JpaRepository<Obra, Long> {
    @Query("""
        select o from Obra o
        join Auditavel a on a.id = o.auditavelId
        where a.excluido = false
        order by o.nome asc
    """)
    List<Obra> listarAtivas();

    @Query("""
        select o from Obra o
        join Auditavel a on a.id = o.auditavelId
        where o.id = :obraId
          and a.excluido = false
    """)
    Optional<Obra> buscarAtiva(@Param("obraId") Long obraId);

    @Query("""
        select distinct o from Obra o
        join Auditavel auditavel on auditavel.id = o.auditavelId
        join AlocacaoObra alocacao on alocacao.obra.id = o.id
        where auditavel.excluido = false
          and alocacao.usuario.id = :usuarioId
          and alocacao.dataFim is null
        order by o.nome asc
    """)
    List<Obra> obrasPorAlocacaoAtiva(@Param("usuarioId") Long usuarioId);
}
