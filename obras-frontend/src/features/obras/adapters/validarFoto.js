export const LIMITE_FOTO_BYTES = 10 * 1024 * 1024;

const ASSINATURA_PNG = [0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a];

function comecaCom(bytes, assinatura) {
  return assinatura.every((byte, indice) => bytes[indice] === byte);
}

export async function validarFoto(arquivo) {
  if (!arquivo) return { valido: false, mensagem: 'Selecione uma imagem.' };
  if (arquivo.size > LIMITE_FOTO_BYTES) {
    return { valido: false, mensagem: 'A imagem deve ter no máximo 10 MB.' };
  }
  if (arquivo.size < 3) return { valido: false, mensagem: 'Arquivo de imagem inválido.' };

  const cabecalho = new Uint8Array(await arquivo.slice(0, 8).arrayBuffer());
  const jpeg = cabecalho[0] === 0xff && cabecalho[1] === 0xd8 && cabecalho[2] === 0xff;
  const png = cabecalho.length >= 8 && comecaCom(cabecalho, ASSINATURA_PNG);
  if (!jpeg && !png) return { valido: false, mensagem: 'Envie somente imagens JPG ou PNG.' };

  return { valido: true, mensagem: '' };
}
