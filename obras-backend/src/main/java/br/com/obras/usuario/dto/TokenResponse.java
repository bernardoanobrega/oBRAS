package br.com.obras.usuario.dto;

public record TokenResponse(String token, String tipo, long expiraEmSegundos, UsuarioDto usuario) {}
