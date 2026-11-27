package br.com.obras.obra.domain;

import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.usuario.domain.Cargo;
import br.com.obras.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class AlocacaoObraTest {
    private final Obra obra = mock(Obra.class);
    private final Usuario usuario = mock(Usuario.class);
    private final Cargo engenheiro = new Cargo("Engenheiro", null);
    private final LocalDate inicio = LocalDate.of(2026, 1, 10);

    @Test
    void mesmoIdSaoIguais() {
        AlocacaoObra a = alocacao(10L);
        AlocacaoObra b = alocacao(10L);

        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void idsDiferentesNaoSaoIguais() {
        assertNotEquals(alocacao(10L), alocacao(11L));
    }

    @Test
    void semIdSoIgualASiMesma() {
        AlocacaoObra a = alocacao(null);
        AlocacaoObra b = alocacao(null);

        assertEquals(a, a);
        assertNotEquals(a, b);
    }

    @Test
    void hashCodeNaoMudaQuandoRecebeId() {
        AlocacaoObra a = alocacao(null);
        Set<AlocacaoObra> conjunto = new HashSet<>(Set.of(a));

        ReflectionTestUtils.setField(a, "id", 10L);

        assertTrue(conjunto.contains(a));
    }

    @Test
    void criaAlocacaoAtivaComCargos() {
        AlocacaoObra alocacao = AlocacaoObra.criar(obra, usuario, inicio, Set.of(engenheiro));

        assertAll(
                () -> assertTrue(alocacao.estaAtiva()),
                () -> assertEquals(inicio, alocacao.getDataInicio()),
                () -> assertEquals(Set.of(engenheiro), alocacao.getCargos())
        );
    }

    @Test
    void rejeitaAlocacaoSemCargos() {
        assertThrows(RegraNegocioException.class,
                () -> AlocacaoObra.criar(obra, usuario, inicio, Set.of()));
    }

    @Test
    void rejeitaDataFimAntesDoInicio() {
        assertThrows(RegraNegocioException.class,
                () -> AlocacaoObra.criarEncerrada(obra, usuario, inicio,
                        inicio.minusDays(1), Set.of(engenheiro)));
    }

    @Test
    void encerraDefinindoDataFim() {
        AlocacaoObra alocacao = AlocacaoObra.criar(obra, usuario, inicio, Set.of(engenheiro));

        alocacao.encerrar(inicio.plusDays(3));

        assertAll(
                () -> assertFalse(alocacao.estaAtiva()),
                () -> assertEquals(inicio.plusDays(3), alocacao.getDataFim())
        );
    }

    @Test
    void naoReativaAlocacaoEncerrada() {
        AlocacaoObra alocacao = AlocacaoObra.criar(obra, usuario, inicio, Set.of(engenheiro));
        alocacao.encerrar(inicio.plusDays(1));

        assertThrows(RegraNegocioException.class, () -> alocacao.encerrar(inicio.plusDays(2)));
    }

    @Test
    void cargosSaoUnicosPorAlocacao() {
        Set<Cargo> cargos = new HashSet<>(List.of(engenheiro, engenheiro));

        AlocacaoObra alocacao = AlocacaoObra.criar(obra, usuario, inicio, cargos);

        assertEquals(1, alocacao.getCargos().size());
    }

    private static AlocacaoObra alocacao(Long id) {
        AlocacaoObra a = new AlocacaoObra();
        ReflectionTestUtils.setField(a, "id", id);
        return a;
    }
}
