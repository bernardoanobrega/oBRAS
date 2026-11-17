package br.com.obras.compras.domain;

public enum StatusPedido {
    SOLICITADO, EM_COTACAO, COMPRADO, EM_TRANSITO, ENTREGUE, CANCELADO;

    /** Transições do diagrama de estados do PedidoCompra. */
    public boolean podeIrPara(StatusPedido destino) {
        return switch (this) {
            case SOLICITADO -> destino == EM_COTACAO || destino == CANCELADO;
            case EM_COTACAO -> destino == COMPRADO || destino == CANCELADO;
            case COMPRADO -> destino == EM_TRANSITO;
            case EM_TRANSITO -> destino == ENTREGUE;
            case ENTREGUE, CANCELADO -> false;
        };
    }
}
