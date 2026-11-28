package br.com.obras.obra.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import br.com.obras.obra.domain.Obra;
import br.com.obras.obra.domain.StatusObra;
import br.com.obras.obra.repository.ObraRepository;
import br.com.obras.shared.port.ObraResumo;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ObraAdapterTest {

    private static final Long OBRA_ID = 1L;

    @Mock
    private ObraRepository obraRepository;

    @InjectMocks
    private ObraAdapter adapter;

    @ParameterizedTest(name = "{0}")
    @EnumSource(StatusObra.class)
    @DisplayName("Indica que a obra está encerrada apenas quando concluída ou cancelada")
    void indicaObraEncerradaApenasQuandoConcluidaOuCancelada(StatusObra status) {
        Obra obra = mock(Obra.class);
        when(obra.getId()).thenReturn(OBRA_ID);
        when(obra.getNome()).thenReturn("Edifício Aurora");
        when(obra.getStatus()).thenReturn(status);
        when(obraRepository.buscarAtiva(OBRA_ID)).thenReturn(Optional.of(obra));

        Optional<ObraResumo> resumo = adapter.buscarResumo(OBRA_ID);

        boolean esperadoEncerrada = status == StatusObra.CONCLUIDA || status == StatusObra.CANCELADA;
        assertThat(resumo).isPresent();
        assertThat(resumo.get().nome()).isEqualTo("Edifício Aurora");
        assertThat(resumo.get().estaEncerrada()).isEqualTo(esperadoEncerrada);
    }

    @Test
    @DisplayName("Retorna vazio quando a obra não existe ou foi excluída")
    void retornaVazioQuandoObraNaoExisteOuFoiExcluida() {
        when(obraRepository.buscarAtiva(OBRA_ID)).thenReturn(Optional.empty());

        assertThat(adapter.buscarResumo(OBRA_ID)).isEmpty();
    }
}