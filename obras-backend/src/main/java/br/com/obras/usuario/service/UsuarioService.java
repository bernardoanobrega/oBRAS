package br.com.obras.usuario.service;

import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.usuario.domain.Permissao;
import br.com.obras.usuario.domain.Usuario;
import br.com.obras.usuario.dto.*;
import br.com.obras.usuario.repository.PermissaoRepository;
import br.com.obras.usuario.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final PermissaoRepository permissoes;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository usuarios, PermissaoRepository permissoes, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.permissoes = permissoes;
        this.encoder = encoder;
    }

    @Transactional
    public UsuarioDto criar(NovoUsuarioDto dto) {
        if (usuarios.existsByCredencialLogin(dto.login())) {
            throw new RegraNegocioException("Login já utilizado");
        }
        Usuario u = Usuario.cadastrar(dto.nome(), dto.login(), encoder.encode(dto.senha()));
        return UsuarioDto.de(usuarios.save(u));
    }

    @Transactional(readOnly = true)
    public List<UsuarioDto> listar() {
        return usuarios.findAll().stream().map(UsuarioDto::de).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioDto buscar(Long id) { return UsuarioDto.de(obter(id)); }

    @Transactional
    public UsuarioDto atualizar(Long id, AtualizarUsuarioDto dto) {
        Usuario u = obter(id);
        u.atualizarDados(dto.nome());
        return UsuarioDto.de(u);
    }

    @Transactional
    public UsuarioDto desativar(Long id) {
        Usuario u = obter(id);
        u.desativar();
        return UsuarioDto.de(u);
    }

    @Transactional
    public void definirPermissao(Long id, DefinirPermissaoDto dto) {
        Usuario u = obter(id);
        Permissao p = permissoes.findByModuloAndAcao(dto.modulo(), dto.acao())
                .orElseThrow(() -> new RecursoNaoEncontradoException("Permissão", dto.modulo() + ":" + dto.acao()));
        u.definirPermissao(p, dto.concedida());
    }

    private Usuario obter(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
    }
}
