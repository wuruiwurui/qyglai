<template>
  <main v-if="!session" class="login-shell">
    <form class="login-card" @submit.prevent="submitLogin">
      <div class="brand login-brand">
        <span>企</span>
        <div>
          <strong>QYGL AI</strong>
          <small>企业流程自动化平台</small>
        </div>
      </div>
      <h1>登录工作台</h1>
      <label>
        <span>用户名</span>
        <input v-model="loginForm.username" autocomplete="username" />
      </label>
      <label>
        <span>密码</span>
        <input v-model="loginForm.password" type="password" autocomplete="current-password" />
      </label>
      <button class="submit-action" type="submit" :disabled="loginState === 'loading'">
        <Loader2 v-if="loginState === 'loading'" class="spin" :size="17" />
        <LogIn v-else :size="17" />
        登录
      </button>
      <p>{{ loginMessage }}</p>
    </form>
  </main>

  <main v-else class="app-shell">
    <aside class="sidebar">
      <div class="brand">
        <span>企</span>
        <div>
          <strong>QYGL AI</strong>
          <small>{{ session.user.realName }}</small>
        </div>
      </div>

      <nav class="module-nav" aria-label="业务模块">
        <button v-for="group in groups" :key="group" :class="{ active: activeGroup === group }" type="button" @click="switchGroup(group)">
          <component :is="groupIcons[group]" :size="17" />
          <span>{{ group }}</span>
          <em>{{ countEndpoints(group) }}</em>
        </button>
      </nav>

      <section class="sidebar-footer">
        <p>后端状态</p>
        <strong :class="health?.status === 'UP' ? 'up' : 'down'">{{ health?.status ?? "UNKNOWN" }}</strong>
        <small>{{ session.roles.join(", ") || "ADMIN" }}</small>
        <button class="logout-button" type="button" @click="logout">退出登录</button>
      </section>
    </aside>

    <section class="workspace">
      <header class="topbar">
        <div>
          <p>当前页面 / {{ activeGroup }}</p>
          <h1>{{ activeGroup }}专属工作台</h1>
        </div>
        <div class="top-actions">
          <button class="ghost-button" type="button" @click="loadHealth">
            <Wifi :size="17" />
            检查服务
          </button>
          <button class="sync-button" type="button" @click="refreshPage">
            <RefreshCw :size="17" />
            刷新页面
          </button>
        </div>
      </header>

      <section v-if="!isKnowledgeGroup" class="module-hero">
        <div>
          <p>{{ pageDescriptions[activeGroup] }}</p>
          <h2>{{ isWorkflowGroup ? "多级审批、待办处理与完整流程追踪" : selectedEndpoint?.description ?? "选择动作开始处理业务" }}</h2>
        </div>
        <div class="hero-badges">
          <span>{{ activeEndpoints.length }} 个接口</span>
          <span>{{ records.length }} 条数据</span>
          <span>{{ dataStateText }}</span>
        </div>
      </section>

      <section v-if="activeGroup === '经营'" class="overview-grid">
        <section class="assistant-panel">
          <div class="panel-title">
            <span class="icon-box"><Bot :size="21" /></span>
            <div>
              <h2>老板智能经营助手</h2>
              <p>像聊天一样查询销售、财务、客服、合同、复核和报表信息。</p>
            </div>
          </div>

          <div class="chat-window">
            <div class="chat-messages">
              <article v-for="message in chatMessages" :key="message.id" class="chat-message" :class="message.role">
                <div class="chat-avatar">
                  <Bot v-if="message.role === 'assistant'" :size="16" />
                  <Users v-else :size="16" />
                </div>
                <div class="chat-bubble">
                  <div class="chat-meta">
                    <span>{{ message.role === 'assistant' ? '经营助手' : '老板' }}</span>
                    <em v-if="message.intent">{{ intentLabel(message.intent) }}</em>
                  </div>
                  <p>{{ message.content }}</p>
                  <div v-if="message.sources?.length" class="chat-tags">
                    <span v-for="source in message.sources" :key="source">{{ source }}</span>
                  </div>
                  <div v-if="message.actions?.length" class="chat-actions">
                    <button v-for="action in message.actions" :key="action" type="button">{{ action }}</button>
                  </div>
                </div>
              </article>
              <article v-if="assistantLoading" class="chat-message assistant">
                <div class="chat-avatar"><Bot :size="16" /></div>
                <div class="chat-bubble typing">
                  <Loader2 class="spin" :size="16" />
                  <span>正在分析业务数据...</span>
                </div>
              </article>
            </div>

            <div class="quick-row">
              <button v-for="item in quickQuestions" :key="item" type="button" @click="askQuickQuestion(item)">{{ item }}</button>
            </div>

            <form class="ask-form chat-input-form" @submit.prevent="submitQuestion">
              <Search :size="18" />
              <textarea v-model="question" rows="2" placeholder="例如：本周销售有哪些客户该跟进？财务有没有对账异常？" />
              <select v-model="timeRange">
                <option value="today">今日</option>
                <option value="this_week">本周</option>
                <option value="this_month">本月</option>
              </select>
              <button type="submit" title="发送" :disabled="assistantLoading || !question.trim()">
                <Loader2 v-if="assistantLoading" class="spin" :size="18" />
                <Send v-else :size="18" />
              </button>
            </form>
          </div>
        </section>
        <section class="status-panel">
          <div class="status-line">
            <ShieldCheck :size="19" />
            <span>当前用户已通过 JWT 鉴权访问业务接口</span>
          </div>
          <dl>
            <div><dt>模块</dt><dd>{{ modules.length }}</dd></div>
            <div><dt>复核</dt><dd>{{ reviewTasks.length }}</dd></div>
            <div><dt>跟进</dt><dd>{{ followups.length }}</dd></div>
          </dl>
          <p>{{ notice }}</p>
        </section>
      </section>

      <FileWorkbench v-if="isFileGroup" @jump="handleFileJump" />
      <AiGovernanceWorkbench v-if="isAiGovernanceGroup" />
      <PermissionManagementWorkbench v-if="isSystemGroup" />
      <WorkflowApprovalWorkbench v-if="isWorkflowGroup" />
      <KnowledgeWorkbench v-if="isKnowledgeGroup" />

      <section v-if="!isFileGroup && !isAiGovernanceGroup && !isSystemGroup && !isWorkflowGroup && !isKnowledgeGroup" class="metric-grid">
        <article v-for="metric in metricCards" :key="metric.name" class="metric-card" :class="metric.status">
          <span>{{ metric.name }}</span>
          <strong>{{ metric.value }}</strong>
          <small>{{ metric.trend }}</small>
        </article>
      </section>

      <section v-if="!isFileGroup && !isAiGovernanceGroup && !isSystemGroup && !isWorkflowGroup && !isKnowledgeGroup" class="workbench-grid">
        <section class="endpoint-panel">
          <div class="data-header">
            <div>
              <p>{{ activeGroup }}</p>
              <h2>业务动作</h2>
            </div>
            <span class="state-pill" :class="dataState">{{ dataStateText }}</span>
          </div>

          <div class="endpoint-toolbar vertical">
            <button v-for="endpoint in activeEndpoints" :key="endpoint.key" :class="{ selected: selectedEndpoint?.key === endpoint.key, primary: endpoint.primary }" type="button" @click="selectEndpoint(endpoint)">
              <span>{{ endpoint.source === 'python' ? 'AI' : endpoint.method }}</span>
              {{ endpoint.title }}
            </button>
          </div>

          <form v-if="selectedEndpoint?.fields?.length" class="action-form" @submit.prevent="executeSelected">
            <label v-for="field in selectedEndpoint.fields" :key="field.name" :class="{ wide: field.type === 'textarea' || field.type === 'json' || field.type === 'file' }">
              <span>{{ field.label }}<em v-if="field.required">*</em></span>
              <input v-if="field.type === 'text' || field.type === 'number'" :type="field.type" :value="getFormValue(field.name)" @input="setFormValue(field.name, ($event.target as HTMLInputElement).value)" />
              <select v-else-if="field.type === 'select'" :value="getFormValue(field.name)" @change="setFormValue(field.name, ($event.target as HTMLSelectElement).value)">
                <option v-for="option in field.options" :key="option" :value="option">{{ option }}</option>
              </select>
              <textarea v-else-if="field.type === 'textarea' || field.type === 'json'" :value="getFormValue(field.name)" rows="5" @input="setFormValue(field.name, ($event.target as HTMLTextAreaElement).value)" />
              <input v-else-if="field.type === 'file'" type="file" @change="handleFileChange(field.name, $event)" />
              <input v-else-if="field.type === 'switch'" :checked="getBooleanValue(field.name)" type="checkbox" @change="setFormValue(field.name, ($event.target as HTMLInputElement).checked)" />
              <small v-if="field.help">{{ field.help }}</small>
            </label>
            <button class="submit-action" type="submit" :disabled="dataState === 'loading'">
              <Play :size="16" />
              执行动作
            </button>
          </form>
          <button v-else class="submit-action" type="button" :disabled="dataState === 'loading'" @click="executeSelected">
            <Play :size="16" />
            加载数据
          </button>
        </section>

        <section class="data-panel">
          <div class="table-head">
            <div>
              <h2>{{ selectedEndpoint?.title ?? "业务数据" }}</h2>
              <p>{{ filteredRecords.length }} 条记录</p>
            </div>
            <div class="table-tools">
              <Search :size="16" />
              <input v-model="keyword" placeholder="搜索本页数据" />
            </div>
          </div>
          <div v-if="!pagedRecords.length" class="empty-table">暂无数据，选择业务动作后执行。</div>
          <div v-else class="table-wrap">
            <table>
              <thead>
                <tr><th v-for="column in tableColumns" :key="column">{{ fieldLabel(column) }}</th></tr>
              </thead>
              <tbody>
                <tr v-for="(record, index) in pagedRecords" :key="index" @click="selectedRecord = record">
                  <td v-for="column in tableColumns" :key="column">{{ formatValue(record[column]) }}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <footer class="pagination">
            <button type="button" :disabled="page === 1" @click="page -= 1">上一页</button>
            <span>{{ page }} / {{ pageCount }}</span>
            <button type="button" :disabled="page === pageCount" @click="page += 1">下一页</button>
          </footer>
        </section>

        <aside class="side-stack">
          <section class="queue-panel">
            <h3>接口返回</h3>
            <pre>{{ resultPreview }}</pre>
          </section>
          <section class="queue-panel">
            <h3>选中详情</h3>
            <pre>{{ selectedRecordPreview }}</pre>
          </section>
        </aside>
      </section>
    </section>
  </main>
