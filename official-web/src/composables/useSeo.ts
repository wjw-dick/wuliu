import { watch } from 'vue'
import { useRoute } from 'vue-router'

const SITE_NAME = '驰远物流'
const SITE_URL = 'https://www.chiyuan-logistics.com'

function setMeta(name: string, content: string) {
  let el = document.head.querySelector<HTMLMetaElement>(`meta[name="${name}"]`)
  if (!el) {
    el = document.createElement('meta')
    el.setAttribute('name', name)
    document.head.appendChild(el)
  }
  el.setAttribute('content', content)
}

function setProperty(property: string, content: string) {
  let el = document.head.querySelector<HTMLMetaElement>(`meta[property="${property}"]`)
  if (!el) {
    el = document.createElement('meta')
    el.setAttribute('property', property)
    document.head.appendChild(el)
  }
  el.setAttribute('content', content)
}

function setCanonical(path: string) {
  let el = document.head.querySelector<HTMLLinkElement>('link[rel="canonical"]')
  if (!el) {
    el = document.createElement('link')
    el.setAttribute('rel', 'canonical')
    document.head.appendChild(el)
  }
  el.setAttribute('href', `${SITE_URL}${path}`)
}

export function usePageSeo() {
  const route = useRoute()

  watch(
    () => route.fullPath,
    () => {
      const pageTitle = (route.meta.title as string) || '零担专线·整车包车·同城配送'
      const description =
        (route.meta.description as string) ||
        '驰远物流提供零担专线、整车包车、同城配送、仓储与供应链服务，覆盖长三角主要城市，在线询价、运单查询、网点查询一站完成。'
      const keywords =
        (route.meta.keywords as string) ||
        '物流,零担专线,整车运输,同城配送,运单查询,物流公司,驰远物流'

      document.title = `${pageTitle}｜${SITE_NAME}`
      setMeta('description', description)
      setMeta('keywords', keywords)
      setProperty('og:title', `${pageTitle}｜${SITE_NAME}`)
      setProperty('og:description', description)
      setProperty('og:type', 'website')
      setProperty('og:url', `${SITE_URL}${route.path}`)
      setProperty('og:site_name', SITE_NAME)
      setCanonical(route.path)
    },
    { immediate: true }
  )
}

export { SITE_NAME, SITE_URL }
