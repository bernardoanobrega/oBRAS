package br.com.obras.shared.port;

import java.util.Optional;

public interface UsuarioConsultaPort {

    boolean estaAtivo(Long usuarioId);

    default Optional<String> nome(Long usuarioId) {
        return Optional.empty();
    }
}