</template>

<script setup lang="ts">
import { computed, onMounted, ref, watch, type Component } from "vue";
import {
  Activity,
  BarChart3,
  Bell,
  Bot,
  Boxes,
  Cable,
  CheckCircle2,
  ClipboardCheck,
  FileArchive,
  FileSearch,
  Landmark,
  Loader2,
  LogIn,
  MessageSquareText,
  Play,
  RefreshCw,
  Route,
  Search,
  Send,
  ShieldCheck,
  Sparkles,
  Users,
  Wifi
} from "lucide-vue-next";
import FileWorkbench from "./components/FileWorkbench.vue";
import AiGovernanceWorkbench from "./components/AiGovernanceWorkbench.vue";
import PermissionManagementWorkbench from "./components/PermissionManagementWorkbench.vue";
import WorkflowApprovalWorkbench from "./components/WorkflowApprovalWorkbench.vue";
import KnowledgeWorkbench from "./components/KnowledgeWorkbench.vue";
import {
  askBossAssistant,
  clearSession,
  createDefaultPayload,
  endpointCatalog,
  fetchHealth,
  fetchMe,
  fetchModules,
  fetchReviewTasks,
  fetchSalesFollowups,
  getStoredSession,
  login,
  pageDescriptions,
  runEndpoint,
  type AutomationModule,
  type BossChatResponse,
  type DataRecord,
  type EndpointSpec,
  type HealthResponse,
  type LoginResponse,
  type MetricCard,
  type ReviewTask,
  type SalesFollowupTask
} from "./services/api";

