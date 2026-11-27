package br.com.obras.obra.service;

import br.com.obras.obra.domain.AlocacaoObra;
import br.com.obras.obra.domain.Obra;
import br.com.obras.obra.dto.AlocacaoObraDto;
import br.com.obras.obra.dto.NovaAlocacaoDto;
import br.com.obras.obra.mapper.AlocacaoObraMapper;
import br.com.obras.obra.repository.AlocacaoObraRepository;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.shared.port.CargoConsultaPort;
import br.com.obras.shared.port.UsuarioConsultaPort;
import br.com.obras.usuario.domain.Cargo;
import br.com.obras.usuario.domain.Usuario;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AlocacaoObraServiceTest {
    private static final long OBRA_ID = 1L;
    private static final long OUTRA_OBRA_ID = 2L;
    private static final long USUARIO_ID = 10L;
    private static final long CARGO_ID = 20L;

    @Mock private ObraService obras;
    @Mock private AlocacaoObraRepository alocacoes;
    @Mock private UsuarioConsultaPort usuarios;
    @Mock private CargoConsultaPort cargos;
    @Mock private AlocacaoObraMapper mapper;
    @InjectMocks private AlocacaoObraService service;

    private final Obra obra = mock(Obra.class);
    private Usuario usuario;
    private Cargo cargo;

    @BeforeEach
    void setup() {
        usuario = Usuario.cadastrar("Ana", "ana", "hash");
        ReflectionTestUtils.setField(usuario, "id", USUARIO_ID);
        cargo = new Cargo("Engenheiro", null);
        ReflectionTestUtils.setField(cargo, "id", CARGO_ID);

        when(obras.obterEntidade(anyLong())).thenReturn(obra);
        when(usuarios.buscarEntidade(USUARIO_ID)).thenReturn(Optional.of(usuario));
        when(alocacoes.findAtivaPorUsuario(anyLong(), anyLong())).thenReturn(Optional.empty());
        when(cargos.buscarPorIds(anySet())).thenReturn(List.of(cargo));
        when(alocacoes.saveAndFlush(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
        when(mapper.paraDto(any())).thenAnswer(invocacao -> AlocacaoObraDto.de(invocacao.getArgument(0)));
    }

    @Test
    void alocaUsuarioAtivoComCargos() {
        AlocacaoObraDto dto = service.alocar(OBRA_ID, nova(Set.of(CARGO_ID)));

        assertAll(
                () -> assertEquals("Ana", dto.nome()),
                () -> assertEquals(List.of(CARGO_ID), dto.cargos().stream().map(c -> c.id()).toList()),
                () -> assertTrue(dto.dataInicio().equals(LocalDate.now()))
        );
    }

    @Test
    void rejeitaUsuarioInativo() {
        usuario.desativar();

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> service.alocar(OBRA_ID, nova(Set.of(CARGO_ID))));

        assertEquals("Usuário inexistente ou inativo", erro.getMessage());
    }

    @Test
    void rejeitaCargosVazios() {
        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> service.alocar(OBRA_ID, nova(Set.of())));

        assertEquals("A alocação exige ao menos um cargo", erro.getMessage());
    }

    @Test
    void rejeitaCargoInexistente() {
        when(cargos.buscarPorIds(Set.of(999L))).thenReturn(List.of());

        assertThrows(RecursoNaoEncontradoException.class,
                () -> service.alocar(OBRA_ID, nova(Set.of(999L))));
    }

    @Test
    void rejeitaAlocacaoAtivaDuplicada() {
        when(alocacoes.findAtivaPorUsuario(OBRA_ID, USUARIO_ID))
                .thenReturn(Optional.of(mock(AlocacaoObra.class)));

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> service.alocar(OBRA_ID, nova(Set.of(CARGO_ID))));

        assertEquals("Usuário já possui alocação ativa nesta obra", erro.getMessage());
    }

    @Test
    void dataInicioEhHoje() {
        service.alocar(OBRA_ID, nova(Set.of(CARGO_ID)));

        ArgumentCaptor<AlocacaoObra> captor = ArgumentCaptor.forClass(AlocacaoObra.class);
        org.mockito.Mockito.verify(alocacoes).saveAndFlush(captor.capture());
        assertEquals(LocalDate.now(), captor.getValue().getDataInicio());
    }

    @Test
    void alocacaoNaoAceitaUsuarioDeOutraObra() {
        when(alocacoes.findAtivaPorUsuario(OUTRA_OBRA_ID, USUARIO_ID))
                .thenReturn(Optional.of(mock(AlocacaoObra.class)));

        AlocacaoObraDto dto = service.alocar(OBRA_ID, nova(Set.of(CARGO_ID)));

        assertEquals("Ana", dto.nome());
    }

    @Test
    void encerraAlocacaoAtivaComDataDeHoje() {
        AlocacaoObra alocacao = alocacaoAtiva();
        when(alocacoes.buscarAtiva(OBRA_ID, 100L)).thenReturn(Optional.of(alocacao));

        service.encerrar(OBRA_ID, 100L);

        ArgumentCaptor<AlocacaoObra> captor = ArgumentCaptor.forClass(AlocacaoObra.class);
        org.mockito.Mockito.verify(alocacoes).save(captor.capture());
        assertAll(
                () -> assertEquals(LocalDate.now(), captor.getValue().getDataFim()),
                () -> org.junit.jupiter.api.Assertions.assertFalse(captor.getValue().estaAtiva())
        );
    }

    @Test
    void rejeitaEncerrarAlocacaoJaEncerrada() {
        AlocacaoObra encerrada = AlocacaoObra.criarEncerrada(obra, usuario,
                LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 2), Set.of(cargo));
        when(alocacoes.buscarAtiva(OBRA_ID, 100L)).thenReturn(Optional.of(encerrada));

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> service.encerrar(OBRA_ID, 100L));

        assertEquals("A alocação já está encerrada", erro.getMessage());
    }

    @Test
    void rejeitaEncerrarAlocacaoDeOutraObra() {
        when(alocacoes.buscarAtiva(OBRA_ID, 100L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.encerrar(OBRA_ID, 100L));
    }

    @Test
    void novaAlocacaoAposEncerramentoEhPermitida() {
        AlocacaoObra encerrada = alocacaoAtiva();
        encerrada.encerrar(LocalDate.now());
        when(alocacoes.buscarAtiva(OBRA_ID, 100L)).thenReturn(Optional.empty());

        AlocacaoObraDto nova = service.alocar(OBRA_ID, nova(Set.of(CARGO_ID)));

        assertEquals("Ana", nova.nome());
    }

    private AlocacaoObra alocacaoAtiva() {
        return AlocacaoObra.criar(obra, usuario, LocalDate.of(2026, 1, 1), Set.of(cargo));
    }

    private NovaAlocacaoDto nova(Set<Long> cargoIds) {
        return new NovaAlocacaoDto(USUARIO_ID, List.copyOf(cargoIds));
    }
}
