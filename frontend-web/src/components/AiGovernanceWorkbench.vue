<template>
  <section class="ai-governance-workbench">
    <section class="ai-status-grid">
      <article>
        <span>真实模型状态</span>
        <strong :class="status?.enabled ? 'ok' : 'warn'">{{ status?.enabled ? "已启用" : "未启用" }}</strong>
        <small>{{ status?.enabled ? "请求会尝试调用真实大模型" : "当前使用规则或本地兜底" }}</small>
      </article>
      <article>
        <span>供应商 / 模型</span>
        <strong>{{ status?.provider ?? "-" }}</strong>
        <small>{{ status?.model ?? "-" }}</small>
      </article>
      <article>
        <span>最近调用</span>
        <strong>{{ formatStatus(status?.lastCallStatus) }}</strong>
        <small>{{ status?.lastFallbackReason ?? "暂无降级原因" }}</small>
      </article>
      <article>
        <span>文件抽取模式</span>
        <strong>{{ formatMode(status?.fileExtractionMode) }}</strong>
        <small>合同风险：{{ formatMode(status?.contractRiskMode) }}</small>
      </article>
    </section>

    <section class="ai-main-grid">
      <section class="ai-config-panel">
        <div class="file-section-head">
          <div>
            <p>模型配置</p>
            <h2>页面维护真实 AI 接入参数</h2>
          </div>
          <button class="ghost-button" type="button" @click="loadAll">
            <RefreshCw :size="15" />
            刷新
          </button>
        </div>

        <form class="ai-config-form" @submit.prevent="submitConfig">
          <label>
            <span>供应商</span>
            <select v-model="config.provider">
              <option value="doubao">豆包</option>
              <option value="openai">OpenAI兼容</option>
              <option value="local">本地模型</option>
              <option value="mock">模拟模型</option>
            </select>
          </label>
          <label>
            <span>API地址</span>
            <input v-model="config.apiBase" placeholder="https://ark.cn-beijing.volces.com/api/v3" />
          </label>
          <label>
            <span>模型 / Endpoint ID</span>
            <input v-model="config.model" placeholder="ep-xxxxxxxx" />
          </label>
          <label>
            <span>API Key</span>
            <input v-model="config.apiKey" autocomplete="off" placeholder="留空则保留原密钥" />
            <small>当前密钥：{{ config.apiKeyMasked ?? "未配置" }}</small>
          </label>

          <div class="ai-switch-row">
            <label>
              <input v-model="config.enabled" type="checkbox" />
              <span>启用真实模型</span>
            </label>
            <label>
              <input v-model="config.textGenerationEnabled" type="checkbox" />
              <span>文本生成走真实模型</span>
            </label>
          </div>

          <label>
            <span>文件抽取模式</span>
            <select v-model="config.fileExtractionMode">
              <option value="rules_first">规则优先</option>
              <option value="model_first">模型优先</option>
              <option value="rules_only">仅规则</option>
            </select>
          </label>
          <label>
            <span>合同风险模式</span>
            <select v-model="config.contractRiskMode">
              <option value="rules_first">规则优先</option>
              <option value="model_first">模型优先</option>
              <option value="rules_only">仅规则</option>
            </select>
          </label>
          <label class="wide">
            <span>备注</span>
            <textarea v-model="config.remark" rows="3" />
          </label>

          <button class="submit-action" type="submit" :disabled="state === 'loading'">
            <Loader2 v-if="state === 'loading'" class="spin" :size="16" />
            <Save v-else :size="16" />
            保存配置
          </button>
        </form>
        <p class="ai-notice" :class="state">{{ notice }}</p>
      </section>

      <section class="ai-log-panel">
        <div class="file-section-head">
          <div>
            <p>调用观测</p>
            <h2>模型调用日志</h2>
          </div>
          <span class="status-chip">{{ logs.length }} 条</span>
        </div>

        <div class="ai-log-table">
          <table>
            <thead>
              <tr>
                <th>业务场景</th>
                <th>业务类型</th>
                <th>模型</th>
                <th>接口</th>
                <th>耗时</th>
                <th>结果</th>
                <th>时间</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="log in logs.slice(0, 15)" :key="log.id">
                <td>{{ scenarioLabel(log.scenario) }}</td>
                <td>{{ businessTypeLabel(log.businessType) }}</td>
                <td>{{ log.modelName ?? "-" }}</td>
                <td>{{ log.promptTemplateCode ?? "-" }}</td>
                <td>{{ log.latencyMs ?? 0 }}ms</td>
                <td>
                  <span class="result-chip" :class="log.successFlag === 1 ? 'ok' : 'bad'">
                    {{ log.successFlag === 1 ? "成功" : "失败" }}
                  </span>
                </td>
                <td>{{ formatTime(log.createdAt) }}</td>
              </tr>
            </tbody>
          </table>
          <div v-if="!logs.length" class="file-empty">暂无调用日志</div>
        </div>
      </section>
    </section>
  </section>
