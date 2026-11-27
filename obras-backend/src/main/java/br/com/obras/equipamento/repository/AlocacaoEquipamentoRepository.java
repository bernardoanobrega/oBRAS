package br.com.obras.equipamento.repository;

import br.com.obras.equipamento.domain.AlocacaoEquipamento;
import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlocacaoEquipamentoRepository extends JpaRepository<AlocacaoEquipamento, Long> {

    /** Histórico do equipamento, da alocação mais recente para a mais antiga. */
    List<AlocacaoEquipamento> findByEquipamentoIdOrderByDataInicioDescIdDesc(Long equipamentoId);

    /** Alocações ativas nos canteiros informados, com o equipamento carregado, por data prevista. */
    @Query("""
            select a from AlocacaoEquipamento a
            join fetch a.equipamento
            where a.canteiroId in :canteiroIds
              and a.dataFim is null
            order by a.dataPrevistaDevolucao, a.id
            """)
    List<AlocacaoEquipamento> listarAtivasPorCanteiros(@Param("canteiroIds") Collection<Long> canteiroIds);
}