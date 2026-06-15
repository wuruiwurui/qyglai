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
          <section v-if="selectedAiResult" class="workflow-ai-review">
            <div><Bot :size="18" /><span>AI 复核意见</span><em :class="{ pass: selectedAiResult.autoApproved }">{{ selectedAiResult.autoApproved ? "自动通过" : "转人工复核" }}</em></div>
            <p>{{ selectedAiResult.summary }}</p>
            <dl>
              <div><dt>置信度</dt><dd>{{ Math.round(selectedAiResult.confidence * 100) }}%</dd></div>
              <div><dt>使用模型</dt><dd>{{ selectedAiResult.modelName || "-" }}</dd></div>
              <div><dt>模型调用</dt><dd>{{ selectedAiResult.modelSuccess ? "成功" : "失败并降级" }}</dd></div>
            </dl>
            <ul v-if="selectedAiResult.risks?.length"><li v-for="risk in selectedAiResult.risks" :key="risk">{{ risk }}</li></ul>
            <small v-if="selectedAiResult.fallbackReason">{{ selectedAiResult.fallbackReason }}</small>
          </section>
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
      <div class="file-section-head">
        <div><p>流程设计</p><h2>拖拽式审批流程设计器</h2></div>
        <div class="workflow-designer-head-actions">
          <select v-model="selectedDefinitionId" @change="loadSelectedDefinition">
            <option value="">新建流程</option>
            <option v-for="item in definitions" :key="item.id" :value="item.id">{{ item.workflowName }} · V{{ item.versionNo }}</option>
          </select>
          <button class="primary-button" type="button" @click="submitDefinition"><Save :size="16" />发布新版本</button>
        </div>
      </div>

      <section class="workflow-designer">
        <aside class="workflow-palette">
          <div class="workflow-palette-heading">
            <span><strong>节点组件</strong><small>拖入画布或点击添加</small></span>
            <button type="button" title="新增节点组件" @click="showTemplateCreator = !showTemplateCreator"><PlusCircle :size="16" /></button>
          </div>
          <form v-if="showTemplateCreator" class="workflow-template-creator" @submit.prevent="createCustomTemplate">
            <label><span>组件名称</span><input v-model="templateDraft.label" required maxlength="20" placeholder="例如：法务审批" /></label>
            <label><span>执行方式</span><select v-model="templateDraft.nodeType"><option value="approval">人工处理</option><option value="ai">AI 自动复核</option></select></label>
            <label><span>组件说明</span><input v-model="templateDraft.description" maxlength="40" placeholder="说明节点用途" /></label>
            <div><button type="button" @click="showTemplateCreator = false">取消</button><button class="primary-button" type="submit">保存组件</button></div>
          </form>
          <article v-for="item in allNodeTemplates" :key="item.key" class="workflow-palette-item">
            <button type="button" draggable="true" @dragstart="startPaletteDrag(item.key, $event)" @dragend="resetDrag" @click="addNode(item.key)">
              <span :class="`workflow-palette-icon ${item.nodeType}`"><component :is="item.icon" :size="16" /></span>
              <span><strong>{{ item.label }}</strong><small>{{ item.description }}</small></span>
            </button>
            <button v-if="item.custom" type="button" title="删除自定义组件" @click="removeCustomTemplate(item.key)"><Trash2 :size="13" /></button>
          </article>
        </aside>

        <section class="workflow-canvas" @dragover.prevent @drop="dropOnCanvas">
          <div class="workflow-canvas-meta">
            <label><span>流程编码</span><input v-model="definitionForm.workflowCode" required /></label>
            <label><span>流程名称</span><input v-model="definitionForm.workflowName" required /></label>
            <label><span>业务场景</span><input v-model="definitionForm.scenario" required /></label>
            <label><span>状态</span><select v-model="definitionForm.status"><option value="enabled">启用</option><option value="disabled">停用</option></select></label>
          </div>
          <div class="workflow-start-node"><Play :size="15" /><span>流程开始</span></div>
          <div v-if="!designerNodes.length" class="workflow-canvas-empty">
            <Route :size="27" /><strong>把审批节点拖到这里</strong><span>节点顺序就是实际审批流转顺序</span>
          </div>
          <div class="workflow-node-flow">
            <article
              v-for="(node, index) in designerNodes"
              :key="node.uid"
              draggable="true"
              :class="{ active: selectedNodeUid === node.uid, dragging: draggedNodeIndex === index }"
              @dragstart="startNodeDrag(index, $event)"
              @dragend="resetDrag"
              @dragover.prevent
              @drop.stop="dropBeforeNode(index)"
              @click="selectedNodeUid = node.uid"
            >
              <span class="workflow-drag-handle"><GripVertical :size="17" /></span>
              <span :class="`workflow-palette-icon ${node.type}`"><component :is="nodeIcon(node.type)" :size="16" /></span>
              <span class="workflow-node-copy"><strong>{{ node.name }}</strong><small>{{ node.code }} · {{ node.dueHours }} 小时</small></span>
              <button type="button" title="删除节点" @click.stop="removeNode(node.uid)"><Trash2 :size="15" /></button>
            </article>
          </div>
          <div class="workflow-end-node"><CheckCircle2 :size="15" /><span>流程结束</span></div>
        </section>

        <aside class="workflow-node-properties">
          <template v-if="selectedDesignerNode">
            <div><strong>节点属性</strong><small>修改后自动同步流程定义</small></div>
            <label><span>节点名称</span><input v-model="selectedDesignerNode.name" /></label>
            <label><span>节点编码</span><input v-model="selectedDesignerNode.code" /></label>
            <label><span>处理时限（小时）</span><input v-model.number="selectedDesignerNode.dueHours" type="number" min="1" /></label>
            <label><span>指定审批人用户ID</span><input v-model="selectedDesignerNode.assigneeUserId" placeholder="留空则按默认规则分配" /></label>
            <button class="danger-text-button" type="button" @click="removeNode(selectedDesignerNode.uid)"><Trash2 :size="15" />删除节点</button>
          </template>
          <div v-else class="workflow-property-empty"><Settings2 :size="24" /><strong>选择节点编辑属性</strong></div>
        </aside>
      </section>
      <p class="workflow-designer-notice">{{ notice }}</p>
    </section>
  </section>
