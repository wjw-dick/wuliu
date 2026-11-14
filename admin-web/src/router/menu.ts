import type { RouteRecordRaw } from 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    icon?: string
    group?: string
    perm?: string
    scope?: 'platform' | 'tenant'
    hidden?: boolean
    affix?: boolean
    order?: number
    activeMenu?: string
    parentTitle?: string
  }
}

export interface MenuGroupMeta {
  title: string
  icon: string
  order: number
  scope: 'platform' | 'tenant'
}

export const GROUP_META: Record<string, MenuGroupMeta> = {
  'plat-overview': { title: '概览', icon: 'Odometer', order: 1, scope: 'platform' },
  'plat-tenant': { title: '租户运营', icon: 'OfficeBuilding', order: 2, scope: 'platform' },
  'plat-commerce': { title: '商业运营', icon: 'Wallet', order: 3, scope: 'platform' },
  'plat-system': { title: '平台管理', icon: 'Setting', order: 4, scope: 'platform' },
  'tenant-overview': { title: '概览', icon: 'Odometer', order: 1, scope: 'tenant' },
  'tenant-customer': { title: '客户经营', icon: 'User', order: 2, scope: 'tenant' },
  'tenant-business': { title: '业务运营', icon: 'Tickets', order: 3, scope: 'tenant' },
  'tenant-capacity': { title: '运力资源', icon: 'Van', order: 4, scope: 'tenant' },
  'tenant-transit': { title: '在途与交付', icon: 'Location', order: 5, scope: 'tenant' },
  'tenant-finance': { title: '财务结算', icon: 'Money', order: 6, scope: 'tenant' },
  'tenant-service': { title: '售后与报表', icon: 'DataAnalysis', order: 7, scope: 'tenant' },
  'tenant-org': { title: '组织与权限', icon: 'UserFilled', order: 8, scope: 'tenant' },
  'tenant-content': { title: '内容运营', icon: 'Collection', order: 9, scope: 'tenant' },
  'tenant-settings': { title: '系统设置', icon: 'Setting', order: 10, scope: 'tenant' }
}

export interface MenuItem {
  path: string
  title: string
  icon: string
  order: number
  children?: MenuItem[]
}

export interface MenuGroup {
  key: string
  title: string
  icon: string
  order: number
  children: MenuItem[]
}

export function buildMenus(
  routes: RouteRecordRaw[],
  scope: 'platform' | 'tenant',
  hasPerm: (perm?: string) => boolean
): MenuGroup[] {
  const grouped = new Map<string, MenuItem[]>()
  routes
    .filter((r) => r.meta?.scope === scope && !r.meta?.hidden && r.meta?.group)
    .forEach((r) => {
      if (!hasPerm(r.meta?.perm)) return
      const groupKey = r.meta!.group!
      const item: MenuItem = {
        path: r.path,
        title: r.meta?.title || '',
        icon: r.meta?.icon || 'Menu',
        order: r.meta?.order || 99
      }
      if (!grouped.has(groupKey)) grouped.set(groupKey, [])
      grouped.get(groupKey)!.push(item)
    })

  return Object.keys(GROUP_META)
    .filter((key) => GROUP_META[key].scope === scope)
    .map((key) => {
      const meta = GROUP_META[key]
      const children = (grouped.get(key) || []).sort((a, b) => a.order - b.order)
      return { key, title: meta.title, icon: meta.icon, order: meta.order, children }
    })
    .filter((g) => g.children.length > 0)
    .sort((a, b) => a.order - b.order)
}
