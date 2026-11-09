package br.com.obras.usuario.service;

import br.com.obras.shared.exception.CredenciaisInvalidasException;
import br.com.obras.shared.security.JwtService;
import br.com.obras.usuario.domain.Usuario;
import br.com.obras.usuario.dto.LoginRequest;
import br.com.obras.usuario.dto.TokenResponse;
import br.com.obras.usuario.dto.UsuarioDto;
import br.com.obras.usuario.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AutenticacaoService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AutenticacaoService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.jwt = jwt;
    }

    /** RF-27. Mensagem única para login inexistente, senha errada ou usuário inativo. */
    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest req) {
        Usuario u = usuarios.findByCredencialLogin(req.login()).orElseThrow(CredenciaisInvalidasException::new);
        if (!u.estaAtivo() || !encoder.matches(req.senha(), u.getCredencial().getSenhaHash())) {
            throw new CredenciaisInvalidasException();
        }
        String token = jwt.gerar(u.getId(), u.getCredencial().getLogin());
        return new TokenResponse(token, "Bearer", jwt.getExpiracaoSegundos(), UsuarioDto.de(u));
    }
}
