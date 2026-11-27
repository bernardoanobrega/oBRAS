package br.com.obras.obra.dto;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class AlocacaoObraDtoTest {
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void dtoRejeitaCargoIdsVazio() {
        Set<String> propriedades = validator.validate(new NovaAlocacaoDto(1L, List.of())).stream()
                .map(violacao -> violacao.getPropertyPath().toString())
                .collect(Collectors.toSet());

        assertEquals(Set.of("cargoIds"), propriedades);
    }

    @Test
    void dtoNaoAceitaDataFimNoCadastro() {
        boolean temDataFim = Arrays.stream(NovaAlocacaoDto.class.getRecordComponents())
                .anyMatch(componente -> componente.getName().equals("dataFim"));

        assertFalse(temDataFim);
    }
}