</template>

<script setup lang="ts">
import { onMounted, ref } from "vue";
import { Loader2, RefreshCw, Save } from "lucide-vue-next";
import {
  fetchAiModelCallLogs,
  fetchAiRuntimeConfig,
  fetchAiRuntimeStatus,
  saveAiRuntimeConfig,
  type AiModelCallLog,
  type AiRuntimeConfig,
  type AiRuntimeStatus
} from "../services/api";

type LoadState = "idle" | "loading" | "success" | "error";

const status = ref<AiRuntimeStatus | null>(null);
const logs = ref<AiModelCallLog[]>([]);
const state = ref<LoadState>("idle");
const notice = ref("配置保存后会立即影响 Java AI 模型网关运行策略");

// 表单对象保持完整字段，避免保存时覆盖掉服务端已有运行策略。
const config = ref<AiRuntimeConfig>({
  provider: "doubao",
  apiBase: "https://ark.cn-beijing.volces.com/api/v3",
  apiKey: "",
  model: "",
  enabled: false,
  textGenerationEnabled: true,
  fileExtractionMode: "rules_first",
  contractRiskMode: "rules_first",
  remark: ""
});

onMounted(loadAll);

async function loadAll() {
  state.value = "loading";
  try {
    const [runtimeStatus, runtimeConfig, callLogs] = await Promise.all([
      fetchAiRuntimeStatus(),
      fetchAiRuntimeConfig(),
      fetchAiModelCallLogs()
    ]);
    status.value = runtimeStatus;
    config.value = { ...runtimeConfig, apiKey: "" };
    logs.value = callLogs;
    notice.value = "已刷新 AI 运行状态";
    state.value = "success";
  } catch (error) {
    notice.value = error instanceof Error ? error.message : "AI治理数据加载失败";
    state.value = "error";
  }
}

async function submitConfig() {
  state.value = "loading";
  try {
    const saved = await saveAiRuntimeConfig(config.value);
    config.value = { ...saved, apiKey: "" };
    status.value = await fetchAiRuntimeStatus();
    notice.value = "AI 配置已保存";
    state.value = "success";
  } catch (error) {
    notice.value = error instanceof Error ? error.message : "AI配置保存失败";
    state.value = "error";
  }
}

function formatMode(value?: string) {
  return ({
    rules_first: "规则优先",
    model_first: "模型优先",
    rules_only: "仅规则"
  } as Record<string, string>)[value ?? ""] ?? value ?? "-";
}

function formatStatus(value?: string) {
  return ({
    not_called: "尚未调用",
    success: "成功",
    fallback: "已降级",
    failed: "失败"
  } as Record<string, string>)[value ?? ""] ?? value ?? "-";
}

function scenarioLabel(value?: string | null) {
  return ({
    boss_query: "老板问答",
    file_parse_extract: "文件解析抽取"
  } as Record<string, string>)[value ?? ""] ?? value ?? "-";
}

function businessTypeLabel(value?: string | null) {
  return ({
    report: "报表",
    file: "文件",
    contract: "合同",
    invoice: "发票",
    ticket: "客服工单"
  } as Record<string, string>)[value ?? ""] ?? value ?? "-";
}

function formatTime(value?: string | null) {
  if (!value) return "-";
  return value.replace("T", " ").slice(0, 19);
}
</script>