type LoadState = "idle" | "loading" | "success" | "error";
type GroupName = keyof typeof pageDescriptions;
type ChatMessage = {
  id: string;
  role: "user" | "assistant";
  content: string;
  intent?: string;
  sources?: string[];
  actions?: string[];
};

const groupPermissionMap: Partial<Record<GroupName, string>> = {
  文件: "file:view", 合同: "contract:view", 财务: "finance:view", 客服: "ticket:view", 销售: "sales:view",
  知识库: "kb:view", 报表: "report:view", 流程: "workflow:view", 复核: "review:view", 消息: "notification:view",
  系统: "system:manage", 集成: "integration:view", AI治理: "ai:manage", 审计: "audit:view"
};
const groups = computed(() => (Object.keys(pageDescriptions) as GroupName[]).filter((group) => {
  const permissions = session.value?.permissions ?? [];
  if (permissions.includes("*")) return true;
  const permissionCode = groupPermissionMap[group];
  return !permissionCode || permissions.includes(permissionCode);
}));
const groupIcons: Record<string, Component> = {
  经营: BarChart3,
  文件: FileArchive,
  合同: FileSearch,
  财务: Landmark,
  客服: MessageSquareText,
  销售: Activity,
  知识库: Boxes,
  报表: ClipboardCheck,
  流程: Route,
  复核: CheckCircle2,
  消息: Bell,
  系统: Users,
  集成: Cable,
  AI治理: Sparkles,
  审计: ShieldCheck
};

