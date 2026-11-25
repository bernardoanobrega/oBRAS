package br.com.obras.estoque.repository;

import br.com.obras.estoque.domain.SaldoEstoque;
import br.com.obras.estoque.dto.SaldoConsolidadoDto;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface SaldoEstoqueRepository extends JpaRepository<SaldoEstoque, Long> {

    /** Lock pessimista: evita duas saídas simultâneas deixarem o saldo negativo. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SaldoEstoque s "
            + "where s.material.id = :materialId and s.canteiroId = :canteiroId")
    Optional<SaldoEstoque> buscarParaAtualizar(@Param("materialId") Long materialId,
                                               @Param("canteiroId") Long canteiroId);

    @Query("select s from SaldoEstoque s join fetch s.material where s.id = :id")
    Optional<SaldoEstoque> buscarComMaterial(@Param("id") Long id);

    @Query("select s from SaldoEstoque s join fetch s.material "
            + "where s.canteiroId in :canteiroIds order by s.material.nome, s.canteiroId")
    List<SaldoEstoque> listarPorCanteiros(@Param("canteiroIds") Collection<Long> canteiroIds);

    /** calcularSaldoObra: soma dos saldos de todos os canteiros da obra, por material. */
    @Query("select new br.com.obras.estoque.dto.SaldoConsolidadoDto(m.id, m.nome, m.unidadeMedida, "
            + "sum(s.quantidadeAtual)) from SaldoEstoque s join s.material m "
            + "where s.canteiroId in :canteiroIds group by m.id, m.nome, m.unidadeMedida order by m.nome")
    List<SaldoConsolidadoDto> consolidarPorMaterial(@Param("canteiroIds") Collection<Long> canteiroIds);

    Optional<SaldoEstoque> findByMaterialIdAndCanteiroId(Long materialId, Long canteiroId);
}
