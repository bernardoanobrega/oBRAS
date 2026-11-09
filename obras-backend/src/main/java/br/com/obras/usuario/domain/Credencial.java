package br.com.obras.usuario.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
public class Credencial {
    @Column(name = "login", nullable = false, length = 100)
    private String login;

    @Column(name = "senha_hash", nullable = false)
    private String senhaHash;

    public Credencial(String login, String senhaHash) {
        this.login = login;
        this.senhaHash = senhaHash;
    }
}
