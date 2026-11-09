package br.com.obras.usuario.service;

import br.com.obras.usuario.domain.Usuario;
import br.com.obras.usuario.repository.PermissaoRepository;
import br.com.obras.usuario.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Cria o primeiro Administrador (todas as permissões individuais) se não houver nenhum usuário. */
@Component
@ConditionalOnProperty(name = "app.bootstrap.admin.enabled", havingValue = "true")
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UsuarioRepository usuarios;
    private final PermissaoRepository permissoes;
    private final PasswordEncoder encoder;
    private final String login;
    private final String senha;

    public AdminBootstrap(UsuarioRepository usuarios, PermissaoRepository permissoes, PasswordEncoder encoder,
                          @Value("${app.bootstrap.admin.login}") String login,
                          @Value("${app.bootstrap.admin.password}") String senha) {
        this.usuarios = usuarios;
        this.permissoes = permissoes;
        this.encoder = encoder;
        this.login = login;
        this.senha = senha;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (usuarios.count() > 0) return;
        if (senha == null || senha.length() < 8) {
            log.warn("Bootstrap do admin ignorado: defina BOOTSTRAP_ADMIN_PASSWORD (mín. 8 caracteres)");
            return;
        }
        Usuario admin = Usuario.cadastrar("Administrador", login, encoder.encode(senha));
        permissoes.findAll().forEach(p -> admin.definirPermissao(p, true));
        usuarios.save(admin);
        log.info("Administrador inicial criado: login='{}'", login);
    }
}
