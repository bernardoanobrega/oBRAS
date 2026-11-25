package br.com.obras.documento;

import br.com.obras.documento.dto.DocumentosDto.*;
import br.com.obras.documento.service.DocumentoService;
import br.com.obras.shared.exception.*;
import br.com.obras.shared.security.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import java.nio.file.*;
import java.util.List;
import java.util.concurrent.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/** Executar apenas contra um banco descartável: as tabelas são limpas a cada teste. */
@AutoConfigureMockMvc
@SpringBootTest(properties={"app.jwt.secret=test-only-key-with-at-least-thirty-two-characters", "app.bootstrap.admin.enabled=false"})
@EnabledIfEnvironmentVariable(named="OBRAS_TEST_DB_URL",matches=".+")
class DocumentoIntegrationTest {
    static final Path STORAGE;
    static { try { STORAGE = Files.createTempDirectory("obras-test-storage-"); } catch (Exception e) { throw new ExceptionInInitializerError(e); } }
    @DynamicPropertySource static void properties(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", () -> System.getenv("OBRAS_TEST_DB_URL"));
        r.add("spring.datasource.username", () -> "obras");
        r.add("spring.datasource.password", () -> "obras");
        r.add("app.storage.base-path", STORAGE::toString);
    }
    @Autowired MockMvc mvc;
    @Autowired DocumentoService service;
    @Autowired JdbcTemplate jdbc;
    @MockBean AutorizacaoPort autorizacao;
    Long usuario, obra, outraObra;
    void autenticar() {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                new UsuarioAutenticado(usuario,"teste"),null,List.of()));
    }
    @BeforeEach void preparar() {
        jdbc.execute("TRUNCATE historico_documental_prestador,versao_documento,documento,prestador_servico,obra,usuario CASCADE");
        usuario = jdbc.queryForObject("INSERT INTO usuario(nome,situacao,login,senha_hash) VALUES ('Teste','ATIVO','teste','hash') RETURNING id",Long.class);
        obra = criarObra("Obra A"); outraObra = criarObra("Obra B");
        when(autorizacao.possuiPermissao(anyLong(),nullable(Long.class),any(),any())).thenReturn(true);
        autenticar();
    }
    Long criarObra(String nome) {
        return jdbc.queryForObject("INSERT INTO obra(nome,tipologia,endereco,inicio_previsto,fim_previsto,status,responsavel_id) VALUES (?,'RESIDENCIAL','Rua teste',CURRENT_DATE,CURRENT_DATE,'PLANEJADA',?) RETURNING id",Long.class,nome,usuario);
    }
    @AfterEach void limparContexto() { SecurityContextHolder.clearContext(); }
    MockMultipartFile pdf() { return new MockMultipartFile("arquivo","projeto.pdf","application/pdf","%PDF-1.4\nconteudo de teste".getBytes()); }
    Documento criar() { return service.criar(obra,"EST-01","Estrutura","PROJETO_EXECUTIVO","ESTRUTURAL",null,"Primeira revisão",pdf()); }
    @Test void apiExigeAutenticacaoEValidaPartesDoUpload() throws Exception {
        SecurityContextHolder.clearContext();
        mvc.perform(get("/api/documentos/obras")).andExpect(status().isUnauthorized());
        autenticar();
        var auth = SecurityContextHolder.getContext().getAuthentication();
        mvc.perform(multipart("/api/documentos/obras/" + obra).with(authentication(auth))
                .param("codigo","CT-TESTE").param("titulo","Contrato").param("categoria","CONTRATO")
                .param("descricao","Inicial"))
                .andExpect(status().isBadRequest());
        mvc.perform(multipart("/api/documentos/obras/" + obra).file(pdf()).with(authentication(auth))
                .param("codigo","CT-TESTE").param("titulo","Contrato").param("categoria","CONTRATO")
                .param("descricao","Inicial"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.versoes[0].numero").value(1))
                .andExpect(jsonPath("$.versoes[0].localizador").doesNotExist());
    }
    @Test void vinculoPosteriorPreservaIdentidadeEHistorico() {
        var p = service.salvarPrestador(null,new DadosPrestador("Prestador","12345678901","contato"));
        var d = service.criar(obra,"CT-SEM","Contrato","CONTRATO","GERAL",null,"Inicial",pdf());
        assertThat(service.vincular(obra,d.id(),p.id()).prestadorId()).isEqualTo(p.id());
        service.vincular(obra,d.id(),p.id());
        assertThat(service.historico(p.id()).eventos()).hasSize(1);
        var projeto = criar();
        assertThatThrownBy(() -> service.vincular(obra,projeto.id(),p.id())).isInstanceOf(RegraNegocioException.class);
    }
    @Test void preservaVersoesEArquivoOriginal() throws Exception {
        var d = criar();
        var atualizado = service.revisar(obra,d.id(),"Ajuste das vigas",pdf());
        assertThat(atualizado.versoes()).extracting(Versao::numero).containsExactly(2,1);
        assertThat(service.arquivo(obra,d.id(),1).conteudo().getContentAsByteArray()).isEqualTo(pdf().getBytes());
        assertThat(service.listar(obra)).hasSize(1);
    }
    @Test void rejeitaDocumentoDeOutraObraMesmoComPermissaoNaRota() {
        var d = criar();
        assertThatThrownBy(() -> service.arquivo(outraObra,d.id(),1)).isInstanceOf(RecursoNaoEncontradoException.class);
        assertThatThrownBy(() -> service.revisar(outraObra,d.id(),"Tentativa",pdf())).isInstanceOf(RecursoNaoEncontradoException.class);
    }
    @Test void bloqueiaAcessoSemPermissao() {
        var d = criar();
        when(autorizacao.possuiPermissao(eq(usuario),eq(obra),any(),any())).thenReturn(false);
        assertThatThrownBy(() -> service.listar(obra)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> service.arquivo(obra,d.id(),1)).isInstanceOf(AccessDeniedException.class);
        assertThat(service.obras()).extracting(o -> o.id()).doesNotContain(obra);
    }
    @Test void desativacaoPreservaHistoricoEBloqueiaNovoVinculo() {
        var p = service.salvarPrestador(null,new DadosPrestador("Prestador","123.456.789-01","contato"));
        var d = service.criar(obra,"CT-01","Contrato","CONTRATO","GERAL",p.id(),"Inicial",pdf());
        service.desativar(p.id());
        service.revisar(obra,d.id(),"Aditivo",pdf());
        assertThat(service.historico(p.id()).eventos()).hasSize(3);
        assertThatThrownBy(() -> service.criar(obra,"CT-02","Contrato","CONTRATO","GERAL",p.id(),"Inicial",pdf())).isInstanceOf(RegraNegocioException.class);
        when(autorizacao.possuiPermissao(eq(usuario),eq(obra),any(),any())).thenReturn(false);
        assertThat(service.historico(p.id()).eventos()).isEmpty();
        assertThat(service.historico(p.id()).contratos()).isEmpty();
    }
    @Test void normalizaCpfEImpedeDuplicacao() {
        service.salvarPrestador(null,new DadosPrestador("Um","123.456.789-01","contato"));
        assertThatThrownBy(() -> service.salvarPrestador(null,new DadosPrestador("Dois","12345678901","contato"))).isInstanceOf(DataIntegrityViolationException.class);
    }
    @Test void rollbackRemoveArquivoSemExcluirVersaoAnterior() throws Exception {
        criar();
        long antes;
        try (var arquivos = Files.walk(STORAGE)) { antes = arquivos.filter(Files::isRegularFile).count(); }
        assertThatThrownBy(this::criar).isInstanceOf(DataIntegrityViolationException.class);
        try (var arquivos = Files.walk(STORAGE)) { assertThat(arquivos.filter(Files::isRegularFile).count()).isEqualTo(antes); }
        assertThat(service.listar(obra).getFirst().versoes()).hasSize(1);
    }
    @Test void rejeitaArquivoFalsoVazioEAcimaDoLimite() {
        for (var arquivo : List.of(new MockMultipartFile("arquivo","fake.pdf","application/pdf","texto".getBytes()),
                new MockMultipartFile("arquivo","vazio.pdf","application/pdf",new byte[0]),
                new MockMultipartFile("arquivo","grande.pdf","application/pdf",new byte[10*1024*1024+1]))) {
            assertThatThrownBy(() -> service.criar(obra,"INVALIDO","Inválido","LICENCA","GERAL",null,"Inicial",arquivo)).isInstanceOf(RegraNegocioException.class);
        }
        assertThat(service.listar(obra)).isEmpty();
    }
    @Test void publicacoesConcorrentesRecebemNumerosDistintos() throws Exception {
        var d = criar();
        var inicio = new CountDownLatch(1);
        try (var executor = Executors.newFixedThreadPool(2)) {
            Callable<Documento> publicar = () -> { autenticar(); inicio.await(); try { return service.revisar(obra,d.id(),"Concorrente",pdf()); } finally { SecurityContextHolder.clearContext(); } };
            var a=executor.submit(publicar); var b=executor.submit(publicar); inicio.countDown();
            a.get(15,TimeUnit.SECONDS); b.get(15,TimeUnit.SECONDS);
        }
        assertThat(service.buscar(obra,d.id()).versoes()).extracting(Versao::numero).containsExactly(3,2,1);
    }
}
