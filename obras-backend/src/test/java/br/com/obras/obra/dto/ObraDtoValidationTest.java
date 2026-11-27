package br.com.obras.obra.dto;

import br.com.obras.obra.domain.Tipologia;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

class ObraDtoValidationTest {
    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();
    private static final LocalDate INICIO = LocalDate.of(2026, 1, 1);
    private static final LocalDate FIM = LocalDate.of(2026, 12, 31);

    private static <T> Set<String> campos(Set<ConstraintViolation<T>> violacoes) {
        return violacoes.stream()
                .map(v -> v.getPropertyPath().toString())
                .collect(Collectors.toSet());
    }

    @Test
    void novaObraDtoRejeitaCamposObrigatorios() {
        NovaObraDto vazio = new NovaObraDto("  ", null, null, "", null, null, null, null);
        NovaObraDto valido = new NovaObraDto("Obra", null, Tipologia.RESIDENCIAL, "Rua X",
                INICIO, FIM, null, null);
        NovaObraDto longo = new NovaObraDto("a".repeat(201), "d".repeat(1001), Tipologia.RESIDENCIAL,
                "e".repeat(301), INICIO, FIM, "r".repeat(51), null);

        assertAll(
                () -> assertEquals(Set.of("nome", "tipologia", "endereco", "inicioPrevisto", "fimPrevisto"),
                        campos(VALIDATOR.validate(vazio))),
                () -> assertTrue(VALIDATOR.validate(valido).isEmpty()),
                () -> assertEquals(Set.of("nome", "descricao", "endereco", "art"),
                        campos(VALIDATOR.validate(longo)))
        );
    }

    @Test
    void atualizarObraDtoNaoAceitaStatus() {
        AtualizarObraDto comStatus = new AtualizarObraDto("Obra", null, Tipologia.RESIDENCIAL, "Rua X",
                INICIO, FIM, null, null, "EM_ANDAMENTO");
        AtualizarObraDto semStatus = new AtualizarObraDto("Obra", null, Tipologia.RESIDENCIAL, "Rua X",
                INICIO, FIM, null, null, null);

        Set<ConstraintViolation<AtualizarObraDto>> violacoes = VALIDATOR.validate(comStatus);

        assertAll(
                () -> assertEquals(Set.of("status"), campos(violacoes)),
                () -> assertEquals("status só muda por transição",
                        violacoes.iterator().next().getMessage()),
                () -> assertTrue(VALIDATOR.validate(semStatus).isEmpty())
        );
    }
}
