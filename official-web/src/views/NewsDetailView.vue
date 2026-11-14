<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { RouterLink, useRoute } from 'vue-router'
import { getContentDetail, getContents } from '../api/public'
import type { ContentItem } from '../types'
import { formatDate, sanitizeHtml } from '../utils/format'

const route = useRoute()
const article = ref<ContentItem | null>(null)
const related = ref<ContentItem[]>([])
const loading = ref(true)
const notFound = ref(false)
const copied = ref(false)
let articleScript: HTMLScriptElement | null = null

const html = computed(() => (article.value?.content ? sanitizeHtml(article.value.content) : ''))

function updateMeta(item: ContentItem) {
  document.title = `${item.title}｜驰远物流`
  let el = document.head.querySelector<HTMLMetaElement>('meta[name="description"]')
  if (!el) {
    el = document.createElement('meta')
    el.setAttribute('name', 'description')
    document.head.appendChild(el)
  }
  el.setAttribute('content', item.summary || item.title)
}

function injectJsonLd(item: ContentItem) {
  articleScript?.remove()
  articleScript = document.createElement('script')
  articleScript.type = 'application/ld+json'
  articleScript.id = 'news-article-jsonld'
  articleScript.textContent = JSON.stringify({
    '@context': 'https://schema.org',
    '@type': 'NewsArticle',
    headline: item.title,
    description: item.summary,
    datePublished: item.publishedAt?.replace(' ', 'T') || undefined,
    author: { '@type': 'Organization', name: item.author || '驰远物流' },
    publisher: {
      '@type': 'Organization',
      name: '上海驰远物流有限公司',
      logo: { '@type': 'ImageObject', url: 'https://www.chiyuan-logistics.com/favicon.svg' }
    },
    mainEntityOfPage: `https://www.chiyuan-logistics.com/news/${item.id}`
  })
  document.head.appendChild(articleScript)
}

async function share() {
  const url = window.location.href
  try {
    if (navigator.share) {
      await navigator.share({ title: article.value?.title, url })
    } else {
      await navigator.clipboard.writeText(url)
      copied.value = true
      window.setTimeout(() => {
        copied.value = false
      }, 1800)
    }
  } catch {
    copied.value = false
  }
}

async function load(id: number) {
  loading.value = true
  notFound.value = false
  article.value = null
  try {
    const found = await getContentDetail(id)
    if (!found) {
      notFound.value = true
      return
    }
    article.value = found
    updateMeta(found)
    injectJsonLd(found)
    const paged = await getContents('资讯', 1, 10)
    related.value = paged.list.filter((item) => Number(item.id) !== id).slice(0, 3)
  } catch {
    notFound.value = true
  } finally {
    loading.value = false
  }
}

watch(
  () => route.params.id,
  (id) => {
    void load(Number(id))
  },
  { immediate: true }
)

onBeforeUnmount(() => {
  articleScript?.remove()
})
</script>

<template>
  <div class="article-page">
    <div class="article-hero">
      <div class="container">
        <nav class="breadcrumb" aria-label="面包屑">
          <RouterLink to="/">首页</RouterLink>
          <span aria-hidden="true">/</span>
          <RouterLink to="/news">资讯中心</RouterLink>
          <span aria-hidden="true">/</span>
          <span aria-current="page">正文</span>
        </nav>
      </div>
    </div>

    <section class="section section--tight article-main">
      <div class="container">
        <div v-if="loading" class="article-loading">
          <div class="skeleton article-loading__title"></div>
          <div class="skeleton article-loading__meta"></div>
          <div class="skeleton article-loading__line"></div>
          <div class="skeleton article-loading__line"></div>
          <div class="skeleton article-loading__line article-loading__line--short"></div>
        </div>

        <div v-else-if="notFound || !article" class="state-block">
          <div class="state-block__icon" aria-hidden="true">404</div>
          <h3>文章不存在或已下线</h3>
          <p>该资讯可能已被删除，或者链接有误。您可以返回资讯中心查看最新内容。</p>
          <RouterLink class="btn btn--outline btn--sm" to="/news">返回资讯中心</RouterLink>
        </div>

        <div v-else class="article-layout">
          <article class="article">
            <header class="article__head">
              <span class="pill">{{ article.category || '资讯' }}</span>
              <h1>{{ article.title }}</h1>
              <p class="article__summary">{{ article.summary }}</p>
              <div class="article__meta">
                <span>{{ article.author || '品牌中心' }}</span>
                <span>{{ formatDate(article.publishedAt) }}</span>
                <span>{{ article.views || 0 }} 次阅读</span>
                <button type="button" class="article__share" @click="share">{{ copied ? '链接已复制' : '分享文章' }}</button>
              </div>
            </header>

            <div class="article__body rich-text" v-html="html"></div>

            <footer class="article__foot">
              <div class="article__tags">
                <span>#{{ article.category || '物流资讯' }}</span>
                <span>#长三角物流</span>
                <span>#驰远物流</span>
              </div>
              <div class="article__actions">
                <RouterLink class="btn btn--outline btn--sm" to="/news">← 返回列表</RouterLink>
                <RouterLink class="btn btn--primary btn--sm" to="/quote">在线询价</RouterLink>
              </div>
            </footer>
          </article>

          <aside class="article-aside">
            <div class="aside-cta">
              <span class="eyebrow">GET A QUOTE</span>
              <h2>这票货多少钱？</h2>
              <p>30 秒获取含附加费的完整明细，不留手机号也能看。</p>
              <RouterLink class="btn btn--primary btn--block" to="/quote">立即在线询价</RouterLink>
              <a class="aside-cta__phone" href="tel:4008801688">客服热线 400-880-1688</a>
            </div>

            <div v-if="related.length" class="aside-related card">
              <h2>相关阅读</h2>
              <RouterLink v-for="item in related" :key="item.id" :to="`/news/${item.id}`" class="aside-related__item">
                <span>{{ formatDate(item.publishedAt) }}</span>
                <strong>{{ item.title }}</strong>
              </RouterLink>
            </div>

            <div class="aside-links card">
              <h2>常用服务</h2>
              <RouterLink to="/track">运单轨迹查询 →</RouterLink>
              <RouterLink to="/sites">查找附近网点 →</RouterLink>
              <RouterLink to="/monthly">申请企业月结 →</RouterLink>
              <RouterLink to="/services">查看计费标准 →</RouterLink>
            </div>
          </aside>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.article-hero {
  background: #071a33;
  padding: 16px 0;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
}

