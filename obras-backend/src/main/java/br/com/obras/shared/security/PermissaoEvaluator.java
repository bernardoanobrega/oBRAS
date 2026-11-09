package br.com.obras.shared.security;

import org.springframework.stereotype.Component;

/**
 * Uso nos controllers:
 * {@code @PreAuthorize("@perm.pode(#obraId, 'ESTOQUE', 'CRIAR')")}
 * {@code @PreAuthorize("@perm.podeGlobal('USUARIOS', 'VISUALIZAR')")}
 */
@Component("perm")
public class PermissaoEvaluator {
    private final AutorizacaoPort autorizacao;

    public PermissaoEvaluator(AutorizacaoPort autorizacao) { this.autorizacao = autorizacao; }

    public boolean pode(Long obraId, String modulo, String acao) {
        return autorizacao.possuiPermissao(CurrentUser.id(), obraId,
                ModuloSistema.valueOf(modulo), AcaoPermissao.valueOf(acao));
    }

    public boolean podeGlobal(String modulo, String acao) {
        return pode(null, modulo, acao);
    }
}
