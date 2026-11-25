package br.com.obras.orcamento.domain;

/** Estados do diagrama orcamento-states. CANCELADO é final. */
public enum StatusOrcamento {
    ELABORACAO,
    SAUDAVEL,
    ATENCAO,
    CRITICA,
    CANCELADO;

    public static StatusOrcamento de(SaudeFinanceira saude) {
        return switch (saude) {
            case SAUDAVEL -> SAUDAVEL;
            case ATENCAO -> ATENCAO;
            case CRITICA -> CRITICA;
        };
    }
}
