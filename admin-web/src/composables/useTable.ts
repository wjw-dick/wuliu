import { ref } from 'vue'
import type { PageQuery, PageResult } from '@/api/request'

interface UseTableOptions {
  pageSize?: number
  immediate?: boolean
}

export function useTable<T = any>(
  fetcher: (params: PageQuery) => Promise<PageResult<T> | T[] | any>,
  options: UseTableOptions = {}
) {
  const loading = ref(false)
  const list = ref<T[]>([])
  const total = ref(0)
  const page = ref(1)
  const size = ref(options.pageSize || 10)
  const query = ref<Record<string, any>>({})
  let requestSeq = 0

  async function load(): Promise<void> {
    loading.value = true
    const seq = ++requestSeq
    try {
      const params: PageQuery = { ...query.value, page: page.value, size: size.value }
      Object.keys(params).forEach((k) => {
        if (params[k] === '' || params[k] === null || params[k] === undefined) delete params[k]
      })
      const res = await fetcher(params)
      if (seq !== requestSeq) return
      if (Array.isArray(res)) {
        list.value = res
        total.value = res.length
      } else if (res && Array.isArray(res.list)) {
        list.value = res.list
        total.value = Number(res.total ?? res.list.length)
      } else if (res && Array.isArray(res.records)) {
        list.value = res.records
        total.value = Number(res.total ?? res.records.length)
      } else {
        list.value = []
        total.value = 0
      }
    } catch {
      if (seq === requestSeq) {
        list.value = []
        total.value = 0
      }
    } finally {
      if (seq === requestSeq) loading.value = false
    }
  }

  function search(): void {
    page.value = 1
    load()
  }

  function reset(filters: Record<string, any> = {}): void {
    query.value = { ...filters }
    search()
  }

  function onPageChange(p: number): void {
    page.value = p
    load()
  }

  function onSizeChange(s: number): void {
    size.value = s
    page.value = 1
    load()
  }

  function setQuery(patch: Record<string, any>): void {
    query.value = { ...query.value, ...patch }
  }

  if (options.immediate !== false) load()

  return { loading, list, total, page, size, query, load, search, reset, onPageChange, onSizeChange, setQuery }
}
