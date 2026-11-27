package br.com.obras.equipamento.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.obras.shared.exception.RegraNegocioException;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class EquipamentoTest {

    private static final Long CANTEIRO_A = 10L;
    private static final Long CANTEIRO_B = 20L;
    private static final Long RESPONSAVEL = 5L;

    private Equipamento novoEquipamento() {
        return new Equipamento("PAT-001", "Betoneira 400L", TipoEquipamento.MAQUINA);
    }

    private LocalDate daquiADias(long dias) {
        return LocalDate.now().plusDays(dias);
    }

    // ---------- Criação e edição ----------

    @Test
    @DisplayName("Cria equipamento disponível com dados válidos")
    void criaEquipamentoDisponivelComDadosValidos() {
        Equipamento equipamento = novoEquipamento();

        assertThat(equipamento.getStatus()).isEqualTo(StatusEquipamento.DISPONIVEL);
        assertThat(equipamento.getCodigoPatrimonio()).isEqualTo("PAT-001");
        assertThat(equipamento.alocacaoAtiva()).isEmpty();
    }

    @Test
    @DisplayName("Lança exceção quando o código de patrimônio está vazio")
    void lancaExcecaoQuandoCodigoPatrimonioVazio() {
        assertThatThrownBy(() -> new Equipamento(" ", "Betoneira", TipoEquipamento.MAQUINA))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("Edita descrição e tipo de equipamento disponível")
    void editaDescricaoETipoQuandoDisponivel() {
        Equipamento equipamento = novoEquipamento();

        equipamento.editar("Furadeira de impacto", TipoEquipamento.FERRAMENTA);

        assertThat(equipamento.getDescricao()).isEqualTo("Furadeira de impacto");
        assertThat(equipamento.getTipo()).isEqualTo(TipoEquipamento.FERRAMENTA);
    }

    @Test
    @DisplayName("Lança exceção ao editar equipamento inativo")
    void lancaExcecaoQuandoEditarEquipamentoInativo() {
        Equipamento equipamento = novoEquipamento();
        equipamento.desativar();

        assertThatThrownBy(() -> equipamento.editar("Outra descrição", TipoEquipamento.OUTRO))
                .isInstanceOf(RegraNegocioException.class);
    }

    // ---------- Desativar e reativar ----------

    @Test
    @DisplayName("Desativa equipamento disponível")
    void desativaEquipamentoDisponivel() {
        Equipamento equipamento = novoEquipamento();

        equipamento.desativar();

        assertThat(equipamento.getStatus()).isEqualTo(StatusEquipamento.INATIVO);
    }

    @Test
    @DisplayName("Lança exceção ao desativar equipamento alocado")
    void lancaExcecaoQuandoDesativarEquipamentoAlocado() {
        Equipamento equipamento = novoEquipamento();
        equipamento.alocar(CANTEIRO_A, daquiADias(7), RESPONSAVEL, null);

        assertThatThrownBy(equipamento::desativar).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("Lança exceção ao desativar equipamento já inativo")
    void lancaExcecaoQuandoDesativarEquipamentoInativo() {
        Equipamento equipamento = novoEquipamento();
        equipamento.desativar();

        assertThatThrownBy(equipamento::desativar).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("Reativa equipamento inativo")
    void reativaEquipamentoInativo() {
        Equipamento equipamento = novoEquipamento();
        equipamento.desativar();

        equipamento.reativar();

        assertThat(equipamento.getStatus()).isEqualTo(StatusEquipamento.DISPONIVEL);
    }

    @Test
    @DisplayName("Lança exceção ao reativar equipamento disponível")
    void lancaExcecaoQuandoReativarEquipamentoDisponivel() {
        Equipamento equipamento = novoEquipamento();

        assertThatThrownBy(equipamento::reativar).isInstanceOf(RegraNegocioException.class);
    }

    // ---------- Alocar ----------

    @Test
    @DisplayName("Aloca equipamento disponível e registra a localização atual")
    void alocaEquipamentoDisponivel() {
        Equipamento equipamento = novoEquipamento();

        AlocacaoEquipamento alocacao = equipamento.alocar(CANTEIRO_A, daquiADias(7), RESPONSAVEL, "Fundação");

        assertThat(equipamento.getStatus()).isEqualTo(StatusEquipamento.ALOCADO);
        assertThat(equipamento.alocacaoAtiva()).contains(alocacao);
        assertThat(equipamento.localizacaoAtual()).contains(CANTEIRO_A);
        assertThat(alocacao.getResponsavelId()).isEqualTo(RESPONSAVEL);
    }

    @Test
    @DisplayName("Lança exceção quando equipamento já alocado")
    void lancaExcecaoQuandoEquipamentoJaAlocado() {
        Equipamento equipamento = novoEquipamento();
        equipamento.alocar(CANTEIRO_A, daquiADias(7), RESPONSAVEL, null);

        assertThatThrownBy(() -> equipamento.alocar(CANTEIRO_B, daquiADias(7), RESPONSAVEL, null))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("Lança exceção ao alocar equipamento inativo")
    void lancaExcecaoQuandoAlocarEquipamentoInativo() {
        Equipamento equipamento = novoEquipamento();
        equipamento.desativar();

        assertThatThrownBy(() -> equipamento.alocar(CANTEIRO_A, daquiADias(7), RESPONSAVEL, null))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("Lança exceção quando a devolução prevista é anterior ao início")
    void lancaExcecaoQuandoPrevisaoAnteriorAoInicio() {
        Equipamento equipamento = novoEquipamento();

        assertThatThrownBy(() -> equipamento.alocar(CANTEIRO_A, daquiADias(-1), RESPONSAVEL, null))
                .isInstanceOf(RegraNegocioException.class);
        assertThat(equipamento.getStatus()).isEqualTo(StatusEquipamento.DISPONIVEL);
    }

    // ---------- Devolver ----------

    @Test
    @DisplayName("Devolve equipamento alocado e volta para disponível")
    void devolveEquipamentoAlocado() {
        Equipamento equipamento = novoEquipamento();
        AlocacaoEquipamento alocacao = equipamento.alocar(CANTEIRO_A, daquiADias(7), RESPONSAVEL, null);

        equipamento.devolver("Sem avarias");

        assertThat(equipamento.getStatus()).isEqualTo(StatusEquipamento.DISPONIVEL);
        assertThat(equipamento.alocacaoAtiva()).isEmpty();
        assertThat(alocacao.estaAtiva()).isFalse();
        assertThat(alocacao.getObservacaoDevolucao()).isEqualTo("Sem avarias");
    }

    @Test
    @DisplayName("Lança exceção ao devolver equipamento disponível")
    void lancaExcecaoQuandoDevolverEquipamentoDisponivel() {
        Equipamento equipamento = novoEquipamento();

        assertThatThrownBy(() -> equipamento.devolver(null)).isInstanceOf(RegraNegocioException.class);
    }

    // ---------- Transferir ----------

    @Test
    @DisplayName("Transfere mantendo o equipamento alocado com uma única alocação ativa")
    void transfereMantendoEquipamentoAlocado() {
        Equipamento equipamento = novoEquipamento();
        AlocacaoEquipamento anterior = equipamento.alocar(CANTEIRO_A, daquiADias(7), RESPONSAVEL, null);

        AlocacaoEquipamento nova = equipamento.transferir(CANTEIRO_B, daquiADias(14), RESPONSAVEL, "Nova frente");

        assertThat(equipamento.getStatus()).isEqualTo(StatusEquipamento.ALOCADO);
        assertThat(anterior.estaAtiva()).isFalse();
        assertThat(equipamento.alocacaoAtiva()).contains(nova);
        assertThat(equipamento.localizacaoAtual()).contains(CANTEIRO_B);
    }

    @Test
    @DisplayName("Lança exceção ao transferir para o mesmo canteiro")
    void lancaExcecaoQuandoTransferirParaMesmoCanteiro() {
        Equipamento equipamento = novoEquipamento();
        equipamento.alocar(CANTEIRO_A, daquiADias(7), RESPONSAVEL, null);

        assertThatThrownBy(() -> equipamento.transferir(CANTEIRO_A, daquiADias(14), RESPONSAVEL, null))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("Lança exceção ao transferir equipamento disponível")
    void lancaExcecaoQuandoTransferirEquipamentoDisponivel() {
        Equipamento equipamento = novoEquipamento();

        assertThatThrownBy(() -> equipamento.transferir(CANTEIRO_B, daquiADias(14), RESPONSAVEL, null))
                .isInstanceOf(RegraNegocioException.class);
    }

    // ---------- Atraso ----------

    @Test
    @DisplayName("Está atrasado quando a data prevista já passou")
    void estaAtrasadoQuandoPrevisaoVencida() {
        Equipamento equipamento = novoEquipamento();
        equipamento.alocar(CANTEIRO_A, daquiADias(3), RESPONSAVEL, null);

        assertThat(equipamento.estaAtrasado(daquiADias(4))).isTrue();
        assertThat(equipamento.estaAtrasado(daquiADias(3))).isFalse();
    }

    @Test
    @DisplayName("Não está atrasado quando não há alocação ativa")
    void naoEstaAtrasadoQuandoDisponivel() {
        assertThat(novoEquipamento().estaAtrasado(daquiADias(365))).isFalse();
    }
}