package br.com.obras.shared.port;

import java.util.Optional;

/** Acesso de outros módulos a obras. O adapter fica no módulo obra. */
public interface ObraPort {

    Optional<ObraResumo> buscarResumo(Long obraId);
}