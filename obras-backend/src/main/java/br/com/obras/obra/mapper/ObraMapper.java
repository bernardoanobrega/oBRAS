package br.com.obras.obra.mapper;

import br.com.obras.auditoria.domain.Auditavel;
import br.com.obras.obra.domain.Obra;
import br.com.obras.obra.dto.NovaObraDto;
import br.com.obras.obra.dto.ObraDto;
import org.springframework.stereotype.Component;

@Component
public class ObraMapper {

    public ObraDto paraDto(Obra obra) {
        return ObraDto.de(obra);
    }

    public ObraDto paraDto(Obra obra, String responsavelNome) {
        return ObraDto.de(obra, responsavelNome);
    }

    public Obra paraEntidade(NovaObraDto dto, Auditavel auditavel) {
        return Obra.cadastrar(
            dto.nome(),
            dto.descricao(),
            dto.tipologia(),
            dto.endereco(),
            dto.inicioPrevisto(),
            dto.fimPrevisto(),
            dto.art(),
            dto.responsavelId(),
            auditavel
        );
    }
}
