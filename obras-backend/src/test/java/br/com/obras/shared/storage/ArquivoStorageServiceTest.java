package br.com.obras.shared.storage;

import br.com.obras.diario.FotoFixtures;
import br.com.obras.shared.exception.RegraNegocioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ArquivoStorageServiceTest {
    @TempDir Path diretorio;

    private ArquivoStorageService storage;

    @BeforeEach
    void criarStorage() {
        storage = new ArquivoStorageService(diretorio.toString());
    }

    @Test
    void aceitaJpegPelosMagicBytes() throws Exception {
        byte[] bytes = FotoFixtures.jpg();

        assertAll(
                () -> assertEquals(0xFF, bytes[0] & 0xFF),
                () -> assertEquals(0xD8, bytes[1] & 0xFF),
                () -> assertEquals(0xFF, bytes[2] & 0xFF)
        );

        ArquivoArmazenado armazenado = storage.armazenar(FotoFixtures.multipart("fachada.jpg", "image/jpeg", bytes));
        Path salvo = diretorio.resolve(armazenado.localizador());

        assertAll(
                () -> assertEquals("JPG", armazenado.formato()),
                () -> assertEquals(bytes.length, armazenado.tamanhoBytes()),
                () -> assertTrue(Files.exists(salvo)),
                () -> assertArrayEquals(bytes, Files.readAllBytes(salvo))
        );
    }

    @Test
    void aceitaPngPelosMagicBytes() throws Exception {
        byte[] bytes = FotoFixtures.png();
        byte[] assinatura = new byte[]{0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};

        assertArrayEquals(assinatura, Arrays.copyOfRange(bytes, 1, 8));

        ArquivoArmazenado armazenado = storage.armazenar(FotoFixtures.multipart("fachada.png", "image/png", bytes));
        Path salvo = diretorio.resolve(armazenado.localizador());

        assertAll(
                () -> assertEquals((byte) 0x89, bytes[0]),
                () -> assertEquals("PNG", armazenado.formato()),
                () -> assertTrue(Files.exists(salvo)),
                () -> assertArrayEquals(bytes, Files.readAllBytes(salvo))
        );
    }

    @Test
    void rejeitaArquivoAcimaDe10Mb() throws Exception {
        byte[] bytes = FotoFixtures.acimaDe10Mb();
        assertTrue(bytes.length > ArquivoStorageService.TAMANHO_MAXIMO);

        RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                () -> storage.armazenar(FotoFixtures.multipart("grande.jpg", "image/jpeg", bytes)));

        assertEquals("Arquivo excede o limite de 10 MB", erro.getMessage());
        assertEquals(0, arquivos().size());
    }

    @Test
    void rejeitaPathTraversal() throws Exception {
        Path fora = diretorio.resolveSibling("foto-fora-" + UUID.randomUUID() + ".jpg");
        Files.writeString(fora, "segredo");
        String relativo = "../" + fora.getFileName();
        try {
            RegraNegocioException erro = assertThrows(RegraNegocioException.class,
                    () -> storage.carregar(relativo));
            assertEquals("Arquivo não encontrado", erro.getMessage());

            storage.descartar(relativo);
            assertTrue(Files.exists(fora), "descartar não pode sair da base");

            ArquivoArmazenado armazenado = storage.armazenar(
                    FotoFixtures.multipart("../ escapado.jpg", "image/jpeg", FotoFixtures.jpg()));
            Path salvo = diretorio.resolve(armazenado.localizador()).normalize();
            assertTrue(salvo.startsWith(diretorio.toAbsolutePath().normalize()));
        } finally {
            Files.deleteIfExists(fora);
        }
    }

    private java.util.List<Path> arquivos() throws IOException {
        try (var fluxo = Files.walk(diretorio)) {
            return fluxo.filter(Files::isRegularFile).toList();
        }
    }
}
