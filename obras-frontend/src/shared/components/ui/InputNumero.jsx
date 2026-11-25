import { useEffect, useLayoutEffect, useRef, useState } from 'react';
import { exibirNumero, interpretarEntradaNumerica } from '../../utils/entradaNumerica.js';

const CARACTERE_SIGNIFICATIVO = /[\d,-]/;

const contarSignificativos = (texto) =>
  [...texto].filter((caractere) => CARACTERE_SIGNIFICATIVO.test(caractere)).length;

/** Posição no texto formatado logo após o n-ésimo caractere significativo (dígito, vírgula ou sinal). */
function posicaoAposSignificativos(texto, quantidade) {
  let encontrados = 0;
  for (let indice = 0; indice < texto.length; indice += 1) {
    if (encontrados === quantidade) return indice;
    if (CARACTERE_SIGNIFICATIVO.test(texto[indice])) encontrados += 1;
  }
  return texto.length;
}

/**
 * Campo numérico com separador de milhar (1.000.000,50). valor/onChange trabalham com o número
 * normalizado ("1000000.50"), pronto para a API. Mesmo visual e acessibilidade do Input.
 */
export default function InputNumero({
  label,
  erro,
  id,
  name,
  valor,
  onChange,
  casasDecimais = 2,
  permitirNegativo = false,
  className = '',
  ...props
}) {
  const campo = id || name;
  const [exibicao, setExibicao] = useState(() => exibirNumero(valor));
  const ultimoValorEmitido = useRef(valor);
  const entrada = useRef(null);
  const cursorDesejado = useRef(null);

  useEffect(() => {
    if (valor !== ultimoValorEmitido.current) {
      ultimoValorEmitido.current = valor;
      setExibicao(exibirNumero(valor));
    }
  }, [valor]);

  useLayoutEffect(() => {
    if (cursorDesejado.current == null || document.activeElement !== entrada.current) return;
    entrada.current.setSelectionRange(cursorDesejado.current, cursorDesejado.current);
    cursorDesejado.current = null;
  }, [exibicao]);

  function aoDigitar(e) {
    const { value, selectionStart } = e.target;
    const significativosAntesDoCursor = contarSignificativos(value.slice(0, selectionStart));
    const resultado = interpretarEntradaNumerica(value, { casasDecimais, permitirNegativo });
    cursorDesejado.current = posicaoAposSignificativos(
      resultado.exibicao,
      significativosAntesDoCursor,
    );
    ultimoValorEmitido.current = resultado.valor;
    setExibicao(resultado.exibicao);
    onChange(resultado.valor);
  }

  return (
    <div className={className}>
      {label && (
        <label htmlFor={campo} className="mb-1 block text-sm font-medium text-slate-700">
          {label}
        </label>
      )}
      <input
        ref={entrada}
        id={campo}
        name={name}
        type="text"
        inputMode={casasDecimais > 0 ? 'decimal' : 'numeric'}
        autoComplete="off"
        value={exibicao}
        onChange={aoDigitar}
        className={`min-h-[44px] w-full rounded-lg border px-3 text-right text-sm tabular-nums outline-none focus:ring-2 focus:ring-brand-500 md:min-h-[36px] ${
          erro ? 'border-red-500' : 'border-slate-300'
        }`}
        {...props}
      />
      {erro && <p className="mt-1 text-xs text-red-600">{erro}</p>}
    </div>
  );
}
