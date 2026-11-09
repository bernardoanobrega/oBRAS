package br.com.obras.shared;

import br.com.obras.shared.security.JwtService;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {
    private static final String SECRET = "0123456789-0123456789-0123456789-ab";

    @Test
    void geraEValidaToken() {
        var jwt = new JwtService(SECRET, 5);
        var u = jwt.validar(jwt.gerar(42L, "ana")).orElseThrow();
        assertEquals(42L, u.id());
        assertEquals("ana", u.login());
    }

    @Test
    void rejeitaTokenAdulterado() {
        var jwt = new JwtService(SECRET, 5);
        assertTrue(jwt.validar(jwt.gerar(1L, "x") + "a").isEmpty());
    }

    @Test
    void rejeitaTokenDeOutraChave() {
        var a = new JwtService(SECRET, 5);
        var b = new JwtService("outra-chave-outra-chave-outra-chave-123", 5);
        assertTrue(b.validar(a.gerar(1L, "x")).isEmpty());
    }

    @Test
    void rejeitaSegredoCurto() {
        assertThrows(IllegalStateException.class, () -> new JwtService("curto", 5));
    }
}
