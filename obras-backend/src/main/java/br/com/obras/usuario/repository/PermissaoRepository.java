package br.com.obras.usuario.repository;

import br.com.obras.shared.security.AcaoPermissao;
import br.com.obras.shared.security.ModuloSistema;
import br.com.obras.usuario.domain.Permissao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PermissaoRepository extends JpaRepository<Permissao, Long> {
    Optional<Permissao> findByModuloAndAcao(ModuloSistema modulo, AcaoPermissao acao);
}
