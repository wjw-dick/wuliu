<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { getContents } from '../api/public'
import type { ContentItem } from '../types'
import { formatDate } from '../utils/format'

const contents = ref<ContentItem[]>([])
const loading = ref(true)
const error = ref('')
const category = ref('全部')
const page = ref(1)
const pageSize = 6

const categories = computed(() => ['全部', ...Array.from(new Set(contents.value.map((item) => item.category || '资讯')))])

const filtered = computed(() =>
  category.value === '全部'
    ? contents.value
    : contents.value.filter((item) => (item.category || '资讯') === category.value)
)

const totalPages = computed(() => Math.max(1, Math.ceil(filtered.value.length / pageSize)))

const paged = computed(() => {
  const start = (page.value - 1) * pageSize
  return filtered.value.slice(start, start + pageSize)
})

const featured = computed(() => (page.value === 1 && category.value === '全部' ? filtered.value[0] : null))

const list = computed(() => (featured.value ? paged.value.filter((item) => item.id !== featured.value?.id) : paged.value))

const coverTones = ['tone-a', 'tone-b', 'tone-c', 'tone-d']

function changeCategory(value: string) {
  category.value = value
  page.value = 1
}

function goPage(value: number) {
  page.value = Math.min(Math.max(1, value), totalPages.value)
  document.getElementById('news-list')?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

async function load() {
  loading.value = true
  error.value = ''
  try {
    const data = await getContents('资讯', 1, 50)
    contents.value = data.list
  } catch {
    error.value = '资讯加载失败，请稍后重试。'
  } finally {
    loading.value = false
  }
}

onMounted(load)
</script>

<template>
  <div class="news-page">
    <section class="page-hero">
      <div class="container page-hero__inner">
        <span class="eyebrow">NEWS &amp; INSIGHTS</span>
        <h1>资讯中心</h1>
        <p>公司动态、服务公告与物流百科。了解我们的最新网络布局、旺季保障与发货知识。</p>
      </div>
    </section>

    <section id="news-list" class="section section--tight news-main">
      <div class="container">
        <div class="news-filter">
          <div class="news-filter__tabs" role="tablist" aria-label="资讯分类">
            <button
              v-for="item in categories"
              :key="item"
              type="button"
              role="tab"
              :aria-selected="category === item"
              :class="{ 'is-active': category === item }"
              @click="changeCategory(item)"
            >
              {{ item }}
            </button>
          </div>
          <span class="small muted">共 {{ filtered.length }} 篇</span>
        </div>

        <div v-if="loading" class="news-skeleton">
          <div class="skeleton news-skeleton__feature"></div>
          <div class="news-skeleton__grid">
            <div class="skeleton"></div>
            <div class="skeleton"></div>
            <div class="skeleton"></div>
          </div>
        </div>

        <div v-else-if="error" class="state-block">
          <div class="state-block__icon" aria-hidden="true">!</div>
          <h3>资讯暂时无法加载</h3>
          <p>{{ error }}</p>
          <button class="btn btn--outline btn--sm" type="button" @click="load">重新加载</button>
        </div>

        <div v-else-if="!filtered.length" class="state-block">
          <div class="state-block__icon" aria-hidden="true">空</div>
          <h3>该分类暂无内容</h3>
          <p>换个分类看看，或返回首页了解我们的服务。</p>
          <button class="btn btn--outline btn--sm" type="button" @click="changeCategory('全部')">查看全部资讯</button>
        </div>

        <template v-else>
          <RouterLink v-if="featured" :to="`/news/${featured.id}`" class="featured card">
            <div class="featured__cover" :class="coverTones[featured.id % coverTones.length]">
              <span>{{ featured.category || '资讯' }}</span>
              <strong>驰远<br />视角</strong>
              <i class="featured__shape" aria-hidden="true"></i>
            </div>
            <div class="featured__body">
              <span class="pill">置顶推荐</span>
              <h2>{{ featured.title }}</h2>
              <p>{{ featured.summary }}</p>
              <div class="featured__meta">
                <span>{{ featured.author || '品牌中心' }}</span>
                <span>{{ formatDate(featured.publishedAt) }}</span>
                <span>{{ featured.views || 0 }} 次阅读</span>
              </div>
              <span class="text-link">阅读全文 →</span>
            </div>
          </RouterLink>

          <div class="news-grid">
            <RouterLink v-for="(item, index) in list" :key="item.id" :to="`/news/${item.id}`" class="news-card card">
              <div class="news-card__cover" :class="coverTones[(index + 1) % coverTones.length]">
                <span>{{ item.category || '资讯' }}</span>
                <i aria-hidden="true"></i>
              </div>
              <div class="news-card__body">
                <h2>{{ item.title }}</h2>
                <p>{{ item.summary }}</p>
                <div class="news-card__meta">
                  <span>{{ formatDate(item.publishedAt) }}</span>
                  <span>{{ item.views || 0 }} 阅读</span>
                </div>
              </div>
            </RouterLink>
          </div>

          <nav v-if="totalPages > 1" class="pager" aria-label="分页">
            <button type="button" :disabled="page === 1" @click="goPage(page - 1)">上一页</button>
            <button
              v-for="n in totalPages"
              :key="n"
              type="button"
              :class="{ 'is-active': page === n }"
              @click="goPage(n)"
            >
              {{ n }}
            </button>
            <button type="button" :disabled="page === totalPages" @click="goPage(page + 1)">下一页</button>
          </nav>
        </template>

        <div class="news-cta">
          <div>
            <h2>关注驰远物流公众号</h2>
            <p>第一时间获取线路开通、旺季保障与物流知识。</p>
          </div>
          <RouterLink class="btn btn--primary btn--sm" to="/contact">商务合作</RouterLink>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.news-main {
  background: var(--soft);
}

.news-filter {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  margin-bottom: 22px;
  flex-wrap: wrap;
}

.news-filter__tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
}

