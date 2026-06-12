<template>
  <section class="file-workbench">
    <section class="file-upload-panel">
      <div class="file-upload-copy">
        <p>文件处理</p>
        <h2>上传后自动解析、抽取、入库、生成复核任务</h2>
      </div>
      <form class="file-upload-form" @submit.prevent="submitUpload">
        <label>
          <span>业务类型</span>
          <select v-model="businessType">
            <option value="contract">合同</option>
            <option value="invoice">发票</option>
            <option value="statement">对账单</option>
            <option value="general">通用文件</option>
          </select>
        </label>
        <label class="file-picker">
          <input type="file" accept=".pdf,.docx,.xlsx,.xls,.xlsm,.txt,.md,.csv,.json,.png,.jpg,.jpeg,.bmp,.webp,.tif,.tiff" @change="handleFileChange" />
          <UploadCloud :size="20" />
          <strong>{{ selectedFile?.name ?? "选择文档、图片或扫描版 PDF" }}</strong>
          <small>{{ selectedFile ? formatBytes(selectedFile.size) : "图片与扫描件会自动调用当前多模态模型进行 OCR" }}</small>
        </label>
        <button class="submit-action" type="submit" :disabled="state === 'loading' || !selectedFile">
          <Loader2 v-if="state === 'loading'" class="spin" :size="16" />
          <Play v-else :size="16" />
          开始处理
        </button>
      </form>
      <div class="file-upload-status" :class="state">{{ notice }}</div>
    </section>

    <section class="file-summary-grid">
      <article>
        <span>文件总数</span>
        <strong>{{ files.length }}</strong>
      </article>
      <article>
        <span>已完成解析</span>
        <strong>{{ completedCount }}</strong>
      </article>
      <article>
        <span>待复核任务</span>
        <strong>{{ pendingReviewCount }}</strong>
      </article>
      <article>
        <span>当前业务类型</span>
        <strong>{{ businessTypeLabel }}</strong>
      </article>
    </section>

    <section class="file-layout">
      <section class="file-list-panel">
        <div class="file-section-head">
          <div>
            <p>历史文件</p>
            <h2>上传记录</h2>
          </div>
          <button class="ghost-button" type="button" @click="loadFiles">
            <RefreshCw :size="15" />
            刷新
          </button>
        </div>
        <div class="file-list">
          <button
            v-for="file in files"
            :key="file.id"
            :class="{ active: detail?.file.id === file.id }"
            type="button"
            @click="selectFile(file.id)"
          >
            <FileText :size="18" />
            <span>
              <strong>{{ file.originalName }}</strong>
              <small>{{ formatBusinessType(file.businessType) }} · {{ formatBytes(file.fileSize) }} · {{ formatStatus(file.parseStatus) }}</small>
            </span>
          </button>
          <div v-if="!files.length" class="file-empty">暂无文件记录</div>
        </div>
      </section>

      <section class="file-detail-panel">
        <div v-if="!detail" class="file-empty detail-empty">
          <FileSearch :size="28" />
          <span>选择左侧文件查看解析详情</span>
        </div>
        <template v-else>
          <div class="file-section-head">
            <div>
              <p>文件详情</p>
              <h2>{{ detail.file.originalName }}</h2>
            </div>
            <span class="status-chip" :class="detail.file.parseStatus">{{ formatStatus(detail.file.parseStatus) }}</span>
          </div>

          <div class="detail-tabs">
            <button :class="{ active: activeTab === 'fields' }" type="button" @click="activeTab = 'fields'">抽取字段</button>
            <button :class="{ active: activeTab === 'history' }" type="button" @click="openHistory">修正历史</button>
            <button :class="{ active: activeTab === 'text' }" type="button" @click="activeTab = 'text'">解析原文</button>
            <button :class="{ active: activeTab === 'links' }" type="button" @click="activeTab = 'links'">业务入口</button>
          </div>

          <section v-if="activeTab === 'fields'" class="field-grid">
            <div class="field-actions">
              <span v-if="editingFields" class="correction-change-count" :class="{ empty: changedFieldCount === 0 }">
                {{ changedFieldCount > 0 ? `已修改 ${changedFieldCount} 个字段` : "请先修改需要修正的字段" }}
              </span>
              <button v-if="!editingFields" class="ghost-button" type="button" @click="startEditFields">编辑字段</button>
              <button v-if="editingFields" class="ghost-button" type="button" @click="cancelEditFields">取消</button>
              <button v-if="editingFields" class="submit-action" type="button" :disabled="state === 'loading'" @click="saveFields">
                <Loader2 v-if="state === 'loading'" class="spin" :size="15" />
                保存确认
              </button>
            </div>
            <label v-if="editingFields" class="correction-reason">
              <span>修正原因</span>
              <input v-model="correctionReason" :class="{ invalid: correctionMessage && !correctionReason.trim() }" placeholder="必填，例如：对照发票原件核实金额" @input="correctionMessage = ''" />
            </label>
            <p v-if="correctionMessage" class="correction-inline-message" :class="correctionMessageType">{{ correctionMessage }}</p>
            <article v-for="item in fieldItems" :key="item.key" :class="{ editable: editingFields }">
              <span>{{ fieldLabel(item.key) }}</span>
              <input v-if="editingFields" :value="editableFields[item.key] ?? item.value" @input="editableFields[item.key] = ($event.target as HTMLInputElement).value" />
              <strong v-else>{{ item.value }}</strong>
            </article>
            <div v-if="!fieldItems.length" class="file-empty">暂无结构化字段</div>
          </section>

          <section v-if="activeTab === 'history'" class="correction-history">
            <div class="correction-history-head">
              <div><strong>字段修正记录</strong><span>记录修改前后值、原因和操作人</span></div>
              <button class="ghost-button" type="button" @click="loadCorrections"><RefreshCw :size="15" />刷新</button>
            </div>
            <article v-for="item in corrections" :key="item.id">
              <div class="correction-meta">
                <strong>{{ item.fieldName }}</strong>
                <span>{{ item.operatorName || "未知用户" }} · {{ formatDate(item.createdAt) }}</span>
              </div>
              <div class="correction-values">
                <span><small>修改前</small><del>{{ item.oldValue || "空值" }}</del></span>
                <span><small>修改后</small><ins>{{ item.newValue || "空值" }}</ins></span>
              </div>
              <p>{{ item.reason }}</p>
            </article>
            <div v-if="!corrections.length" class="file-empty">该文件暂无字段修正记录</div>
          </section>

          <section v-if="activeTab === 'text'" class="raw-text-panel">
            <div class="ocr-result-meta">
              <span :class="{ active: detail.parseResult?.ocrEngine && detail.parseResult.ocrEngine !== 'text-parser' && detail.parseResult.ocrEngine !== 'manual' }">
                {{ ocrEngineLabel(detail.parseResult?.ocrEngine) }}
              </span>
              <small>识别置信度 {{ formatConfidence(detail.parseResult?.confidence) }}</small>
            </div>
            <pre>{{ detail.parseResult?.rawText ?? "暂无解析原文" }}</pre>
          </section>

          <section v-if="activeTab === 'links'" class="business-link-grid">
            <button v-if="detail.contract" type="button" @click="$emit('jump', 'contract', detail.contract.id)">
              <FileSearch :size="18" />
              <span>
                <strong>查看合同</strong>
                <small>{{ detail.contract.contractNo }} · {{ formatMoney(detail.contract.amount) }}</small>
              </span>
            </button>
            <button v-if="detail.invoice" type="button" @click="$emit('jump', 'invoice', detail.invoice.id)">
              <ReceiptText :size="18" />
              <span>
                <strong>查看发票</strong>
                <small>{{ detail.invoice.invoiceNo }} · {{ formatMoney(detail.invoice.totalAmount ?? detail.invoice.amount) }}</small>
              </span>
            </button>
            <button v-for="task in detail.reviewTasks" :key="task.id" type="button" @click="$emit('jump', 'review', task.id)">
              <ClipboardCheck :size="18" />
              <span>
                <strong>{{ task.title }}</strong>
                <small>{{ formatRiskLevel(task.riskLevel) }} · {{ formatStatus(task.status) }}</small>
              </span>
            </button>
            <button v-for="workflow in detail.workflowInstances" :key="workflow.id" type="button" @click="$emit('jump', 'workflow', workflow.id)">
              <Route :size="18" />
              <span>
                <strong>查看审批流程</strong>
                <small>{{ formatStatus(workflow.status) }} · {{ workflow.currentNode }}</small>
              </span>
            </button>
            <div v-if="!detail.contract && !detail.invoice && !detail.reviewTasks.length && !detail.workflowInstances.length" class="file-empty">暂无关联业务记录</div>
          </section>

          <section class="risk-strip">
            <strong>{{ detail.file.parseStatus === "correction_required" ? "审批已驳回" : "风险提示" }}</strong>
            <span v-if="detail.file.parseStatus === 'correction_required'">请编辑并修正字段，保存后系统将自动重新发起审批。</span>
            <span v-for="risk in riskItems" :key="risk">{{ risk }}</span>
            <em v-if="!riskItems.length">当前文件没有生成风险项</em>
          </section>
        </template>
      </section>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from "vue";
