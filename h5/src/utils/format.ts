export function money(value: number | null | undefined): string {
  const number = Number(value) || 0
  return number.toLocaleString('zh-CN', {
    minimumFractionDigits: 2,
    maximumFractionDigits: 2
  })
}

export function formatDateTime(value: string | undefined): string {
  if (!value) return '—'
  return value.replace('T', ' ').slice(0, 16)
}
