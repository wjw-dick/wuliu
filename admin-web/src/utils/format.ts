import dayjs from 'dayjs'

export function formatMoney(value?: number | string | null, symbol = true): string {
  if (value === undefined || value === null || value === '') return symbol ? '¥ 0.00' : '0.00'
  const n = Number(value)
  if (Number.isNaN(n)) return String(value)
  const fixed = n.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
  return symbol ? `¥ ${fixed}` : fixed
}

export function formatNumber(value?: number | string | null, digits = 0): string {
  if (value === undefined || value === null || value === '') return '0'
  const n = Number(value)
  if (Number.isNaN(n)) return String(value)
  return n.toLocaleString('zh-CN', { minimumFractionDigits: digits, maximumFractionDigits: digits })
}

export function formatPercent(value?: number | string | null, digits = 1): string {
  if (value === undefined || value === null || value === '') return '0%'
  return `${Number(value).toFixed(digits)}%`
}

export function formatDate(value?: string | Date | null, template = 'YYYY-MM-DD'): string {
  if (!value) return '—'
  const d = dayjs(value)
  return d.isValid() ? d.format(template) : String(value)
}

export function formatDateTime(value?: string | Date | null): string {
  return formatDate(value, 'YYYY-MM-DD HH:mm')
}

export function today(): string {
  return dayjs().format('YYYY-MM-DD')
}

export function daysFromNow(days: number): string {
  return dayjs().add(days, 'day').format('YYYY-MM-DD')
}

export function downloadText(filename: string, content: string, mime = 'text/plain;charset=utf-8'): void {
  const blob = new Blob(['\ufeff' + content], { type: mime })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = filename
  document.body.appendChild(a)
  a.click()
  document.body.removeChild(a)
  URL.revokeObjectURL(url)
}

export function toCsv(headers: string[], rows: (string | number)[][]): string {
  const escape = (v: string | number) => `"${String(v).replace(/"/g, '""')}"`
  return [headers.map(escape).join(','), ...rows.map((r) => r.map(escape).join(','))].join('\n')
}

export function relativeTime(value?: string): string {
  if (!value) return '—'
  const diff = dayjs().diff(dayjs(value), 'minute')
  if (diff < 1) return '刚刚'
  if (diff < 60) return `${diff} 分钟前`
  if (diff < 1440) return `${Math.floor(diff / 60)} 小时前`
  return `${Math.floor(diff / 1440)} 天前`
}
