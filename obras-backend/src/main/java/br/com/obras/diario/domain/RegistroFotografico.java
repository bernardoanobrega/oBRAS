package br.com.obras.diario.domain;

import br.com.obras.auditoria.domain.Auditavel;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "registro_fotografico")
@Getter
@NoArgsConstructor
public class RegistroFotografico {
    @Id
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "id")
    private Auditavel auditavel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ocorrencia_id", nullable = false)
    private Ocorrencia ocorrencia;

    @Column(name = "arquivo", nullable = false, length = 500)
    private String arquivo;

    @Column(name = "legenda", length = 300)
    private String legenda;

    @Column(name = "data_envio", nullable = false)
    private LocalDateTime dataEnvio;

    public static RegistroFotografico criar(
        Auditavel auditavel,
        Ocorrencia ocorrencia,
        String arquivo,
        String legenda
    ) {
        RegistroFotografico f = new RegistroFotografico();
        f.auditavel = auditavel;
        f.ocorrencia = ocorrencia;
        f.arquivo = arquivo;
        f.legenda = legenda;
        f.dataEnvio = LocalDateTime.now();
        return f;
    }
}
