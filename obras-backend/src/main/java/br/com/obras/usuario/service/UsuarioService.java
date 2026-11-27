package br.com.obras.usuario.service;

import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.shared.port.UsuarioConsultaPort;
import br.com.obras.usuario.domain.Permissao;
import br.com.obras.usuario.domain.Usuario;
import br.com.obras.usuario.dto.AtualizarUsuarioDto;
import br.com.obras.usuario.dto.DefinirPermissaoDto;
import br.com.obras.usuario.dto.NovoUsuarioDto;
import br.com.obras.usuario.dto.PermissaoIndividualDto;
import br.com.obras.usuario.dto.UsuarioDto;
import br.com.obras.usuario.repository.PermissaoRepository;
import br.com.obras.usuario.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UsuarioService implements UsuarioConsultaPort {
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

    @Override
    @Transactional(readOnly = true)
    public boolean estaAtivo(Long usuarioId) {
        return usuarios.findById(usuarioId)
                .map(Usuario::estaAtivo)
                .orElse(false);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<String> nome(Long usuarioId) {
        return usuarios.findById(usuarioId).map(Usuario::getNome);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Usuario> buscarEntidade(Long usuarioId) {
        return usuarios.findById(usuarioId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Usuario> listarAtivos() {
        return usuarios.findAll().stream().filter(Usuario::estaAtivo).toList();
    }

    private Usuario obter(Long id) {
        return usuarios.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Usuário", id));
    }

    @Transactional(readOnly = true)
    public List<PermissaoIndividualDto> listarPermissoesIndividuais(Long id) {
        return obter(id).getPermissoes().stream()
                .map(pu -> new PermissaoIndividualDto(pu.getPermissao().getModulo(),
                        pu.getPermissao().getAcao(), pu.isConcedida()))
                .toList();
    }
}
