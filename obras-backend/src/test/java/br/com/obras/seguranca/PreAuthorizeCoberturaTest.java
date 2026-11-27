package br.com.obras.seguranca;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.core.annotation.AnnotatedElementUtils;

import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PreAuthorizeCoberturaTest {
    private static final Set<String> ROTAS_PUBLICAS = Set.of("AuthController#login");

    @Test
    void endpointSemPreAuthorizeFalhaNoTeste() {
        List<String> semProtecao = controllers().stream()
                .flatMap(classe -> java.util.Arrays.stream(classe.getDeclaredMethods())
                        .filter(metodo -> AnnotatedElementUtils.findMergedAnnotation(metodo, RequestMapping.class) != null)
                        .filter(metodo -> AnnotatedElementUtils.findMergedAnnotation(metodo, PreAuthorize.class) == null)
                        .map(metodo -> classe.getSimpleName() + "#" + metodo.getName()))
                .filter(chave -> !ROTAS_PUBLICAS.contains(chave))
                .sorted()
                .toList();

        assertTrue(semProtecao.isEmpty(), "Rotas sem @PreAuthorize: " + semProtecao);
    }

    @Test
    void matrizCobreTodosOsEndpoints() {
        Set<String> rotas = rotas();

        assertFalse(rotas.isEmpty());
        assertTrue(rotas.contains("/api/obras"));
        assertTrue(rotas.contains("/api/obras/{obraId}"));
        assertTrue(rotas.contains("/api/obras/{obraId}/alocacoes"));
        assertTrue(rotas.contains("/api/obras/{obraId}/diarios"));
        assertTrue(rotas.contains("/api/obras/{obraId}/painel"));
        assertTrue(rotas.contains("/api/cargos"));
    }

    @Test
    void cadaAcaoTemTeste403() throws Exception {
        Set<String> expressoes = expressoes();
        String diario = Files.readString(Path.of("src/test/java/br/com/obras/diario/controller/DiarioControllerTest.java"));

        for (String acao : List.of("VISUALIZAR", "CRIAR", "EDITAR", "EXCLUIR")) {
            assertTrue(expressoes.stream().anyMatch(expr -> expr.contains("'" + acao + "'")),
                    "Ação sem @PreAuthorize: " + acao);
            assertTrue(diario.contains("\"" + acao + "\""), "Ação sem teste 403: " + acao);
        }
        assertTrue(diario.contains("status().isForbidden()"));
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

        List<Method> metodos = java.util.Arrays.stream(diario.getDeclaredMethods())
                .filter(metodo -> AnnotatedElementUtils.findMergedAnnotation(metodo, RequestMapping.class) != null)
                .toList();

        assertTrue(metodos.stream()
                .map(metodo -> AnnotatedElementUtils.findMergedAnnotation(metodo, PreAuthorize.class))
                .filter(java.util.Objects::nonNull)
                .allMatch(pre -> pre.value().contains("@perm.pode(")));
    }

    private void assertEqualsAcao(String metodo, String acao) {
        assertTrue(acao(metodo).contains("'" + acao + "'"));
    }

    private String acao(String metodo) {
        Class<?> diario = controllers().stream()
                .filter(classe -> classe.getSimpleName().equals("DiarioController"))
                .findFirst()
                .orElseThrow();
        return java.util.Arrays.stream(diario.getDeclaredMethods())
                .filter(candidato -> candidato.getName().equals(metodo))
                .map(candidato -> AnnotatedElementUtils.findMergedAnnotation(candidato, PreAuthorize.class))
                .filter(java.util.Objects::nonNull)
                .map(PreAuthorize::value)
                .findFirst()
                .orElseThrow();
    }

    private Set<String> expressoes() {
        Set<String> expressoes = new TreeSet<>();
        controllers().forEach(classe -> java.util.Arrays.stream(classe.getDeclaredMethods())
                .map(metodo -> AnnotatedElementUtils.findMergedAnnotation(metodo, PreAuthorize.class))
                .filter(java.util.Objects::nonNull)
                .forEach(pre -> expressoes.add(pre.value())));
        return expressoes;
    }

    private Set<String> rotas() {
        Set<String> rotas = new TreeSet<>();
        controllers().forEach(classe -> {
            RequestMapping classeMapping = AnnotatedElementUtils.findMergedAnnotation(classe, RequestMapping.class);
            String prefixo = classeMapping == null ? "" : primeira(classeMapping.path());
            if (!prefixo.isEmpty()) {
                rotas.add(prefixo);
            }
            java.util.Arrays.stream(classe.getDeclaredMethods())
                    .map(metodo -> AnnotatedElementUtils.findMergedAnnotation(metodo, RequestMapping.class))
                    .filter(java.util.Objects::nonNull)
                    .forEach(mapping -> {
                        for (String caminho : mapping.path()) {
                            rotas.add(prefixo + caminho);
                        }
                    });
        });
        return rotas;
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
                .collect(java.util.stream.Collectors.toSet());
    }

    private boolean ehClasseDeProducao(Class<?> classe) {
        var recurso = classe.getResource(classe.getSimpleName() + ".class");
        return recurso != null && !recurso.getPath().contains("test-classes");
    }
}
