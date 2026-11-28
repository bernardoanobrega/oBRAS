package br.com.obras.seguranca;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

class PreAuthorizeCoberturaTest {
    private static final Set<String> ROTAS_PUBLICAS = Set.of("AuthController#login");
    private static final Set<String> CONTROLADORES_AUDITADOS = Set.of(
            "ObraController", "AlocacaoObraController", "CargoController", "DiarioController",
            "PainelObraController", "DocumentoController", "PrestadorController");
    private static final Set<String> CONTROLADORES_FORA_MATRIZ = Set.of(
            "AuthController", "UsuarioController", "FornecedorController", "PedidoCompraController",
            "EstoqueController", "MaterialController", "OrcamentoController", "OrcamentoListagemController");
    private static final Set<String> METODOS_HTTP = Set.of("GET", "POST", "PUT", "PATCH", "DELETE");
    private static final String MATRIZ = "../docs/seguranca/permissoes-detalhe-obra.md";
    private static final List<String> TESTES_403 = List.of(
            "src/test/java/br/com/obras/obra/controller/ObraControllerTest.java",
            "src/test/java/br/com/obras/obra/controller/AlocacaoObraControllerTest.java",
            "src/test/java/br/com/obras/obra/controller/CargoControllerTest.java",
            "src/test/java/br/com/obras/obra/controller/PainelObraControllerTest.java",
            "src/test/java/br/com/obras/diario/controller/DiarioControllerTest.java",
            "src/test/java/br/com/obras/documento/controller/DocumentoControllerSecurityTest.java",
            "src/test/java/br/com/obras/documento/controller/PrestadorControllerSecurityTest.java");

    private record LinhaMatriz(String metodo, String rota, String modulo, String acao, String escopo) {
        String chave() {
            return metodo + " " + rota;
        }
    }

    @Test
    void endpointSemPreAuthorizeFalhaNoTeste() {
        List<String> semProtecao = controllers().stream()
                .flatMap(classe -> Arrays.stream(classe.getDeclaredMethods())
                        .filter(metodo -> AnnotatedElementUtils.findMergedAnnotation(metodo, RequestMapping.class) != null)
                        .filter(metodo -> AnnotatedElementUtils.findMergedAnnotation(metodo, PreAuthorize.class) == null)
                        .map(metodo -> classe.getSimpleName() + "#" + metodo.getName()))
                .filter(chave -> !ROTAS_PUBLICAS.contains(chave))
                .sorted()
                .toList();

        assertTrue(semProtecao.isEmpty(), "Rotas sem @PreAuthorize: " + semProtecao);
    }

    @Test
    void matrizCobreTodosOsEndpoints() throws Exception {
        Set<String> matriz = linhasDaMatriz().stream()
                .map(LinhaMatriz::chave)
                .collect(Collectors.toCollection(TreeSet::new));
        Set<String> reais = expressoesPorRota().keySet().stream()
                .collect(Collectors.toCollection(TreeSet::new));

        assertFalse(matriz.isEmpty());
        assertEquals(matriz, reais, "Divergência matriz ↔ controllers");
    }

    @Test
    void todoControllerFicaAuditadoOuExplicitamenteForaDaMatriz() {
        Set<String> reais = controllers().stream()
                .map(Class::getSimpleName)
                .collect(Collectors.toCollection(TreeSet::new));
        Set<String> naoClassificados = reais.stream()
                .filter(nome -> !CONTROLADORES_AUDITADOS.contains(nome))
                .filter(nome -> !CONTROLADORES_FORA_MATRIZ.contains(nome))
                .collect(Collectors.toCollection(TreeSet::new));
        Set<String> auditadosInexistentes = CONTROLADORES_AUDITADOS.stream()
                .filter(nome -> !reais.contains(nome))
                .collect(Collectors.toCollection(TreeSet::new));

        assertAll(
                () -> assertTrue(naoClassificados.isEmpty(), "Controllers sem classificação: " + naoClassificados),
                () -> assertTrue(auditadosInexistentes.isEmpty(), "Auditados sem controller: " + auditadosInexistentes)
        );
    }

