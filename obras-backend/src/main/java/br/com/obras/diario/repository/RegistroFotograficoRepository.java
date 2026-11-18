package br.com.obras.diario.repository;

import br.com.obras.diario.domain.RegistroFotografico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface RegistroFotograficoRepository extends JpaRepository<RegistroFotografico, Long> {
    @Query("""
        select f from RegistroFotografico f
        join f.auditavel a
        where f.id = :fotoId
          and f.ocorrencia.registroDiario.obra.id = :obraId
          and a.excluido = false
    """)
    Optional<RegistroFotografico> buscar(@Param("obraId") Long obraId, @Param("fotoId") Long fotoId);

    @Query("""
        select f from RegistroFotografico f
        join f.auditavel a
        where f.ocorrencia.id = :ocorrenciaId
          and a.excluido = false
        order by f.dataEnvio desc
    """)
    List<RegistroFotografico> listarPorOcorrencia(@Param("ocorrenciaId") Long ocorrenciaId);
}