import {
  ClipboardCheck,
  FileSearch,
  FileText,
  Loader2,
  Play,
  ReceiptText,
  RefreshCw,
  Route,
  UploadCloud
} from "lucide-vue-next";
import {
  confirmFileFields,
  fetchFileCorrections,
  fetchFileDetail,
  fetchFiles,
  processFileWithAi,
  type FileAiProcessPayload,
  type FileAsset,
  type FileAssetDetail,
  type FieldCorrectionHistory
} from "../services/api";

type LoadState = "idle" | "loading" | "success" | "error";
type DetailTab = "fields" | "history" | "text" | "links";

defineEmits<{
  jump: [target: "contract" | "invoice" | "review" | "workflow", id: string];
}>();

const files = ref<FileAsset[]>([]);
const detail = ref<FileAssetDetail | null>(null);
const latestResult = ref<FileAiProcessPayload | null>(null);
const selectedFile = ref<File | null>(null);
const businessType = ref("contract");
const state = ref<LoadState>("idle");
const notice = ref("等待上传文件");
const activeTab = ref<DetailTab>("fields");
const editingFields = ref(false);
const editableFields = ref<Record<string, string>>({});
const correctionReason = ref("");
const corrections = ref<FieldCorrectionHistory[]>([]);
const correctionMessage = ref("");
const correctionMessageType = ref<"error" | "success">("error");