    @Test
    void matrizAcaoBateComPreAuthorize() throws Exception {
        Map<String, String> expressoes = expressoesPorRota();

        for (LinhaMatriz linha : linhasDaMatriz()) {
            String expressao = expressoes.get(linha.chave());
            assertNotNull(expressao, "Rota da matriz sem controller: " + linha.chave());
            if (linha.chave().equals("GET /api/obras")) {
                assertTrue(expressao.contains("isAuthenticated()"), "GET /api/obras deve exigir autenticação");
                continue;
            }

            String alvo = "'" + linha.modulo() + "','" + linha.acao() + "'";
            Set<String> pares = new HashSet<>();
            Matcher matcher = Pattern.compile("'[^']+'\\s*,\\s*'[^']+'").matcher(expressao);
            while (matcher.find()) pares.add(matcher.group().replaceAll("\\s+", ""));
            assertEquals(Set.of(alvo), pares,
                    "Par módulo/ação divergente em " + linha.chave() + ": " + expressao);
            assertEscopo(linha, expressao);
        }
    }

    @Test
    void cadaAcaoTemTeste403() throws Exception {
        String corpus = "";
        for (String caminho : TESTES_403) {
            String conteudo = Files.readString(Path.of(caminho));
            assertTrue(conteudo.contains("status().isForbidden()"), "Sem teste 403: " + caminho);
            assertTrue(conteudo.contains("status().isUnauthorized()"), "Sem teste 401: " + caminho);
            corpus = corpus + "\n" + conteudo;
        }

        for (String acao : List.of("VISUALIZAR", "CRIAR", "EDITAR", "EXCLUIR")) {
            assertTrue(corpus.contains("\"" + acao + "\""), "Ação sem teste 403: " + acao);
        }
    }

    @Test
    void usuarioComVisualizarNaoCria() {
        assertEqualsAcao("listar", "VISUALIZAR");
        assertEqualsAcao("criar", "CRIAR");
        assertFalse(acao("listar").equals(acao("criar")));
    }

    @Test
    void usuarioComCriarNaoExclui() {
        assertEqualsAcao("criar", "CRIAR");
        assertEqualsAcao("excluir", "EXCLUIR");
        assertFalse(acao("criar").equals(acao("excluir")));
    }

    @Test
    void permissaoGlobalNaoSubstituiEscopoDeObraSemRegra() {
        Class<?> diario = controllers().stream()
                .filter(classe -> classe.getSimpleName().equals("DiarioController"))
                .findFirst()
                .orElseThrow();

        List<Method> metodos = Arrays.stream(diario.getDeclaredMethods())
                .filter(metodo -> AnnotatedElementUtils.findMergedAnnotation(metodo, RequestMapping.class) != null)
                .toList();

        assertTrue(metodos.stream()
                .map(metodo -> AnnotatedElementUtils.findMergedAnnotation(metodo, PreAuthorize.class))
                .filter(java.util.Objects::nonNull)
                .allMatch(pre -> pre.value().contains("@perm.pode(")));
    }

    private void assertEscopo(LinhaMatriz linha, String expressao) {
        String escopo = linha.escopo();
        if (escopo.equals("Obra")) {
            assertTrue(Pattern.compile("@perm\\.pode\\(\\s*#obraId\\s*,").matcher(expressao).find(),
                    "Escopo de obra divergente em " + linha.chave() + ": " + expressao);
            assertFalse(expressao.contains("@perm.podeGlobal("),
                    "podeGlobal não substitui escopo de obra em " + linha.chave());
        } else if (escopo.startsWith("Global ou alguma obra")) {
            assertTrue(expressao.contains("@perm.podeGlobal(")
                            && expressao.contains("@perm.podeEmAlgumaObra("),
                    "Escopo global/alguma obra divergente em " + linha.chave() + ": " + expressao);
        } else if (escopo.startsWith("Global")) {
            assertTrue(expressao.contains("@perm.podeGlobal("),
                    "Escopo global divergente em " + linha.chave() + ": " + expressao);
        } else if (escopo.startsWith("Autenticado")) {
            assertTrue(expressao.contains("isAuthenticated()"),
                    "Escopo autenticado divergente em " + linha.chave() + ": " + expressao);
        } else {
            fail("Escopo não reconhecido em " + linha.chave() + ": " + escopo);
        }
    }

