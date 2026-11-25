import { useEffect, useLayoutEffect, useRef, useState } from 'react';
import { createPortal } from 'react-dom';
import { MoreVertical } from 'lucide-react';

const DISTANCIA_DO_BOTAO = 4;
const MARGEM_DA_TELA = 8;

/**
 * Menu "⋮" de cada linha da planilha. acoes: [{ rotulo, aoClicar, desabilitada, dica }].
 * O menu é renderizado em portal com posição fixa: dentro da tabela (overflow-x-auto) ele seria cortado.
 */
export default function ItemAcoesMenu({ descricaoItem, acoes }) {
  const [aberto, setAberto] = useState(false);
  const [posicao, setPosicao] = useState(null);
  const botao = useRef(null);
  const menu = useRef(null);

  useLayoutEffect(() => {
    if (!aberto) {
      setPosicao(null);
      return;
    }
    const ancora = botao.current.getBoundingClientRect();
    const { offsetHeight: altura, offsetWidth: largura } = menu.current;
    const cabeAbaixo = ancora.bottom + DISTANCIA_DO_BOTAO + altura <= window.innerHeight;
    setPosicao({
      top: cabeAbaixo
        ? ancora.bottom + DISTANCIA_DO_BOTAO
        : ancora.top - altura - DISTANCIA_DO_BOTAO,
      left: Math.max(MARGEM_DA_TELA, ancora.right - largura),
    });
  }, [aberto]);

  useEffect(() => {
    if (!aberto) return undefined;
    const fechar = () => setAberto(false);
    const fecharAoClicarFora = (e) => {
      const clicouNoMenu = menu.current?.contains(e.target) || botao.current?.contains(e.target);
      if (!clicouNoMenu) fechar();
    };
    const fecharComEsc = (e) => e.key === 'Escape' && fechar();
    document.addEventListener('mousedown', fecharAoClicarFora);
    document.addEventListener('keydown', fecharComEsc);
    window.addEventListener('scroll', fechar, true);
    window.addEventListener('resize', fechar);
    return () => {
      document.removeEventListener('mousedown', fecharAoClicarFora);
      document.removeEventListener('keydown', fecharComEsc);
      window.removeEventListener('scroll', fechar, true);
      window.removeEventListener('resize', fechar);
    };
  }, [aberto]);

  if (acoes.length === 0) return null;

  return (
    <>
      <button
        ref={botao}
        type="button"
        aria-label={`Ações do item ${descricaoItem}`}
        aria-haspopup="menu"
        aria-expanded={aberto}
        onClick={() => setAberto((a) => !a)}
        className="flex h-11 w-11 items-center justify-center rounded-lg text-slate-500 hover:bg-slate-100 md:h-9 md:w-9"
      >
        <MoreVertical size={18} />
      </button>
      {aberto &&
        createPortal(
          <ul
            ref={menu}
            role="menu"
            style={{
              position: 'fixed',
              top: posicao?.top ?? 0,
              left: posicao?.left ?? 0,
              visibility: posicao ? 'visible' : 'hidden',
            }}
            className="z-50 w-48 overflow-hidden rounded-lg border border-slate-200 bg-white py-1 text-left shadow-lg"
          >
            {acoes.map(({ rotulo, aoClicar, desabilitada, dica }) => (
              <li key={rotulo} role="none">
                <button
                  type="button"
                  role="menuitem"
                  disabled={desabilitada}
                  title={dica}
                  onClick={() => {
                    setAberto(false);
                    aoClicar();
                  }}
                  className="block min-h-[44px] w-full px-4 text-left text-sm text-slate-700 hover:bg-slate-100 disabled:cursor-not-allowed disabled:text-slate-400 md:min-h-[36px]"
                >
                  {rotulo}
                </button>
              </li>
            ))}
          </ul>,
          document.body,
        )}
    </>
  );
}
