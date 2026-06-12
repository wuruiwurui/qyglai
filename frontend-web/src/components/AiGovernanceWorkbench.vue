<template>
  <section class="ai-governance-workbench">
    <section class="ai-current-banner">
      <div class="ai-current-icon"><Cpu :size="22" /></div>
      <div>
        <span>当前调用模型</span>
        <h2>{{ currentProfile?.name ?? "未配置真实模型" }}</h2>
        <p>{{ currentProfile ? `${providerLabel(currentProfile.provider)} · ${currentProfile.model}` : "请新增并切换一个可用模型" }}</p>
      </div>
      <div class="ai-current-meta">
        <span :class="status?.lastCallStatus === 'success' ? 'ok' : 'warn'">{{ formatStatus(status?.lastCallStatus) }}</span>
        <small>{{ status?.lastFallbackReason ?? "切换后下一次调用立即生效" }}</small>
      </div>
    </section>

    <section class="ai-model-layout">
      <aside class="ai-profile-panel">
        <header class="file-section-head">
          <div><p>模型配置</p><h2>可用模型</h2></div>
          <button class="primary-button" type="button" @click="startCreate"><Plus :size="15" />新增</button>
        </header>
        <div class="ai-profile-list">
          <button
            v-for="profile in profiles"
            :key="profile.id"
            type="button"
            :class="{ active: profile.id === config.id, current: profile.current }"
            @click="editProfile(profile)"
          >
            <span class="ai-provider-mark"><Cpu :size="16" /></span>
            <span>
              <strong>{{ profile.name }}</strong>
              <small>{{ providerLabel(profile.provider) }} · {{ profile.model }}</small>
            </span>
            <em v-if="profile.current">当前</em>
            <em v-else-if="!profile.enabled" class="disabled">停用</em>
          </button>
          <div v-if="!profiles.length" class="file-empty">暂无模型配置</div>
        </div>
      </aside>

      <section class="ai-config-panel">
        <div class="file-section-head">
          <div>
            <p>{{ config.id ? "编辑模型" : "新增模型" }}</p>
            <h2>{{ config.name || "真实模型配置" }}</h2>
          </div>
          <div class="ai-form-actions">
            <button v-if="config.id && !config.current && config.enabled" class="switch-model-button" type="button" :disabled="state === 'loading'" @click="switchCurrent">
              <CheckCircle2 :size="15" />设为当前模型
            </button>
            <button v-if="config.id && !config.current" class="danger-icon-button" type="button" title="删除模型" :disabled="state === 'loading'" @click="removeCurrent">
              <Trash2 :size="16" />
            </button>
          </div>
        </div>

        <form class="ai-config-form" @submit.prevent="submitConfig">
          <label><span>配置名称</span><input v-model="config.name" required placeholder="例如：豆包生产模型" /></label>
          <label><span>供应商</span><select v-model="config.provider"><option value="doubao">豆包</option><option value="openai">OpenAI兼容</option><option value="local">本地模型</option><option value="mock">模拟模型</option></select></label>
          <label class="wide"><span>API地址</span><input v-model="config.apiBase" required placeholder="https://ark.cn-beijing.volces.com/api/v3" /></label>
          <label><span>模型 / Endpoint ID</span><input v-model="config.model" required placeholder="ep-xxxxxxxx" /></label>
          <label><span>API Key</span><input v-model="config.apiKey" autocomplete="off" placeholder="编辑时留空保留原密钥" /><small>当前密钥：{{ config.apiKeyMasked ?? "未配置" }}</small></label>
          <div class="ai-switch-row wide">
            <label><input v-model="config.enabled" type="checkbox" /><span>启用该模型</span></label>
            <label><input v-model="config.textGenerationEnabled" type="checkbox" /><span>文本生成调用真实模型</span></label>
          </div>
          <label><span>文件抽取模式</span><select v-model="config.fileExtractionMode"><option value="rules_first">规则优先</option><option value="model_first">模型优先</option><option value="rules_only">仅规则</option></select></label>
          <label><span>合同风险模式</span><select v-model="config.contractRiskMode"><option value="rules_first">规则优先</option><option value="model_first">模型优先</option><option value="rules_only">仅规则</option></select></label>
          <label class="wide"><span>备注</span><textarea v-model="config.remark" rows="3" /></label>
          <button class="primary-button" type="submit" :disabled="state === 'loading'"><Loader2 v-if="state === 'loading'" class="spin" :size="16" /><Save v-else :size="16" />保存模型</button>
        </form>
        <p class="ai-notice" :class="state">{{ notice }}</p>
      </section>
    </section>

    <section class="ai-routing-panel">
      <div class="file-section-head">
        <div><p>调用路由</p><h2>场景模型路由与备用切换</h2></div>
        <span class="status-chip">{{ routes.length }} 个场景已配置</span>
      </div>
      <div class="ai-routing-layout">
        <form class="ai-route-form" @submit.prevent="submitRoute">
          <label><span>业务场景</span><select v-model="routeDraft.scenario" @change="loadRouteDraft">
            <option v-for="item in routeScenarios" :key="item.value" :value="item.value">{{ item.label }}</option>
          </select></label>
          <label><span>主模型</span><select v-model="routeDraft.primaryProfileId" required>
            <option value="" disabled>请选择主模型</option>
            <option v-for="profile in enabledProfiles" :key="profile.id" :value="profile.id">{{ profile.name }} · {{ profile.model }}</option>
          </select></label>
          <fieldset>
            <legend>备用模型顺序</legend>
            <label v-for="profile in fallbackProfiles" :key="profile.id">
              <input :checked="routeDraft.fallbackProfileIds.includes(profile.id ?? '')" type="checkbox" @change="toggleFallback(profile.id ?? '', ($event.target as HTMLInputElement).checked)" />
              <span>{{ profile.name }} · {{ profile.model }}</span>
            </label>
            <small>勾选顺序即故障切换顺序；主模型请求失败或返回无效 JSON 时自动尝试备用模型。</small>
          </fieldset>
          <div class="ai-route-actions">
            <button class="primary-button" type="submit" :disabled="state === 'loading' || !routeDraft.primaryProfileId"><Route :size="16" />保存路由</button>
            <button v-if="currentRoute" class="ghost-button" type="button" @click="removeRoute"><Trash2 :size="15" />恢复默认模型</button>
          </div>
        </form>
        <div class="ai-route-list">
          <article v-for="route in routes" :key="route.scenario" :class="{ active: route.scenario === routeDraft.scenario }" @click="selectRoute(route.scenario)">
            <span><Route :size="16" /></span>
            <div><strong>{{ scenarioLabel(route.scenario) }}</strong><small>主模型：{{ route.primaryModel }}</small></div>
            <em>{{ route.fallbackModels?.length ?? 0 }} 个备用</em>
          </article>
          <div v-if="!routes.length" class="file-empty">尚未配置场景路由，所有场景使用当前默认模型</div>
        </div>
      </div>
    </section>

    <section v-if="evaluation" class="ai-evaluation-center">
      <div class="file-section-head">
        <div><p>效果评估</p><h2>AI 业务效果评估中心</h2></div>
        <select v-model.number="evaluationDays" class="ai-evaluation-range" @change="loadEvaluation">
          <option :value="7">最近 7 天</option><option :value="30">最近 30 天</option>
          <option :value="90">最近 90 天</option><option :value="0">全部数据</option>
        </select>
      </div>
      <div class="ai-evaluation-metrics">
        <article><span><Activity :size="16" />模型调用</span><strong>{{ evaluation.overview.totalCalls }}</strong><small>失败 {{ evaluation.overview.failedCalls }} 次</small></article>
        <article><span><CheckCircle2 :size="16" />调用成功率</span><strong>{{ evaluation.overview.successRate }}%</strong><small>成功 {{ evaluation.overview.successfulCalls }} 次</small></article>
        <article><span><Gauge :size="16" />平均耗时</span><strong>{{ evaluation.overview.averageLatencyMs }}ms</strong><small>累计 Token {{ evaluation.overview.totalTokens }}</small></article>
        <article><span><GitCompare :size="16" />AI 自动通过率</span><strong>{{ evaluation.feedback.aiAutoApprovalRate }}%</strong><small>转人工 {{ evaluation.feedback.aiManualReview }} 次</small></article>
      </div>
      <div class="ai-evaluation-grid">
        <section>
          <header><div><strong>模型表现</strong><small>成功率、耗时与调用量对比</small></div></header>
          <div class="ai-performance-list">
            <article v-for="item in evaluation.modelPerformance.slice(0, 6)" :key="item.name">
              <div><strong>{{ item.name }}</strong><small>{{ item.calls }} 次 · {{ item.averageLatencyMs }}ms</small></div>
              <div class="ai-rate-track"><span :style="{ width: `${item.successRate}%` }"></span></div><em>{{ item.successRate }}%</em>
            </article>
          </div>
        </section>
        <section>
          <header><div><strong>高频修正字段</strong><small>人工修正暴露的模型薄弱字段</small></div></header>
          <div class="ai-correction-list">
            <article v-for="item in evaluation.fieldCorrections.slice(0, 6)" :key="item.fieldKey">
              <span><PencilLine :size="15" /></span>
              <div><strong>{{ item.fieldName }}</strong><small>影响 {{ item.affectedFiles }} 个文件</small></div>
              <em>{{ item.correctionCount }} 次</em>
            </article>
            <div v-if="!evaluation.fieldCorrections.length" class="file-empty">暂无人工字段修正记录</div>
          </div>
        </section>
        <section>
          <header><div><strong>业务场景表现</strong><small>定位高失败率或高延迟的 AI 场景</small></div></header>
          <div class="ai-performance-list">
            <article v-for="item in evaluation.scenarioPerformance.slice(0, 6)" :key="item.name">
              <div><strong>{{ scenarioLabel(item.name) }}</strong><small>{{ item.calls }} 次 · {{ item.averageLatencyMs }}ms</small></div>
              <div class="ai-rate-track"><span :style="{ width: `${item.successRate}%` }"></span></div><em>{{ item.successRate }}%</em>
            </article>
          </div>
        </section>
        <section class="ai-feedback-panel">
          <header><div><strong>反馈闭环</strong><small>AI 输出进入人工处理后的反馈数据</small></div></header>
          <dl>
            <div><dt>字段修正批次</dt><dd>{{ evaluation.feedback.correctionBatches }}</dd></div>
            <div><dt>人工复核任务</dt><dd>{{ evaluation.feedback.reviewTasks }}</dd></div>
            <div><dt>已完成复核</dt><dd>{{ evaluation.feedback.completedReviews }}</dd></div>
            <div><dt>有效反馈样本</dt><dd>{{ evaluation.feedback.feedbackSamples }}</dd></div>
            <div><dt>AI 自动通过</dt><dd>{{ evaluation.feedback.aiAutoApproved }}</dd></div>
            <div><dt>AI 转人工</dt><dd>{{ evaluation.feedback.aiManualReview }}</dd></div>
          </dl>
        </section>
      </div>
    </section>

    <section class="ai-log-panel">
      <div class="file-section-head">
        <div><p>调用观测</p><h2>模型调用日志</h2></div>
        <span class="status-chip">{{ logs.length }} 条</span>
      </div>
      <div class="ai-log-table">
        <table>
          <thead><tr><th>业务场景</th><th>业务类型</th><th>调用模型</th><th>耗时</th><th>结果</th><th>时间</th></tr></thead>
          <tbody>
            <tr v-for="log in logs.slice(0, 15)" :key="log.id">
              <td>{{ scenarioLabel(log.scenario) }}</td><td>{{ businessTypeLabel(log.businessType) }}</td>
              <td>{{ log.modelName ?? "-" }}</td><td>{{ log.latencyMs ?? 0 }}ms</td>
              <td><span class="result-chip" :class="log.successFlag === 1 ? 'ok' : 'bad'">{{ log.successFlag === 1 ? "成功" : "失败" }}</span></td>
              <td>{{ formatTime(log.createdAt) }}</td>
            </tr>
          </tbody>
        </table>
        <div v-if="!logs.length" class="file-empty">暂无调用日志</div>
      </div>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import { Activity, CheckCircle2, Cpu, Gauge, GitCompare, Loader2, PencilLine, Plus, Route, Save, Trash2 } from "lucide-vue-next";