    private List<LinhaMatriz> linhasDaMatriz() throws Exception {
        return Files.readAllLines(Path.of(MATRIZ)).stream()
                .filter(linha -> linha.startsWith("|"))
                .map(linha -> Arrays.stream(linha.split("\\|")).map(String::trim).toList())
                .filter(colunas -> colunas.size() >= 6)
                .filter(colunas -> METODOS_HTTP.contains(colunas.get(1)))
                .map(colunas -> new LinhaMatriz(
                        colunas.get(1),
                        colunas.get(2).replace("`", "").replaceAll("\\{[^}]+\\}", "{}"),
                        colunas.get(3),
                        colunas.get(4),
                        colunas.get(5)))
                .toList();
    }

    private Map<String, String> expressoesPorRota() {
        Map<String, String> expressoes = new TreeMap<>();
        controllers().stream()
                .filter(classe -> CONTROLADORES_AUDITADOS.contains(classe.getSimpleName()))
                .forEach(classe -> {
                    RequestMapping classeMapping = AnnotatedElementUtils.findMergedAnnotation(classe, RequestMapping.class);
                    String prefixo = classeMapping == null ? "" : primeira(classeMapping.path());
                    Arrays.stream(classe.getDeclaredMethods())
                            .map(metodo -> Map.entry(
                                    metodo,
                                    AnnotatedElementUtils.findMergedAnnotation(metodo, RequestMapping.class)))
                            .filter(entry -> entry.getValue() != null)
                            .forEach(entry -> {
                                Method metodo = entry.getKey();
                                RequestMapping mapping = entry.getValue();
                                PreAuthorize pre = AnnotatedElementUtils.findMergedAnnotation(metodo, PreAuthorize.class);
                                assertFalse(mapping.method().length == 0,
                                        "Rota sem método HTTP: " + classe.getSimpleName() + "#" + metodo.getName());
                                String[] caminhos = mapping.path().length == 0 ? new String[]{""} : mapping.path();
                                for (RequestMethod metodoHttp : mapping.method()) {
                                    for (String caminho : caminhos) {
                                        String chave = (metodoHttp.name() + " " + prefixo + caminho)
                                                .replaceAll("\\{[^}]+\\}", "{}");
                                        expressoes.put(chave, pre == null ? "" : pre.value());
                                    }
                                }
                            });
                });
        return expressoes;
    }

    private void assertEqualsAcao(String metodo, String acao) {
        assertTrue(acao(metodo).contains("'" + acao + "'"));
    }

    private String acao(String metodo) {
        Class<?> diario = controllers().stream()
                .filter(classe -> classe.getSimpleName().equals("DiarioController"))
                .findFirst()
                .orElseThrow();
        return Arrays.stream(diario.getDeclaredMethods())
                .filter(candidato -> candidato.getName().equals(metodo))
                .map(candidato -> AnnotatedElementUtils.findMergedAnnotation(candidato, PreAuthorize.class))
                .filter(java.util.Objects::nonNull)
                .map(PreAuthorize::value)
                .findFirst()
                .orElseThrow();
    }

    private String primeira(String[] valores) {
        return valores.length == 0 ? "" : valores[0];
    }

    private Set<Class<?>> controllers() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(RestController.class));
        return scanner.findCandidateComponents("br.com.obras").stream()
                .map(definicao -> {
                    try {
                        return Class.forName(definicao.getBeanClassName());
                    } catch (ClassNotFoundException erro) {
                        throw new IllegalStateException(erro);
                    }
                })
                .filter(this::ehClasseDeProducao)
                .collect(Collectors.toSet());
    }

    private boolean ehClasseDeProducao(Class<?> classe) {
        var recurso = classe.getResource(classe.getSimpleName() + ".class");
        return recurso != null && !recurso.getPath().contains("test-classes");
    }
}
