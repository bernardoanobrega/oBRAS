package br.com.obras.obra.mapper;

import br.com.obras.obra.domain.AlocacaoObra;
import br.com.obras.obra.dto.AlocacaoObraDto;
import br.com.obras.usuario.domain.Cargo;
import br.com.obras.usuario.domain.Usuario;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

class AlocacaoObraMapperTest {
    private final AlocacaoObraMapper mapper = new AlocacaoObraMapper();

    @Test
    void mapperIncluiCargosENomeDoUsuario() {
        AlocacaoObraDto dto = mapper.paraDto(alocacao());

        assertAll(
                () -> assertEquals("Ana", dto.nome()),
                () -> assertEquals("Ana", dto.nomeUsuario()),
                () -> assertEquals(List.of(1L, 2L), dto.cargos().stream().map(c -> c.id()).toList()),
                () -> assertEquals(List.of("Analista", "Engenheiro"),
                        dto.cargos().stream().map(c -> c.nome()).toList()),
                () -> assertEquals(LocalDate.of(2026, 1, 10), dto.dataInicio())
        );
    }

    private AlocacaoObra alocacao() {
        Usuario usuario = new Usuario();
        ReflectionTestUtils.setField(usuario, "id", 10L);
        ReflectionTestUtils.setField(usuario, "nome", "Ana");

        Cargo engenheiro = new Cargo("Engenheiro", null);
        ReflectionTestUtils.setField(engenheiro, "id", 2L);
        Cargo analista = new Cargo("Analista", null);
        ReflectionTestUtils.setField(analista, "id", 1L);

        AlocacaoObra alocacao = new AlocacaoObra();
        ReflectionTestUtils.setField(alocacao, "id", 100L);
        ReflectionTestUtils.setField(alocacao, "usuario", usuario);
        ReflectionTestUtils.setField(alocacao, "dataInicio", LocalDate.of(2026, 1, 10));
        ReflectionTestUtils.setField(alocacao, "cargos", new HashSet<>(Arrays.asList(engenheiro, analista)));
        return alocacao;
    }
}