import {
  deleteAiModelProfile, deleteAiScenarioRoute, fetchAiEvaluationDashboard, fetchAiModelCallLogs, fetchAiModelProfiles,
  fetchAiRuntimeStatus, fetchAiScenarioRoutes, saveAiModelProfile, saveAiScenarioRoute, switchAiModelProfile,
  type AiEvaluationDashboard, type AiModelCallLog, type AiModelProfile, type AiRuntimeStatus, type AiScenarioRoute
} from "../services/api";

type LoadState = "idle" | "loading" | "success" | "error";
const status = ref<AiRuntimeStatus | null>(null);
const profiles = ref<AiModelProfile[]>([]);
const logs = ref<AiModelCallLog[]>([]);
const evaluation = ref<AiEvaluationDashboard | null>(null);
const evaluationDays = ref(30);
const routes = ref<AiScenarioRoute[]>([]);
const state = ref<LoadState>("idle");
const notice = ref("模型切换后，下一次真实模型调用立即生效");
const emptyProfile = (): AiModelProfile => ({
  name: "", provider: "doubao", apiBase: "https://ark.cn-beijing.volces.com/api/v3", apiKey: "", model: "",
  enabled: true, textGenerationEnabled: true, fileExtractionMode: "rules_first", contractRiskMode: "rules_first", remark: ""
});
const config = ref<AiModelProfile>(emptyProfile());
const currentProfile = computed(() => profiles.value.find((item) => item.current));
const routeScenarios = [
  { value: "boss_query", label: "老板问答" }, { value: "file_parse_extract", label: "文件解析抽取" },
  { value: "document_ocr", label: "图片与扫描件 OCR" }, { value: "knowledge_query", label: "知识问答" },
  { value: "workflow_ai_review", label: "工作流 AI 复核" }, { value: "ticket_classify", label: "客服分类" },
  { value: "reconciliation_analysis", label: "对账分析" }, { value: "review_advice", label: "复核建议" },
  { value: "report_generate", label: "报表生成" }, { value: "sales_followup", label: "销售跟进建议" }
];
const routeDraft = ref<AiScenarioRoute>({ scenario: "boss_query", primaryProfileId: "", fallbackProfileIds: [] });
const enabledProfiles = computed(() => profiles.value.filter((item) => item.enabled && item.id));
const fallbackProfiles = computed(() => enabledProfiles.value.filter((item) => item.id !== routeDraft.value.primaryProfileId));
const currentRoute = computed(() => routes.value.find((item) => item.scenario === routeDraft.value.scenario));

