<template>
  <section class="workflow-workbench">
    <section class="workflow-summary">
      <article><span>我的待办</span><strong>{{ tasks.length }}</strong><small>等待审批处理</small></article>
      <article><span>运行中</span><strong>{{ runningCount }}</strong><small>正在流转</small></article>
      <article><span>已通过</span><strong>{{ approvedCount }}</strong><small>审批闭环完成</small></article>
      <article><span>流程模板</span><strong>{{ enabledDefinitions.length }}</strong><small>当前启用</small></article>
    </section>

    <div class="workflow-tabs">
      <button v-for="item in tabs" :key="item.key" :class="{ active: tab === item.key }" type="button" @click="tab = item.key">
        <component :is="item.icon" :size="16" />{{ item.label }}
      </button>
      <button class="ghost-button" type="button" @click="loadAll"><RefreshCw :size="15" />刷新</button>
    </div>

    <section v-if="tab === 'tasks'" class="workflow-columns">
      <section class="workflow-list-panel">
        <div class="file-section-head"><div><p>待办中心</p><h2>需要我审批</h2></div><span class="status-chip">{{ notice }}</span></div>
        <button v-for="task in tasks" :key="task.id" class="workflow-list-item" :class="{ active: selectedTask?.id === task.id }" type="button" @click="selectTask(task)">
          <span class="workflow-node-icon"><ClipboardCheck :size="17" /></span>
          <span><strong>{{ task.nodeName }}</strong><small>实例 {{ task.instanceId }} · {{ formatDate(task.dueTime) }} 截止</small></span>
          <em>待审批</em>
        </button>
        <div v-if="!tasks.length" class="file-empty">当前没有审批待办</div>
      </section>

      <section class="workflow-detail-panel">
        <template v-if="selectedTask && detail">
          <div class="file-section-head"><div><p>审批处理</p><h2>{{ detail.definition.workflowName }}</h2></div><span class="status-chip">{{ statusLabel(detail.instance.status) }}</span></div>
          <div class="workflow-business">
            <div><span>业务类型</span><strong>{{ detail.instance.businessType }}</strong></div>
            <div><span>业务ID</span><strong>{{ detail.instance.businessId || "-" }}</strong></div>
            <div><span>当前节点</span><strong>{{ selectedTask.nodeName }}</strong></div>
            <div><span>发起时间</span><strong>{{ formatDate(detail.instance.startedAt) }}</strong></div>
          </div>
          <label class="workflow-comment"><span>审批意见</span><textarea v-model="comment" rows="4" placeholder="填写审批意见或驳回原因" /></label>
          <label class="workflow-transfer"><span>转交用户ID</span><input v-model="targetUserId" placeholder="转交时填写" /></label>
          <div class="workflow-actions">
            <button class="approve-button" type="button" @click="act('approve')"><CheckCircle2 :size="16" />通过</button>
            <button class="reject-button" type="button" @click="act('reject')"><XCircle :size="16" />驳回</button>
            <button class="ghost-button" type="button" @click="act('transfer')"><Forward :size="16" />转交</button>
          </div>
          <WorkflowTimeline :detail="detail" />
        </template>
        <div v-else class="workflow-empty-detail"><ClipboardCheck :size="32" /><strong>选择一个待办开始审批</strong><span>审批意见和流转轨迹会被完整记录</span></div>
      </section>
    </section>

    <section v-if="tab === 'instances'" class="workflow-columns">
      <section class="workflow-list-panel">
        <div class="file-section-head"><div><p>流程监控</p><h2>全部审批实例</h2></div></div>
        <button v-for="instance in instances" :key="instance.id" class="workflow-list-item" :class="{ active: detail?.instance.id === instance.id }" type="button" @click="selectInstance(instance.id)">
          <span class="workflow-node-icon"><Route :size="17" /></span>
          <span><strong>{{ definitionName(instance.definitionId) }}</strong><small>{{ instance.businessType }} · {{ formatDate(instance.startedAt) }}</small></span>
          <em :class="instance.status">{{ statusLabel(instance.status) }}</em>
        </button>
      </section>
      <section class="workflow-detail-panel">
        <template v-if="detail">
          <div class="file-section-head"><div><p>实例详情</p><h2>{{ detail.definition.workflowName }}</h2></div><span class="status-chip">{{ statusLabel(detail.instance.status) }}</span></div>
          <div class="workflow-business">
            <div><span>实例ID</span><strong>{{ detail.instance.id }}</strong></div><div><span>业务类型</span><strong>{{ detail.instance.businessType }}</strong></div>
            <div><span>当前节点</span><strong>{{ detail.instance.currentNode }}</strong></div><div><span>发起人ID</span><strong>{{ detail.instance.initiatorUserId || "-" }}</strong></div>
          </div>
          <WorkflowTimeline :detail="detail" />
        </template>
        <div v-else class="workflow-empty-detail"><Route :size="32" /><strong>选择流程查看完整轨迹</strong></div>
      </section>
    </section>

    <section v-if="tab === 'start'" class="workflow-form-band">
      <div class="file-section-head"><div><p>发起中心</p><h2>发起新的业务审批</h2></div><span class="status-chip">{{ notice }}</span></div>
      <form class="workflow-start-form" @submit.prevent="submitStart">
        <label><span>审批流程</span><select v-model="startForm.workflowCode" required><option v-for="item in enabledDefinitions" :key="item.id" :value="item.workflowCode">{{ item.workflowName }}</option></select></label>
        <label><span>业务类型</span><input v-model="startForm.businessType" required placeholder="例如 contract" /></label>
        <label><span>业务记录ID</span><input v-model="startForm.businessId" placeholder="关联合同、发票等记录ID" /></label>
        <label class="wide"><span>审批标题</span><input v-model="startForm.title" required placeholder="例如：XX采购合同审批" /></label>
        <button class="submit-action" type="submit"><Send :size="16" />提交审批</button>
      </form>
      <section class="workflow-definition-cards">
        <article v-for="item in enabledDefinitions" :key="item.id"><Route :size="18" /><div><strong>{{ item.workflowName }}</strong><small>{{ nodeNames(item).join(" → ") }}</small></div><em>V{{ item.versionNo }}</em></article>
      </section>
    </section>

    <section v-if="tab === 'definitions'" class="workflow-form-band">
      <div class="file-section-head"><div><p>流程设计</p><h2>发布新的流程版本</h2></div><span class="status-chip">{{ notice }}</span></div>
      <form class="workflow-start-form" @submit.prevent="submitDefinition">
        <label><span>流程编码</span><input v-model="definitionForm.workflowCode" required /></label>
        <label><span>流程名称</span><input v-model="definitionForm.workflowName" required /></label>
        <label><span>业务场景</span><input v-model="definitionForm.scenario" required /></label>
        <label><span>状态</span><select v-model="definitionForm.status"><option value="enabled">启用</option><option value="disabled">停用</option></select></label>
        <label class="wide"><span>节点定义 JSON</span><textarea v-model="definitionForm.definitionJson" rows="8" required /></label>
        <button class="submit-action" type="submit"><Save :size="16" />发布新版本</button>
      </form>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, onMounted, ref, type PropType } from "vue";
