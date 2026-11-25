import { describe, it, expect } from "vitest";
import { filtrarDocumentos } from "../features/documentos/components/documentosUtils.js";
import { validarArquivo, TAMANHO_MAXIMO } from "../shared/utils/arquivo.js";

describe("consulta documental", () => {
  const docs = [
    {
      codigo: "EST-01",
      titulo: "Vigas do pavimento",
      categoria: "PROJETO_EXECUTIVO",
      disciplina: "ESTRUTURAL",
    },
    {
      codigo: "CT-01",
      titulo: "Contrato de serviços",
      categoria: "CONTRATO",
      disciplina: "GERAL",
    },
  ];
  it("combina busca por código ou título com categoria e disciplina", () => {
    expect(
      filtrarDocumentos(docs, " est-01 ", "PROJETO_EXECUTIVO", "ESTRUTURAL"),
    ).toEqual([docs[0]]);
    expect(filtrarDocumentos(docs, "VIGAS", "", "")).toEqual([docs[0]]);
    expect(filtrarDocumentos(docs, "CT", "PROJETO_EXECUTIVO", "")).toEqual([]);
    expect(filtrarDocumentos(docs, "", "", "")).toHaveLength(2);
  });
  it("aceita o limite e rejeita arquivos vazios, grandes ou de formato não permitido", () => {
    expect(
      validarArquivo({ type: "application/pdf", size: TAMANHO_MAXIMO }),
    ).toBeNull();
    expect(validarArquivo({ type: "application/pdf", size: 0 })).toBeTruthy();
    expect(
      validarArquivo({ type: "application/pdf", size: TAMANHO_MAXIMO + 1 }),
    ).toBeTruthy();
    expect(validarArquivo({ type: "text/html", size: 10 })).toBeTruthy();
  });
});
