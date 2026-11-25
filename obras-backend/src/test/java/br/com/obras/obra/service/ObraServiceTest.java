package br.com.obras.obra.service;

import br.com.obras.obra.domain.AlocacaoObra;
import br.com.obras.obra.domain.Obra;
import br.com.obras.obra.domain.StatusObra;
import br.com.obras.obra.domain.Tipologia;
import br.com.obras.obra.dto.EquipeMembroDto;
import br.com.obras.obra.dto.ObraDto;
import br.com.obras.obra.repository.AlocacaoObraRepository;
import br.com.obras.obra.repository.ObraRepository;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.security.AcaoPermissao;
import br.com.obras.shared.security.AutorizacaoPort;
import br.com.obras.shared.security.ModuloSistema;
import br.com.obras.shared.security.UsuarioAutenticado;
import br.com.obras.usuario.domain.Cargo;
import br.com.obras.usuario.domain.Usuario;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ObraServiceTest {
    private static final Long USUARIO_ID = 10L;
    
    @Mock private ObraRepository obras;
    @Mock private AlocacaoObraRepository alocacoes;
    @Mock private AutorizacaoPort autorizacao;
    @InjectMocks private ObraService service;

    @BeforeEach
    void autenticar() {
        var auth = new UsernamePasswordAuthenticationToken( new UsuarioAutenticado(USUARIO_ID, "ana"), null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void listarRetornaSomenteObrasComPermissao() {
        Obra permitida = obraMock(1L);
        Obra negada = mock(Obra.class);
        when(negada.getId()).thenReturn(2L);

        when(obras.findAllByOrderByNomeAsc()).thenReturn(List.of(permitida, negada));
        when(autorizacao.possuiPermissao(USUARIO_ID, 1L, ModuloSistema.DETALHES_OBRA, AcaoPermissao.VISUALIZAR)).thenReturn(true);
        when(autorizacao.possuiPermissao(USUARIO_ID, 2L, ModuloSistema.DETALHES_OBRA, AcaoPermissao.VISUALIZAR)).thenReturn(false);

        List<ObraDto> result = service.listar();

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
        assertEquals("Obra 1", result.get(0).nome());
    }

    @Test
    void buscarRetornaDetalhe() {
        Obra obra = obraMock(1L);
        when(obras.findById(1L)).thenReturn(Optional.of(obra));

        ObraDto dto = service.buscar(1L);

        assertEquals(1L, dto.id());
        assertEquals("RESIDENCIAL", dto.tipologia());
        assertEquals("PLANEJADA", dto.status());
        assertEquals(99L, dto.responsavelId());
    }

    @Test
    void buscarInexistenteLanca404() {
        when(obras.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.buscar(99L));
    }

    @Test
    void equipeRetornaMembrosAtivos() {
        Obra obra = mock(Obra.class);
        when(obras.findById(1L)).thenReturn(Optional.of(obra));

        Usuario usuario = mock(Usuario.class);
        when(usuario.getId()).thenReturn(5L);
        when(usuario.getNome()).thenReturn("Carlos Mendes");

        Cargo cargo = mock(Cargo.class);
        when(cargo.getNome()).thenReturn("Engenheiro Residente");

        AlocacaoObra alocacao = mock(AlocacaoObra.class);
        when(alocacao.getUsuario()).thenReturn(usuario);
        when(alocacao.getCargos()).thenReturn(Set.of(cargo));
        when(alocacao.getDataInicio()).thenReturn(LocalDate.of(2026, 1, 10));
        
        when(alocacoes.findEquipeAtiva(1L)).thenReturn(List.of(alocacao));

        List<EquipeMembroDto> equipe = service.equipe(1L);

        assertEquals(1, equipe.size());
        assertEquals(5L, equipe.get(0).usuarioId());
        assertEquals("Carlos Mendes", equipe.get(0).nome());
        assertEquals(List.of("Engenheiro Residente"), equipe.get(0).cargos());
    }

    @Test
    void equipeDeObraInexistenteLanca404() {
        when(obras.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RecursoNaoEncontradoException.class, () -> service.equipe(99L));
        verifyNoInteractions(alocacoes);
    }

    private Obra obraMock(Long id) {
        Obra o = mock(Obra.class);
        when(o.getId()).thenReturn(id);
        when(o.getNome()).thenReturn("Obra " + id);
        when(o.getDescricao()).thenReturn("Descrição " + id);
        when(o.getTipologia()).thenReturn(Tipologia.RESIDENCIAL);
        when(o.getEndereco()).thenReturn("Rua " + id);
        when(o.getInicioPrevisto()).thenReturn(LocalDate.of(2026, 1, 1));
        when(o.getFimPrevisto()).thenReturn(LocalDate.of(2026, 12, 31));
        when(o.getStatus()).thenReturn(StatusObra.PLANEJADA);
        when(o.getArt()).thenReturn("ART-" + id);
        when(o.getResponsavelId()).thenReturn(99L);
        return o;
    }
}
