package br.com.obras.equipamento.controller;

/** Expressões de autorização do módulo, para que módulo e ação fiquem em um único lugar. */
final class PermissoesEquipamento {

    static final String MODULO = "MAQUINAS_EQUIPAMENTOS";

    static final String VISUALIZAR_NA_OBRA = "@perm.pode(#obraId, '" + MODULO + "', 'VISUALIZAR')";
    static final String CRIAR_NA_OBRA = "@perm.pode(#obraId, '" + MODULO + "', 'CRIAR')";
    static final String EDITAR_NA_OBRA = "@perm.pode(#obraId, '" + MODULO + "', 'EDITAR')";

    /** Transferir exige editar na obra de origem (rota) e criar na obra de destino (corpo). */
    static final String TRANSFERIR_ENTRE_OBRAS =
            EDITAR_NA_OBRA + " and @perm.pode(#dto.obraDestinoId(), '" + MODULO + "', 'CRIAR')";

    static final String VISUALIZAR_GLOBAL = "@perm.podeGlobal('" + MODULO + "', 'VISUALIZAR')";
    static final String CRIAR_GLOBAL = "@perm.podeGlobal('" + MODULO + "', 'CRIAR')";
    static final String EDITAR_GLOBAL = "@perm.podeGlobal('" + MODULO + "', 'EDITAR')";
    static final String EXCLUIR_GLOBAL = "@perm.podeGlobal('" + MODULO + "', 'EXCLUIR')";

    private PermissoesEquipamento() {
    }
}