</template>

<script setup lang="ts">
import { computed, defineComponent, h, onMounted, ref, type Component, type PropType } from "vue";
import { Bot, CheckCircle2, ClipboardCheck, Forward, GripVertical, History, Landmark, Play, PlusCircle, RefreshCw, Route, Save, Send, Settings2, ShieldCheck, Trash2, UserRoundCheck, XCircle } from "lucide-vue-next";
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
type WorkflowAiResult = {
  decision: string; confidence: number; summary: string; risks: string[];
  autoApproved: boolean; modelSuccess: boolean; modelName?: string; fallbackReason?: string;
};
const selectedAiResult = computed<WorkflowAiResult | null>(() => {
  if (!selectedTask.value?.resultJson) return null;
  try {
    const result = JSON.parse(selectedTask.value.resultJson) as WorkflowAiResult;
    return typeof result?.summary === "string" && typeof result?.autoApproved === "boolean" ? result : null;
  } catch {
    return null;
  }
});
const startForm = ref({ workflowCode: "", businessType: "contract", businessId: "", title: "" });
const definitionForm = ref({
  workflowCode: "custom_approval", workflowName: "自定义审批流程", scenario: "custom", status: "enabled",
  definitionJson: ""
});
type DesignerNode = { uid: string; type: string; code: string; name: string; dueHours: number; assigneeUserId?: string };
type NodeTemplate = { key: string; nodeType: string; label: string; description: string; icon: Component; custom?: boolean };
type StoredNodeTemplate = Omit<NodeTemplate, "icon" | "custom">;
const NODE_TEMPLATE_STORAGE_KEY = "qyglai-workflow-node-templates";
const nodeTemplates: NodeTemplate[] = [
  { key: "approval", nodeType: "approval", label: "人工审批", description: "通用审批节点", icon: UserRoundCheck },
  { key: "department", nodeType: "department", label: "部门审批", description: "部门负责人处理", icon: ShieldCheck },
  { key: "finance", nodeType: "finance", label: "财务审批", description: "财务人员处理", icon: Landmark },
  { key: "ai", nodeType: "ai", label: "AI 复核", description: "AI辅助检查节点", icon: Bot }
];
const customNodeTemplates = ref<StoredNodeTemplate[]>(loadCustomTemplates());
const allNodeTemplates = computed<NodeTemplate[]>(() => [
  ...nodeTemplates,
  ...customNodeTemplates.value.map((item) => ({ ...item, custom: true, icon: item.nodeType === "ai" ? Bot : UserRoundCheck }))
]);
const showTemplateCreator = ref(false);
const templateDraft = ref({ label: "", description: "", nodeType: "approval" });
const designerNodes = ref<DesignerNode[]>([]);
const selectedNodeUid = ref("");
const selectedDefinitionId = ref("");
const draggedNodeIndex = ref<number | null>(null);
const draggedTemplateType = ref("");
const selectedDesignerNode = computed(() => designerNodes.value.find((node) => node.uid === selectedNodeUid.value));

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
    if (!designerNodes.value.length) throw new Error("流程至少需要一个审批节点");
    const codes = designerNodes.value.map((node) => node.code.trim());
    if (codes.some((code) => !code)) throw new Error("节点编码不能为空");
    if (new Set(codes).size !== codes.length) throw new Error("节点编码不能重复");
    definitionForm.value.definitionJson = JSON.stringify({
      nodes: designerNodes.value.map((node) => ({
        type: node.type, code: node.code.trim(), name: node.name.trim() || node.code.trim(), dueHours: Math.max(1, Number(node.dueHours) || 24),
        ...(node.assigneeUserId ? { assigneeUserId: Number(node.assigneeUserId) } : {})
      }))
    });
    await saveWorkflowDefinition(definitionForm.value);
    notice.value = "流程新版本已发布";
    await loadAll();
  } catch (error) {
    notice.value = errorMessage(error);
  }
}

