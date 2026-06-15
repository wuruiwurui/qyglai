<template>
  <section class="operations-workbench" :data-tone="profile.tone">
    <header class="operations-hero">
      <div class="operations-hero-copy">
        <span class="operations-kicker"><component :is="profile.icon" :size="15" />{{ profile.kicker }}</span>
        <h2>{{ profile.title }}</h2>
        <p>{{ profile.description }}</p>
      </div>
      <div class="operations-hero-actions">
        <button class="icon-button" type="button" title="刷新当前视图" @click="executeSelected"><RefreshCw :size="16" /></button>
        <button v-if="actionEndpoints.length" class="primary-button" type="button" @click="actionOpen = !actionOpen">
          <Plus :size="16" />{{ actionOpen ? "收起操作" : profile.actionLabel }}
        </button>
      </div>
    </header>

    <section class="operations-metrics">
      <article v-for="metric in metrics" :key="metric.label">
        <span>{{ metric.label }}</span>
        <strong>{{ metric.value }}</strong>
        <small>{{ metric.hint }}</small>
      </article>
    </section>

    <form v-if="actionOpen && selectedEndpoint?.fields?.length" class="operations-action-form" @submit.prevent="executeSelected">
      <header><div><span>业务操作</span><h3>{{ selectedEndpoint.title }}</h3></div><p>{{ selectedEndpoint.description }}</p></header>
      <div class="operations-form-grid">
        <label v-for="field in selectedEndpoint.fields" :key="field.name" :class="{ wide: ['textarea', 'json', 'file'].includes(field.type) }">
          <span>{{ field.label }}</span>
          <input v-if="field.type === 'text' || field.type === 'number'" :value="formValue(field.name)" :type="field.type" @input="payload[field.name] = ($event.target as HTMLInputElement).value" />
          <select v-else-if="field.type === 'select'" v-model="payload[field.name]"><option v-for="option in field.options" :key="option" :value="option">{{ option }}</option></select>
          <textarea v-else-if="field.type === 'textarea' || field.type === 'json'" :value="formValue(field.name)" rows="3" @input="payload[field.name] = ($event.target as HTMLTextAreaElement).value" />
          <input v-else-if="field.type === 'file'" type="file" @change="setFile(field.name, $event)" />
          <input v-else-if="field.type === 'switch'" v-model="payload[field.name]" type="checkbox" />
        </label>
      </div>
      <footer>
        <span :class="['operations-notice', state]">{{ notice }}</span>
        <button class="primary-button" type="submit" :disabled="state === 'loading'">
          <Loader2 v-if="state === 'loading'" class="spin" :size="16" /><Play v-else :size="16" />执行操作
        </button>
      </footer>
    </form>

    <section class="operations-content">
      <aside class="operations-view-nav">
        <span>业务视图</span>
        <button v-for="endpoint in endpoints" :key="endpoint.key" :class="{ active: endpoint.key === selectedEndpoint?.key }" type="button" @click="selectEndpoint(endpoint)">
          <component :is="endpoint.method === 'GET' ? ListFilter : WandSparkles" :size="15" />
          <span><strong>{{ endpoint.title }}</strong><small>{{ endpoint.method === "GET" ? "数据视图" : "业务操作" }}</small></span>
        </button>
      </aside>

      <section class="operations-list-panel">
        <header class="operations-list-head">
          <div><span>{{ profile.listLabel }}</span><h3>{{ selectedEndpoint?.title ?? "业务数据" }}</h3></div>
          <label><Search :size="15" /><input v-model="keyword" placeholder="搜索当前视图" /></label>
        </header>
        <div v-if="state === 'loading'" class="operations-empty"><Loader2 class="spin" :size="22" />正在加载业务数据</div>
        <div v-else-if="!pagedRecords.length" class="operations-empty"><Inbox :size="24" />当前视图暂无数据</div>
        <div v-else class="operations-table-wrap">
          <table>
            <thead><tr><th v-for="column in columns" :key="column">{{ fieldLabel(column) }}</th><th>操作</th></tr></thead>
            <tbody>
              <tr v-for="(record, index) in pagedRecords" :key="String(record.id ?? index)" @click="selectedRecord = record">
                <td v-for="column in columns" :key="column"><span v-if="isStatus(column)" :class="['business-status', statusClass(record[column])]">{{ formatValue(record[column]) }}</span><template v-else>{{ formatValue(record[column]) }}</template></td>
                <td><button class="row-detail-button" type="button">查看</button></td>
              </tr>
            </tbody>
          </table>
        </div>
        <footer class="operations-pagination">
          <span>共 {{ filteredRecords.length }} 条记录</span>
          <div><button type="button" :disabled="page <= 1" @click="page--"><ChevronLeft :size="15" /></button><em>{{ page }} / {{ pageCount }}</em><button type="button" :disabled="page >= pageCount" @click="page++"><ChevronRight :size="15" /></button></div>
        </footer>
      </section>
    </section>

    <div v-if="selectedRecord" class="detail-drawer-mask" @click.self="selectedRecord = null">
      <aside class="detail-drawer operations-detail">
        <header><div><span>{{ group }}业务档案</span><h2>{{ recordTitle }}</h2></div><button class="icon-button" type="button" title="关闭详情" @click="selectedRecord = null"><X :size="18" /></button></header>
        <section class="operations-detail-summary"><component :is="profile.icon" :size="20" /><div><strong>{{ profile.detailTitle }}</strong><small>来自 {{ selectedEndpoint?.title }} 的实时业务记录</small></div></section>
        <dl class="detail-field-list"><div v-for="[key, value] in recordEntries" :key="key"><dt>{{ fieldLabel(key) }}</dt><dd>{{ formatValue(value) }}</dd></div></dl>
      </aside>
    </div>
  </section>
