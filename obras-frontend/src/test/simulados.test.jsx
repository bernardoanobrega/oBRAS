import { describe, expect, it } from 'vitest';
import { renderToStaticMarkup } from 'react-dom/server';
import { paraClimaDia, paraFoto, paraOcorrencia } from '../features/obras/api/adaptadoresDiario.js';
import ClimaDiaCard from '../features/obras/components/detalhe/ClimaDiaCard.jsx';
import OcorrenciasRecentes from '../features/obras/components/detalhe/OcorrenciasRecentes.jsx';
import MuralFotos from '../features/obras/components/detalhe/MuralFotos.jsx';

const clima = paraClimaDia({ data: '2026-11-18', clima: 'ENSOLARADO' });
const ocorrencia = paraOcorrencia({
  id: 4,
  categoria: 'LOGISTICA',
  descricao: 'Atraso na entrega de aço',
  dataHora: '2026-11-18T10:30:00',
  paralisacao: null,
  fotos: [],
});
const foto = paraFoto({ id: 6, ocorrenciaId: 4, nomeArquivo: 'foto.png', legenda: 'Foto real', dataEnvio: null });

describe('cards com dados reais', () => {
  it('clima não sinaliza dado simulado', () => {
    const html = renderToStaticMarkup(<ClimaDiaCard clima={clima} />);
    expect(html).toContain('Ensolarado');
    expect(html).not.toContain('SIMULADO');
  });

  it('ocorrências não sinalizam dado simulado', () => {
    const html = renderToStaticMarkup(<OcorrenciasRecentes ocorrencias={[ocorrencia]} />);
    expect(html).toContain(ocorrencia.texto);
    expect(html).not.toContain('SIMULADO');
  });

  it('mural não sinaliza dado simulado', () => {
    const html = renderToStaticMarkup(<MuralFotos obraId="2" fotos={[foto]} />);
    expect(html).toContain(foto.legenda);
    expect(html).not.toContain('SIMULADO');
  });
});