function uid() {
  return `${Date.now()}-${Math.random().toString(16).slice(2)}`;
}

function addNode(templateKey: string, index = designerNodes.value.length) {
  const template = allNodeTemplates.value.find((item) => item.key === templateKey) ?? nodeTemplates[0];
  const sequence = designerNodes.value.length + 1;
  const node: DesignerNode = {
    uid: uid(), type: template.nodeType, code: `${template.key}_review_${sequence}`, name: template.label, dueHours: 24
  };
  designerNodes.value.splice(index, 0, node);
  selectedNodeUid.value = node.uid;
  notice.value = `已添加节点：${template.label}`;
}

function startPaletteDrag(templateKey: string, event: DragEvent) {
  draggedTemplateType.value = templateKey;
  draggedNodeIndex.value = null;
  event.dataTransfer?.setData("text/plain", `workflow-template:${templateKey}`);
  if (event.dataTransfer) event.dataTransfer.effectAllowed = "copy";
}

function startNodeDrag(index: number, event: DragEvent) {
  draggedNodeIndex.value = index;
  draggedTemplateType.value = "";
  event.dataTransfer?.setData("text/plain", `workflow-node:${index}`);
  if (event.dataTransfer) event.dataTransfer.effectAllowed = "move";
}

function dropOnCanvas() {
  if (draggedTemplateType.value) addNode(draggedTemplateType.value);
  else if (draggedNodeIndex.value !== null) moveNode(draggedNodeIndex.value, designerNodes.value.length);
  resetDrag();
}

function dropBeforeNode(index: number) {
  if (draggedTemplateType.value) addNode(draggedTemplateType.value, index);
  else if (draggedNodeIndex.value !== null) moveNode(draggedNodeIndex.value, index);
  resetDrag();
}

function moveNode(from: number, to: number) {
  if (from === to) return;
  const [node] = designerNodes.value.splice(from, 1);
  designerNodes.value.splice(from < to ? to - 1 : to, 0, node);
}

function resetDrag() {
  draggedNodeIndex.value = null;
  draggedTemplateType.value = "";
}

