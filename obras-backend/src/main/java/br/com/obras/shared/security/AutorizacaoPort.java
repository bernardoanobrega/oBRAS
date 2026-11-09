package br.com.obras.shared.security;

import java.util.Set;

/** Implementada pelo módulo usuario. obraId == null => somente permissões individuais (módulos globais). */
public interface AutorizacaoPort {
    boolean possuiPermissao(Long usuarioId, Long obraId, ModuloSistema modulo, AcaoPermissao acao);
    Set<PermissaoChave> permissoesEfetivas(Long usuarioId, Long obraId);
}
