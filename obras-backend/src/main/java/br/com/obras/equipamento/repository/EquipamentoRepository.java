package br.com.obras.equipamento.repository;
 
import br.com.obras.equipamento.domain.Equipamento;
import br.com.obras.equipamento.domain.StatusEquipamento;
import br.com.obras.equipamento.domain.TipoEquipamento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
 
public interface EquipamentoRepository extends JpaRepository<Equipamento, Long> {
 
    boolean existsByCodigoPatrimonio(String codigoPatrimonio);
 
    /**
     * Busca paginada. {@code status} e {@code tipo} podem ser nulos (sem filtro). {@code busca} nunca
     * deve ser nulo: use texto vazio para não filtrar por código ou descrição.
     */
    @Query("""
            select e from Equipamento e
            where (:status is null or e.status = :status)
              and (:tipo is null or e.tipo = :tipo)
              and (lower(e.codigoPatrimonio) like lower(concat('%', :busca, '%'))
                   or lower(e.descricao) like lower(concat('%', :busca, '%')))
            """)
    Page<Equipamento> buscar(@Param("status") StatusEquipamento status, @Param("tipo") TipoEquipamento tipo,
            @Param("busca") String busca, Pageable pageable);
}
 