.breadcrumb {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  flex-wrap: wrap;
}

.breadcrumb a:hover {
  color: #fff;
}

.breadcrumb span[aria-current='page'] {
  color: rgba(255, 255, 255, 0.82);
}

.article-main {
  background: var(--soft);
  min-height: 600px;
}

.article-layout {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: 28px;
  align-items: start;
}

.article {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  overflow: hidden;
}

.article__head {
  padding: 34px 38px 26px;
  border-bottom: 1px solid var(--line);
}

.article__head h1 {
  font-size: clamp(25px, 3vw, 34px);
  line-height: 1.4;
  letter-spacing: -0.015em;
  margin: 14px 0 12px;
}

.article__summary {
  font-size: 14px;
  line-height: 1.8;
  color: var(--muted);
  padding: 14px 16px;
  border-radius: 9px;
  background: #f7faff;
  border-left: 3px solid var(--primary);
}

.article__meta {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  gap: 16px;
  margin-top: 16px;
  font-size: 12px;
  color: var(--muted-light);
}

.article__share {
  margin-left: auto;
  border: 1px solid var(--line);
  background: #fff;
  border-radius: 6px;
  padding: 4px 10px;
  font-size: 11.5px;
  color: var(--muted);
}

.article__share:hover {
  border-color: var(--primary);
  color: var(--primary);
}

.article__body {
  padding: 30px 38px 34px;
}

.article__foot {
  padding: 20px 38px 28px;
  border-top: 1px solid var(--line);
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.article__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.article__tags span {
  font-size: 11px;
  color: var(--muted);
  background: var(--soft);
  border-radius: 5px;
  padding: 3px 8px;
}

.article__actions {
  display: flex;
  gap: 8px;
}

.article-aside {
  position: sticky;
  top: 110px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.aside-cta {
  padding: 24px;
  border-radius: var(--radius);
  background: linear-gradient(145deg, #0a2b5e, #1557c0);
  color: rgba(255, 255, 255, 0.7);
}

.aside-cta .eyebrow {
  color: #8dc0ff;
}

.aside-cta .eyebrow::before {
  background: #8dc0ff;
}

.aside-cta h2 {
  color: #fff;
  font-size: 21px;
  margin: 10px 0 8px;
}

.aside-cta p {
  font-size: 12.5px;
  line-height: 1.7;
  margin-bottom: 16px;
}

.aside-cta__phone {
  display: block;
  text-align: center;
  margin-top: 12px;
  font-size: 12px;
  color: #cfe2ff;
}

.aside-related,
.aside-links {
  padding: 20px;
}

.aside-related h2,
.aside-links h2 {
  font-size: 14px;
  margin-bottom: 12px;
}

.aside-related__item {
  display: block;
  padding: 11px 0;
  border-top: 1px solid var(--line);
}

.aside-related__item span {
  display: block;
  font-size: 10.5px;
  color: var(--muted-light);
  margin-bottom: 3px;
}

.aside-related__item strong {
  font-size: 12.5px;
  font-weight: 600;
  line-height: 1.55;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.aside-related__item:hover strong {
  color: var(--primary);
}

.aside-links {
  display: flex;
  flex-direction: column;
}

.aside-links a {
  font-size: 12.5px;
  color: var(--muted);
  padding: 9px 0;
  border-top: 1px solid var(--line);
}

.aside-links a:hover {
  color: var(--primary);
}

.article-loading {
  background: #fff;
  border: 1px solid var(--line);
  border-radius: var(--radius);
  padding: 36px;
}

.article-loading__title {
  height: 36px;
  width: 80%;
  margin-bottom: 16px;
}

.article-loading__meta {
  height: 14px;
  width: 40%;
  margin-bottom: 32px;
}

.article-loading__line {
  height: 15px;
  margin-bottom: 14px;
}

.article-loading__line--short {
  width: 60%;
}

@media (max-width: 980px) {
  .article-layout {
    grid-template-columns: 1fr;
  }

  .article-aside {
    position: static;
    display: grid;
    grid-template-columns: 1fr 1fr;
  }

  .aside-cta {
    grid-column: 1 / -1;
  }
}

@media (max-width: 680px) {
  .article__head,
  .article__body,
  .article__foot {
    padding-left: 20px;
    padding-right: 20px;
  }

  .article-aside {
    grid-template-columns: 1fr;
  }

  .article__foot {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
