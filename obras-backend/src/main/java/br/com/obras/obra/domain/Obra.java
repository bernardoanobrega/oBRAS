package br.com.obras.obra.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "obra")
@Getter
@NoArgsConstructor
public class Obra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String nome;

    @Column(length = 1000)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Tipologia tipologia;

    @Column(nullable = false, length = 300)
    private String endereco;

    @Column(name = "inicio_previsto", nullable = false)
    private LocalDate inicioPrevisto;

    @Column(name = "fim_previsto", nullable = false)
    private LocalDate fimPrevisto;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusObra status;

    @Column(length = 50)
    private String art;

    @Column(name = "responsavel_id", nullable = false)
    private Long responsavelId;
}