onMounted(loadAll);

async function loadAll() {
  state.value = "loading";
  try {
    [profiles.value, status.value, logs.value, evaluation.value, routes.value] = await Promise.all([
      fetchAiModelProfiles(), fetchAiRuntimeStatus(), fetchAiModelCallLogs(), fetchAiEvaluationDashboard(evaluationDays.value),
      fetchAiScenarioRoutes()
    ]);
    const selected = profiles.value.find((item) => item.id === config.value.id) ?? currentProfile.value ?? profiles.value[0];
    config.value = selected ? { ...selected, apiKey: "" } : emptyProfile();
    loadRouteDraft();
    notice.value = "已加载模型配置";
    state.value = "success";
  } catch (error) { fail(error, "模型配置加载失败"); }
}

async function loadEvaluation() {
  try { evaluation.value = await fetchAiEvaluationDashboard(evaluationDays.value); }
  catch (error) { fail(error, "AI效果评估加载失败"); }
}

function startCreate() { config.value = emptyProfile(); notice.value = "填写新模型连接信息"; }
function editProfile(profile: AiModelProfile) { config.value = { ...profile, apiKey: "" }; notice.value = profile.current ? "当前调用模型" : "可编辑或切换此模型"; }

async function submitConfig() {
  state.value = "loading";
  try {
    const { current: _current, apiKeyMasked: _apiKeyMasked, ...payload } = config.value;
    const saved = await saveAiModelProfile(payload);
    notice.value = "模型配置已保存";
    config.value = { ...saved, apiKey: "" };
    await loadAll();
  } catch (error) { fail(error, "模型配置保存失败"); }
}

