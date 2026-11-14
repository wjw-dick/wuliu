<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import SiteMap from '../components/SiteMap.vue'
import { getSites } from '../api/public'
import type { Site } from '../types'

const route = useRoute()
const sites = ref<Site[]>([])
const loading = ref(true)
const view = ref<'map' | 'list'>('map')
const city = ref<string>('全部')
const keyword = ref('')
const selectedId = ref<number | null>(null)

const cities = computed(() => ['全部', ...Array.from(new Set(sites.value.map((site) => site.city)))])

const filtered = computed(() => {
  const text = keyword.value.trim()
  return sites.value.filter((site) => {
    const cityMatch = city.value === '全部' || site.city === city.value
    const textMatch =
      !text ||
      [site.name, site.address, site.serviceArea, site.city, site.phone].some((field) =>
        field?.includes(text)
      )
    return cityMatch && textMatch
  })
})

function reset() {
  city.value = '全部'
  keyword.value = ''
  selectedId.value = filtered.value[0]?.id ?? null
}

function selectSite(site: Site) {
  selectedId.value = site.id
  if (view.value === 'map') {
    document.getElementById(`site-${site.id}`)?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }
}

onMounted(async () => {
  sites.value = await getSites()
  const queryCity = typeof route.query.city === 'string' ? route.query.city : ''
  if (queryCity && sites.value.some((site) => site.city === queryCity)) {
    city.value = queryCity
  }
  selectedId.value = filtered.value[0]?.id ?? null
  loading.value = false
})
</script>

<template>
  <div class="sites-page">
    <section class="page-hero">
      <div class="container page-hero__inner">
        <span class="eyebrow">SERVICE NETWORK</span>
        <h1>找到离您最近的网点</h1>
        <p>长三角 8 个直营网点与分拨中心，支持上门提货、自送自提与末端配送。点击地图图钉查看地址与电话，可直接一键拨号。</p>
      </div>
    </section>

    <section class="section section--tight sites-main">
      <div class="container">
        <div class="sites-toolbar card">
          <div class="sites-search">
            <span aria-hidden="true">⌕</span>
            <label class="sr-only" for="site-keyword">搜索网点</label>
            <input id="site-keyword" v-model="keyword" type="search" placeholder="搜索城市、网点名称或地址" />
          </div>
          <div class="sites-cities" role="group" aria-label="按城市筛选">
            <button
              v-for="item in cities"
              :key="item"
              type="button"
              :class="{ 'is-active': city === item }"
              @click="city = item; selectedId = filtered[0]?.id ?? null"
            >
              {{ item }}
            </button>
          </div>
          <div class="sites-view" role="group" aria-label="视图切换">
            <button type="button" :class="{ 'is-active': view === 'map' }" @click="view = 'map'">地图</button>
            <button type="button" :class="{ 'is-active': view === 'list' }" @click="view = 'list'">列表</button>
          </div>
        </div>

        <div v-if="loading" class="sites-loading">
          <div class="skeleton sites-loading__map"></div>
          <div class="sites-loading__list">
            <div class="skeleton"></div>
            <div class="skeleton"></div>
            <div class="skeleton"></div>
          </div>
        </div>

        <div v-else-if="!filtered.length" class="state-block">
          <div class="state-block__icon" aria-hidden="true">⌕</div>
          <h3>没有找到匹配的网点</h3>
          <p>试试其他城市或关键词，也可以直接致电客服，我们会安排就近网点对接。</p>
          <button class="btn btn--outline btn--sm" type="button" @click="reset">清除筛选</button>
        </div>

        <template v-else>
          <p class="sites-count">
            共找到 <strong>{{ filtered.length }}</strong> 个网点
            <span v-if="city !== '全部'">· {{ city }}</span>
            <span v-if="keyword">· 关键词「{{ keyword }}」</span>
          </p>

          <div v-if="view === 'map'" class="map-layout">
            <SiteMap :sites="filtered" :selected-id="selectedId" @select="selectSite" />
            <div class="map-list">
              <button
                v-for="site in filtered"
                :id="`site-${site.id}`"
                :key="site.id"
                type="button"
                class="map-list__item"
                :class="{ 'is-active': selectedId === site.id }"
                @click="selectedId = site.id"
              >
                <div class="map-list__top">
                  <span class="map-list__city">{{ site.city }}</span>
                  <span class="map-list__type">{{ site.type || '营业网点' }}</span>
                </div>
                <strong>{{ site.name }}</strong>
                <p>{{ site.address }}</p>
                <span class="map-list__phone" @click.stop>
                  <a :href="`tel:${site.phone}`">☎ {{ site.phone }}</a>
                </span>
              </button>
            </div>
          </div>

          <div v-else class="grid grid--2 site-cards">
            <article
              v-for="site in filtered"
              :id="`site-${site.id}`"
              :key="site.id"
              class="site-card card"
              :class="{ 'is-active': selectedId === site.id }"
              @mouseenter="selectedId = site.id"
            >
              <div class="site-card__head">
                <span class="site-card__badge">{{ site.city }}</span>
                <span class="tag tag--gray">{{ site.type || '营业网点' }}</span>
              </div>
              <h2>{{ site.name }}</h2>
              <p class="site-card__address">{{ site.address }}</p>
              <dl class="site-card__meta">
                <div>
                  <dt>营业时间</dt>
                  <dd>{{ site.businessHours }}</dd>
                </div>
                <div>
                  <dt>服务范围</dt>
                  <dd>{{ site.serviceArea }}</dd>
                </div>
              </dl>
              <div class="site-card__actions">
                <a class="btn btn--primary btn--sm" :href="`tel:${site.phone}`">一键拨号 {{ site.phone }}</a>
                <a class="btn btn--outline btn--sm" target="_blank" rel="noopener" :href="`https://uri.amap.com/search?keyword=${encodeURIComponent(site.address)}`">查看地图</a>
              </div>
            </article>
          </div>
        </template>

        <div class="sites-tip">
          <div>
            <h2>找不到合适的网点？</h2>
            <p>我们支持上门提货与全国联运，即使当地没有直营网点，也可通过合作网络为您安排。</p>
          </div>
          <div class="sites-tip__actions">
            <a class="btn btn--primary btn--sm" href="tel:4008801688">400-880-1688</a>
            <RouterLink class="btn btn--outline btn--sm" to="/contact">在线留言</RouterLink>
          </div>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.sites-main {
  background: var(--soft);
}