const businessTypeLabel = computed(() => ({
  contract: "合同",
  invoice: "发票",
  statement: "对账单",
  general: "通用"
}[businessType.value] ?? businessType.value));

const completedCount = computed(() => files.value.filter((file) => file.parseStatus === "completed").length);
const pendingReviewCount = computed(() => detail.value?.reviewTasks.filter((task) => task.status === "pending").length ?? 0);
const fieldItems = computed(() => {
  const extractionFields = latestResult.value?.extraction.fields ?? detailExtractionFields();
  if (extractionFields) return Object.entries(extractionFields).map(([key, value]) => ({ key, value }));
  if (detail.value?.contract) {
    return [
      { key: "contract_no", value: detail.value.contract.contractNo },
      { key: "party_a", value: detail.value.contract.partyA ?? "-" },
      { key: "party_b", value: detail.value.contract.partyB ?? "-" },
      { key: "amount", value: formatMoney(detail.value.contract.amount) },
      { key: "payment_terms", value: detail.value.contract.paymentTerms ?? "-" },
      { key: "risk_level", value: formatRiskLevel(detail.value.contract.riskLevel) }
    ];
  }
  if (detail.value?.invoice) {
    return [
      { key: "scenario", value: "invoice" },
      { key: "amount", value: formatPlainAmount(detail.value.invoice.totalAmount ?? detail.value.invoice.amount) },
      { key: "invoice_no", value: detail.value.invoice.invoiceNo },
      { key: "invoice_code", value: detail.value.invoice.invoiceCode ?? "-" },
      { key: "invoice_date", value: detail.value.invoice.invoiceDate ?? "-" },
      { key: "buyer_name", value: detail.value.invoice.buyerName ?? "-" },
      { key: "buyer_tax_no", value: detail.value.invoice.buyerTaxNo ?? "-" },
      { key: "seller_name", value: detail.value.invoice.sellerName ?? "-" },
      { key: "seller_tax_no", value: detail.value.invoice.sellerTaxNo ?? "-" },
      { key: "line_amount", value: formatPlainAmount(detail.value.invoice.amount) },
      { key: "tax_amount", value: formatPlainAmount(detail.value.invoice.taxAmount) },
      { key: "total_amount", value: formatPlainAmount(detail.value.invoice.totalAmount) },
      { key: "tax_rate", value: detail.value.invoice.taxRate ?? "-" }
    ];
  }
  return [];
});
const riskItems = computed(() => {
  const detailExtraction = detailExtractionPayload();
  if (latestResult.value?.extraction.risks.length) return latestResult.value.extraction.risks;
  if (detailExtraction?.risks?.length) return detailExtraction.risks;
  return detail.value?.reviewTasks.map((task) => `${task.riskLevel}：${task.title}`) ?? [];
});
const changedFieldCount = computed(() => fieldItems.value.filter((item) =>
  String(editableFields.value[item.key] ?? item.value ?? "").trim() !== String(item.value ?? "").trim()
).length);

