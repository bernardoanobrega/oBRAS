package br.com.obras.diario;

import br.com.obras.shared.storage.ArquivoStorageService;
import org.springframework.mock.web.MockMultipartFile;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class FotoFixtures {
    private static final byte[] JPEG = imagem("jpg");
    private static final byte[] PNG = imagem("png");
    private static final byte[] PDF = """
            %PDF-1.4
            1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj
            2 0 obj<</Type/Pages/Count 0>>endobj
            trailer<</Root 1 0 R>>
            %%EOF
            """.getBytes(StandardCharsets.US_ASCII);
    private static final byte[] EXTENSAO_FALSA = "isto nao e uma imagem".getBytes(StandardCharsets.UTF_8);

    private FotoFixtures() {}

    public static byte[] jpg() {
        return JPEG.clone();
    }

    public static byte[] png() {
        return PNG.clone();
    }

    public static byte[] pdf() {
        return PDF.clone();
    }

    public static byte[] extensaoFalsa() {
        return EXTENSAO_FALSA.clone();
    }

    public static byte[] acimaDe10Mb() {
        byte[] bytes = new byte[(int) ArquivoStorageService.TAMANHO_MAXIMO + 1];
        bytes[0] = (byte) 0xFF;
        bytes[1] = (byte) 0xD8;
        bytes[2] = (byte) 0xFF;
        Arrays.fill(bytes, 3, bytes.length, (byte) '0');
        return bytes;
    }

    public static MockMultipartFile jpg(String nome) {
        return multipart(nome, "image/jpeg", jpg());
    }

    public static MockMultipartFile png(String nome) {
        return multipart(nome, "image/png", png());
    }

    public static MockMultipartFile pdf(String nome) {
        return multipart(nome, "application/pdf", pdf());
    }

    public static MockMultipartFile extensaoFalsa(String nome) {
        return multipart(nome, "image/jpeg", extensaoFalsa());
    }

    public static MockMultipartFile acimaDe10Mb(String nome) {
        return multipart(nome, "image/jpeg", acimaDe10Mb());
    }

    public static MockMultipartFile multipart(String nome, String contentType, byte[] conteudo) {
        return new MockMultipartFile("arquivo", nome, contentType, conteudo);
    }

    private static byte[] imagem(String formato) {
        BufferedImage imagem = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        imagem.setRGB(0, 0, Color.WHITE.getRGB());
        imagem.setRGB(1, 0, Color.RED.getRGB());
        imagem.setRGB(0, 1, Color.GREEN.getRGB());
        imagem.setRGB(1, 1, Color.BLUE.getRGB());
        try (var out = new ByteArrayOutputStream()) {
            if (!ImageIO.write(imagem, formato, out)) {
                throw new IllegalStateException("Writer " + formato + " indisponível na JDK");
            }
            return out.toByteArray();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