const session = ref<LoginResponse | null>(getStoredSession());
const loginForm = ref({ username: "admin", password: "123456" });
const loginState = ref<LoadState>("idle");
const loginMessage = ref("默认账号：admin / 123456");
const activeGroup = ref<GroupName>("经营");
const selectedEndpoint = ref<EndpointSpec | null>(null);
const actionPayload = ref<DataRecord>({});
const records = ref<DataRecord[]>([]);
const selectedRecord = ref<DataRecord | null>(null);
const lastResult = ref<unknown>(null);
const keyword = ref("");
const page = ref(1);
const pageSize = 10;
const dataState = ref<LoadState>("idle");
const health = ref<HealthResponse | null>(null);
const modules = ref<AutomationModule[]>([]);
const reviewTasks = ref<ReviewTask[]>([]);
const followups = ref<SalesFollowupTask[]>([]);
const answer = ref<BossChatResponse | null>(null);
const question = ref("");
const timeRange = ref("today");
const notice = ref("等待操作");
const assistantLoading = ref(false);
const quickQuestions = [
  "本周销售有哪些客户该跟进？",
  "财务有没有对账异常？",
  "客服投诉主要集中在哪里？",
  "合同里有哪些高风险事项？",
  "今天有哪些复核待办？",
  "生成一份老板日报"
];
const chatMessages = ref<ChatMessage[]>([
  {
    id: "welcome",
    role: "assistant",
    content: "你可以直接问我经营问题，不限于报表。例如销售跟进、财务对账、客服投诉、合同风险和复核待办，我会按问题类型给出结论、指标和下一步动作。",
    intent: "overview",
    sources: ["CRM", "财务系统", "客服系统", "合同台账"]
  }
]);

