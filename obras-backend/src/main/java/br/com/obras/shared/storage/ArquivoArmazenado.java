package br.com.obras.shared.storage;

/** Corresponde ao ArquivoDocumento (@Embeddable) do diagrama. */
public record ArquivoArmazenado(String nomeOriginal, String formato, long tamanhoBytes, String localizador) {}
