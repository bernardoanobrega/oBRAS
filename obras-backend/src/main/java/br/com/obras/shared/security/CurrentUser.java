package br.com.obras.shared.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUser {
    private CurrentUser() {}

    public static UsuarioAutenticado get() {
        Authentication a = SecurityContextHolder.getContext().getAuthentication();
        if (a == null || !(a.getPrincipal() instanceof UsuarioAutenticado u)) {
            throw new IllegalStateException("Nenhum usuário autenticado no contexto");
        }
        return u;
    }

    public static Long id() { return get().id(); }
}
