package br.com.obras.usuario.repository;

import br.com.obras.usuario.domain.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByCredencialLogin(String login);
    boolean existsByCredencialLogin(String login);
}
