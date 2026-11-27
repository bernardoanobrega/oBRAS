package br.com.obras.equipamento.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import br.com.obras.shared.port.CanteiroPort;
import br.com.obras.shared.port.CanteiroResumo;
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
    private static final Long CANTEIRO_ORIGEM = 10L;
    private static final Long CANTEIRO_DESTINO = 20L;
    private static final Long RESPONSAVEL = 5L;

    @Mock
    private EquipamentoRepository equipamentoRepository;

    @Mock
    private AlocacaoEquipamentoRepository alocacaoRepository;

    @Mock
    private CanteiroPort canteiroPort;

    @Mock
    private UsuarioPort usuarioPort;

    @InjectMocks
    private AlocacaoEquipamentoService service;

    private Equipamento novoEquipamento() {
        return new Equipamento("PAT-001", "Betoneira 400L", TipoEquipamento.MAQUINA);
    }

    private Equipamento novoEquipamentoAlocadoNoCanteiroOrigem() {
        Equipamento equipamento = novoEquipamento();
        equipamento.alocar(CANTEIRO_ORIGEM, daquiADias(7), RESPONSAVEL, null);
        return equipamento;
    }

    private LocalDate daquiADias(long dias) {
        return LocalDate.now().plusDays(dias);
    }

    private void canteiroExisteNaObra(Long canteiroId, Long obraId) {
        when(canteiroPort.buscarResumo(canteiroId))
                .thenReturn(Optional.of(new CanteiroResumo(canteiroId, obraId, "Canteiro " + canteiroId)));
    }

    private void responsavelAtivo() {
        when(usuarioPort.buscarResumo(RESPONSAVEL))
                .thenReturn(Optional.of(new UsuarioResumo(RESPONSAVEL, "Maria", true)));
    }

    private void equipamentoExiste(Equipamento equipamento) {
        when(equipamentoRepository.findById(EQUIPAMENTO_ID)).thenReturn(Optional.of(equipamento));
    }

    private AlocarEquipamentoDto dtoAlocar(Long canteiroId) {
        return new AlocarEquipamentoDto(canteiroId, RESPONSAVEL, daquiADias(7), null);
    }

    private TransferirEquipamentoDto dtoTransferir(Long canteiroDestinoId) {
        return new TransferirEquipamentoDto(canteiroDestinoId, RESPONSAVEL, daquiADias(14), null);
    }

    // ---------- alocar ----------

    @Test
    @DisplayName("Aloca equipamento disponível em canteiro da obra")
    void alocaEquipamentoEmCanteiroDaObra() {
        Equipamento equipamento = novoEquipamento();
        equipamentoExiste(equipamento);
        canteiroExisteNaObra(CANTEIRO_ORIGEM, OBRA);
        responsavelAtivo();
        when(alocacaoRepository.save(any(AlocacaoEquipamento.class))).thenAnswer(chamada -> chamada.getArgument(0));

        AlocacaoEquipamentoDto resultado = service.alocar(OBRA, EQUIPAMENTO_ID, dtoAlocar(CANTEIRO_ORIGEM));

        assertThat(resultado.estaAtiva()).isTrue();
        assertThat(resultado.canteiroId()).isEqualTo(CANTEIRO_ORIGEM);
        assertThat(equipamento.getStatus()).isEqualTo(StatusEquipamento.ALOCADO);
    }

    @Test
    @DisplayName("Lança exceção quando o canteiro pertence a outra obra")
    void lancaExcecaoQuandoCanteiroDeOutraObra() {
        equipamentoExiste(novoEquipamento());
        canteiroExisteNaObra(CANTEIRO_ORIGEM, OUTRA_OBRA);

        assertThatThrownBy(() -> service.alocar(OBRA, EQUIPAMENTO_ID, dtoAlocar(CANTEIRO_ORIGEM)))
                .isInstanceOf(RegraNegocioException.class);
        verify(alocacaoRepository, never()).save(any(AlocacaoEquipamento.class));
    }

    @Test
    @DisplayName("Lança exceção quando o canteiro não existe")
    void lancaExcecaoQuandoCanteiroInexistente() {
        equipamentoExiste(novoEquipamento());
        when(canteiroPort.buscarResumo(CANTEIRO_ORIGEM)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.alocar(OBRA, EQUIPAMENTO_ID, dtoAlocar(CANTEIRO_ORIGEM)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Lança exceção quando o responsável está inativo")
    void lancaExcecaoQuandoResponsavelInativo() {
        equipamentoExiste(novoEquipamento());
        canteiroExisteNaObra(CANTEIRO_ORIGEM, OBRA);
        when(usuarioPort.buscarResumo(RESPONSAVEL))
                .thenReturn(Optional.of(new UsuarioResumo(RESPONSAVEL, "Maria", false)));

        assertThatThrownBy(() -> service.alocar(OBRA, EQUIPAMENTO_ID, dtoAlocar(CANTEIRO_ORIGEM)))
                .isInstanceOf(RegraNegocioException.class);
        verify(alocacaoRepository, never()).save(any(AlocacaoEquipamento.class));
    }

    @Test
    @DisplayName("Lança exceção quando equipamento já alocado")
    void lancaExcecaoQuandoEquipamentoJaAlocado() {
        equipamentoExiste(novoEquipamentoAlocadoNoCanteiroOrigem());
        canteiroExisteNaObra(CANTEIRO_DESTINO, OBRA);
        responsavelAtivo();

        assertThatThrownBy(() -> service.alocar(OBRA, EQUIPAMENTO_ID, dtoAlocar(CANTEIRO_DESTINO)))
                .isInstanceOf(RegraNegocioException.class);
        verify(alocacaoRepository, never()).save(any(AlocacaoEquipamento.class));
    }

    // ---------- devolver ----------

    @Test
    @DisplayName("Devolve equipamento alocado em canteiro da obra")
    void devolveEquipamentoAlocadoNaObra() {
        Equipamento equipamento = novoEquipamentoAlocadoNoCanteiroOrigem();
        equipamentoExiste(equipamento);
        canteiroExisteNaObra(CANTEIRO_ORIGEM, OBRA);

        AlocacaoEquipamentoDto resultado =
                service.devolver(OBRA, EQUIPAMENTO_ID, new DevolverEquipamentoDto("Sem avarias"));

        assertThat(resultado.estaAtiva()).isFalse();
        assertThat(resultado.observacaoDevolucao()).isEqualTo("Sem avarias");
        assertThat(equipamento.getStatus()).isEqualTo(StatusEquipamento.DISPONIVEL);
    }

    @Test
    @DisplayName("Lança exceção ao devolver equipamento alocado em outra obra")
    void lancaExcecaoQuandoDevolverEquipamentoAlocadoEmOutraObra() {
        Equipamento equipamento = novoEquipamentoAlocadoNoCanteiroOrigem();
        equipamentoExiste(equipamento);
        canteiroExisteNaObra(CANTEIRO_ORIGEM, OUTRA_OBRA);

        assertThatThrownBy(() -> service.devolver(OBRA, EQUIPAMENTO_ID, new DevolverEquipamentoDto(null)))
                .isInstanceOf(RecursoNaoEncontradoException.class);
        assertThat(equipamento.getStatus()).isEqualTo(StatusEquipamento.ALOCADO);
    }

    @Test
    @DisplayName("Lança exceção ao devolver equipamento disponível")
    void lancaExcecaoQuandoDevolverEquipamentoDisponivel() {
        equipamentoExiste(novoEquipamento());

        assertThatThrownBy(() -> service.devolver(OBRA, EQUIPAMENTO_ID, new DevolverEquipamentoDto(null)))
                .isInstanceOf(RegraNegocioException.class);
    }

    // ---------- transferir ----------

    @Test
    @DisplayName("Transfere para outro canteiro da obra gravando o encerramento antes da nova alocação")
    void transfereParaOutroCanteiroDaObraGravandoEncerramentoAntes() {
        Equipamento equipamento = novoEquipamentoAlocadoNoCanteiroOrigem();
        equipamentoExiste(equipamento);
        canteiroExisteNaObra(CANTEIRO_ORIGEM, OBRA);
        canteiroExisteNaObra(CANTEIRO_DESTINO, OBRA);
        responsavelAtivo();
        when(alocacaoRepository.save(any(AlocacaoEquipamento.class))).thenAnswer(chamada -> chamada.getArgument(0));

        AlocacaoEquipamentoDto resultado = service.transferir(OBRA, EQUIPAMENTO_ID, dtoTransferir(CANTEIRO_DESTINO));

        AlocacaoEquipamento nova = equipamento.alocacaoAtiva().orElseThrow();
        InOrder ordem = inOrder(alocacaoRepository);
        ordem.verify(alocacaoRepository).flush();
        ordem.verify(alocacaoRepository).save(nova);
        assertThat(resultado.canteiroId()).isEqualTo(CANTEIRO_DESTINO);
        assertThat(equipamento.getStatus()).isEqualTo(StatusEquipamento.ALOCADO);
    }

    @Test
    @DisplayName("Lança exceção ao transferir para canteiro de outra obra")
    void lancaExcecaoQuandoTransferirParaCanteiroDeOutraObra() {
        equipamentoExiste(novoEquipamentoAlocadoNoCanteiroOrigem());
        canteiroExisteNaObra(CANTEIRO_ORIGEM, OBRA);
        canteiroExisteNaObra(CANTEIRO_DESTINO, OUTRA_OBRA);

        assertThatThrownBy(() -> service.transferir(OBRA, EQUIPAMENTO_ID, dtoTransferir(CANTEIRO_DESTINO)))
                .isInstanceOf(RegraNegocioException.class);
        verify(alocacaoRepository, never()).flush();
        verify(alocacaoRepository, never()).save(any(AlocacaoEquipamento.class));
    }

    // ---------- prorrogar ----------

    @Test
    @DisplayName("Prorroga alocação de canteiro da obra")
    void prorrogaAlocacaoDaObra() {
        AlocacaoEquipamento alocacao = novoEquipamentoAlocadoNoCanteiroOrigem().alocacaoAtiva().orElseThrow();
        when(alocacaoRepository.findById(ALOCACAO_ID)).thenReturn(Optional.of(alocacao));
        canteiroExisteNaObra(CANTEIRO_ORIGEM, OBRA);
        LocalDate novaData = daquiADias(30);

        AlocacaoEquipamentoDto resultado = service.prorrogar(OBRA, ALOCACAO_ID, new ProrrogarAlocacaoDto(novaData));

        assertThat(resultado.dataPrevistaDevolucao()).isEqualTo(novaData);
    }

    @Test
    @DisplayName("Lança exceção ao prorrogar alocação de outra obra")
    void lancaExcecaoQuandoProrrogarAlocacaoDeOutraObra() {
        AlocacaoEquipamento alocacao = novoEquipamentoAlocadoNoCanteiroOrigem().alocacaoAtiva().orElseThrow();
        when(alocacaoRepository.findById(ALOCACAO_ID)).thenReturn(Optional.of(alocacao));
        canteiroExisteNaObra(CANTEIRO_ORIGEM, OUTRA_OBRA);

        assertThatThrownBy(() -> service.prorrogar(OBRA, ALOCACAO_ID, new ProrrogarAlocacaoDto(daquiADias(30))))
                .isInstanceOf(RecursoNaoEncontradoException.class);
    }

    // ---------- consultas ----------

    @Test
    @DisplayName("Lista as alocações ativas dos canteiros da obra")
    void listaAlocacoesAtivasDosCanteirosDaObra() {
        AlocacaoEquipamento alocacao = novoEquipamentoAlocadoNoCanteiroOrigem().alocacaoAtiva().orElseThrow();
        when(canteiroPort.listarIdsPorObra(OBRA)).thenReturn(List.of(CANTEIRO_ORIGEM));
        when(alocacaoRepository.listarAtivasPorCanteiros(List.of(CANTEIRO_ORIGEM))).thenReturn(List.of(alocacao));

        List<AlocacaoEquipamentoDto> resultado = service.listarAtivasDaObra(OBRA);

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).codigoPatrimonio()).isEqualTo("PAT-001");
    }

    @Test
    @DisplayName("Não consulta alocações quando a obra não tem canteiros")
    void naoConsultaAlocacoesQuandoObraNaoTemCanteiros() {
        when(canteiroPort.listarIdsPorObra(OBRA)).thenReturn(List.of());

        List<AlocacaoEquipamentoDto> resultado = service.listarAtivasDaObra(OBRA);

        assertThat(resultado).isEmpty();
        verify(alocacaoRepository, never()).listarAtivasPorCanteiros(any());
    }

    @Test
    @DisplayName("Lista o histórico do equipamento")
    void listaHistoricoDoEquipamento() {
        AlocacaoEquipamento alocacao = novoEquipamentoAlocadoNoCanteiroOrigem().alocacaoAtiva().orElseThrow();
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