const activeEndpoints = computed(() => endpointCatalog.filter((item) => item.group === activeGroup.value));
const isFileGroup = computed(() => activeGroup.value === "\u6587\u4ef6");
const isAiGovernanceGroup = computed(() => activeGroup.value === "\u0041\u0049\u6cbb\u7406");
const isSystemGroup = computed(() => activeGroup.value === "\u7cfb\u7edf");
const isWorkflowGroup = computed(() => activeGroup.value === "\u6d41\u7a0b");
const isKnowledgeGroup = computed(() => activeGroup.value === "\u77e5\u8bc6\u5e93");
const filteredRecords = computed(() => {
  const query = keyword.value.trim().toLowerCase();
  return query ? records.value.filter((item) => JSON.stringify(item).toLowerCase().includes(query)) : records.value;
});
const pageCount = computed(() => Math.max(1, Math.ceil(filteredRecords.value.length / pageSize)));
const pagedRecords = computed(() => filteredRecords.value.slice((page.value - 1) * pageSize, page.value * pageSize));
const tableColumns = computed(() => {
  const keys = new Set<string>();
  filteredRecords.value.forEach((record) => Object.keys(record ?? {}).forEach((key) => keys.add(key)));
  return Array.from(keys).slice(0, 8);
});
const metricCards = computed<MetricCard[]>(() => answer.value?.metrics ?? [
  { name: "自动化模块", value: String(modules.value.length), trend: "已启用", status: "normal" },
  { name: "人工复核", value: String(reviewTasks.value.length), trend: "待处理", status: reviewTasks.value.length > 0 ? "warning" : "normal" },
  { name: "销售跟进", value: String(followups.value.length), trend: "任务池", status: "normal" },
  { name: "当前接口", value: String(activeEndpoints.value.length), trend: activeGroup.value, status: "normal" }
]);
const resultPreview = computed(() => stringify(translateForDisplay(lastResult.value ?? "暂无接口返回")));
const selectedRecordPreview = computed(() => stringify(translateForDisplay(selectedRecord.value ?? "点击表格行查看详情")));
const dataStateText = computed(() => dataState.value === "loading" ? "加载中" : dataState.value === "success" ? "已连接" : dataState.value === "error" ? "异常" : "待命");

onMounted(async () => {
  if (session.value) await boot();
});

watch(keyword, () => {
  page.value = 1;
});

async function submitLogin() {
  loginState.value = "loading";
  try {
    session.value = await login(loginForm.value.username, loginForm.value.password);
    loginState.value = "success";
    await boot();
  } catch (error) {
    loginMessage.value = error instanceof Error ? error.message : "登录失败";
    loginState.value = "error";
  }
}

async function boot() {
  try {
    if (session.value) session.value = await fetchMe();
  } catch {
    clearSession();
    session.value = null;
    return;
  }
  selectEndpoint(activeEndpoints.value.find((item) => item.primary) ?? activeEndpoints.value[0]);
  await Promise.all([loadHealth(), refreshOverview()]);
}

function logout() {
  clearSession();
  session.value = null;
}

function switchGroup(group: GroupName) {
  activeGroup.value = group;
  records.value = [];
  lastResult.value = null;
  selectEndpoint(endpointCatalog.find((item) => item.group === group && item.primary) ?? activeEndpoints.value[0]);
}

function handleFileJump(target: "contract" | "invoice" | "review" | "workflow", id: string) {
  const groupMap = {
    contract: "\u5408\u540c",
    invoice: "\u8d22\u52a1",
    review: "\u590d\u6838",
    workflow: "\u6d41\u7a0b"
  } as const;
  activeGroup.value = groupMap[target] as GroupName;
  records.value = [];
  lastResult.value = null;
  notice.value = `已跳转到关联记录 ${id}`;
  selectEndpoint(endpointCatalog.find((item) => item.group === activeGroup.value && item.primary) ?? activeEndpoints.value[0]);
}

function countEndpoints(group: string) {
  return endpointCatalog.filter((item) => item.group === group).length;
}

function selectEndpoint(endpoint?: EndpointSpec) {
  if (!endpoint) return;
  selectedEndpoint.value = endpoint;
  actionPayload.value = createDefaultPayload(endpoint);
  selectedRecord.value = null;
  if (endpoint.method === "GET") executeSelected();
}

async function refreshPage() {
  await Promise.all([executeSelected(), refreshOverview(), loadHealth()]);
}

async function loadHealth() {
  try {
    health.value = await fetchHealth();
  } catch {
    health.value = { service: "automation-backend", status: "DOWN", time: new Date().toISOString() };
  }
}

