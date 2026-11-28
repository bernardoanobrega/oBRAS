package br.com.obras.obra.service;

import br.com.obras.obra.domain.Obra;
import br.com.obras.obra.domain.StatusObra;
import br.com.obras.obra.repository.ObraRepository;
import br.com.obras.shared.port.ObraPort;
import br.com.obras.shared.port.ObraResumo;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Expõe dados mínimos de obra a outros módulos. Obras excluídas logicamente não são retornadas. */
@Component
@RequiredArgsConstructor
public class ObraAdapter implements ObraPort {

    private final ObraRepository obraRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<ObraResumo> buscarResumo(Long obraId) {
        return obraRepository.buscarAtiva(obraId)
                .map(obra -> new ObraResumo(obra.getId(), obra.getNome(), estaEncerrada(obra)));
    }

    private static boolean estaEncerrada(Obra obra) {
        return obra.getStatus() == StatusObra.CONCLUIDA || obra.getStatus() == StatusObra.CANCELADA;
    }
}