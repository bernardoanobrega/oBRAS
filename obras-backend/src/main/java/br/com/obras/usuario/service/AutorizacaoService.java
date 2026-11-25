package br.com.obras.usuario.service;

import br.com.obras.shared.security.AcaoPermissao;
import br.com.obras.shared.security.AutorizacaoPort;
import br.com.obras.shared.security.ContextoAlocacaoPort;
import br.com.obras.shared.security.ModuloSistema;
import br.com.obras.shared.security.PermissaoChave;
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
        Usuario usuario = usuarios.findById(usuarioId).orElse(null);
        if (usuario == null || !usuario.estaAtivo()) return Set.of();
        List<Long> cargoIds = obraId == null ? List.of() : contexto.cargoIdsAtivos(usuarioId, obraId);
        return calcular(usuario, cargoIds);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean possuiPermissao(Long usuarioId, Long obraId, ModuloSistema modulo, AcaoPermissao acao) {
        return permissoesEfetivas(usuarioId, obraId).contains(new PermissaoChave(modulo, acao));
    }

    @Override
    @Transactional(readOnly = true)
    public boolean possuiPermissaoEmQualquerObra(Long usuarioId, ModuloSistema modulo, AcaoPermissao acao) {
        Usuario usuario = usuarios.findById(usuarioId).orElse(null);
        if (usuario == null || !usuario.estaAtivo()) return false;
        List<Long> cargoIds = contexto.cargoIdsAtivosEmQualquerObra(usuarioId);
        return calcular(usuario, cargoIds).contains(new PermissaoChave(modulo, acao));
    }

    private Set<PermissaoChave> calcular(Usuario usuario, List<Long> cargoIds) {
        Set<PermissaoChave> efetivas = new HashSet<>();
        if (!cargoIds.isEmpty()) {
            cargos.findComPermissoes(cargoIds).forEach(c ->
                    c.getPermissoes().forEach(p -> efetivas.add(new PermissaoChave(p.getModulo(), p.getAcao()))));
        }
        for (PermissaoUsuario pu : usuario.getPermissoes()) {
            var chave = new PermissaoChave(pu.getPermissao().getModulo(), pu.getPermissao().getAcao());
            if (pu.isConcedida()) efetivas.add(chave); else efetivas.remove(chave);
        }
        return efetivas;
    }
}
