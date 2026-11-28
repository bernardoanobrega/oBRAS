import { useCallback, useState } from 'react';
import { cleanup, fireEvent, render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { afterEach, describe, expect, it, vi } from 'vitest';
import Modal from '../shared/components/ui/Modal.jsx';

function ModalHarness({ onFechar }) {
  const [aberto, setAberto] = useState(false);
  const fechar = useCallback(() => {
    setAberto(false);
    onFechar?.();
  }, [onFechar]);

  return (
    <>
      <button type="button" onClick={() => setAberto(true)}>Abrir modal</button>
      <Modal aberto={aberto} titulo="Modal de teste" onFechar={fechar}>
        <button type="button">Salvar</button>
      </Modal>
    </>
  );
}

afterEach(cleanup);

describe('acessibilidade DOM do modal', () => {
  it('modalFechaComEsc', async () => {
    render(<ModalHarness />);
    const usuario = userEvent.setup();
    await usuario.click(screen.getByRole('button', { name: 'Abrir modal' }));
    expect(screen.getByRole('dialog', { name: 'Modal de teste' })).toBeTruthy();

    fireEvent.keyDown(screen.getByRole('dialog'), { key: 'Escape' });
    expect(screen.queryByRole('dialog')).toBeNull();
  });

  it('focoVoltaAoBotaoAposFechar', async () => {
    const aoFechar = vi.fn();
    render(<ModalHarness onFechar={aoFechar} />);
    const usuario = userEvent.setup();
    const gatilho = screen.getByRole('button', { name: 'Abrir modal' });

    await usuario.click(gatilho);
    const dialogo = screen.getByRole('dialog');
    expect(dialogo.contains(document.activeElement)).toBe(true);

    fireEvent.keyDown(screen.getByRole('button', { name: 'Fechar' }), { key: 'Escape' });
    await waitFor(() => expect(document.activeElement).toBe(gatilho));
    expect(aoFechar).toHaveBeenCalledTimes(1);
  });

  it('navegacaoPorTabFunciona', async () => {
    render(<ModalHarness />);
    const usuario = userEvent.setup();
    await usuario.click(screen.getByRole('button', { name: 'Abrir modal' }));

    const fechar = screen.getByRole('button', { name: 'Fechar' });
    const salvar = screen.getByRole('button', { name: 'Salvar' });
    expect(document.activeElement).toBe(fechar);

    await usuario.tab();
    expect(document.activeElement).toBe(salvar);

    await usuario.tab();
    expect(document.activeElement).toBe(fechar);

    await usuario.tab({ shift: true });
    expect(document.activeElement).toBe(salvar);
  });
});
