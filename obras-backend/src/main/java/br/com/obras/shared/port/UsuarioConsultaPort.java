package br.com.obras.shared.port;

import br.com.obras.usuario.domain.Usuario;

import java.util.List;
import java.util.Optional;

public interface UsuarioConsultaPort {

    boolean estaAtivo(Long usuarioId);

    default Optional<String> nome(Long usuarioId) {
        return Optional.empty();
    }

    default Optional<Usuario> buscarEntidade(Long usuarioId) {
        return Optional.empty();
    }

    default List<Usuario> listarAtivos() {
        return List.of();
    }
}
