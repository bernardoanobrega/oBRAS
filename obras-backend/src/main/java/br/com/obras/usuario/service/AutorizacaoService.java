package br.com.obras.usuario.service;

import br.com.obras.shared.security.*;
import br.com.obras.usuario.domain.PermissaoUsuario;
import br.com.obras.usuario.domain.Usuario;
import br.com.obras.usuario.repository.CargoRepository;
import br.com.obras.usuario.repository.UsuarioRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Permissões efetivas = permissões dos cargos da alocação ativa na obra
 * + exceções individuais (concedida=true adiciona, false remove).
 * Administrador não tem alocação: só permissões individuais.
 */
@Service
public class AutorizacaoService implements AutorizacaoPort {
    private final UsuarioRepository usuarios;
    private final CargoRepository cargos;
    private final ContextoAlocacaoPort contexto;

    public AutorizacaoService(UsuarioRepository usuarios, CargoRepository cargos, ContextoAlocacaoPort contexto) {
        this.usuarios = usuarios;
        this.cargos = cargos;
        this.contexto = contexto;
    }

    @Override
    @Transactional(readOnly = true)
    public Set<PermissaoChave> permissoesEfetivas(Long usuarioId, Long obraId) {
        Usuario u = usuarios.findById(usuarioId).orElse(null);
        if (u == null || !u.estaAtivo()) return Set.of();

        Set<PermissaoChave> efetivas = new HashSet<>();
        if (obraId != null) {
            List<Long> cargoIds = contexto.cargoIdsAtivos(usuarioId, obraId);
            if (!cargoIds.isEmpty()) {
                cargos.findComPermissoes(cargoIds).forEach(c ->
                        c.getPermissoes().forEach(p -> efetivas.add(new PermissaoChave(p.getModulo(), p.getAcao()))));
            }
        }
        for (PermissaoUsuario pu : u.getPermissoes()) {
            var chave = new PermissaoChave(pu.getPermissao().getModulo(), pu.getPermissao().getAcao());
            if (pu.isConcedida()) efetivas.add(chave); else efetivas.remove(chave);
        }
        return efetivas;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean possuiPermissao(Long usuarioId, Long obraId, ModuloSistema modulo, AcaoPermissao acao) {
        return permissoesEfetivas(usuarioId, obraId).contains(new PermissaoChave(modulo, acao));
    }
}
