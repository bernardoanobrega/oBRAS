package br.com.obras.estoque.repository;

import br.com.obras.estoque.domain.MovimentacaoEstoque;
import br.com.obras.estoque.domain.TipoMovimentacao;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface MovimentacaoEstoqueRepository extends JpaRepository<MovimentacaoEstoque, Long> {

    @Query("select m from MovimentacaoEstoque m join fetch m.material "
            + "where (m.canteiroOrigemId in :canteiroIds or m.canteiroDestinoId in :canteiroIds) "
            + "and m.tipo in :tipos and m.dataMovimentacao >= :inicio and m.dataMovimentacao < :fim "
            + "order by m.dataMovimentacao desc, m.id desc")
    List<MovimentacaoEstoque> listar(@Param("canteiroIds") Collection<Long> canteiroIds,
                                     @Param("tipos") Collection<TipoMovimentacao> tipos,
                                     @Param("inicio") LocalDateTime inicio,
                                     @Param("fim") LocalDateTime fim,
                                     Pageable pagina);

    /** Consumo (SAIDA_CONSUMO) por material e canteiro desde uma data; base da média semanal. */
    @Query("select new br.com.obras.estoque.repository.ConsumoAgregado(m.material.id, m.canteiroOrigemId, "
            + "sum(m.quantidade)) from MovimentacaoEstoque m "
            + "where m.tipo = br.com.obras.estoque.domain.TipoMovimentacao.SAIDA_CONSUMO "
            + "and m.canteiroOrigemId in :canteiroIds and m.dataMovimentacao >= :desde "
            + "group by m.material.id, m.canteiroOrigemId")
    List<ConsumoAgregado> somarConsumo(@Param("canteiroIds") Collection<Long> canteiroIds,
                                       @Param("desde") LocalDateTime desde);
}