async function switchCurrent() {
  if (!config.value.id) return;
  state.value = "loading";
  try { await switchAiModelProfile(config.value.id); notice.value = `已切换到 ${config.value.name}`; await loadAll(); }
  catch (error) { fail(error, "模型切换失败"); }
}

async function removeCurrent() {
  if (!config.value.id) return;
  state.value = "loading";
  try { await deleteAiModelProfile(config.value.id); config.value = emptyProfile(); notice.value = "模型配置已删除"; await loadAll(); }
  catch (error) { fail(error, "模型删除失败"); }
}

function loadRouteDraft() {
  const saved = currentRoute.value;
  routeDraft.value = saved ? { ...saved, fallbackProfileIds: [...saved.fallbackProfileIds] }
    : { scenario: routeDraft.value.scenario, primaryProfileId: currentProfile.value?.id ?? enabledProfiles.value[0]?.id ?? "", fallbackProfileIds: [] };
}
function selectRoute(scenario: string) { routeDraft.value.scenario = scenario; loadRouteDraft(); }
function toggleFallback(id: string, checked: boolean) {
  if (!id) return;
  routeDraft.value.fallbackProfileIds = checked
    ? [...routeDraft.value.fallbackProfileIds.filter((item) => item !== id), id]
    : routeDraft.value.fallbackProfileIds.filter((item) => item !== id);
}
async function submitRoute() {
  state.value = "loading";
  try { await saveAiScenarioRoute(routeDraft.value); notice.value = "场景模型路由已保存"; await loadAll(); }
  catch (error) { fail(error, "场景模型路由保存失败"); }
}
async function removeRoute() {
  state.value = "loading";
  try { await deleteAiScenarioRoute(routeDraft.value.scenario); notice.value = "该场景已恢复使用当前默认模型"; await loadAll(); }
  catch (error) { fail(error, "场景模型路由删除失败"); }
}

