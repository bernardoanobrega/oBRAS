package br.com.obras.obra.mapper;

import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.obra.domain.Obra;
import br.com.obras.obra.domain.StatusObra;
import br.com.obras.obra.domain.Tipologia;
import br.com.obras.obra.dto.NovaObraDto;
import br.com.obras.obra.dto.ObraDto;
import br.com.obras.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.RecordComponent;
import java.time.LocalDate;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

class ObraMapperTest {
    private static final LocalDate INICIO = LocalDate.of(2026, 1, 1);
    private static final LocalDate FIM = LocalDate.of(2026, 12, 31);

    private final ObraMapper mapper = new ObraMapper();

    private static Auditavel auditavel(Long id) {
        Auditavel a = Auditavel.novo();
        ReflectionTestUtils.setField(a, "id", id);
        return a;
    }

    @Test
    void mapeiaObraParaDtoSemExporEntidade() {
        Obra obra = Obra.cadastrar("Obra teste", "desc", Tipologia.RESIDENCIAL, "Rua teste",
                INICIO, FIM, "ART-1", 10L, auditavel(99L));
        ReflectionTestUtils.setField(obra, "id", 5L);

        ObraDto dto = mapper.paraDto(obra);

        assertAll(
                () -> assertTrue(Arrays.stream(ObraDto.class.getRecordComponents())
                        .map(RecordComponent::getType)
                        .noneMatch(t -> Obra.class.isAssignableFrom(t) || Auditavel.class.isAssignableFrom(t))),
                () -> assertEquals(99L, dto.auditavelId()),
                () -> assertEquals(5L, dto.id()),
                () -> assertEquals("Obra teste", dto.nome()),
                () -> assertEquals("RESIDENCIAL", dto.tipologia()),
                () -> assertEquals("PLANEJADA", dto.status()),
                () -> assertEquals(10L, dto.responsavelId())
        );
    }

    @Test
    void paraEntidadeUsaFabricaCadastrar() {
        NovaObraDto novo = new NovaObraDto("Obra nova", "desc", Tipologia.COMERCIAL, "Rua nova",
                INICIO, FIM, "ART-2", null);
        NovaObraDto invertido = new NovaObraDto("Obra nova", null, Tipologia.COMERCIAL, "Rua nova",
                FIM, INICIO, null, null);

        Obra obra = mapper.paraEntidade(novo, auditavel(7L));

        assertAll(
                () -> assertEquals(StatusObra.PLANEJADA, obra.getStatus()),
                () -> assertEquals("Obra nova", obra.getNome()),
                () -> assertEquals(Tipologia.COMERCIAL, obra.getTipologia()),
                () -> assertEquals("Rua nova", obra.getEndereco()),
                () -> assertEquals("ART-2", obra.getArt()),
                () -> assertNull(obra.getResponsavelId()),
                () -> assertEquals(7L, obra.getAuditavelId()),
                () -> assertNotNull(obra.getAuditavel()),
                () -> assertThrows(RegraNegocioException.class,
                        () -> mapper.paraEntidade(invertido, auditavel(7L)))
        );
    }
}
