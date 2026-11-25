package br.com.obras.orcamento.service;

import br.com.obras.orcamento.domain.Orcamento;
import br.com.obras.orcamento.domain.StatusOrcamento;
import br.com.obras.orcamento.dto.NovoItemOrcadoDto;
import br.com.obras.orcamento.dto.NovoOrcamentoDto;
import br.com.obras.orcamento.dto.OrcamentoDto;
import br.com.obras.orcamento.dto.OrcamentoResumoDto;
import br.com.obras.orcamento.repository.OrcamentoRepository;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.shared.security.AcaoPermissao;
import br.com.obras.shared.security.AutorizacaoPort;
import br.com.obras.shared.security.ModuloSistema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrcamentoServiceTest {
    private static final Long OBRA_ID = 3L;
    private static final Long USUARIO_ID = 9L;
    private static final BigDecimal LIMITE_ALERTA = new BigDecimal("90");

    @Mock
    private OrcamentoRepository orcamentos;

    @Mock
    private AutorizacaoPort autorizacao;

    private OrcamentoService service;

    @BeforeEach
    void configurar() {
        service = new OrcamentoService(orcamentos, autorizacao);
    }

    @Test
    void elaboraOrcamentoEmElaboracao() {
        when(orcamentos.existsByObraId(OBRA_ID)).thenReturn(false);
        when(orcamentos.save(any(Orcamento.class))).thenAnswer(chamada -> chamada.getArgument(0));

        OrcamentoDto orcamento = service.elaborar(OBRA_ID, new NovoOrcamentoDto(LIMITE_ALERTA));

        assertEquals(StatusOrcamento.ELABORACAO, orcamento.status());
        assertEquals(OBRA_ID, orcamento.obraId());
    }

    @Test
    void lancaExcecaoQuandoObraJaPossuiOrcamento() {
        when(orcamentos.existsByObraId(OBRA_ID)).thenReturn(true);

        assertThrows(RegraNegocioException.class,
                () -> service.elaborar(OBRA_ID, new NovoOrcamentoDto(LIMITE_ALERTA)));
        verify(orcamentos, never()).save(any());
    }

    @Test
    void lancaExcecaoQuandoObraSemOrcamento() {
        when(orcamentos.findByObraId(OBRA_ID)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscarPorObra(OBRA_ID));
    }

    @Test
    void adicionarItemIniciaAcompanhamento() {
        Orcamento orcamento = Orcamento.elaborar(OBRA_ID, LIMITE_ALERTA);
        when(orcamentos.findByObraId(OBRA_ID)).thenReturn(Optional.of(orcamento));
        var dto = new NovoItemOrcadoDto("01", "Fundação", "m3", BigDecimal.TEN, new BigDecimal("50"));

        service.adicionarItem(OBRA_ID, dto);

        assertEquals(StatusOrcamento.SAUDAVEL, orcamento.getStatus());
        verify(orcamentos).flush();
    }

    @Test
    void listaSomenteOrcamentosDeObrasPermitidas() {
        Orcamento permitido = Orcamento.elaborar(1L, LIMITE_ALERTA);
        Orcamento negado = Orcamento.elaborar(2L, LIMITE_ALERTA);
        when(orcamentos.findAllByOrderByObraIdAsc()).thenReturn(List.of(permitido, negado));
        when(autorizacao.possuiPermissao(USUARIO_ID, 1L, ModuloSistema.ORCAMENTO, AcaoPermissao.VISUALIZAR))
                .thenReturn(true);
        when(autorizacao.possuiPermissao(USUARIO_ID, 2L, ModuloSistema.ORCAMENTO, AcaoPermissao.VISUALIZAR))
                .thenReturn(false);

        List<OrcamentoResumoDto> visiveis = service.listarVisiveis(USUARIO_ID);

        assertEquals(1, visiveis.size());
        assertEquals(1L, visiveis.get(0).obraId());
    }
}
