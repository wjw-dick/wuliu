<script setup lang="ts">
import { computed } from 'vue'
import type { QuoteResult } from '../types'
import { money } from '../utils/format'

const props = defineProps<{ quote: QuoteResult }>()

interface Row {
  label: string
  value: number
  hint: string
}

const detailHint = (keyword: string): string => {
  const item = props.quote.detail?.find((detail) => detail.name.includes(keyword))
  return item?.formula || ''
}

const rows = computed<Row[]>(() => {
  const list: Row[] = [
    { label: '基础运费', value: props.quote.baseFreight, hint: detailHint('基础运费') },
    { label: '燃油附加', value: props.quote.fuelSurcharge, hint: detailHint('燃油附加') },
    { label: '装卸费', value: props.quote.loadingFee, hint: detailHint('装卸') },
    { label: '上楼费', value: props.quote.upstairsFee, hint: detailHint('上楼') },
    { label: '保险', value: props.quote.insuranceFee, hint: detailHint('保险') },
    { label: '代收手续费', value: props.quote.codFee, hint: detailHint('代收') }
  ]
  if (Number(props.quote.remoteFee) > 0) {
    list.push({ label: '远程附加', value: props.quote.remoteFee, hint: detailHint('远程') })
  }
  if (Number(props.quote.holidayFee) > 0) {
    list.push({ label: '节假日附加', value: props.quote.holidayFee, hint: detailHint('节假日') })
  }
  return list
})
</script>

<template>
  <div class="breakdown">
    <div class="breakdown__head">
      <div>
        <span class="small muted">报价单号</span>
        <p class="mono breakdown__no">{{ quote.quoteNo }}</p>
      </div>
      <div class="breakdown__source">
        <span class="pill">{{ quote.priceSource || '线路标准价' }}</span>
        <span class="small muted">{{ quote.ruleVersion }}</span>
      </div>
    </div>

    <table class="data-table breakdown__table">
      <thead>
        <tr>
          <th scope="col">费用项目</th>
          <th scope="col">计费说明</th>
          <th scope="col">金额（元）</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="row in rows" :key="row.label" :class="{ 'breakdown__row--zero': Number(row.value) === 0 }">
          <td>{{ row.label }}</td>
          <td class="breakdown__hint">{{ row.hint || '—' }}</td>
          <td class="mono">{{ money(row.value) }}</td>
        </tr>
      </tbody>
      <tfoot>
        <tr>
          <td colspan="2">合计（含附加费，不含税）</td>
          <td class="breakdown__total mono">¥ {{ money(quote.totalAmount) }}</td>
        </tr>
      </tfoot>
    </table>

    <p v-if="quote.validUntil" class="breakdown__note">{{ quote.validUntil }}</p>
  </div>
</template>

<style scoped>
.breakdown__head {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  padding: 18px 18px 12px;
  flex-wrap: wrap;
}

.breakdown__no {
  font-size: 15px;
  font-weight: 600;
  color: var(--ink);
}

.breakdown__source {
  display: flex;
  align-items: center;
  gap: 8px;
}

.breakdown__table {
  font-size: 13.5px;
}

.breakdown__row--zero td {
  color: var(--muted-light);
}

.breakdown__hint {
  color: var(--muted);
  font-size: 12px;
  max-width: 330px;
}

.breakdown__total {
  color: var(--primary);
  font-size: 19px;
  font-weight: 700;
}

.breakdown__note {
  padding: 12px 18px 16px;
  font-size: 12px;
  color: var(--muted-light);
}

@media (max-width: 620px) {
  .breakdown__hint {
    display: none;
  }

  .data-table th:nth-child(2) {
    display: none;
  }

  .breakdown__table td,
  .breakdown__table th {
    padding: 10px;
  }

  .breakdown__total {
    font-size: 17px;
  }
}
</style>
