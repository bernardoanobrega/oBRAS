package br.com.obras.shared.domain;

import java.util.List;

/** Consulta mínima de obras usada pelo repositório documental, sem depender de repositories externos. */
public interface ObraDocumentalPort {
    record ObraResumo(Long id, String nome) {}
    List<ObraResumo> listar();
    boolean existe(Long id);
}