async function refreshOverview() {
  const [moduleData, reviewData, followupData] = await Promise.allSettled([fetchModules(), fetchReviewTasks(), fetchSalesFollowups()]);
  if (moduleData.status === "fulfilled") modules.value = moduleData.value;
  if (reviewData.status === "fulfilled") reviewTasks.value = reviewData.value;
  if (followupData.status === "fulfilled") followups.value = followupData.value;
}

async function submitQuestion() {
  const text = question.value.trim();
  if (!text || assistantLoading.value) return;
  chatMessages.value.push({ id: `user-${Date.now()}`, role: "user", content: text });
  question.value = "";
  assistantLoading.value = true;
  try {
    const response = await askBossAssistant(text, timeRange.value);
    answer.value = response;
    chatMessages.value.push({
      id: `assistant-${Date.now()}`,
      role: "assistant",
      content: response.answer,
      intent: response.intent,
      sources: response.sources,
      actions: response.actions
    });
  } catch (error) {
    chatMessages.value.push({
      id: `assistant-error-${Date.now()}`,
      role: "assistant",
      content: error instanceof Error ? error.message : "老板问答调用失败，请稍后重试。",
      intent: "error"
    });
  } finally {
    assistantLoading.value = false;
  }
}

function askQuickQuestion(text: string) {
  question.value = text;
  submitQuestion();
}

function intentLabel(intent?: string) {
  return ({
    overview: "经营概览",
    sales: "销售",
    finance: "财务",
    customer_service: "客服",
    contract: "合同",
    review: "复核",
    report: "报表",
    error: "异常"
  } as Record<string, string>)[intent ?? ""] ?? "经营";
}

async function executeSelected() {
  if (!selectedEndpoint.value) return;
  dataState.value = "loading";
  try {
    const result = await runEndpoint(selectedEndpoint.value, actionPayload.value);
    lastResult.value = result.data;
    records.value = Array.isArray(result.data) ? result.data as DataRecord[] : result.data ? [result.data as DataRecord] : [];
    selectedRecord.value = records.value[0] ?? null;
    notice.value = `${selectedEndpoint.value.title} 执行成功`;
    page.value = 1;
    dataState.value = "success";
    if (selectedEndpoint.value.method !== "GET") await refreshOverview();
  } catch (error) {
    records.value = [];
    selectedRecord.value = null;
    lastResult.value = error instanceof Error ? error.message : "接口执行失败";
    notice.value = error instanceof Error ? error.message : "接口执行失败";
    dataState.value = "error";
    if (notice.value.includes("登录")) logout();
  }
}

function handleFileChange(fieldName: string, event: Event) {
  const target = event.target as HTMLInputElement;
  actionPayload.value[fieldName] = target.files?.[0];
}

function getFormValue(fieldName: string) {
  const value = actionPayload.value[fieldName];
  return value === undefined || value === null ? "" : String(value);
}

function setFormValue(fieldName: string, value: unknown) {
  actionPayload.value[fieldName] = value;
}

function getBooleanValue(fieldName: string) {
  return Boolean(actionPayload.value[fieldName]);
}

function formatValue(value: unknown) {
  if (value === null || value === undefined || value === "") return "-";
  if (typeof value === "string") return valueLabel(value);
  return typeof value === "object" ? JSON.stringify(value) : String(value);
}

function stringify(value: unknown) {
  return typeof value === "string" ? value : JSON.stringify(value, null, 2);
}