function fail(error: unknown, fallback: string) { notice.value = error instanceof Error ? error.message : fallback; state.value = "error"; }
function providerLabel(value?: string) { return ({ doubao: "豆包", openai: "OpenAI兼容", local: "本地模型", mock: "模拟模型" } as Record<string, string>)[value ?? ""] ?? value ?? "-"; }
function formatStatus(value?: string) { return ({ not_called: "尚未调用", success: "最近调用成功", fallback: "当前已降级", failed: "调用失败" } as Record<string, string>)[value ?? ""] ?? value ?? "-"; }
function scenarioLabel(value?: string | null) { return ({ boss_query: "老板问答", file_parse_extract: "文件解析抽取", document_ocr: "图片与扫描件 OCR", knowledge_query: "知识问答", workflow_ai_review: "工作流 AI 复核", ticket_classify: "客服分类", reconciliation_analysis: "对账分析", review_advice: "复核建议", report_generate: "报表生成", sales_followup: "销售跟进建议" } as Record<string, string>)[value ?? ""] ?? value ?? "-"; }
function businessTypeLabel(value?: string | null) { return ({ report: "报表", file: "文件", contract: "合同", invoice: "发票", ticket: "客服工单", company: "全公司知识库", legal: "法务知识库", finance: "财务知识库", sales: "销售知识库" } as Record<string, string>)[value ?? ""] ?? value ?? "-"; }
function formatTime(value?: string | null) { return value ? value.replace("T", " ").slice(0, 19) : "-"; }
</script>
