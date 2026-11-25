package br.com.obras.estoque.service;

import br.com.obras.estoque.domain.Material;
import br.com.obras.estoque.dto.MaterialDto;
import br.com.obras.estoque.dto.MaterialRequest;
import br.com.obras.estoque.repository.MaterialRepository;
import br.com.obras.shared.exception.RecursoNaoEncontradoException;
import br.com.obras.shared.exception.RegraNegocioException;
import br.com.obras.shared.port.MaterialPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Catálogo de materiais. Também implementa MaterialPort, usado pelo módulo compras. */
@Service
public class MaterialService implements MaterialPort {
    private final MaterialRepository materiais;

    public MaterialService(MaterialRepository materiais) { this.materiais = materiais; }

    @Transactional
    public MaterialDto criar(MaterialRequest dto) {
        if (materiais.existsByNomeIgnoreCaseAndUnidadeMedidaIgnoreCase(dto.nome().trim(), dto.unidadeMedida().trim())) {
            throw new RegraNegocioException("Já existe um material com este nome e unidade de medida");
        }
        return MaterialDto.de(materiais.save(Material.cadastrar(dto.nome(), dto.descricao(), dto.unidadeMedida())));
    }

    @Transactional
    public MaterialDto atualizar(Long id, MaterialRequest dto) {
        Material material = obter(id);
        if (materiais.existsByNomeIgnoreCaseAndUnidadeMedidaIgnoreCaseAndIdNot(
                dto.nome().trim(), dto.unidadeMedida().trim(), id)) {
            throw new RegraNegocioException("Já existe um material com este nome e unidade de medida");
        }
        material.atualizar(dto.nome(), dto.descricao(), dto.unidadeMedida());
        return MaterialDto.de(material);
    }

    @Transactional(readOnly = true)
    public List<MaterialDto> listar(String busca) {
        List<Material> lista = (busca == null || busca.isBlank())
                ? materiais.findAllByOrderByNomeAsc()
                : materiais.findByNomeContainingIgnoreCaseOrderByNomeAsc(busca.trim());
        return lista.stream().map(MaterialDto::de).toList();
    }

    @Transactional(readOnly = true)
    public MaterialDto buscarDto(Long id) { return MaterialDto.de(obter(id)); }

    @Override
    @Transactional(readOnly = true)
    public Optional<MaterialResumo> buscar(Long materialId) {
        return materiais.findById(materialId).map(MaterialService::resumo);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, MaterialResumo> buscarVarios(Collection<Long> materialIds) {
        if (materialIds.isEmpty()) return Map.of();
        return materiais.findAllById(materialIds).stream()
                .map(MaterialService::resumo)
                .collect(Collectors.toMap(MaterialResumo::id, Function.identity()));
    }

    private Material obter(Long id) {
        return materiais.findById(id).orElseThrow(() -> new RecursoNaoEncontradoException("Material", id));
    }

    private static MaterialResumo resumo(Material m) {
        return new MaterialResumo(m.getId(), m.getNome(), m.getUnidadeMedida());
    }
}
