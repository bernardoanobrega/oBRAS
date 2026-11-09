package br.com.obras.usuario.dto;

import br.com.obras.usuario.domain.Usuario;

import java.time.LocalDateTime;

public record UsuarioDto(Long id, String nome, String login, String situacao, LocalDateTime dataCadastro) {
    public static UsuarioDto de(Usuario u) {
        return new UsuarioDto(u.getId(), u.getNome(), u.getCredencial().getLogin(),
                u.getSituacao().name(), u.getDataCadastro());
    }
}
