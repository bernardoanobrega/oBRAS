package br.com.obras.obra.domain;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class AlocacaoObraTest {

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

    private static AlocacaoObra alocacao(Long id) {
        AlocacaoObra a = new AlocacaoObra();
        ReflectionTestUtils.setField(a, "id", id);
        return a;
    }
}