function removeNode(uidValue: string) {
  designerNodes.value = designerNodes.value.filter((node) => node.uid !== uidValue);
  if (selectedNodeUid.value === uidValue) selectedNodeUid.value = designerNodes.value[0]?.uid ?? "";
}

function loadCustomTemplates(): StoredNodeTemplate[] {
  try {
    const value = JSON.parse(localStorage.getItem(NODE_TEMPLATE_STORAGE_KEY) ?? "[]");
    return Array.isArray(value) ? value.filter((item) => item?.key && item?.label && item?.nodeType) : [];
  } catch {
    return [];
  }
}

function persistCustomTemplates() {
  localStorage.setItem(NODE_TEMPLATE_STORAGE_KEY, JSON.stringify(customNodeTemplates.value));
}

function createCustomTemplate() {
  const label = templateDraft.value.label.trim();
  if (!label) return;
  const key = `custom_${Date.now()}`;
  customNodeTemplates.value.push({
    key,
    nodeType: templateDraft.value.nodeType,
    label,
    description: templateDraft.value.description.trim() || (templateDraft.value.nodeType === "ai" ? "自定义 AI 复核节点" : "自定义人工处理节点")
  });
  persistCustomTemplates();
  templateDraft.value = { label: "", description: "", nodeType: "approval" };
  showTemplateCreator.value = false;
  notice.value = `节点组件“${label}”已创建`;
}

function removeCustomTemplate(key: string) {
  const template = customNodeTemplates.value.find((item) => item.key === key);
  customNodeTemplates.value = customNodeTemplates.value.filter((item) => item.key !== key);
  persistCustomTemplates();
  notice.value = template ? `节点组件“${template.label}”已删除，画布中的已有节点不受影响` : "节点组件已删除";
}

function nodeIcon(type: string) {
  return nodeTemplates.find((item) => item.nodeType === type)?.icon ?? UserRoundCheck;
}

function loadSelectedDefinition() {
  const definition = definitions.value.find((item) => item.id === selectedDefinitionId.value);
  if (!definition) {
    definitionForm.value = { workflowCode: "custom_approval", workflowName: "自定义审批流程", scenario: "custom", status: "enabled", definitionJson: "" };
    designerNodes.value = [];
    selectedNodeUid.value = "";
    return;
  }
  definitionForm.value = {
    workflowCode: definition.workflowCode, workflowName: definition.workflowName, scenario: definition.scenario,
    status: definition.status, definitionJson: definition.definitionJson
  };
  try {
    const parsed = JSON.parse(definition.definitionJson);
    designerNodes.value = (parsed.nodes ?? []).map((node: string | Record<string, unknown>, index: number) => {
      if (typeof node === "string") return { uid: uid(), type: "approval", code: node, name: node, dueHours: 24 };
      const code = String(node.code ?? `approval_${index + 1}`);
      return {
        uid: uid(), type: inferNodeType(code), code, name: String(node.name ?? code),
        dueHours: Number(node.dueHours ?? 24), assigneeUserId: node.assigneeUserId ? String(node.assigneeUserId) : ""
      };
    });
    selectedNodeUid.value = designerNodes.value[0]?.uid ?? "";
    notice.value = `已载入 ${definition.workflowName}，发布时会创建新版本`;
  } catch {
    designerNodes.value = [];
    selectedNodeUid.value = "";
    notice.value = "原流程定义无法解析，请重新设计";
  }
}

function inferNodeType(code: string) {
  if (code.toLowerCase().includes("finance")) return "finance";
  if (code.toLowerCase().includes("department")) return "department";
  if (code.toLowerCase().includes("ai")) return "ai";
  return "approval";
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
  return ({ start: "发起审批", arrive: "进入节点", approve: "审批通过", approved: "审批通过", reject: "审批驳回", rejected: "审批驳回", transfer: "任务转交", complete: "流程完成", ai_auto_approved: "AI 复核自动通过", ai_manual_review: "AI 复核转人工" } as Record<string, string>)[value] || value;
}

function formatDate(value?: string) {
  return value ? new Date(value).toLocaleString("zh-CN", { hour12: false }) : "-";
}

function errorMessage(error: unknown) {
  return error instanceof Error ? error.message : "操作失败";
}
</script>
