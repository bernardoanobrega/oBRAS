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
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cria o Administrador inicial e, em ambiente dev, garante dados mínimos (Obra #1, Canteiros #1 e #2,
 * Materiais, Saldos em Alerta/Crítico, Fornecedores e Pedidos no Kanban) para operação dos módulos.
 */
@Component
@ConditionalOnProperty(name = "app.bootstrap.admin.enabled", havingValue = "true")
public class AdminBootstrap implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final UsuarioRepository usuarios;
    private final PermissaoRepository permissoes;
    private final PasswordEncoder encoder;
    private final JdbcClient jdbc;
    private final String login;
    private final String senha;

    public AdminBootstrap(UsuarioRepository usuarios, PermissaoRepository permissoes,
                          PasswordEncoder encoder, JdbcClient jdbc,
                          @Value("${app.bootstrap.admin.login}") String login,
                          @Value("${app.bootstrap.admin.password}") String senha) {
        this.usuarios = usuarios;
        this.permissoes = permissoes;
        this.encoder = encoder;
        this.jdbc = jdbc;
        this.login = login;
        this.senha = senha;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        Usuario admin = garantirAdmin();
        if (admin == null) return;
        garantirObraECanteiros(admin.getId());
        garantirMateriaisESaldos();
        garantirFornecedoresEPedidos(admin.getId());
    }

    private Usuario garantirAdmin() {
        var existente = usuarios.findByCredencialLogin(login);
        if (existente.isPresent()) return existente.get();

        if (senha == null || senha.length() < 8) {
            log.warn("Bootstrap do admin ignorado: defina BOOTSTRAP_ADMIN_PASSWORD (mín. 8 caracteres)");
            return null;
        }
        Usuario admin = Usuario.cadastrar("Administrador", login, encoder.encode(senha));
        permissoes.findAll().forEach(p -> admin.definirPermissao(p, true));
        Usuario salvo = usuarios.saveAndFlush(admin);
        log.info("Administrador inicial criado: login='{}'", login);
        return salvo;
    }

    private void garantirObraECanteiros(Long adminId) {
        Long totalObras = jdbc.sql("SELECT count(*) FROM obra").query(Long.class).single();
        if (totalObras > 0) return;

        Long obraId = jdbc.sql("""
                INSERT INTO obra (nome, descricao, tipologia, endereco, inicio_previsto, fim_previsto, status, art, responsavel_id)
                VALUES ('Residencial Parque das Águas', 'Torres A e B - 18 pavimentos', 'RESIDENCIAL',
                        'Av. Beira Mar, 1200 - Aracaju/SE', CURRENT_DATE - 60, CURRENT_DATE + 300,
                        'EM_ANDAMENTO', 'ART-SE-2026-00981', :respId)
                RETURNING id
                """).param("respId", adminId).query(Long.class).single();

        jdbc.sql("INSERT INTO canteiro (obra_id, nome_canteiro) VALUES (:obraId, 'Almoxarifado Central - Bloco A')")
                .param("obraId", obraId).update();
        jdbc.sql("INSERT INTO canteiro (obra_id, nome_canteiro) VALUES (:obraId, 'Canteiro Avançado - Fundação')")
                .param("obraId", obraId).update();

        log.info("Obra inicial #{} e 2 canteiros criados para testes.", obraId);
    }

    private void garantirMateriaisESaldos() {
        Long totalMateriais = jdbc.sql("SELECT count(*) FROM material").query(Long.class).single();
        if (totalMateriais > 0) return;

        Long canteiro1 = jdbc.sql("SELECT id FROM canteiro ORDER BY id LIMIT 1").query(Long.class).single();

        record MatSeed(String nome, String desc, String un, double atual, double min) {}
        var seeds = java.util.List.of(
                new MatSeed("Cimento Portland CP-II 50kg", "Saco 50kg estrutural", "saco", 0.0, 120.0),
                new MatSeed("Vergalhão CA-50 10mm (3/8\")", "Barra 12 metros nervurada", "barra", 0.0, 80.0),
                new MatSeed("Tubo PVC Esgoto 100mm", "Vara 6 metros série normal", "un", 0.0, 40.0),
                new MatSeed("Areia Lavada Média", "Agregado miúdo para argamassa", "m3", 12.0, 25.0),
                new MatSeed("Brita Graduada nº 1", "Agregado graúdo para concreto", "m3", 18.0, 30.0),
                new MatSeed("Argamassa Colante AC-III", "Saco 20kg piso sobre piso", "saco", 35.0, 60.0)
        );

        for (MatSeed m : seeds) {
            Long matId = jdbc.sql("""
                    INSERT INTO material (nome, descricao, unidade_medida)
                    VALUES (:nome, :desc, :un) RETURNING id
                    """)
                    .param("nome", m.nome()).param("desc", m.desc()).param("un", m.un())
                    .query(Long.class).single();

            jdbc.sql("""
                    INSERT INTO saldo_estoque (material_id, canteiro_id, quantidade_atual, quantidade_minima)
                    VALUES (:matId, :cantId, :atual, :min)
                    """)
                    .param("matId", matId).param("cantId", canteiro1)
                    .param("atual", m.atual()).param("min", m.min())
                    .update();
        }
        log.info("6 materiais e saldos iniciais (Críticos e em Alerta) criados.");
    }

    private void garantirFornecedoresEPedidos(Long adminId) {
        Long totalFornecedores = jdbc.sql("SELECT count(*) FROM fornecedor").query(Long.class).single();
        if (totalFornecedores > 0) return;

        Long f1 = inserirFornecedor("Votorantim Cimentos N/NE", "10656452000180", "vendas@vcimentos.com.br", "(79) 3214-8800");
        Long f2 = inserirFornecedor("Gerdau Aços Longos S.A.", "33611500000119", "comercial.se@gerdau.com.br", "(79) 3249-1100");
        Long f3 = inserirFornecedor("Tigre Materiais Hidráulicos", "84684471000194", "engenharia@tigre.com.br", "(79) 3211-4500");
        Long f4 = inserirFornecedor("Mineradora São Cristóvão Britas", "41892310000155", "contato@scbritas.com.br", "(79) 99812-3400");

        Long obraId = jdbc.sql("SELECT id FROM obra ORDER BY id LIMIT 1").query(Long.class).single();
        var matIds = jdbc.sql("SELECT id FROM material ORDER BY id").query(Long.class).list();

        // 1. SOLICITADO (Urgente e Normal)
        criarPedidoSeed(obraId, adminId, null, "SOLICITADO", "ALTA", 5, matIds.get(0), 150, 38.50);
        criarPedidoSeed(obraId, adminId, null, "SOLICITADO", "MEDIA", 10, matIds.get(2), 50, 64.00);

        // 2. EM_COTACAO
        Long pCot1 = criarPedidoSeed(obraId, adminId, null, "EM_COTACAO", "MEDIA", 7, matIds.get(1), 100, 52.00);
        inserirCotacaoSeed(pCot1, f2, 5200.00, 4, false);
        inserirCotacaoSeed(pCot1, f1, 5450.00, 6, false);

        Long pCot2 = criarPedidoSeed(obraId, adminId, null, "EM_COTACAO", "ALTA", 3, matIds.get(3), 25, 110.00);
        inserirCotacaoSeed(pCot2, f4, 2750.00, 2, false);

        // 3. COMPRADO
        Long pComp = criarPedidoSeed(obraId, adminId, f3, "COMPRADO", "MEDIA", 6, matIds.get(2), 80, 62.00);
        inserirCotacaoSeed(pComp, f3, 4960.00, 5, true);

        // 4. EM_TRANSITO (pronto para ser recebido no módulo de Estoque!)
        Long pTrans1 = criarPedidoSeed(obraId, adminId, f1, "EM_TRANSITO", "ALTA", 1, matIds.get(0), 200, 37.90);
        inserirCotacaoSeed(pTrans1, f1, 7580.00, 3, true);

        Long pTrans2 = criarPedidoSeed(obraId, adminId, f2, "EM_TRANSITO", "ALTA", 2, matIds.get(1), 120, 51.50);
        inserirCotacaoSeed(pTrans2, f2, 6180.00, 4, true);

        // 5. ENTREGUE
        Long pEnt = criarPedidoSeed(obraId, adminId, f4, "ENTREGUE", "BAIXA", 0, matIds.get(4), 30, 125.00);
        inserirCotacaoSeed(pEnt, f4, 3750.00, 2, true);

        log.info("4 fornecedores e 8 pedidos de compra iniciais criados no Kanban.");
    }

    private Long inserirFornecedor(String nome, String doc, String email, String tel) {
        return jdbc.sql("""
                INSERT INTO fornecedor (nome_razao_social, cpf_cnpj, email, telefone, situacao)
                VALUES (:nome, :doc, :email, :tel, 'ATIVO') RETURNING id
                """)
                .param("nome", nome).param("doc", doc).param("email", email).param("tel", tel)
                .query(Long.class).single();
    }

    private Long criarPedidoSeed(Long obraId, Long userId, Long fornId, String status,
                                 String prioridade, int diasPrev, Long matId, double qtd, double vlrUnit) {
        double total = qtd * vlrUnit;
        Long pedidoId = jdbc.sql("""
                INSERT INTO pedido_compra (obra_id, usuario_id, fornecedor_id, data_solicitacao, data_prevista, valor_total, status, prioridade)
                VALUES (:obraId, :userId, :fornId, CURRENT_DATE, CURRENT_DATE + :dias, :total, :status, :prio)
                RETURNING id
                """)
                .param("obraId", obraId).param("userId", userId).param("fornId", fornId)
                .param("dias", diasPrev).param("total", total)
                .param("status", status).param("prio", prioridade)
                .query(Long.class).single();

        jdbc.sql("""
                INSERT INTO item_pedido (pedido_compra_id, material_id, quantidade, valor_unitario)
                VALUES (:pedId, :matId, :qtd, :vlr)
                """)
                .param("pedId", pedidoId).param("matId", matId).param("qtd", qtd).param("vlr", vlrUnit)
                .update();

        jdbc.sql("""
                INSERT INTO historico_status_pedido (pedido_compra_id, status, data_hora, usuario_id)
                VALUES (:pedId, :status, now(), :userId)
                """)
                .param("pedId", pedidoId).param("status", status).param("userId", userId)
                .update();

        return pedidoId;
    }

    private void inserirCotacaoSeed(Long pedidoId, Long fornId, double valor, int prazo, boolean selecionada) {
        jdbc.sql("""
                INSERT INTO cotacao (pedido_compra_id, fornecedor_id, data_cotacao, valor_total_proposto, prazo_entrega_dias, selecionada)
                VALUES (:pedId, :fornId, now(), :valor, :prazo, :sel)
                """)
                .param("pedId", pedidoId).param("fornId", fornId)
                .param("valor", valor).param("prazo", prazo).param("sel", selecionada)
                .update();
    }
}