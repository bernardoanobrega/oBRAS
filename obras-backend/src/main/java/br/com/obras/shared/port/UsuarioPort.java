package br.com.obras.shared.port;

import java.util.Optional;

/** Acesso de outros módulos a usuários. O adapter fica no módulo usuario. */
public interface UsuarioPort {

    Optional<UsuarioResumo> buscarResumo(Long usuarioId);
}