onMounted(loadFiles);

async function loadFiles() {
  files.value = await fetchFiles();
  if (!detail.value && files.value[0]) await selectFile(files.value[0].id);
}

async function selectFile(id: string) {
  detail.value = await fetchFileDetail(id);
  latestResult.value = null;
  editingFields.value = false;
  editableFields.value = {};
  correctionReason.value = "";
  correctionMessage.value = "";
  corrections.value = [];
  activeTab.value = "fields";
}

function handleFileChange(event: Event) {
  const target = event.target as HTMLInputElement;
  selectedFile.value = target.files?.[0] ?? null;
}

async function submitUpload() {
  if (!selectedFile.value) return;
  state.value = "loading";
  notice.value = "正在上传并调用 AI 解析";
  try {
    latestResult.value = await processFileWithAi(selectedFile.value, businessType.value);
    await loadFiles();
    detail.value = await fetchFileDetail(latestResult.value.file.id);
    editingFields.value = false;
    editableFields.value = {};
    selectedFile.value = null;
    activeTab.value = "fields";
    state.value = "success";
    notice.value = "处理完成，已生成解析结果和业务记录";
    if (latestResult.value.workflowInstance) {
      notice.value = `处理完成，已自动发起审批 ${latestResult.value.workflowInstance.instanceId}`;
    }
  } catch (error) {
    state.value = "error";
    notice.value = error instanceof Error ? error.message : "文件处理失败";
  }
}

function startEditFields() {
  editableFields.value = Object.fromEntries(fieldItems.value.map((item) => [item.key, String(item.value ?? "")]));
  correctionMessage.value = "";
  editingFields.value = true;
}

function cancelEditFields() {
  editingFields.value = false;
  editableFields.value = {};
  correctionReason.value = "";
  correctionMessage.value = "";
}

async function saveFields() {
  if (!detail.value) return;
  if (!correctionReason.value.trim()) {
    state.value = "error";
    notice.value = "请填写字段修正原因";
    correctionMessageType.value = "error";
    correctionMessage.value = "保存前必须填写修正原因。";
    return;
  }
  if (changedFieldCount.value === 0) {
    state.value = "error";
    notice.value = "没有字段发生变化";
    correctionMessageType.value = "error";
    correctionMessage.value = "当前字段值没有变化，请修改后再保存。";
    return;
  }
  state.value = "loading";
  notice.value = "正在保存人工确认字段";
  correctionMessage.value = "";
  try {
    detail.value = await confirmFileFields(detail.value.file.id, editableFields.value, correctionReason.value.trim());
    latestResult.value = null;
    editingFields.value = false;
    editableFields.value = {};
    correctionReason.value = "";
    await loadCorrections();
    state.value = "success";
    notice.value = "字段已确认，业务记录已同步更新";
    correctionMessageType.value = "success";
    correctionMessage.value = "保存成功，修正历史和审计记录已生成。";
  } catch (error) {
    state.value = "error";
    notice.value = error instanceof Error ? error.message : "字段确认失败";
    correctionMessageType.value = "error";
    correctionMessage.value = notice.value;
  }
}