.news-filter__tabs button {
  border: 1px solid var(--line);
  background: #fff;
  color: var(--muted);
  border-radius: 999px;
  padding: 7px 15px;
  min-height: 36px;
  font-size: 12.5px;
}

.news-filter__tabs button.is-active {
  background: var(--primary);
  border-color: var(--primary);
  color: #fff;
  font-weight: 600;
}

.featured {
  display: grid;
  grid-template-columns: 0.8fr 1.2fr;
  overflow: hidden;
  margin-bottom: 22px;
  transition: box-shadow 0.2s ease, transform 0.2s ease;
}

.featured:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow);
}

.featured__cover {
  position: relative;
  overflow: hidden;
  min-height: 280px;
  padding: 26px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  color: #fff;
}

.featured__cover span {
  position: relative;
  z-index: 1;
  align-self: flex-start;
  font-size: 11px;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.18);
  border: 1px solid rgba(255, 255, 255, 0.22);
  padding: 3px 9px;
  border-radius: 5px;
}

.featured__cover strong {
  position: relative;
  z-index: 1;
  font-size: 34px;
  line-height: 1.2;
  letter-spacing: 0.06em;
  opacity: 0.94;
}

.featured__shape {
  position: absolute;
  right: -50px;
  bottom: -70px;
  width: 240px;
  height: 240px;
  border-radius: 50%;
  border: 34px solid rgba(255, 255, 255, 0.1);
}

.tone-a {
  background: linear-gradient(145deg, #0a2b5e, #1f6feb);
}

.tone-b {
  background: linear-gradient(145deg, #064e3b, #0f9960);
}

.tone-c {
  background: linear-gradient(145deg, #5b2c86, #8b5cf6);
}

.tone-d {
  background: linear-gradient(145deg, #7c2d12, #f59e0b);
}

.featured__body {
  padding: 32px;
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.featured__body h2 {
  font-size: clamp(21px, 2.4vw, 28px);
  line-height: 1.45;
  margin: 14px 0 12px;
  letter-spacing: -0.01em;
}

.featured__body > p {
  font-size: 13.5px;
  color: var(--muted);
  line-height: 1.8;
  flex: 1;
}

.featured__meta {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin: 16px 0;
  font-size: 11.5px;
  color: var(--muted-light);
}

.news-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
}

.news-card {
  display: flex;
  flex-direction: column;
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.news-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow);
}

.news-card__cover {
  height: 120px;
  position: relative;
  overflow: hidden;
  padding: 14px;
}

.news-card__cover span {
  position: relative;
  z-index: 1;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  background: rgba(255, 255, 255, 0.18);
  padding: 2px 8px;
  border-radius: 4px;
}

.news-card__cover i {
  position: absolute;
  right: -20px;
  bottom: -50px;
  width: 130px;
  height: 130px;
  border-radius: 50%;
  border: 20px solid rgba(255, 255, 255, 0.12);
}

.news-card__body {
  padding: 18px;
  display: flex;
  flex-direction: column;
  flex: 1;
}

.news-card__body h2 {
  font-size: 15px;
  line-height: 1.55;
  margin-bottom: 7px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.news-card__body p {
  font-size: 12px;
  color: var(--muted);
  line-height: 1.7;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  flex: 1;
}

.news-card__meta {
  display: flex;
  justify-content: space-between;
  gap: 8px;
  margin-top: 14px;
  padding-top: 12px;
  border-top: 1px solid var(--line);
  font-size: 10.5px;
  color: var(--muted-light);
}

.pager {
  display: flex;
  justify-content: center;
  gap: 6px;
  margin-top: 28px;
}

.pager button {
  min-width: 38px;
  height: 38px;
  border: 1px solid var(--line);
  background: #fff;
  border-radius: 7px;
  font-size: 12.5px;
  color: var(--muted);
  padding: 0 10px;
}

.pager button.is-active {
  background: var(--primary);
  border-color: var(--primary);
  color: #fff;
}

.pager button:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}

.news-skeleton__feature {
  height: 300px;
  margin-bottom: 22px;
}

.news-skeleton__grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 18px;
}

.news-skeleton__grid .skeleton {
  height: 240px;
}

.news-cta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
  margin-top: 30px;
  padding: 24px 28px;
  border-radius: var(--radius);
  background: linear-gradient(120deg, #0a2b5e, #1557c0);
  color: rgba(255, 255, 255, 0.65);
}

.news-cta h2 {
  color: #fff;
  font-size: 18px;
  margin-bottom: 4px;
}

.news-cta p {
  font-size: 12.5px;
}

@media (max-width: 900px) {
  .featured {
    grid-template-columns: 1fr;
  }

  .featured__cover {
    min-height: 180px;
  }

  .news-grid,
  .news-skeleton__grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 620px) {
  .news-grid,
  .news-skeleton__grid {
    grid-template-columns: 1fr;
  }

  .featured__body {
    padding: 22px;
  }

  .news-filter {
    align-items: flex-start;
    flex-direction: column;
    gap: 8px;
  }

  .news-cta {
    flex-direction: column;
    align-items: flex-start;
  }

  .news-cta .btn {
    width: 100%;
  }
}
</style>
