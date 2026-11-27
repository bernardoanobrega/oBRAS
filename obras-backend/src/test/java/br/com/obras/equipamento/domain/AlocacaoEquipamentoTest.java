package br.com.obras.equipamento.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import br.com.obras.shared.exception.RegraNegocioException;
import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class AlocacaoEquipamentoTest {

    private static final Long CANTEIRO = 10L;
    private static final Long RESPONSAVEL = 5L;

    private AlocacaoEquipamento novaAlocacaoAtiva(LocalDate dataPrevista) {
        var equipamento = new Equipamento("PAT-001", "Betoneira 400L", TipoEquipamento.MAQUINA);
        return equipamento.alocar(CANTEIRO, dataPrevista, RESPONSAVEL, null);
    }

    @Test
    @DisplayName("Prorroga alocação ativa para data posterior")
    void prorrogaAlocacaoAtivaParaDataPosterior() {
        AlocacaoEquipamento alocacao = novaAlocacaoAtiva(LocalDate.now().plusDays(5));
        LocalDate novaData = LocalDate.now().plusDays(10);

        alocacao.prorrogar(novaData);

        assertThat(alocacao.getDataPrevistaDevolucao()).isEqualTo(novaData);
    }

    @Test
    @DisplayName("Lança exceção quando a nova data não é posterior à atual")
    void lancaExcecaoQuandoNovaDataNaoPosterior() {
        LocalDate dataPrevista = LocalDate.now().plusDays(5);
        AlocacaoEquipamento alocacao = novaAlocacaoAtiva(dataPrevista);

        assertThatThrownBy(() -> alocacao.prorrogar(dataPrevista)).isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("Lança exceção ao prorrogar alocação encerrada")
    void lancaExcecaoQuandoProrrogarAlocacaoEncerrada() {
        AlocacaoEquipamento alocacao = novaAlocacaoAtiva(LocalDate.now().plusDays(5));
        alocacao.encerrar(LocalDate.now(), null);

        assertThatThrownBy(() -> alocacao.prorrogar(LocalDate.now().plusDays(20)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("Encerra alocação ativa registrando data de fim e observação")
    void encerraAlocacaoAtiva() {
        AlocacaoEquipamento alocacao = novaAlocacaoAtiva(LocalDate.now().plusDays(5));

        alocacao.encerrar(LocalDate.now(), "Devolvido ao almoxarifado");

        assertThat(alocacao.estaAtiva()).isFalse();
        assertThat(alocacao.getDataFim()).isEqualTo(LocalDate.now());
        assertThat(alocacao.getObservacaoDevolucao()).isEqualTo("Devolvido ao almoxarifado");
    }

    @Test
    @DisplayName("Lança exceção ao encerrar com data anterior ao início")
    void lancaExcecaoQuandoEncerrarComDataAnteriorAoInicio() {
        AlocacaoEquipamento alocacao = novaAlocacaoAtiva(LocalDate.now().plusDays(5));

        assertThatThrownBy(() -> alocacao.encerrar(LocalDate.now().minusDays(1), null))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("Lança exceção ao encerrar alocação já encerrada")
    void lancaExcecaoQuandoEncerrarAlocacaoJaEncerrada() {
        AlocacaoEquipamento alocacao = novaAlocacaoAtiva(LocalDate.now().plusDays(5));
        alocacao.encerrar(LocalDate.now(), null);

        assertThatThrownBy(() -> alocacao.encerrar(LocalDate.now(), null))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    @DisplayName("Está atrasada somente enquanto ativa e após a data prevista")
    void estaAtrasadaSomenteQuandoAtivaEPrevisaoVencida() {
        AlocacaoEquipamento alocacao = novaAlocacaoAtiva(LocalDate.now().plusDays(2));

        assertThat(alocacao.estaAtrasada(LocalDate.now().plusDays(3))).isTrue();
        assertThat(alocacao.estaAtrasada(LocalDate.now().plusDays(2))).isFalse();

        alocacao.encerrar(LocalDate.now(), null);

        assertThat(alocacao.estaAtrasada(LocalDate.now().plusDays(30))).isFalse();
    }
}