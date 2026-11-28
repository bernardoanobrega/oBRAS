export default function ErroFormulario({ erroGeral }) {
  if (!erroGeral) return null;
  return (
    <p role="alert" className="rounded-lg border border-perigo/40 bg-perigo-fundo p-2 text-sm text-perigo">
      {erroGeral}
    </p>
  );
}
