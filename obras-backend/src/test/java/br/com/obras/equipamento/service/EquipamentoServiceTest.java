package br.com.obras.equipamento.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.obras.equipamento.domain.Equipamento;
import br.com.obras.equipamento.domain.StatusEquipamento;
import br.com.obras.equipamento.domain.TipoEquipamento;
import br.com.obras.equipamento.dto.EquipamentoCriacaoDto;
import br.com.obras.equipamento.dto.EquipamentoDto;
import br.com.obras.equipamento.dto.EquipamentoEdicaoDto;
import br.com.obras.equipamento.repository.EquipamentoRepository;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class EquipamentoServiceTest {

    private static final Long ID = 1L;

    @Mock
    private EquipamentoRepository equipamentoRepository;

    @InjectMocks
    private EquipamentoService service;

    private Equipamento novoEquipamento() {
        return new Equipamento("PAT-001", "Betoneira 400L", TipoEquipamento.MAQUINA);
    }

    @Test
    @DisplayName("Cadastra equipamento disponível quando o código de patrimônio é inédito")
    void cadastraEquipamentoDisponivelQuandoCodigoInedito() {
        when(equipamentoRepository.existsByCodigoPatrimonio("PAT-001")).thenReturn(false);
        when(equipamentoRepository.save(any(Equipamento.class))).thenAnswer(chamada -> chamada.getArgument(0));

        EquipamentoDto resultado = service.cadastrar(
                new EquipamentoCriacaoDto(" PAT-001 ", "Betoneira 400L", TipoEquipamento.MAQUINA));

        assertThat(resultado.codigoPatrimonio()).isEqualTo("PAT-001");
        assertThat(resultado.status()).isEqualTo(StatusEquipamento.DISPONIVEL);
    }

    @Test
    @DisplayName("Lança exceção quando o código de patrimônio já está cadastrado")
    void lancaExcecaoQuandoCodigoPatrimonioJaCadastrado() {
        when(equipamentoRepository.existsByCodigoPatrimonio("PAT-001")).thenReturn(true);

        assertThatThrownBy(() -> service.cadastrar(
                new EquipamentoCriacaoDto("PAT-001", "Betoneira 400L", TipoEquipamento.MAQUINA)))
                .isInstanceOf(RegraNegocioException.class);
        verify(equipamentoRepository, never()).save(any(Equipamento.class));
    }

    @Test
    @DisplayName("Edita descrição e tipo do equipamento")
    void editaDescricaoETipo() {
        when(equipamentoRepository.findById(ID)).thenReturn(Optional.of(novoEquipamento()));

        EquipamentoDto resultado = service.editar(ID,
                new EquipamentoEdicaoDto("Furadeira de impacto", TipoEquipamento.FERRAMENTA));

        assertThat(resultado.descricao()).isEqualTo("Furadeira de impacto");
        assertThat(resultado.tipo()).isEqualTo(TipoEquipamento.FERRAMENTA);
    }

    @Test
    @DisplayName("Lança exceção quando o equipamento não é encontrado")
    void lancaExcecaoQuandoEquipamentoNaoEncontrado() {
        when(equipamentoRepository.findById(ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.buscar(ID)).isInstanceOf(RecursoNaoEncontradoException.class);
    }

    @Test
    @DisplayName("Desativa e reativa o equipamento")
    void desativaEReativaEquipamento() {
        when(equipamentoRepository.findById(ID)).thenReturn(Optional.of(novoEquipamento()));

        EquipamentoDto desativado = service.desativar(ID);
        EquipamentoDto reativado = service.reativar(ID);

        assertThat(desativado.status()).isEqualTo(StatusEquipamento.INATIVO);
        assertThat(reativado.status()).isEqualTo(StatusEquipamento.DISPONIVEL);
    }

    @Test
    @DisplayName("Lança exceção ao desativar equipamento alocado")
    void lancaExcecaoQuandoDesativarEquipamentoAlocado() {
        Equipamento equipamento = novoEquipamento();
        equipamento.alocar(10L, LocalDate.now().plusDays(7), 5L, null);
        when(equipamentoRepository.findById(ID)).thenReturn(Optional.of(equipamento));

        assertThatThrownBy(() -> service.desativar(ID)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("Lista sem filtro de texto quando a busca não é informada")
    void listaSemFiltroDeTextoQuandoBuscaNaoInformada() {
        Pageable pagina = PageRequest.of(0, 10);
        Page<Equipamento> resultado = new PageImpl<>(List.of(novoEquipamento()), pagina, 1);
        when(equipamentoRepository.buscar(null, null, "", pagina)).thenReturn(resultado);

        Page<EquipamentoDto> equipamentos = service.listar(null, null, null, pagina);

        assertThat(equipamentos.getContent()).hasSize(1);
    }
}