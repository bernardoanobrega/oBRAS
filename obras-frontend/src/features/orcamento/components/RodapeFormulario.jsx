import Button from '../../../shared/components/ui/Button.jsx';

export default function RodapeFormulario({
  erroGeral,
  enviando,
  onCancelar,
  rotuloEnviar = 'Salvar',
}) {
  return (
    <>
      {erroGeral && (
        <p role="alert" className="rounded bg-perigo-fundo p-2 text-sm text-perigo">
          {erroGeral}
        </p>
      )}
      <div className="flex justify-end gap-2 pt-2">
        <Button variante="secundario" onClick={onCancelar}>
          Cancelar
        </Button>
        <Button type="submit" disabled={enviando}>
          {enviando ? 'Salvando...' : rotuloEnviar}
        </Button>
      </div>
    </>
  );
}