.sites-toolbar {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 14px 16px;
  margin-bottom: 18px;
  flex-wrap: wrap;
}

.sites-search {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1 1 260px;
  min-height: 42px;
  padding: 0 12px;
  border: 1px solid #d8e1ee;
  border-radius: 9px;
  background: #fbfdff;
  color: var(--muted-light);
}

.sites-search:focus-within {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(31, 111, 235, 0.1);
  background: #fff;
}

.sites-search input {
  flex: 1;
  border: 0;
  background: transparent;
  font-size: 13.5px;
  outline: none;
  min-width: 0;
}

.sites-cities {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.sites-cities button {
  border: 0;
  background: transparent;
  color: var(--muted);
  font-size: 12.5px;
  padding: 7px 11px;
  min-height: 36px;
  border-radius: 7px;
}

.sites-cities button:hover {
  background: var(--soft);
  color: var(--primary);
}

.sites-cities button.is-active {
  background: #eaf3ff;
  color: var(--primary);
  font-weight: 700;
}

.sites-view {
  display: inline-flex;
  padding: 3px;
  background: #f1f5fb;
  border-radius: 8px;
  margin-left: auto;
}

.sites-view button {
  border: 0;
  background: transparent;
  font-size: 12.5px;
  padding: 7px 14px;
  min-height: 36px;
  border-radius: 6px;
  color: var(--muted);
}

.sites-view button.is-active {
  background: #fff;
  color: var(--primary);
  font-weight: 700;
  box-shadow: 0 1px 5px rgba(15, 39, 71, 0.08);
}

.sites-count {
  font-size: 12.5px;
  color: var(--muted);
  margin-bottom: 12px;
}

.sites-count strong {
  color: var(--primary);
}

.sites-loading {
  display: grid;
  grid-template-columns: 1fr 330px;
  gap: 20px;
}

.sites-loading__map {
  min-height: 440px;
}

.sites-loading__list {
  display: grid;
  gap: 12px;
  align-content: start;
}

.sites-loading__list .skeleton {
  height: 120px;
}

.map-layout {
  display: grid;
  grid-template-columns: 1fr 330px;
  gap: 18px;
  align-items: stretch;
}

.map-layout :deep(.site-map) {
  min-height: 560px;
}

.map-list {
  display: flex;
  flex-direction: column;
  gap: 10px;
  max-height: 560px;
  overflow-y: auto;
  padding-right: 4px;
}

.map-list__item {
  text-align: left;
  padding: 14px 16px;
  border-radius: 12px;
  border: 1px solid var(--line);
  background: #fff;
  transition: border-color 0.16s ease, box-shadow 0.16s ease;
}

.map-list__item:hover {
  border-color: #bcd7f8;
}

.map-list__item.is-active {
  border-color: var(--primary);
  box-shadow: 0 0 0 3px rgba(31, 111, 235, 0.1);
}

.map-list__top {
  display: flex;
  align-items: center;
  gap: 6px;
  margin-bottom: 5px;
}

.map-list__city {
  font-size: 10px;
  font-weight: 700;
  color: #fff;
  background: var(--primary);
  border-radius: 4px;
  padding: 1px 6px;
}

.map-list__type {
  font-size: 10px;
  color: var(--muted-light);
}

.map-list__item strong {
  display: block;
  font-size: 13.5px;
  color: var(--ink);
}

.map-list__item p {
  font-size: 11.5px;
  color: var(--muted);
  line-height: 1.5;
  margin: 3px 0 5px;
}

.map-list__phone a {
  font-size: 11.5px;
  color: var(--primary);
  font-weight: 600;
}

.site-card {
  padding: 22px;
  transition: border-color 0.18s ease, box-shadow 0.18s ease;
  scroll-margin-top: 120px;
}

.site-card.is-active {
  border-color: #bcd7f8;
  box-shadow: var(--shadow);
}

.site-card__head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 12px;
}