function fieldLabel(key: string) {
  const labels: Record<string, string> = {
    id: "ID",
    fileId: "文件ID",
    fileName: "存储文件名",
    originalName: "原始文件名",
    fileExt: "文件类型",
    mimeType: "MIME类型",
    fileSize: "文件大小",
    fileHash: "文件哈希",
    storageBucket: "存储桶",
    storageKey: "存储路径",
    businessType: "业务类型",
    parseStatus: "解析状态",
    createdAt: "创建时间",
    updatedAt: "更新时间",
    contractNo: "合同编号",
    partyA: "甲方",
    partyB: "乙方",
    amount: "金额",
    currency: "币种",
    paymentTerms: "付款条款",
    riskLevel: "风险等级",
    reviewStatus: "复核状态",
    invoiceNo: "发票号码",
    invoiceCode: "发票代码",
    invoiceDate: "开票日期",
    buyerName: "购买方名称",
    buyerTaxNo: "购买方税号",
    sellerName: "销售方名称",
    sellerTaxNo: "销售方税号",
    taxAmount: "税额",
    totalAmount: "价税合计",
    taxRate: "税率",
    verifyStatus: "校验状态",
    duplicateFlag: "重复标识",
    provider: "供应商",
    apiBase: "API地址",
    apiKeyMasked: "脱敏密钥",
    model: "模型",
    enabled: "真实模型启用",
    textGenerationEnabled: "文本生成真实模型",
    fileExtractionMode: "文件抽取模式",
    contractRiskMode: "合同风险模式",
    lastCallStatus: "最近调用状态",
    lastFallbackReason: "最近降级原因",
    modelName: "模型名称",
    promptTemplateCode: "调用接口",
    requestTokens: "请求Token估算",
    responseTokens: "响应Token估算",
    costAmount: "调用成本",
    latencyMs: "耗时毫秒",
    successFlag: "是否成功",
    errorMessage: "错误信息",
    requestHash: "请求哈希",
    taskId: "任务ID",
    taskNo: "任务编号",
    scenario: "业务场景",
    title: "标题",
    assignee: "处理人",
    status: "状态",
    reportType: "报表类型",
    summary: "摘要",
    owner: "负责人",
    code: "编码",
    name: "名称",
    description: "说明"
    ,
    spaceCode: "知识库空间编码",
    spaceName: "知识库空间名称",
    permissionScope: "权限范围",
    ownerOrgId: "归属组织ID",
    documentId: "知识文档ID",
    docType: "文档类型",
    sourceUrl: "来源地址",
    versionNo: "版本号",
    indexingStatus: "索引状态",
    chunkCount: "切片数量",
    chunkId: "知识切片ID",
    content: "内容",
    score: "相似度",
    confidence: "置信度",
    citations: "引用来源",
    humanHandoffSuggested: "建议人工处理",
    answer: "回答"
    ,
    operatorUserId: "操作人用户ID",
    operatorName: "操作人",
    actionCode: "操作编码",
    actionName: "操作名称",
    targetType: "目标类型",
    targetId: "目标ID",
    requestIp: "请求IP",
    userAgent: "用户代理",
    beforeJson: "修改前快照",
    afterJson: "修改后快照"
  };
  return labels[key] ?? key;
}

function valueLabel(value: string) {
  const labels: Record<string, string> = {
    contract: "合同",
    invoice: "发票",
    statement: "对账单",
    general: "通用",
    completed: "已完成",
    uploaded: "已上传",
    pending: "待处理",
    pending_review: "待复核",
    pending_approval: "审批中",
    approved: "已通过",
    rejected: "已驳回",
    correction_required: "待修正",
    passed: "已通过",
    failed: "失败",
    open: "待处理",
    high: "高风险",
    medium: "中风险",
    low: "低风险",
    enabled: "启用",
    disabled: "停用"
    ,
    rules_first: "规则优先",
    model_first: "模型优先",
    rules_only: "仅规则",
    not_called: "尚未调用",
    fallback: "已降级",
    success: "成功"
    ,
    FILE_FIELD_CORRECTION: "文件字段修正",
    file_asset: "文件资产"
  };
  return labels[value] ?? value;
}

function translateForDisplay(value: unknown): unknown {
  if (Array.isArray(value)) return value.map((item) => translateForDisplay(item));
  if (value && typeof value === "object") {
    return Object.fromEntries(
      Object.entries(value as Record<string, unknown>).map(([key, item]) => [fieldLabel(key), translateForDisplay(item)])
    );
  }
  return typeof value === "string" ? valueLabel(value) : value;
}
</script>
