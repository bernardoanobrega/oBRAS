export const CATEGORIAS = {
  CONTRATO: "Contratos e terceirizados",
  LICENCA: "Licenças",
  ALVARA: "Alvarás e ARTs",
  PROJETO_EXECUTIVO: "Projetos executivos",
  ESPECIFICACAO_TECNICA: "Especificações técnicas",
};
export const DISCIPLINAS = {
  GERAL: "Geral",
  ARQUITETONICO: "Arquitetônico",
  ESTRUTURAL: "Estrutural",
  ELETRICO: "Instalações elétricas",
  HIDROSSANITARIO: "Instalações hidrossanitárias",
};
export const dataHora = (data) => new Date(data).toLocaleString("pt-BR");
export const tamanho = (bytes) => bytes < 1024 * 1024 ? `${Math.max(1, Math.round(bytes / 1024))} KB` : `${(bytes / 1024 / 1024).toFixed(2)} MB`;
export const revisao = (numero) => `REV ${String(numero).padStart(2, "0")}`;
export function filtrarDocumentos(documentos, busca, categoria, disciplina) {
  const termo = busca.trim().toLocaleLowerCase("pt-BR");
  return documentos.filter(
    (d) =>
      (!categoria || d.categoria === categoria) &&
      (!disciplina || d.disciplina === disciplina) &&
      `${d.codigo} ${d.titulo}`.toLocaleLowerCase("pt-BR").includes(termo),
  );
}
