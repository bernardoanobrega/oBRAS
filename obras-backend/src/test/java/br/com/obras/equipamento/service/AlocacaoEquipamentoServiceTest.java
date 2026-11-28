package br.com.obras.equipamento.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.obras.auditoria.domain.AcaoAuditoria;
import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.auditoria.service.AuditoriaService;
import br.com.obras.equipamento.domain.AlocacaoEquipamento;
import br.com.obras.equipamento.domain.Equipamento;
import br.com.obras.equipamento.domain.StatusEquipamento;
import br.com.obras.equipamento.domain.TipoEquipamento;
import br.com.obras.equipamento.dto.AlocacaoEquipamentoDto;
import br.com.obras.equipamento.dto.AlocarEquipamentoDto;
import br.com.obras.equipamento.dto.DevolverEquipamentoDto;
import br.com.obras.equipamento.dto.ProrrogarAlocacaoDto;
import br.com.obras.equipamento.dto.TransferirEquipamentoDto;
import br.com.obras.equipamento.repository.AlocacaoEquipamentoRepository;
import br.com.obras.equipamento.repository.EquipamentoRepository;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.shared.port.ObraPort;
import br.com.obras.shared.port.ObraResumo;
import br.com.obras.shared.port.UsuarioPort;
import br.com.obras.shared.port.UsuarioResumo;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AlocacaoEquipamentoServiceTest {

    private static final Long OBRA = 1L;
    private static final Long OUTRA_OBRA = 2L;
    private static final Long EQUIPAMENTO_ID = 7L;
    private static final Long ALOCACAO_ID = 9L;
    private static final Long RESPONSAVEL = 5L;

    @Mock
    private EquipamentoRepository equipamentoRepository;

    @Mock
    private AlocacaoEquipamentoRepository alocacaoRepository;

    @Mock
    private ObraPort obraPort;

    @Mock
    private UsuarioPort usuarioPort;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private AlocacaoEquipamentoService service;

    private Equipamento novoEquipamento() {
        return new Equipamento(Auditavel.novo(), "PAT-001", "Betoneira 400L", TipoEquipamento.MAQUINA);
    }

    private Equipamento novoEquipamentoAlocadoNaObra() {
        Equipamento equipamento = novoEquipamento();
        equipamento.alocar(Auditavel.novo(), OBRA, daquiADias(7), RESPONSAVEL, null);
        return equipamento;
    }

    private LocalDate daquiADias(long dias) {
        return LocalDate.now().plusDays(dias);
    }

    private void obraExiste(Long obraId) {
        when(obraPort.buscarResumo(obraId)).thenReturn(Optional.of(new ObraResumo(obraId, "Obra " + obraId, false)));
    }

    private void obraEncerrada(Long obraId) {
        when(obraPort.buscarResumo(obraId)).thenReturn(Optional.of(new ObraResumo(obraId, "Obra " + obraId, true)));
    }

    private void responsavelAtivo() {
        when(usuarioPort.buscarResumo(RESPONSAVEL))
                .thenReturn(Optional.of(new UsuarioResumo(RESPONSAVEL, "Maria", true)));
    }

    private void equipamentoExiste(Equipamento equipamento) {
        when(equipamentoRepository.findById(EQUIPAMENTO_ID)).thenReturn(Optional.of(equipamento));
    }

    private void auditoriaCriaAuditavel() {
        when(auditoriaService.criar(eq(AcaoAuditoria.CRIACAO), anyString())).thenReturn(Auditavel.novo());
    }

    private AlocarEquipamentoDto dtoAlocar() {
        return new AlocarEquipamentoDto(RESPONSAVEL, daquiADias(7), null);
    }

    private TransferirEquipamentoDto dtoTransferir(Long obraDestinoId) {
        return new TransferirEquipamentoDto(obraDestinoId, RESPONSAVEL, daquiADias(14), null);
    }

    // ---------- alocar ----------

    @Test
    @DisplayName("Aloca equipamento disponível na obra e registra a auditoria")
    void alocaEquipamentoDisponivelNaObraERegistraAuditoria() {
        Equipamento equipamento = novoEquipamento();
        equipamentoExiste(equipamento);
        obraExiste(OBRA);
        responsavelAtivo();
        auditoriaCriaAuditavel();
        when(alocacaoRepository.save(any(AlocacaoEquipamento.class))).thenAnswer(chamada -> chamada.getArgument(0));

        AlocacaoEquipamentoDto resultado = service.alocar(OBRA, EQUIPAMENTO_ID, dtoAlocar());

        assertThat(resultado.estaAtiva()).isTrue();
        assertThat(resultado.obraId()).isEqualTo(OBRA);
        assertThat(equipamento.getStatus()).isEqualTo(StatusEquipamento.ALOCADO);
        verify(auditoriaService).criar(eq(AcaoAuditoria.CRIACAO), anyString());
        verify(auditoriaService).registrar(eq(equipamento.getAuditavel()), eq(AcaoAuditoria.EDICAO), anyString());
    }

    @Test
    @DisplayName("Lança exceção quando a obra não existe")
    void lancaExcecaoQuandoObraNaoExiste() {
        equipamentoExiste(novoEquipamento());
        when(obraPort.buscarResumo(OBRA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.alocar(OBRA, EQUIPAMENTO_ID, dtoAlocar()))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(alocacaoRepository, never()).save(any(AlocacaoEquipamento.class));
    }

    @Test
    @DisplayName("Lança exceção ao alocar em obra concluída ou cancelada")
    void lancaExcecaoQuandoAlocarEmObraEncerrada() {
        equipamentoExiste(novoEquipamento());
        obraEncerrada(OBRA);

        assertThatThrownBy(() -> service.alocar(OBRA, EQUIPAMENTO_ID, dtoAlocar()))
                .isInstanceOf(RegraNegocioException.class);
        verify(alocacaoRepository, never()).save(any(AlocacaoEquipamento.class));
    }

    @Test
    @DisplayName("Lança exceção quando o responsável está inativo")
    void lancaExcecaoQuandoResponsavelInativo() {
        equipamentoExiste(novoEquipamento());
        obraExiste(OBRA);
        when(usuarioPort.buscarResumo(RESPONSAVEL))
                .thenReturn(Optional.of(new UsuarioResumo(RESPONSAVEL, "Maria", false)));

        assertThatThrownBy(() -> service.alocar(OBRA, EQUIPAMENTO_ID, dtoAlocar()))
                .isInstanceOf(RegraNegocioException.class);
        verify(alocacaoRepository, never()).save(any(AlocacaoEquipamento.class));
    }

    @Test
    @DisplayName("Lança exceção quando equipamento já alocado")
    void lancaExcecaoQuandoEquipamentoJaAlocado() {
        equipamentoExiste(novoEquipamentoAlocadoNaObra());
        obraExiste(OUTRA_OBRA);
        responsavelAtivo();
        auditoriaCriaAuditavel();

        assertThatThrownBy(() -> service.alocar(OUTRA_OBRA, EQUIPAMENTO_ID, dtoAlocar()))
                .isInstanceOf(RegraNegocioException.class);
        verify(alocacaoRepository, never()).save(any(AlocacaoEquipamento.class));
    }

    // ---------- devolver ----------

    @Test
    @DisplayName("Devolve equipamento alocado na obra e registra a auditoria")
    void devolveEquipamentoAlocadoNaObraERegistraAuditoria() {
        Equipamento equipamento = novoEquipamentoAlocadoNaObra();
        AlocacaoEquipamento alocacao = equipamento.alocacaoAtiva().orElseThrow();
        equipamentoExiste(equipamento);

        AlocacaoEquipamentoDto resultado =
                service.devolver(OBRA, EQUIPAMENTO_ID, new DevolverEquipamentoDto("Sem avarias"));

        assertThat(resultado.estaAtiva()).isFalse();
        assertThat(resultado.observacaoDevolucao()).isEqualTo("Sem avarias");
        assertThat(equipamento.getStatus()).isEqualTo(StatusEquipamento.DISPONIVEL);
        verify(auditoriaService).registrar(eq(alocacao.getAuditavel()), eq(AcaoAuditoria.EDICAO), anyString());
        verify(auditoriaService).registrar(eq(equipamento.getAuditavel()), eq(AcaoAuditoria.EDICAO), anyString());
    }

    @Test
    @DisplayName("Lança exceção ao devolver equipamento alocado em outra obra")
    void lancaExcecaoQuandoDevolverEquipamentoAlocadoEmOutraObra() {
        Equipamento equipamento = novoEquipamentoAlocadoNaObra();
        equipamentoExiste(equipamento);

        assertThatThrownBy(() -> service.devolver(OUTRA_OBRA, EQUIPAMENTO_ID, new DevolverEquipamentoDto(null)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThat(equipamento.getStatus()).isEqualTo(StatusEquipamento.ALOCADO);
    }

    @Test
    @DisplayName("Lança exceção ao devolver equipamento disponível")
    void lancaExcecaoQuandoDevolverEquipamentoDisponivel() {
        equipamentoExiste(novoEquipamento());

        assertThatThrownBy(() -> service.devolver(OBRA, EQUIPAMENTO_ID, new DevolverEquipamentoDto(null)))
                .isInstanceOf(RegraNegocioException.class);
        verify(auditoriaService, never()).registrar(any(Auditavel.class), any(AcaoAuditoria.class), anyString());
    }

    // ---------- transferir ----------

    @Test
    @DisplayName("Transfere para outra obra gravando o encerramento antes da nova alocação")
    void transfereParaOutraObraGravandoEncerramentoAntes() {
        Equipamento equipamento = novoEquipamentoAlocadoNaObra();
        AlocacaoEquipamento anterior = equipamento.alocacaoAtiva().orElseThrow();
        equipamentoExiste(equipamento);
        obraExiste(OUTRA_OBRA);
        responsavelAtivo();
        auditoriaCriaAuditavel();
        when(alocacaoRepository.save(any(AlocacaoEquipamento.class))).thenAnswer(chamada -> chamada.getArgument(0));

        AlocacaoEquipamentoDto resultado = service.transferir(OBRA, EQUIPAMENTO_ID, dtoTransferir(OUTRA_OBRA));

        AlocacaoEquipamento nova = equipamento.alocacaoAtiva().orElseThrow();
        InOrder ordem = inOrder(alocacaoRepository);
        ordem.verify(alocacaoRepository).flush();
        ordem.verify(alocacaoRepository).save(nova);
        assertThat(resultado.obraId()).isEqualTo(OUTRA_OBRA);
        assertThat(equipamento.getStatus()).isEqualTo(StatusEquipamento.ALOCADO);
        verify(auditoriaService).criar(eq(AcaoAuditoria.CRIACAO), anyString());
        verify(auditoriaService).registrar(eq(anterior.getAuditavel()), eq(AcaoAuditoria.EDICAO), anyString());
    }

    @Test
    @DisplayName("Lança exceção ao transferir para obra inexistente")
    void lancaExcecaoQuandoTransferirParaObraInexistente() {
        equipamentoExiste(novoEquipamentoAlocadoNaObra());
        when(obraPort.buscarResumo(OUTRA_OBRA)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.transferir(OBRA, EQUIPAMENTO_ID, dtoTransferir(OUTRA_OBRA)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(alocacaoRepository, never()).flush();
        verify(alocacaoRepository, never()).save(any(AlocacaoEquipamento.class));
    }

    @Test
    @DisplayName("Lança exceção ao transferir para obra concluída ou cancelada")
    void lancaExcecaoQuandoTransferirParaObraEncerrada() {
        equipamentoExiste(novoEquipamentoAlocadoNaObra());
        obraEncerrada(OUTRA_OBRA);

        assertThatThrownBy(() -> service.transferir(OBRA, EQUIPAMENTO_ID, dtoTransferir(OUTRA_OBRA)))
                .isInstanceOf(RegraNegocioException.class);
        verify(alocacaoRepository, never()).flush();
        verify(alocacaoRepository, never()).save(any(AlocacaoEquipamento.class));
    }

    @Test
    @DisplayName("Lança exceção ao transferir para a mesma obra")
    void lancaExcecaoQuandoTransferirParaAMesmaObra() {
        equipamentoExiste(novoEquipamentoAlocadoNaObra());
        obraExiste(OBRA);
        responsavelAtivo();
        auditoriaCriaAuditavel();

        assertThatThrownBy(() -> service.transferir(OBRA, EQUIPAMENTO_ID, dtoTransferir(OBRA)))
                .isInstanceOf(RegraNegocioException.class);
        verify(alocacaoRepository, never()).flush();
        verify(alocacaoRepository, never()).save(any(AlocacaoEquipamento.class));
    }

    @Test
    @DisplayName("Lança exceção ao transferir equipamento alocado em outra obra")
    void lancaExcecaoQuandoTransferirEquipamentoAlocadoEmOutraObra() {
        equipamentoExiste(novoEquipamentoAlocadoNaObra());

        assertThatThrownBy(() -> service.transferir(OUTRA_OBRA, EQUIPAMENTO_ID, dtoTransferir(3L)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(alocacaoRepository, never()).save(any(AlocacaoEquipamento.class));
    }

    // ---------- prorrogar ----------

    @Test
    @DisplayName("Prorroga alocação da obra e registra a auditoria")
    void prorrogaAlocacaoDaObraERegistraAuditoria() {
        AlocacaoEquipamento alocacao = novoEquipamentoAlocadoNaObra().alocacaoAtiva().orElseThrow();
        when(alocacaoRepository.findById(ALOCACAO_ID)).thenReturn(Optional.of(alocacao));
        LocalDate novaData = daquiADias(30);

        AlocacaoEquipamentoDto resultado = service.prorrogar(OBRA, ALOCACAO_ID, new ProrrogarAlocacaoDto(novaData));

        assertThat(resultado.dataPrevistaDevolucao()).isEqualTo(novaData);
        verify(auditoriaService, times(1))
                .registrar(eq(alocacao.getAuditavel()), eq(AcaoAuditoria.EDICAO), anyString());
    }

    @Test
    @DisplayName("Lança exceção ao prorrogar alocação de outra obra")
    void lancaExcecaoQuandoProrrogarAlocacaoDeOutraObra() {
        AlocacaoEquipamento alocacao = novoEquipamentoAlocadoNaObra().alocacaoAtiva().orElseThrow();
        when(alocacaoRepository.findById(ALOCACAO_ID)).thenReturn(Optional.of(alocacao));

        assertThatThrownBy(() -> service.prorrogar(OUTRA_OBRA, ALOCACAO_ID, new ProrrogarAlocacaoDto(daquiADias(30))))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        verify(auditoriaService, never()).registrar(any(Auditavel.class), any(AcaoAuditoria.class), anyString());
    }

    // ---------- consultas ----------

    @Test
    @DisplayName("Lista as alocações ativas da obra")
    void listaAlocacoesAtivasDaObra() {
        AlocacaoEquipamento alocacao = novoEquipamentoAlocadoNaObra().alocacaoAtiva().orElseThrow();
        when(alocacaoRepository.listarAtivasPorObra(OBRA)).thenReturn(List.of(alocacao));

        List<AlocacaoEquipamentoDto> resultado = service.listarAtivasDaObra(OBRA);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).codigoPatrimonio()).isEqualTo("PAT-001");
    }

    @Test
    @DisplayName("Lista o histórico do equipamento")
    void listaHistoricoDoEquipamento() {
        AlocacaoEquipamento alocacao = novoEquipamentoAlocadoNaObra().alocacaoAtiva().orElseThrow();
        when(equipamentoRepository.existsById(EQUIPAMENTO_ID)).thenReturn(true);
        when(alocacaoRepository.findByEquipamentoIdOrderByDataInicioDescIdDesc(EQUIPAMENTO_ID))
                .thenReturn(List.of(alocacao));

        List<AlocacaoEquipamentoDto> resultado = service.listarHistorico(EQUIPAMENTO_ID);

        assertThat(resultado).hasSize(1);
    }

    @Test
    @DisplayName("Lança exceção ao listar histórico de equipamento inexistente")
    void lancaExcecaoQuandoListarHistoricoDeEquipamentoInexistente() {
        when(equipamentoRepository.existsById(EQUIPAMENTO_ID)).thenReturn(false);

        assertThatThrownBy(() -> service.listarHistorico(EQUIPAMENTO_ID))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }
}