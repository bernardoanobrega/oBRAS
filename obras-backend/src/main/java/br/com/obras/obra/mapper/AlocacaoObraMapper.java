package br.com.obras.obra.mapper;

import br.com.obras.obra.domain.AlocacaoObra;
import br.com.obras.obra.dto.AlocacaoObraDto;
import org.springframework.stereotype.Component;

@Component
public class AlocacaoObraMapper {
    public AlocacaoObraDto paraDto(AlocacaoObra alocacao) {
        return AlocacaoObraDto.de(alocacao);
    }
}
