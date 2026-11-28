import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

public final class GerarFixturesFoto {
    private GerarFixturesFoto() {}

    public static void main(String[] args) throws IOException {
        Path destino = Path.of("src/test/resources/diario/fotos");
        Files.createDirectories(destino);

        BufferedImage imagem = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        imagem.setRGB(0, 0, Color.WHITE.getRGB());
        imagem.setRGB(1, 0, Color.RED.getRGB());
        imagem.setRGB(0, 1, Color.GREEN.getRGB());
        imagem.setRGB(1, 1, Color.BLUE.getRGB());
        if (!ImageIO.write(imagem, "jpg", destino.resolve("valida-1x1.jpg").toFile())) {
            throw new IOException("Writer JPEG indisponível na JDK");
        }
        if (!ImageIO.write(imagem, "png", destino.resolve("valida-1x1.png").toFile())) {
            throw new IOException("Writer PNG indisponível na JDK");
        }

        String pdf = """
                %PDF-1.4
                1 0 obj<</Type/Catalog/Pages 2 0 R>>endobj
                2 0 obj<</Type/Pages/Count 0>>endobj
                trailer<</Root 1 0 R>>
                %%EOF
                """;
        Files.write(destino.resolve("invalido.pdf"), pdf.getBytes(StandardCharsets.US_ASCII));
        Files.write(destino.resolve("extensao-falsa.jpg"),
                "isto nao e uma imagem".getBytes(StandardCharsets.UTF_8));

        byte[] grande = new byte[10 * 1024 * 1024 + 3];
        grande[0] = (byte) 0xFF;
        grande[1] = (byte) 0xD8;
        grande[2] = (byte) 0xFF;
        Arrays.fill(grande, 3, grande.length, (byte) '0');
        Files.write(destino.resolve("acima-de-10mb.jpg"), grande);

        System.out.println("Fixtures gravadas em " + destino.toAbsolutePath());
    }
}