import { CheckCircle2, ClipboardCheck, Forward, History, PlusCircle, RefreshCw, Route, Save, Send, Settings2, XCircle } from "lucide-vue-next";
import {
  fetchWorkflowDefinitions, fetchWorkflowDetail, fetchWorkflowInstances, fetchWorkflowTasks, handleWorkflowTask,
  saveWorkflowDefinition, startWorkflowApproval, type WorkflowDefinition, type WorkflowInstance,
  type WorkflowInstanceDetail, type WorkflowTask
} from "../services/api";

const WorkflowTimeline = defineComponent({
  props: { detail: { type: Object as PropType<WorkflowInstanceDetail>, required: true } },
  setup(props) {
    return () => h("section", { class: "workflow-timeline" }, [
      h("h3", [h(History, { size: 16 }), "审批轨迹"]),
      ...props.detail.history.map((item) => h("article", { key: item.id }, [
        h("span", { class: `timeline-dot ${item.action}` }),
        h("div", [h("strong", actionLabel(item.action)), h("small", `${item.nodeCode} · 操作人 ${item.operatorUserId || "-"} · ${formatDate(item.createdAt)}`), item.comment ? h("p", item.comment) : null])
      ]))
    ]);
  }
});

const tabs = [
  { key: "tasks", label: "我的待办", icon: ClipboardCheck }, { key: "instances", label: "流程实例", icon: Route },
  { key: "start", label: "发起审批", icon: PlusCircle }, { key: "definitions", label: "流程设计", icon: Settings2 }
] as const;
const tab = ref<(typeof tabs)[number]["key"]>("tasks");
const definitions = ref<WorkflowDefinition[]>([]);
const instances = ref<WorkflowInstance[]>([]);
const tasks = ref<WorkflowTask[]>([]);
const detail = ref<WorkflowInstanceDetail | null>(null);
const selectedTask = ref<WorkflowTask | null>(null);
const comment = ref("");
const targetUserId = ref("");
const notice = ref("工作流已就绪");
const startForm = ref({ workflowCode: "", businessType: "contract", businessId: "", title: "" });
const definitionForm = ref({
  workflowCode: "custom_approval", workflowName: "自定义审批流程", scenario: "custom", status: "enabled",
  definitionJson: '{\n  "nodes": [\n    {"code":"department_review","name":"部门负责人审批","dueHours":24},\n    {"code":"final_review","name":"最终审批","dueHours":48}\n  ]\n}'
});

