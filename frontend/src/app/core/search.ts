export function matchesSearch(value: string, query: string): boolean {
  const normalizedValue = normalizeSearch(value);
  const terms = normalizeSearch(query).split(/\s+/).filter(Boolean);

  return terms.every((term) => normalizedValue.includes(term));
}

function normalizeSearch(value: string): string {
  return value
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLocaleLowerCase('pt-BR')
    .trim();
}