.site-card__badge {
  font-size: 11px;
  font-weight: 700;
  color: #fff;
  background: var(--primary);
  border-radius: 5px;
  padding: 2px 9px;
}

.site-card h2 {
  font-size: 18px;
  margin-bottom: 6px;
}

.site-card__address {
  font-size: 13px;
  color: var(--muted);
  line-height: 1.7;
}

.site-card__meta {
  display: grid;
  gap: 10px;
  margin: 16px 0;
  padding: 14px 0;
  border-top: 1px solid var(--line);
  border-bottom: 1px solid var(--line);
}

.site-card__meta div {
  display: flex;
  gap: 12px;
  font-size: 12px;
}

.site-card__meta dt {
  color: var(--muted-light);
  flex: 0 0 60px;
}

.site-card__meta dd {
  margin: 0;
  color: #32465e;
}

.site-card__actions {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.sites-tip {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  margin-top: 30px;
  padding: 24px 28px;
  border-radius: var(--radius);
  background: #071a33;
  color: rgba(255, 255, 255, 0.68);
}

.sites-tip h2 {
  color: #fff;
  font-size: 17px;
  margin-bottom: 4px;
}

.sites-tip p {
  font-size: 12.5px;
}

.sites-tip__actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.sites-tip .btn--outline {
  background: transparent;
  border-color: rgba(255, 255, 255, 0.28);
  color: #fff;
}

@media (max-width: 1080px) {
  .map-layout,
  .sites-loading {
    grid-template-columns: 1fr;
  }

  .map-list {
    max-height: none;
    display: grid;
    grid-template-columns: repeat(2, 1fr);
    overflow: visible;
  }

  .map-layout :deep(.site-map) {
    min-height: 460px;
  }

  .sites-view {
    margin-left: 0;
  }
}

@media (max-width: 760px) {
  .map-list {
    grid-template-columns: 1fr;
  }

  .sites-toolbar {
    gap: 10px;
  }

  .sites-cities {
    order: 3;
    width: 100%;
  }

  .sites-tip {
    flex-direction: column;
    align-items: flex-start;
  }

  .sites-tip__actions {
    width: 100%;
  }

  .sites-tip__actions .btn {
    flex: 1;
  }
}
</style>
