package br.com.obras.diario.dto;

import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;

public record FotoDownloadDto(Resource resource, MediaType mediaType, String nomeArquivo) {}
