package br.com.obras.usuario.repository;

import br.com.obras.usuario.domain.Cargo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface CargoRepository extends JpaRepository<Cargo, Long> {
    @Query("select distinct c from Cargo c left join fetch c.permissoes where c.id in :ids")
    List<Cargo> findComPermissoes(Collection<Long> ids);
}
