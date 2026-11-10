// RNF-10: somente PDF, PNG e JPG, até 10 MB. O backend valida de novo.
export const TAMANHO_MAXIMO = 10 * 1024 * 1024;
const TIPOS = ['application/pdf', 'image/png', 'image/jpeg'];

export function validarArquivo(file) {
  if (!file) return 'Selecione um arquivo.';
  if (!TIPOS.includes(file.type)) return 'Formato não permitido. Use PDF, PNG ou JPG.';
  if (file.size > TAMANHO_MAXIMO) return 'Arquivo excede o limite de 10 MB.';
  return null;
}
