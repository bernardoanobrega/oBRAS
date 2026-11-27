package br.com.obras.equipamento.controller;

/**
 * Substituto do bean {@code perm} nos testes de controller: nega tudo por padrão e cada teste libera
 * apenas o par módulo/ação esperado. Garante que o módulo e a ação de cada endpoint estão corretos.
 */
public class PermissaoFalsa {

    public boolean pode(Long obraId, String modulo, String acao) {
        return false;
    }

    public boolean podeGlobal(String modulo, String acao) {
        return false;
    }
}