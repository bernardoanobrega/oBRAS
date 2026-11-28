package br.com.obras.usuario.service;

import br.com.obras.shared.port.UsuarioPort;
import br.com.obras.shared.port.UsuarioResumo;
import br.com.obras.usuario.repository.UsuarioRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Expõe dados mínimos de usuário a outros módulos, sem revelar credenciais nem permissões. */
@Component
@RequiredArgsConstructor
public class UsuarioAdapter implements UsuarioPort {

    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<UsuarioResumo> buscarResumo(Long usuarioId) {
        return usuarioRepository.findById(usuarioId)
                .map(usuario -> new UsuarioResumo(usuario.getId(), usuario.getNome(), usuario.estaAtivo()));
    }
}