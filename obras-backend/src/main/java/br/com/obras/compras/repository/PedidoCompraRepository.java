package br.com.obras.compras.repository;

import br.com.obras.compras.domain.PedidoCompra;
import br.com.obras.compras.domain.StatusPedido;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PedidoCompraRepository extends JpaRepository<PedidoCompra, Long> {
    Optional<PedidoCompra> findByIdAndObraId(Long id, Long obraId);

    @EntityGraph(attributePaths = "fornecedor")
    List<PedidoCompra> findByObraIdOrderByIdDesc(Long obraId);

    @EntityGraph(attributePaths = "fornecedor")
    List<PedidoCompra> findByObraIdAndStatusOrderByIdDesc(Long obraId, StatusPedido status);

    @EntityGraph(attributePaths = "fornecedor")
    List<PedidoCompra> findByFornecedorIdOrderByIdDesc(Long fornecedorId);

    /** Trava a linha para impedir entrada de estoque duplicada do mesmo pedido. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PedidoCompra p where p.id = :id")
    Optional<PedidoCompra> buscarParaAtualizar(@Param("id") Long id);
}
