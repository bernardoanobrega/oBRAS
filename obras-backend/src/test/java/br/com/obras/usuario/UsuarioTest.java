package br.com.obras.usuario;

import br.com.obras.shared.security.AcaoPermissao;
import br.com.obras.shared.security.ModuloSistema;
import br.com.obras.usuario.domain.Permissao;
import br.com.obras.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class UsuarioTest {
    @Test
    void cadastraAtivoEDesativaSemApagar() {
        Usuario u = Usuario.cadastrar("Ana", "ana", "hash");
        assertTrue(u.estaAtivo());
        u.desativar();
        assertFalse(u.estaAtivo());
    }

    @Test
    void definirPermissaoFazUpsert() {
        Usuario u = Usuario.cadastrar("Ana", "ana", "hash");
        Permissao p = new Permissao(ModuloSistema.ESTOQUE, AcaoPermissao.CRIAR);
        u.definirPermissao(p, true);
        u.definirPermissao(p, false);
        assertEquals(1, u.getPermissoes().size());
        assertFalse(u.getPermissoes().get(0).isConcedida());
    }
}