</template>

<script setup lang="ts">
import { computed, markRaw, ref, watch } from "vue";
import { Bell, BookOpenCheck, Cable, ChevronLeft, ChevronRight, CircleDollarSign, ClipboardList, FileCheck2, Inbox, LifeBuoy, ListFilter, Loader2, Plus, Play, RefreshCw, Search, ShieldCheck, TrendingUp, WandSparkles, X } from "lucide-vue-next";
import { createDefaultPayload, runEndpoint, type DataRecord, type EndpointSpec } from "../services/api";

const props = defineProps<{ group: string; endpoints: EndpointSpec[] }>();
const profiles: Record<string, { title: string; kicker: string; description: string; actionLabel: string; listLabel: string; detailTitle: string; tone: string; icon: unknown }> = {
  经营: { title: "经营运行总览", kicker: "经营驾驶舱", description: "集中查看平台能力、服务状态与关键业务运行情况。", actionLabel: "执行经营操作", listLabel: "运行数据", detailTitle: "经营运行详情", tone: "green", icon: markRaw(TrendingUp) },
  合同: { title: "合同履约与风险工作台", kicker: "法务与采购协同", description: "聚焦合同金额、履约状态、风险等级和待复核事项。", actionLabel: "处理合同", listLabel: "合同台账", detailTitle: "合同档案", tone: "indigo", icon: markRaw(FileCheck2) },
  财务: { title: "财务票据与对账工作台", kicker: "财务运营", description: "统一核验发票、对账批次与差异明细，快速定位资金风险。", actionLabel: "发起财务处理", listLabel: "财务记录", detailTitle: "财务业务凭证", tone: "gold", icon: markRaw(CircleDollarSign) },
  客服: { title: "客户服务处置工作台", kicker: "服务运营", description: "按优先级、情绪和分类管理工单，快速推进客户问题闭环。", actionLabel: "智能分类", listLabel: "服务队列", detailTitle: "客户问题档案", tone: "cyan", icon: markRaw(LifeBuoy) },
  销售: { title: "销售跟进作战工作台", kicker: "增长与客户推进", description: "聚焦高价值客户、商机阶段、跟进时效和下一步动作。", actionLabel: "生成跟进建议", listLabel: "跟进队列", detailTitle: "客户跟进档案", tone: "red", icon: markRaw(TrendingUp) },
  报表: { title: "经营报表中心", kicker: "数据汇报", description: "生成、归档和追踪经营日报、周报与管理层摘要。", actionLabel: "生成报表", listLabel: "报表档案", detailTitle: "报表详情", tone: "indigo", icon: markRaw(ClipboardList) },
  复核: { title: "人工复核质量工作台", kicker: "人机协同", description: "集中处理模型低置信度结果，沉淀可追踪的人工反馈。", actionLabel: "获取复核建议", listLabel: "复核队列", detailTitle: "复核任务详情", tone: "red", icon: markRaw(BookOpenCheck) },
  消息: { title: "消息触达中心", kicker: "通知运营", description: "查看站内通知与业务提醒，追踪消息触达和处理状态。", actionLabel: "创建通知", listLabel: "消息队列", detailTitle: "消息详情", tone: "cyan", icon: markRaw(Bell) },
  集成: { title: "企业系统集成中心", kicker: "连接与事件", description: "观察第三方连接器和 Webhook 事件，定位系统间数据流转问题。", actionLabel: "接收事件", listLabel: "集成运行记录", detailTitle: "集成事件详情", tone: "gold", icon: markRaw(Cable) },
  审计: { title: "安全审计与风险追踪", kicker: "合规治理", description: "追踪关键业务操作、责任主体和异常行为。", actionLabel: "审计操作", listLabel: "审计轨迹", detailTitle: "审计事件详情", tone: "red", icon: markRaw(ShieldCheck) }
};
const profile = computed(() => profiles[props.group] ?? profiles.经营);
const selectedEndpoint = ref<EndpointSpec>();
const payload = ref<DataRecord>({});
const records = ref<DataRecord[]>([]);
const selectedRecord = ref<DataRecord | null>(null);
const keyword = ref("");
const page = ref(1);
const state = ref<"idle" | "loading" | "success" | "error">("idle");
const notice = ref("请选择业务操作");
const actionOpen = ref(false);
const pageSize = 10;
const actionEndpoints = computed(() => props.endpoints.filter(item => item.method !== "GET"));
const filteredRecords = computed(() => keyword.value ? records.value.filter(item => JSON.stringify(item).toLowerCase().includes(keyword.value.toLowerCase())) : records.value);
const pageCount = computed(() => Math.max(1, Math.ceil(filteredRecords.value.length / pageSize)));
const pagedRecords = computed(() => filteredRecords.value.slice((page.value - 1) * pageSize, page.value * pageSize));
const columns = computed(() => Array.from(new Set(filteredRecords.value.flatMap(item => Object.keys(item)))).filter(key => !hidden.has(key)).slice(0, 7));
const recordEntries = computed(() => Object.entries(selectedRecord.value ?? {}).filter(([key]) => !hidden.has(key)));
const recordTitle = computed(() => String(selectedRecord.value?.name ?? selectedRecord.value?.title ?? selectedRecord.value?.customerName ?? selectedRecord.value?.contractNo ?? selectedRecord.value?.invoiceNo ?? selectedRecord.value?.id ?? "业务记录"));
const metrics = computed(() => {
  const statuses = records.value.map(item => String(item.status ?? item.reviewStatus ?? item.verifyStatus ?? item.riskLevel ?? "").toLowerCase());
  const amounts = records.value.reduce((sum, item) => sum + Number(item.amount ?? item.totalAmount ?? item.opportunityAmount ?? 0), 0);
  return [
    { label: profile.value.listLabel, value: records.value.length, hint: "当前视图记录数" },
    { label: "待处理", value: statuses.filter(value => /pending|waiting|open|待/.test(value)).length, hint: "需要人工关注" },
    { label: "风险与异常", value: statuses.filter(value => /high|risk|error|failed|异常|驳回/.test(value)).length, hint: "建议优先处理" },
    { label: amounts > 0 ? "关联金额" : "已完成", value: amounts > 0 ? formatMoney(amounts) : statuses.filter(value => /complete|success|approved|已完成|通过/.test(value)).length, hint: amounts > 0 ? "当前视图金额汇总" : "已闭环记录" }
  ];
});
const hidden = new Set(["deleted", "storageBucket", "storageKey", "requestHash", "userAgent", "orgId", "ownerUserId"]);

