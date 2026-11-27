package br.com.obras.shared.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Raiz da hierarquia de entidades auditáveis (herança JOINED). Autoria e edições ficam em
 * {@code registro_auditoria}; entidades que nunca são excluídas não usam {@code excluido}.
 */
@Entity
@Table(name = "auditavel")
@Inheritance(strategy = InheritanceType.JOINED)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class Auditavel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private boolean excluido;
}