// Campo numérico no padrão brasileiro: ponto separa milhar e vírgula separa decimais (1.000.000,50).
// "valor" é o número normalizado que vai para a API ("1000000.50"); "exibicao" é o texto do campo.

const SEPARADOR_MILHAR = /\B(?=(\d{3})+(?!\d))/g;

const agruparMilhar = (inteiro) => inteiro.replace(SEPARADOR_MILHAR, '.');

/** Interpreta o que o usuário digitou. Pontos digitados são ignorados (são só separador de milhar). */
export function interpretarEntradaNumerica(
  texto,
  { casasDecimais = 2, permitirNegativo = false } = {},
) {
  const bruto = String(texto ?? '');
  const negativo = permitirNegativo && bruto.trim().startsWith('-');
  const sinal = negativo ? '-' : '';
  const [inteiroDigitado, ...restos] = bruto.replace(/[^\d,]/g, '').split(',');
  const possuiVirgula = casasDecimais > 0 && restos.length > 0;
  const decimais = restos.join('').slice(0, casasDecimais);
  const inteiro = inteiroDigitado.replace(/^0+(?=\d)/, '');

  if (inteiro === '' && !possuiVirgula) return { exibicao: sinal, valor: '' };

  const inteiroOuZero = inteiro || '0';
  return {
    exibicao: `${sinal}${agruparMilhar(inteiroOuZero)}${possuiVirgula ? `,${decimais}` : ''}`,
    valor: `${sinal}${inteiroOuZero}${decimais ? `.${decimais}` : ''}`,
  };
}

/** Valor vindo da API ou do estado (1250.5, "1250.50") para o texto do campo ("1.250,50"). */
export function exibirNumero(valor) {
  if (valor == null || valor === '') return '';
  const [inteiro, decimais] = String(valor).split('.');
  const negativo = inteiro.startsWith('-');
  const digitos = negativo ? inteiro.slice(1) : inteiro;
  return `${negativo ? '-' : ''}${agruparMilhar(digitos)}${decimais ? `,${decimais}` : ''}`;
}
