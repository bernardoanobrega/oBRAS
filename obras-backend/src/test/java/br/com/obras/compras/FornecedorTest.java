package br.com.obras.compras;

import br.com.obras.compras.domain.Fornecedor;
import br.com.obras.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FornecedorTest {
    @Test
    void cadastraAtivoENormalizaDocumento() {
        Fornecedor f = Fornecedor.cadastrar("ACME", "12.345.678/0001-99", null, null);
        assertTrue(f.estaAtivo());
        assertEquals("12345678000199", f.getCpfCnpj());
    }

    @Test
    void aceitaCpfComMascara() {
        assertEquals("12345678901", Fornecedor.normalizarDocumento("123.456.789-01"));
    }

    @Test
    void rejeitaDocumentoComTamanhoInvalido() {
        assertThrows(RegraNegocioException.class, () -> Fornecedor.cadastrar("ACME", "123", null, null));
    }

    @Test
    void rejeitaNomeVazio() {
        assertThrows(RegraNegocioException.class, () -> Fornecedor.cadastrar(" ", "12345678901", null, null));
    }

    @Test
    void desativarNaoApaga() {
        Fornecedor f = Fornecedor.cadastrar("ACME", "12345678901", null, null);
        f.desativar();
        assertFalse(f.estaAtivo());
    }
}