watch(() => props.group, initialize, { immediate: true });
watch(keyword, () => page.value = 1);

function initialize() { selectEndpoint(props.endpoints.find(item => item.primary && item.method === "GET") ?? props.endpoints.find(item => item.method === "GET") ?? props.endpoints[0]); }
function selectEndpoint(endpoint?: EndpointSpec) { if (!endpoint) return; selectedEndpoint.value = endpoint; payload.value = createDefaultPayload(endpoint); actionOpen.value = endpoint.method !== "GET"; if (endpoint.method === "GET") executeSelected(); }
async function executeSelected() { if (!selectedEndpoint.value) return; state.value = "loading"; try { const result = await runEndpoint(selectedEndpoint.value, payload.value); records.value = Array.isArray(result.data) ? result.data as DataRecord[] : result.data ? [result.data as DataRecord] : []; notice.value = `${selectedEndpoint.value.title}执行成功`; state.value = "success"; page.value = 1; } catch (error) { notice.value = error instanceof Error ? error.message : "业务操作失败"; state.value = "error"; } }
function setFile(name: string, event: Event) { payload.value[name] = (event.target as HTMLInputElement).files?.[0]; }
function formValue(name: string) { const value = payload.value[name]; return value === null || value === undefined ? "" : String(value); }
function formatMoney(value: number) { return value >= 10000 ? `¥${(value / 10000).toFixed(1)}万` : `¥${value.toFixed(2)}`; }
function isStatus(key: string) { return /status|risk|priority|level/i.test(key); }
function statusClass(value: unknown) { const text = String(value ?? "").toLowerCase(); return /success|complete|approved|low|通过|完成/.test(text) ? "ok" : /high|error|failed|reject|异常|驳回/.test(text) ? "bad" : "warn"; }
function formatValue(value: unknown) { if (value === null || value === undefined || value === "") return "-"; if (typeof value === "object") return JSON.stringify(value); return String(value); }
function fieldLabel(key: string) { return ({ id: "ID", name: "名称", title: "标题", status: "状态", createdAt: "创建时间", updatedAt: "更新时间", contractNo: "合同编号", partyA: "甲方", partyB: "乙方", amount: "金额", currency: "币种", riskLevel: "风险等级", reviewStatus: "复核状态", invoiceNo: "发票号码", invoiceCode: "发票代码", invoiceDate: "开票日期", buyerName: "购买方", sellerName: "销售方", taxAmount: "税额", totalAmount: "价税合计", verifyStatus: "校验状态", customerName: "客户名称", opportunityName: "商机名称", opportunityStage: "商机阶段", nextFollowupAt: "下次跟进", priority: "优先级", category: "分类", sentiment: "情绪", channel: "渠道", content: "内容", eventType: "事件类型", action: "操作", operatorName: "操作人", reportType: "报表类型" } as Record<string, string>)[key] ?? key; }
</script>
