import { describe, expect, it } from 'vitest';
import { renderToStaticMarkup } from 'react-dom/server';
import CampoSenha from '../shared/components/ui/CampoSenha.jsx';

const render = (props = {}) =>
  renderToStaticMarkup(<CampoSenha label="Senha" name="senha" value="segredo" onChange={() => {}} {...props} />);

describe('CampoSenha', () => {
  it('senhaComecaOcultaComBotaoMostrar', () => {
    const html = render();
    expect(html).toContain('type="password"');
    expect(html).toContain('aria-label="Mostrar senha"');
  });

  it('senhaVisivelViraTextoComBotaoOcultar', () => {
    const html = render({ visivelInicial: true });
    expect(html).toContain('type="text"');
    expect(html).toContain('aria-label="Ocultar senha"');
  });

  it('typeRecebidoNaoSobrescreveOControleDoBotao', () => {
    expect(render({ type: 'text' })).toContain('type="password"');
  });

  it('botaoNaoEnviaOFormulario', () => {
    // type="button": clicar no olho não dispara o login
    expect(render()).toContain('<button type="button"');
  });
});
