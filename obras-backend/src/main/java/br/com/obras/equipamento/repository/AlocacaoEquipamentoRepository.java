package br.com.obras.equipamento.repository;

import br.com.obras.equipamento.domain.AlocacaoEquipamento;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AlocacaoEquipamentoRepository extends JpaRepository<AlocacaoEquipamento, Long> {

    /** Histórico do equipamento, da alocação mais recente para a mais antiga. */
    List<AlocacaoEquipamento> findByEquipamentoIdOrderByDataInicioDescIdDesc(Long equipamentoId);

    /** Alocações ativas na obra, com o equipamento carregado, por data prevista de devolução. */
    @Query("""
            select a from AlocacaoEquipamento a
            join fetch a.equipamento
            where a.obraId = :obraId
              and a.dataFim is null
            order by a.dataPrevistaDevolucao, a.id
            """)
    List<AlocacaoEquipamento> listarAtivasPorObra(@Param("obraId") Long obraId);
}