async function openHistory() {
  activeTab.value = "history";
  await loadCorrections();
}

async function loadCorrections() {
  if (!detail.value) return;
  corrections.value = await fetchFileCorrections(detail.value.file.id);
}

function formatDate(value?: string) {
  return value ? new Date(value).toLocaleString("zh-CN", { hour12: false }) : "-";
}

function ocrEngineLabel(value?: string) {
  return ({
    "multimodal-model": "图片 OCR",
    "pdfbox+multimodal-model": "扫描 PDF OCR",
    "text-parser": "原生文本解析",
    manual: "人工录入"
  } as Record<string, string>)[value ?? ""] ?? value ?? "原生文本解析";
}

function formatConfidence(value?: number) {
  return value === undefined || value === null ? "-" : `${Math.round(Number(value) * 100)}%`;
}

function formatBytes(size?: number) {
  if (!size) return "0 B";
  if (size < 1024) return `${size} B`;
  if (size < 1024 * 1024) return `${(size / 1024).toFixed(1)} KB`;
  return `${(size / 1024 / 1024).toFixed(1)} MB`;
}

function formatMoney(value?: number) {
  if (value === undefined || value === null) return "未识别金额";
  return new Intl.NumberFormat("zh-CN", { style: "currency", currency: "CNY" }).format(value);
}

function formatPlainAmount(value?: number) {
  if (value === undefined || value === null) return "未识别";
  return Number(value).toFixed(2);
}

function detailExtractionFields() {
  return detailExtractionPayload()?.fields;
}

function detailExtractionPayload(): { fields?: Record<string, string>; risks?: string[] } | null {
  const raw = detail.value?.parseResult?.layoutJson;
  if (!raw) return null;
  try {
    const payload = JSON.parse(raw) as { extraction?: { fields?: Record<string, string>; risks?: string[] }; fields?: Record<string, string>; risks?: string[] };
    return payload.extraction ?? payload;
  } catch {
    return null;
  }
}

function fieldLabel(key: string) {
  const labels: Record<string, string> = {
    scenario: "业务场景",
    amount: "价税合计",
    line_amount: "不含税金额",
    total_amount: "价税合计",
    tax_amount: "税额",
    tax_rate: "税率",
    invoice_no: "发票号码",
    invoiceNo: "发票号码",
    invoice_code: "发票代码",
    invoiceCode: "发票代码",
    invoice_date: "开票日期",
    invoiceDate: "开票日期",
    buyer_name: "购买方名称",
    buyerName: "购买方名称",
    buyer_tax_no: "购买方税号",
    buyerTaxNo: "购买方税号",
    seller_name: "销售方名称",
    sellerName: "销售方名称",
    seller_tax_no: "销售方税号",
    sellerTaxNo: "销售方税号",
    order_no: "订单号",
    flight_info: "航班信息",
    contractNo: "合同编号",
    contract_no: "合同编号",
    partyA: "甲方",
    partyB: "乙方",
    paymentTerms: "付款条款",
    riskLevel: "风险等级",
    risk_level: "风险等级",
    party_a: "甲方",
    party_b: "乙方",
    payment_terms: "付款条款"
  };
  return labels[key] ?? key;
}

function formatBusinessType(value?: string) {
  return ({ contract: "合同", invoice: "发票", statement: "对账单", general: "通用文件", kb: "知识库" } as Record<string, string>)[value ?? ""] ?? value ?? "-";
}

function formatStatus(value?: string) {
  return ({
    completed: "已完成",
    uploaded: "已上传",
    pending: "待处理",
    pending_review: "待复核",
    pending_approval: "审批中",
    approved: "已通过",
    rejected: "已驳回",
    correction_required: "待修正",
    passed: "已通过",
    success: "成功",
    failed: "失败",
    open: "待处理"
  } as Record<string, string>)[value ?? ""] ?? value ?? "-";
}

function formatRiskLevel(value?: string) {
  return ({ high: "高风险", medium: "中风险", low: "低风险" } as Record<string, string>)[value ?? ""] ?? value ?? "-";
}
</script>