const enabledDefinitions = computed(() => definitions.value.filter((item) => item.status === "enabled"));
const runningCount = computed(() => instances.value.filter((item) => item.status === "running").length);
const approvedCount = computed(() => instances.value.filter((item) => item.status === "approved").length);

onMounted(loadAll);

async function loadAll() {
  try {
    [definitions.value, instances.value, tasks.value] = await Promise.all([fetchWorkflowDefinitions(), fetchWorkflowInstances(), fetchWorkflowTasks()]);
    if (!startForm.value.workflowCode && enabledDefinitions.value.length) startForm.value.workflowCode = enabledDefinitions.value[0].workflowCode;
    notice.value = "数据已刷新";
  } catch (error) {
    notice.value = errorMessage(error);
  }
}

async function selectTask(task: WorkflowTask) {
  selectedTask.value = task;
  detail.value = await fetchWorkflowDetail(task.instanceId);
}

async function selectInstance(id: string) {
  detail.value = await fetchWorkflowDetail(id);
}

async function act(action: string) {
  if (!selectedTask.value) return;
  try {
    detail.value = await handleWorkflowTask(selectedTask.value.id, { action, comment: comment.value, targetUserId: targetUserId.value || null });
    notice.value = actionLabel(action);
    selectedTask.value = null;
    comment.value = "";
    targetUserId.value = "";
    await loadAll();
  } catch (error) {
    notice.value = errorMessage(error);
  }
}

async function submitStart() {
  try {
    const result = await startWorkflowApproval({
      workflowCode: startForm.value.workflowCode,
      initiator: "web",
      variables: { businessType: startForm.value.businessType, businessId: startForm.value.businessId || null, title: startForm.value.title }
    });
    notice.value = `审批已发起：${result.instanceId}`;
    startForm.value.businessId = "";
    startForm.value.title = "";
    await loadAll();
    tab.value = "instances";
  } catch (error) {
    notice.value = errorMessage(error);
  }
}

async function submitDefinition() {
  try {
    await saveWorkflowDefinition(definitionForm.value);
    notice.value = "流程新版本已发布";
    await loadAll();
  } catch (error) {
    notice.value = errorMessage(error);
  }
}

function definitionName(id: string) {
  return definitions.value.find((item) => item.id === id)?.workflowName || "审批流程";
}

function nodeNames(item: WorkflowDefinition) {
  try {
    return JSON.parse(item.definitionJson).nodes.map((node: string | { name?: string; code?: string }) => typeof node === "string" ? node : node.name || node.code);
  } catch {
    return ["节点定义待修复"];
  }
}

function statusLabel(value: string) {
  return ({ running: "审批中", approved: "已通过", rejected: "已驳回", pending: "待审批" } as Record<string, string>)[value] || value;
}

function actionLabel(value: string) {
  return ({ start: "发起审批", arrive: "进入节点", approve: "审批通过", approved: "审批通过", reject: "审批驳回", rejected: "审批驳回", transfer: "任务转交", complete: "流程完成" } as Record<string, string>)[value] || value;
}

function formatDate(value?: string) {
  return value ? new Date(value).toLocaleString("zh-CN", { hour12: false }) : "-";
}

function errorMessage(error: unknown) {
  return error instanceof Error ? error.message : "操作失败";
}
</script>
