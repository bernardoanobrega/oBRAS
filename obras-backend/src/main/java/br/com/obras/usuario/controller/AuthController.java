package br.com.obras.usuario.controller;

import br.com.obras.shared.security.AutorizacaoPort;
import br.com.obras.shared.security.CurrentUser;
import br.com.obras.shared.security.PermissaoChave;
import br.com.obras.usuario.dto.LoginRequest;
import br.com.obras.usuario.dto.TokenResponse;
import br.com.obras.usuario.service.AutenticacaoService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("/api")
public class AuthController {
    private final AutenticacaoService autenticacao;
    private final AutorizacaoPort autorizacao;

    public AuthController(AutenticacaoService autenticacao, AutorizacaoPort autorizacao) {
        this.autenticacao = autenticacao;
        this.autorizacao = autorizacao;
    }

    @PostMapping("/auth/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest req) {
        return autenticacao.login(req);
    }

    /** O front usa isto para montar menu e guards de rota. obraId opcional. */
    @GetMapping("/me/permissoes")
    public Set<PermissaoChave> minhasPermissoes(@RequestParam(required = false) Long obraId) {
        return autorizacao.permissoesEfetivas(CurrentUser.id(), obraId);
    }
}
