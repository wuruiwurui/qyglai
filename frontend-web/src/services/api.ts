/**
 * 前端统一 API 层。
 *
 * 这里负责登录态、鉴权请求头、Java 后端接口和页面动作元数据。
 */

export type ApiResponse<T> = {
  /** 后端业务编码。 */
  code: string;
  /** 后端提示信息。 */
  message: string;
  /** 后端业务数据。 */
  data: T;
  /** 响应时间。 */
  time: string;
};

export type CurrentUser = {
  /** 用户ID。 */
  id: number;
  /** 登录用户名。 */
  username: string;
  /** 真实姓名。 */
  realName: string;
  /** 组织ID。 */
  orgId: number;
  /** 头像地址。 */
  avatarUrl?: string;
};

export type LoginResponse = {
  /** JWT访问令牌。 */
  token: string;
  /** 令牌类型。 */
  tokenType: string;
  /** 过期秒数。 */
  expiresIn: number;
  /** 当前用户。 */
  user: CurrentUser;
  /** 角色编码。 */
  roles: string[];
  /** 权限编码。 */
  permissions: string[];
};

export type MetricCard = {
  /** 指标名称。 */
  name: string;
  /** 指标值。 */
  value: string;
  /** 趋势说明。 */
  trend: string;
  /** 指标状态。 */
  status: string;
};

export type BossChatResponse = {
  /** 问题意图。 */
  intent?: string;
  /** 老板助手回答。 */
  answer: string;
  /** 指标卡片。 */
  metrics: MetricCard[];
  /** 建议动作。 */
  actions: string[];
  /** 数据来源。 */
  sources: string[];
};

export type AutomationModule = {
  /** 模块编码。 */
  code: string;
  /** 模块名称。 */
  name: string;
  /** 模块说明。 */
  description: string;
  /** 负责部门。 */
  owner: string;
  /** 模块状态。 */
  status: string;
};

export type ReviewTask = {
  /** 复核任务ID。 */
  taskId: string;
  /** 业务场景。 */
  scenario: string;
  /** 任务标题。 */
  title: string;
  /** 风险等级。 */
  riskLevel: string;
  /** 处理人。 */
  assignee: string;
  /** 创建时间。 */
  createdAt: string;
  /** 复核任务状态。 */
  status?: string;
  /** AI复核建议JSON。 */
  aiAdvice?: string;
};

export type SalesFollowupTask = {
  /** 任务ID。 */
  taskId: string;
  /** 客户名称。 */
  customerName: string;
  /** 商机阶段。 */
  opportunityStage: string;
  /** 下一步动作。 */
  nextAction: string;
  /** 截止时间。 */
  dueTime: string;
  /** 负责人。 */
  owner: string;
  /** 状态。 */
  status: string;
};

export type HealthResponse = {
  /** 服务名称。 */
  service: string;
  /** 服务状态。 */
  status: string;
  /** 服务时间。 */
  time: string;
};

export type WorkflowDefinition = {
  /** 流程定义ID。 */
  id: string;
  /** 流程编码。 */
  workflowCode: string;
  /** 流程名称。 */
  workflowName: string;
  /** 业务场景。 */
  scenario: string;
  /** 版本号。 */
  versionNo: number;
  /** 节点定义JSON。 */
  definitionJson: string;
  /** 状态。 */
  status: string;
};

export type WorkflowInstance = {
  /** 流程实例ID。 */
  id: string;
  /** 流程定义ID。 */
  definitionId: string;
  /** 业务类型。 */
  businessType: string;
  /** 业务记录ID。 */
  businessId?: string;
  /** 发起人用户ID。 */
  initiatorUserId?: string;
  /** 当前节点。 */
  currentNode: string;
  /** 流程变量JSON。 */
  variablesJson?: string;
  /** 实例状态。 */
  status: string;
  /** 发起时间。 */
  startedAt?: string;
  /** 结束时间。 */
  endedAt?: string;
};

export type WorkflowTask = {
  /** 审批任务ID。 */
  id: string;
  /** 流程实例ID。 */
  instanceId: string;
  /** 节点编码。 */
  nodeCode: string;
  /** 节点名称。 */
  nodeName: string;
  /** 处理人用户ID。 */
  assigneeUserId?: string;
  /** 任务类型：人工审批或自动节点。 */
  taskType?: string;
  /** 任务状态。 */
  status: string;
  /** 截止时间。 */
  dueTime?: string;
  /** 完成时间。 */
  completedAt?: string;
  /** 节点执行结果，AI复核节点会保存模型结论、置信度和风险。 */
  resultJson?: string;
};

export type WorkflowActionLog = {
  /** 动作日志ID。 */
  id: string;
  /** 流程实例ID。 */
  instanceId: string;
  /** 流程任务ID。 */
  taskId?: string;
  /** 节点编码。 */
  nodeCode: string;
  /** 动作类型。 */
  action: string;
  /** 操作人用户ID。 */
  operatorUserId?: string;
  /** 目标用户ID。 */
  targetUserId?: string;
  /** 审批意见。 */
  comment?: string;
  /** 动作时间。 */
  createdAt?: string;
};

export type WorkflowInstanceDetail = {
  /** 流程实例。 */
  instance: WorkflowInstance;
  /** 流程定义。 */
  definition: WorkflowDefinition;
  /** 节点任务。 */
  tasks: WorkflowTask[];
  /** 审批历史。 */
  history: WorkflowActionLog[];
};

export type FileAsset = {
  id: string;
  fileName: string;
  originalName: string;
  fileExt?: string;
  mimeType?: string;
  fileSize: number;
  fileHash: string;
  storageBucket: string;
  storageKey: string;
  businessType: string;
  parseStatus: string;
  createdAt?: string;
  updatedAt?: string;
};

export type DocParseResult = {
  id: string;
  fileId: string;
  pageNo: number;
  rawText?: string;
  layoutJson?: string;
  ocrEngine?: string;
  confidence?: number;
  status: string;
  createdAt?: string;
};

export type ContractRecord = {
  id: string;
  contractNo: string;
  fileId?: string;
  partyA?: string;
  partyB?: string;
  amount?: number;
  currency?: string;
  paymentTerms?: string;
  riskLevel?: string;
  reviewStatus?: string;
  createdAt?: string;
};

export type InvoiceRecord = {
  id: string;
  invoiceNo: string;
  invoiceCode?: string;
  fileId?: string;
  invoiceDate?: string;
  buyerName?: string;
  buyerTaxNo?: string;
  sellerName?: string;
  sellerTaxNo?: string;
  amount?: number;
  taxAmount?: number;
  totalAmount?: number;
  taxRate?: string;
  verifyStatus?: string;
  createdAt?: string;
};

export type ReviewTaskRecord = {
  id: string;
  taskNo: string;
  scenario: string;
  businessType: string;
  businessId?: string;
  title: string;
  riskLevel: string;
  status: string;
  createdAt?: string;
};

export type ExtractionPayload = {
  scenario: string;
  confidence: number;
  fields: Record<string, string>;
  risks: string[];
  reviewRequired: boolean;
};

export type FileAiProcessPayload = {
  file: FileAsset;
  parseResult: DocParseResult;
  extraction: ExtractionPayload;
  businessRecord?: ContractRecord | InvoiceRecord | Record<string, unknown>;
  reviewTask?: ReviewTaskRecord | null;
  workflowInstance?: { instanceId: string; workflowCode: string; currentNode: string; status: string } | null;
};

export type FileAssetDetail = {
  file: FileAsset;
  parseResult?: DocParseResult | null;
  contract?: ContractRecord | null;
  invoice?: InvoiceRecord | null;
  reviewTasks: ReviewTaskRecord[];
  workflowInstances: WorkflowInstance[];
};

export type FieldCorrectionHistory = {
  /** 修正记录ID。 */
  id: string;
  /** 修正批次ID。 */
  batchId: string;
  /** 文件ID。 */
  fileId: string;
  /** 业务类型。 */
  businessType?: string;
  /** 业务记录ID。 */
  businessId?: string;
  /** 字段编码。 */
  fieldKey: string;
  /** 字段中文名称。 */
  fieldName: string;
  /** 修正前值。 */
  oldValue?: string;
  /** 修正后值。 */
  newValue?: string;
  /** 修正原因。 */
  reason: string;
  /** 操作人用户ID。 */
  operatorUserId?: string;
  /** 操作人名称。 */
  operatorName?: string;
  /** 修正时间。 */
  createdAt?: string;
};

export type AiRuntimeStatus = {
  provider: string;
  model: string;
  enabled: boolean;
  apiBase?: string | null;
  textGenerationEnabled: boolean;
  fileExtractionMode: string;
  contractRiskMode: string;
  lastCallStatus: string;
  lastFallbackReason?: string | null;
};

export type AiRuntimeConfig = {
  provider: string;
  apiBase?: string | null;
  apiKey?: string | null;
  apiKeyMasked?: string | null;
  model: string;
  enabled: boolean;
  textGenerationEnabled: boolean;
  fileExtractionMode: string;
  contractRiskMode: string;
  remark?: string | null;
};

export type AiModelProfile = AiRuntimeConfig & {
  /** 配置唯一标识。 */
  id?: string;
  /** 页面展示名称。 */
  name: string;
  /** 是否为当前使用模型。 */
  current?: boolean;
};

export type AiScenarioRoute = {
  scenario: string;
  primaryProfileId: string;
  primaryModel?: string;
  fallbackProfileIds: string[];
  fallbackModels?: string[];
};

export type AiModelCallLog = {
  id: string;
  providerId?: string | null;
  scenario?: string | null;
  businessType?: string | null;
  businessId?: string | null;
  modelName?: string | null;
  promptTemplateCode?: string | null;
  requestTokens?: number | null;
  responseTokens?: number | null;
  costAmount?: number | null;
  latencyMs?: number | null;
  successFlag?: number | null;
  errorMessage?: string | null;
  requestHash?: string | null;
  createdAt?: string | null;
};

export type AiEvaluationPerformance = {
  name: string;
  calls: number;
  successfulCalls: number;
  successRate: number;
  averageLatencyMs: number;
  totalTokens: number;
};

export type AiEvaluationDashboard = {
  overview: {
    totalCalls: number; successfulCalls: number; successRate: number;
    averageLatencyMs: number; totalTokens: number; failedCalls: number;
  };
  modelPerformance: AiEvaluationPerformance[];
  scenarioPerformance: AiEvaluationPerformance[];
  fieldCorrections: Array<{
    fieldKey: string; fieldName: string; correctionCount: number;
    affectedFiles: number; affectedBusinesses: number;
  }>;
  feedback: {
    correctionCount: number; correctionBatches: number; affectedFiles: number;
    reviewTasks: number; completedReviews: number; feedbackSamples: number;
    aiAutoApproved: number; aiManualReview: number; aiAutoApprovalRate: number;
  };
};

export type KnowledgeSpace = {
  id: string;
  spaceCode: string;
  spaceName: string;
  permissionScope: string;
  ownerOrgId?: string;
  status: string;
  createdAt?: string;
};

export type KnowledgeDocument = {
  id: string;
  spaceId: string;
  fileId?: string;
  title: string;
  docType: string;
  sourceUrl?: string;
  versionNo: string;
  indexingStatus: string;
  status: string;
  createdAt?: string;
};

export type KnowledgeSearchHit = {
  chunkId: string;
  documentId: string;
  title: string;
  content: string;
  score: number;
  sourceUrl?: string;
};

export type KnowledgeAnswer = {
  answer: string;
  confidence: number;
  citations: string[];
  humanHandoffSuggested: boolean;
};

export type KnowledgeIndexResult = {
  documentId: string;
  title: string;
  chunkCount: number;
  indexingStatus: string;
};

export type SystemOrg = {
  id: string;
  parentId: string;
  orgCode: string;
  orgName: string;
  orgType: string;
  sortOrder: number;
  status: string;
};

export type SystemUser = {
  id: string;
  orgId: string;
  username: string;
  realName: string;
  mobile?: string;
  email?: string;
  status: string;
  lastLoginAt?: string;
};

export type SystemRole = {
  id: string;
  roleCode: string;
  roleName: string;
  dataScope: string;
  status: string;
};

export type SystemPermission = {
  id: string;
  permissionCode: string;
  permissionName: string;
  permissionType: string;
  resourcePath?: string;
  parentId: string;
  sortOrder: number;
  status: string;
};

export type DataRecord = Record<string, unknown>;
export type FieldType = "text" | "textarea" | "number" | "select" | "switch" | "json" | "file";

export type EndpointField = {
  /** 字段名。 */
  name: string;
  /** 字段标签。 */
  label: string;
  /** 字段控件类型。 */
  type: FieldType;
  /** 默认值。 */
  defaultValue?: unknown;
  /** 下拉选项。 */
  options?: string[];
  /** 是否必填。 */
  required?: boolean;
  /** 帮助说明。 */
  help?: string;
};

export type EndpointSpec = {
  /** 动作唯一键。 */
  key: string;
  /** 所属页面。 */
  group: string;
  /** 动作标题。 */
  title: string;
  /** 动作说明。 */
  description: string;
  /** 请求方法。 */
  method: "GET" | "POST" | "PUT";
  /** 后端路径。 */
  path: string;
  /** 查询参数字段。 */
  queryFields?: string[];
  /** 表单字段。 */
  fields?: EndpointField[];
  /** 是否上传文件。 */
  upload?: boolean;
  /** 是否作为页面主动作。 */
  primary?: boolean;
  /** 接口来源。 */
  source?: "java" | "python";
};

export type EndpointRunResult = {
  /** 实际路径。 */
  path: string;
  /** 返回数据。 */
  data: unknown;
};

const API_BASE = import.meta.env.VITE_API_BASE ?? "";
const TOKEN_KEY = "qyglai_token";
const SESSION_KEY = "qyglai_session";

function isApiResponse<T>(payload: unknown): payload is ApiResponse<T> {
  return Boolean(payload && typeof payload === "object" && "code" in payload && "data" in payload);
}

export function getToken() {
  return localStorage.getItem(TOKEN_KEY) ?? "";
}

export function getStoredSession(): LoginResponse | null {
  const raw = localStorage.getItem(SESSION_KEY);
  return raw ? JSON.parse(raw) as LoginResponse : null;
}

export function clearSession() {
  localStorage.removeItem(TOKEN_KEY);
  localStorage.removeItem(SESSION_KEY);
}

function saveSession(session: LoginResponse) {
  localStorage.setItem(TOKEN_KEY, session.token);
  localStorage.setItem(SESSION_KEY, JSON.stringify(session));
}

function authHeaders(headers: HeadersInit = {}) {
  const token = getToken();
  return {
    "Content-Type": "application/json;charset=UTF-8",
    ...(token ? { Authorization: `Bearer ${token}` } : {}),
    ...headers
  };
}

export async function request<T>(path: string, options: RequestInit = {}): Promise<T> {
  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers: authHeaders(options.headers)
  });
  if (response.status === 401 || response.status === 403) {
    clearSession();
    throw new Error("登录已失效，请重新登录");
  }
  if (!response.ok) throw new Error(await response.text() || `HTTP ${response.status}`);
  const payload = await response.json() as T | ApiResponse<T>;
  return isApiResponse<T>(payload) ? payload.data : payload as T;
}

export async function login(username: string, password: string): Promise<LoginResponse> {
  const session = await request<LoginResponse>("/api/auth/login", {
    method: "POST",
    body: JSON.stringify({ username, password })
  });
  saveSession(session);
  return session;
}

export function fetchMe(): Promise<LoginResponse> {
  return request<LoginResponse>("/api/auth/me");
}

export function askBossAssistant(question: string, timeRange = "this_week"): Promise<BossChatResponse> {
  return request<BossChatResponse>("/api/boss-assistant/ask", {
    method: "POST",
    body: JSON.stringify({ question, timeRange, userId: "boss-demo" })
  });
}

export function fetchHealth(): Promise<HealthResponse> {
  return request<HealthResponse>("/api/health");
}

export function fetchModules(): Promise<AutomationModule[]> {
  return request<AutomationModule[]>("/api/modules");
}

export function fetchReviewTasks(): Promise<ReviewTask[]> {
  return request<ReviewTask[]>("/api/review/tasks");
}

export function fetchSalesFollowups(): Promise<SalesFollowupTask[]> {
  return request<SalesFollowupTask[]>("/api/sales/followups");
}

export function fetchFiles(): Promise<FileAsset[]> {
  return request<FileAsset[]>("/api/files");
}

export function fetchFileDetail(id: string): Promise<FileAssetDetail> {
  return request<FileAssetDetail>(`/api/files/${id}/detail`);
}

export function confirmFileFields(id: string, fields: Record<string, string>, reason: string): Promise<FileAssetDetail> {
  return request<FileAssetDetail>(`/api/files/${id}/confirm-fields`, {
    method: "POST",
    body: JSON.stringify({ fields, reason })
  });
}

export function fetchFileCorrections(id: string): Promise<FieldCorrectionHistory[]> {
  return request<FieldCorrectionHistory[]>(`/api/files/${id}/corrections`);
}

export async function fetchAiRuntimeStatus(): Promise<AiRuntimeStatus> {
  return request<AiRuntimeStatus>("/api/ai-governance/runtime-status");
}

export async function fetchAiRuntimeConfig(): Promise<AiRuntimeConfig> {
  return request<AiRuntimeConfig>("/api/ai-governance/runtime-config");
}

export async function saveAiRuntimeConfig(config: AiRuntimeConfig): Promise<AiRuntimeConfig> {
  return request<AiRuntimeConfig>("/api/ai-governance/runtime-config", {
    method: "POST",
    body: JSON.stringify(config)
  });
}

export function fetchAiModelProfiles(): Promise<AiModelProfile[]> {
  return request<AiModelProfile[]>("/api/ai-governance/model-profiles");
}

export function saveAiModelProfile(profile: AiModelProfile): Promise<AiModelProfile> {
  return request<AiModelProfile>("/api/ai-governance/model-profiles", {
    method: "POST",
    body: JSON.stringify(profile)
  });
}

export function switchAiModelProfile(id: string): Promise<AiModelProfile> {
  return request<AiModelProfile>(`/api/ai-governance/model-profiles/${id}/switch`, { method: "POST" });
}

export function deleteAiModelProfile(id: string): Promise<void> {
  return request<void>(`/api/ai-governance/model-profiles/${id}`, { method: "DELETE" });
}

export function fetchAiScenarioRoutes(): Promise<AiScenarioRoute[]> {
  return request<AiScenarioRoute[]>("/api/ai-governance/scenario-routes");
}

export function saveAiScenarioRoute(route: AiScenarioRoute): Promise<AiScenarioRoute> {
  return request<AiScenarioRoute>("/api/ai-governance/scenario-routes", { method: "POST", body: JSON.stringify(route) });
}

export function deleteAiScenarioRoute(scenario: string): Promise<void> {
  return request<void>(`/api/ai-governance/scenario-routes/${encodeURIComponent(scenario)}`, { method: "DELETE" });
}

export function fetchAiModelCallLogs(): Promise<AiModelCallLog[]> {
  return request<AiModelCallLog[]>("/api/ai-governance/model-call-logs");
}

export function fetchAiEvaluationDashboard(days = 30): Promise<AiEvaluationDashboard> {
  return request<AiEvaluationDashboard>(`/api/ai-governance/evaluation-dashboard?days=${days}`);
}

export function fetchKnowledgeSpaces(): Promise<KnowledgeSpace[]> {
  return request<KnowledgeSpace[]>("/api/kb/spaces");
}

export function createKnowledgeSpace(payload: Record<string, unknown>): Promise<KnowledgeSpace> {
  return request<KnowledgeSpace>("/api/kb/spaces", { method: "POST", body: JSON.stringify(payload) });
}

export function fetchKnowledgeDocuments(scope?: string): Promise<KnowledgeDocument[]> {
  const query = scope ? `?scope=${encodeURIComponent(scope)}` : "";
  return request<KnowledgeDocument[]>(`/api/kb/documents${query}`);
}

export function indexKnowledgeText(payload: Record<string, unknown>): Promise<KnowledgeIndexResult> {
  return request<KnowledgeIndexResult>("/api/kb/documents/text", { method: "POST", body: JSON.stringify(payload) });
}

export async function indexKnowledgeFile(file: File, spaceId: string, title?: string): Promise<KnowledgeIndexResult> {
  const form = new FormData();
  form.append("file", file);
  form.append("spaceId", spaceId);
  if (title) form.append("title", title);
  const response = await fetch(`${API_BASE}/api/kb/documents/file`, {
    method: "POST",
    headers: getToken() ? { Authorization: `Bearer ${getToken()}` } : undefined,
    body: form
  });
  if (!response.ok) throw new Error(await response.text() || `HTTP ${response.status}`);
  const payload = await response.json() as ApiResponse<KnowledgeIndexResult> | KnowledgeIndexResult;
  return isApiResponse<KnowledgeIndexResult>(payload) ? payload.data : payload;
}

export function queryKnowledge(question: string, scope: string): Promise<KnowledgeAnswer> {
  return request<KnowledgeAnswer>("/api/kb/query", {
    method: "POST",
    body: JSON.stringify({ question, scope, userId: "ui" })
  });
}

export function searchKnowledge(question: string, scope: string, topK = 5): Promise<KnowledgeSearchHit[]> {
  const params = new URLSearchParams({ question, scope, topK: String(topK) });
  return request<KnowledgeSearchHit[]>(`/api/kb/search?${params}`);
}

export function fetchSystemOrgs(): Promise<SystemOrg[]> {
  return request<SystemOrg[]>("/api/system/orgs");
}

export function fetchSystemUsers(): Promise<SystemUser[]> {
  return request<SystemUser[]>("/api/system/users");
}

export function fetchSystemRoles(): Promise<SystemRole[]> {
  return request<SystemRole[]>("/api/system/roles");
}

export function fetchSystemPermissions(): Promise<SystemPermission[]> {
  return request<SystemPermission[]>("/api/system/permissions");
}

export function saveSystemUser(payload: Record<string, unknown>, id?: string): Promise<SystemUser> {
  return request<SystemUser>(id ? `/api/system/users/${id}` : "/api/system/users", {
    method: id ? "PUT" : "POST",
    body: JSON.stringify(payload)
  });
}

export function saveSystemRole(payload: Record<string, unknown>, id?: string): Promise<SystemRole> {
  return request<SystemRole>(id ? `/api/system/roles/${id}` : "/api/system/roles", {
    method: id ? "PUT" : "POST",
    body: JSON.stringify(payload)
  });
}

export function saveSystemPermission(payload: Record<string, unknown>, id?: string): Promise<SystemPermission> {
  return request<SystemPermission>(id ? `/api/system/permissions/${id}` : "/api/system/permissions", {
    method: id ? "PUT" : "POST",
    body: JSON.stringify(payload)
  });
}

export function saveSystemOrg(payload: Record<string, unknown>, id?: string): Promise<SystemOrg> {
  return request<SystemOrg>(id ? `/api/system/orgs/${id}` : "/api/system/orgs/manage", {
    method: id ? "PUT" : "POST",
    body: JSON.stringify(payload)
  });
}

export function fetchUserRoleIds(id: string): Promise<string[]> {
  return request<string[]>(`/api/system/users/${id}/role-ids`);
}

export function assignUserRoles(id: string, ids: string[]): Promise<string[]> {
  return request<string[]>(`/api/system/users/${id}/role-ids`, { method: "PUT", body: JSON.stringify({ ids }) });
}

export function fetchRolePermissionIds(id: string): Promise<string[]> {
  return request<string[]>(`/api/system/roles/${id}/permission-ids`);
}

export function assignRolePermissions(id: string, ids: string[]): Promise<string[]> {
  return request<string[]>(`/api/system/roles/${id}/permission-ids`, { method: "PUT", body: JSON.stringify({ ids }) });
}

export function fetchWorkflowDefinitions(): Promise<WorkflowDefinition[]> {
  return request<WorkflowDefinition[]>("/api/workflows/definitions");
}

export function fetchWorkflowInstances(): Promise<WorkflowInstance[]> {
  return request<WorkflowInstance[]>("/api/workflows/instances");
}

export function fetchWorkflowTasks(): Promise<WorkflowTask[]> {
  return request<WorkflowTask[]>("/api/workflows/tasks");
}

export function fetchWorkflowDetail(instanceId: string): Promise<WorkflowInstanceDetail> {
  return request<WorkflowInstanceDetail>(`/api/workflows/instances/${instanceId}`);
}

export function startWorkflowApproval(payload: Record<string, unknown>): Promise<{ instanceId: string }> {
  return request<{ instanceId: string }>("/api/workflows/start", { method: "POST", body: JSON.stringify(payload) });
}

export function handleWorkflowTask(taskId: string, payload: Record<string, unknown>): Promise<WorkflowInstanceDetail> {
  return request<WorkflowInstanceDetail>(`/api/workflows/tasks/${taskId}/actions`, { method: "POST", body: JSON.stringify(payload) });
}

export function saveWorkflowDefinition(payload: Record<string, unknown>): Promise<WorkflowDefinition> {
  return request<WorkflowDefinition>("/api/workflows/definitions", { method: "POST", body: JSON.stringify(payload) });
}

export async function processFileWithAi(file: File, businessType: string): Promise<FileAiProcessPayload> {
  const form = new FormData();
  form.append("file", file);
  form.append("businessType", businessType);
  form.append("operatorId", "ui");
  const response = await fetch(`${API_BASE}/api/files/ai-process`, {
    method: "POST",
    headers: getToken() ? { Authorization: `Bearer ${getToken()}` } : undefined,
    body: form
  });
  if (response.status === 401 || response.status === 403) {
    clearSession();
    throw new Error("登录已失效，请重新登录");
  }
  if (!response.ok) throw new Error(await response.text() || `HTTP ${response.status}`);
  const payload = await response.json() as ApiResponse<FileAiProcessPayload> | FileAiProcessPayload;
  return isApiResponse<FileAiProcessPayload>(payload) ? payload.data : payload;
}

export function createDefaultPayload(endpoint: EndpointSpec): DataRecord {
  return Object.fromEntries((endpoint.fields ?? []).map((field) => [field.name, field.defaultValue ?? ""]));
}

function buildPath(path: string, payload: DataRecord, queryFields: string[] = []) {
  let targetPath = path.replace(/\{(\w+)}/g, (_, key: string) => encodeURIComponent(String(payload[key] ?? "")));
  const params = new URLSearchParams();
  queryFields.forEach((field) => {
    const value = payload[field];
    if (value !== undefined && value !== null && value !== "") params.set(field, String(value));
  });
  const query = params.toString();
  return query ? `${targetPath}?${query}` : targetPath;
}

function coerceJsonValue(value: unknown) {
  if (typeof value !== "string") return value;
  const text = value.trim();
  if (!(text.startsWith("{") || text.startsWith("["))) return value;
  try {
    return JSON.parse(text);
  } catch {
    return value;
  }
}

function buildJsonBody(endpoint: EndpointSpec, payload: DataRecord) {
  const excluded = new Set([...(endpoint.queryFields ?? []), "file"]);
  Array.from(endpoint.path.matchAll(/\{(\w+)}/g)).forEach((item) => excluded.add(item[1]));
  return Object.fromEntries(
    Object.entries(payload)
      .filter(([key, value]) => !excluded.has(key) && value !== undefined && value !== null)
      .map(([key, value]) => [key, coerceJsonValue(value)])
  );
}

export async function runEndpoint(endpoint: EndpointSpec, payload: DataRecord): Promise<EndpointRunResult> {
  const path = buildPath(endpoint.path, payload, endpoint.queryFields);
  if (endpoint.upload) {
    const form = new FormData();
    if (payload.file instanceof File) form.append("file", payload.file);
    Object.entries(payload).forEach(([key, value]) => {
      if (key !== "file" && value !== undefined && value !== null && value !== "") {
        form.append(key, String(value));
      }
    });
    const response = await fetch(`${API_BASE}${path}`, {
      method: endpoint.method,
      headers: getToken() ? { Authorization: `Bearer ${getToken()}` } : undefined,
      body: form
    });
    if (!response.ok) throw new Error(await response.text() || `HTTP ${response.status}`);
    const data = await response.json();
    return { path, data: isApiResponse<unknown>(data) ? data.data : data };
  }

  if (endpoint.method === "GET") return { path, data: await request<unknown>(path) };
  return { path, data: await request<unknown>(path, { method: endpoint.method, body: JSON.stringify(buildJsonBody(endpoint, payload)) }) };
}

const textProcessFields: EndpointField[] = [
  { name: "content", label: "待处理文本", type: "textarea", required: true, defaultValue: "示例业务文本" },
  { name: "scenario", label: "业务场景", type: "select", options: ["contract", "invoice", "ticket"], defaultValue: "contract" },
  { name: "operatorId", label: "操作人", type: "text", defaultValue: "ui" }
];

const simpleCreateFields: EndpointField[] = [
  { name: "code", label: "编码", type: "text", defaultValue: `CODE-${Date.now()}` },
  { name: "name", label: "名称", type: "text", defaultValue: "示例名称" },
  { name: "type", label: "类型", type: "text", defaultValue: "default" },
  { name: "content", label: "内容", type: "textarea", defaultValue: "示例内容" },
  { name: "amount", label: "金额", type: "number", defaultValue: 1000 }
];

export const pageDescriptions: Record<string, string> = {
  经营: "老板视角经营总览、服务健康和自动化模块。",
  文件: "上传业务文件，由 Java 服务解析 Word、Excel、PDF、文本并通过 AI 抽取字段。",
  合同: "合同台账、合同信息抽取、风险识别和复核触发。",
  财务: "发票解析、供应商对账批次、差异明细和财务处理建议。",
  客服: "客服工单分类、优先级判断、情绪识别和建议回复。",
  销售: "销售跟进任务、逾期提醒和重点客户推进。",
  知识库: "企业知识库空间、制度问答和引用来源。",
  报表: "经营日报、周报、老板摘要生成和报表记录。",
  流程: "企业审批流程发起、多级流转、任务转交、审批历史和流程设计。",
  复核: "人工复核任务、复核完成和 AI 复核建议。",
  消息: "站内通知、系统提醒和待办推送。",
  系统: "组织、用户、角色、权限和 RBAC 基础管理。",
  集成: "第三方连接器、Webhook 事件接入和外部系统集成。",
  AI治理: "模型供应商、Prompt 模板、调用日志、评测样本和质量评估。",
  审计: "关键操作审计日志和风险追踪。"
};

export const endpointCatalog: EndpointSpec[] = [
  { key: "modules", group: "经营", title: "自动化模块", description: "查看平台已启用能力", method: "GET", path: "/api/modules", primary: true },
  { key: "health", group: "经营", title: "服务健康", description: "检查 Java 后端服务状态", method: "GET", path: "/api/health" },
  { key: "ai-file-extract", group: "文件", title: "AI文件解析抽取", description: "调用 Java 服务解析文件并抽取字段", method: "POST", path: "/api/files/parse-and-extract", upload: true, primary: true, fields: [
    { name: "file", label: "文件", type: "file", required: true },
    { name: "scenario", label: "抽取场景", type: "select", options: ["contract", "invoice", "statement", "general"], defaultValue: "contract" }
  ] },
  { key: "file-ai-process", group: "文件", title: "完整AI入库闭环", description: "上传文件后自动解析、抽取、保存原文、生成合同/发票台账和复核任务", method: "POST", path: "/api/files/ai-process", upload: true, primary: true, fields: [
    { name: "file", label: "文件", type: "file", required: true },
    { name: "businessType", label: "业务类型", type: "select", options: ["contract", "invoice", "statement", "general"], defaultValue: "contract" },
    { name: "operatorId", label: "操作人", type: "text", defaultValue: "ui" }
  ] },
  { key: "file-upload", group: "文件", title: "业务文件上传", description: "上传文件到 Java 后端资产库", method: "POST", path: "/api/files/upload", upload: true, fields: [
    { name: "file", label: "文件", type: "file", required: true },
    { name: "businessType", label: "业务类型", type: "select", options: ["contract", "invoice", "statement", "kb"], defaultValue: "contract" },
    { name: "operatorId", label: "操作人", type: "text", defaultValue: "ui" }
  ] },
  { key: "contracts", group: "合同", title: "合同台账", description: "查看合同抽取结果", method: "GET", path: "/api/contracts", primary: true },
  { key: "contract-extract", group: "合同", title: "合同抽取", description: "触发合同字段抽取、风险识别和流程", method: "POST", path: "/api/contracts/extract", primary: true, fields: textProcessFields.map((field) => field.name === "content" ? { ...field, defaultValue: "甲方采购软件服务，合同金额120000元，付款周期30天，含违约条款。" } : field.name === "scenario" ? { ...field, defaultValue: "contract" } : field) },
  { key: "invoices", group: "财务", title: "发票记录", description: "查看发票识别记录", method: "GET", path: "/api/invoices", primary: true },
  { key: "invoice-parse", group: "财务", title: "发票解析", description: "触发发票识别和校验", method: "POST", path: "/api/invoices/parse", primary: true, fields: textProcessFields.map((field) => field.name === "content" ? { ...field, defaultValue: "发票号码 FP20260604001，软件服务费8600元，税率6%。" } : field.name === "scenario" ? { ...field, defaultValue: "invoice" } : field) },
  { key: "reconciliation-batches", group: "财务", title: "对账批次", description: "查看供应商对账批次", method: "GET", path: "/api/reconciliation/batches" },
  { key: "reconciliation-items", group: "财务", title: "对账明细", description: "查看对账差异明细", method: "GET", path: "/api/reconciliation/items" },
  { key: "reconciliation-ai", group: "财务", title: "AI对账分析", description: "调用 Java 分析供应商对账差异", method: "POST", path: "/api/ai-tasks/reconciliation/analyze", fields: [
    { name: "supplierName", label: "供应商", type: "text", defaultValue: "示例供应商" },
    { name: "statementAmount", label: "对账单金额", type: "number", defaultValue: 10000 },
    { name: "invoiceAmount", label: "发票金额", type: "number", defaultValue: 8600 },
    { name: "paidAmount", label: "已付款", type: "number", defaultValue: 1000 }
  ] },
  { key: "tickets", group: "客服", title: "客服工单", description: "查看工单分类结果", method: "GET", path: "/api/tickets", primary: true },
  { key: "ticket-classify", group: "客服", title: "工单分类", description: "触发工单分类与建议回复", method: "POST", path: "/api/tickets/classify", primary: true, fields: textProcessFields.map((field) => field.name === "content" ? { ...field, defaultValue: "客户投诉交付进度很慢，希望今天给出明确排期。" } : field.name === "scenario" ? { ...field, defaultValue: "ticket" } : field) },
  { key: "sales-followups", group: "销售", title: "销售跟进", description: "查看销售跟进任务", method: "GET", path: "/api/sales/followups", primary: true },
  { key: "sales-ai", group: "销售", title: "AI跟进提醒", description: "调用 Java 生成销售跟进动作", method: "POST", path: "/api/ai-tasks/sales/followup-reminder", fields: [
    { name: "customerName", label: "客户", type: "text", defaultValue: "重点客户A" },
    { name: "opportunityStage", label: "阶段", type: "select", options: ["线索", "报价", "谈判", "签约"], defaultValue: "报价" },
    { name: "lastContactDays", label: "未联系天数", type: "number", defaultValue: 4 },
    { name: "amount", label: "商机金额", type: "number", defaultValue: 120000 }
  ] },
  { key: "kb-spaces", group: "知识库", title: "知识库空间", description: "查看知识库空间", method: "GET", path: "/api/kb/spaces", primary: true },
  { key: "kb-space-create", group: "知识库", title: "创建知识库空间", description: "创建带权限范围的知识库空间", method: "POST", path: "/api/kb/spaces", fields: [
    { name: "name", label: "空间名称", type: "text", required: true, defaultValue: "企业制度库" },
    { name: "code", label: "空间编码", type: "text", defaultValue: `KB-${Date.now()}` },
    { name: "scope", label: "权限范围", type: "select", options: ["company", "finance", "legal", "sales"], defaultValue: "company" },
    { name: "ownerOrgId", label: "归属组织ID", type: "number", defaultValue: "" }
  ] },
  { key: "kb-documents", group: "知识库", title: "知识文档", description: "查看已经完成索引的知识文档", method: "GET", path: "/api/kb/documents", queryFields: ["scope"], fields: [
    { name: "scope", label: "权限范围", type: "select", options: ["company", "finance", "legal", "sales"], defaultValue: "company" }
  ] },
  { key: "kb-index-text", group: "知识库", title: "录入知识文本", description: "将制度、流程或业务资料切片并建立向量索引", method: "POST", path: "/api/kb/documents/text", primary: true, fields: [
    { name: "spaceId", label: "知识库空间ID", type: "number", required: true },
    { name: "title", label: "文档标题", type: "text", required: true, defaultValue: "合同审批制度" },
    { name: "content", label: "文档正文", type: "textarea", required: true, defaultValue: "合同金额超过10万元时，需要部门负责人和财务负责人共同审批；包含自动续约或违约责任条款时，需要法务复核。" },
    { name: "sourceUrl", label: "来源地址", type: "text", defaultValue: "" }
  ] },
  { key: "kb-index-file", group: "知识库", title: "上传知识文件", description: "解析文件并自动建立切片和向量索引", method: "POST", path: "/api/kb/documents/file", upload: true, primary: true, fields: [
    { name: "file", label: "知识文件", type: "file", required: true },
    { name: "spaceId", label: "知识库空间ID", type: "number", required: true },
    { name: "title", label: "文档标题", type: "text", defaultValue: "" }
  ] },
  { key: "kb-search", group: "知识库", title: "知识切片检索", description: "查看问题实际命中的知识片段和相似度", method: "GET", path: "/api/kb/search", queryFields: ["question", "scope", "topK"], fields: [
    { name: "question", label: "检索问题", type: "textarea", required: true, defaultValue: "合同金额超过10万元如何审批？" },
    { name: "scope", label: "权限范围", type: "select", options: ["company", "finance", "legal", "sales"], defaultValue: "company" },
    { name: "topK", label: "返回数量", type: "number", defaultValue: 5 }
  ] },
  { key: "kb-query", group: "知识库", title: "知识库问答", description: "向企业知识库提问", method: "POST", path: "/api/kb/query", primary: true, fields: [
    { name: "question", label: "问题", type: "textarea", required: true, defaultValue: "合同金额超过10万如何审批？" },
    { name: "scope", label: "范围", type: "select", options: ["company", "finance", "legal", "sales"], defaultValue: "company" },
    { name: "userId", label: "用户", type: "text", defaultValue: "ui" }
  ] },
  { key: "reports", group: "报表", title: "报表记录", description: "查看日报周报记录", method: "GET", path: "/api/reports", primary: true },
  { key: "report-generate", group: "报表", title: "生成报表", description: "生成经营日报、周报或老板摘要", method: "POST", path: "/api/reports/generate", primary: true, fields: [
    { name: "reportType", label: "报表类型", type: "select", options: ["daily", "weekly", "boss_summary"], defaultValue: "daily" },
    { name: "timeRange", label: "时间范围", type: "select", options: ["today", "this_week", "this_month"], defaultValue: "today" },
    { name: "audience", label: "接收对象", type: "select", options: ["boss", "finance", "sales", "operation"], defaultValue: "boss" },
    { name: "autoSend", label: "自动发送", type: "switch", defaultValue: false }
  ] },
  { key: "workflow-tasks", group: "流程", title: "流程任务", description: "查看企业审批待办任务", method: "GET", path: "/api/workflows/tasks", primary: true },
  { key: "workflow-start", group: "流程", title: "启动流程", description: "启动自动化流程实例", method: "POST", path: "/api/workflows/start", primary: true, fields: [
    { name: "workflowCode", label: "流程编码", type: "text", defaultValue: "ui_demo_flow" },
    { name: "initiator", label: "发起人", type: "text", defaultValue: "ui" },
    { name: "variables", label: "流程变量", type: "json", defaultValue: "{\n  \"businessId\": 1,\n  \"source\": \"frontend\"\n}" }
  ] },
  { key: "review-tasks", group: "复核", title: "复核任务", description: "查看人工复核任务", method: "GET", path: "/api/review/tasks", primary: true },
  { key: "review-ai", group: "复核", title: "AI复核建议", description: "调用 Java 生成复核建议", method: "POST", path: "/api/ai-tasks/review/advice", fields: [
    { name: "scenario", label: "场景", type: "select", options: ["contract", "invoice", "ticket"], defaultValue: "contract" },
    { name: "content", label: "复核内容", type: "textarea", defaultValue: "合同金额120000元，含违约条款。" },
    { name: "riskLevel", label: "风险等级", type: "select", options: ["low", "medium", "high"], defaultValue: "high" }
  ] },
  { key: "notifications", group: "消息", title: "消息通知", description: "查看站内消息通知", method: "GET", path: "/api/notifications", primary: true },
  { key: "notification-create", group: "消息", title: "创建通知", description: "创建一条站内通知", method: "POST", path: "/api/notifications", fields: [
    { name: "channel", label: "渠道", type: "select", options: ["system", "email", "sms"], defaultValue: "system" },
    { name: "title", label: "标题", type: "text", defaultValue: "经营提醒" },
    { name: "content", label: "内容", type: "textarea", defaultValue: "有新的复核任务需要处理。" }
  ] },
  { key: "system-orgs", group: "系统", title: "组织", description: "查看组织架构", method: "GET", path: "/api/system/orgs", primary: true },
  { key: "system-users", group: "系统", title: "用户", description: "查看系统用户", method: "GET", path: "/api/system/users" },
  { key: "system-roles", group: "系统", title: "角色", description: "查看系统角色", method: "GET", path: "/api/system/roles" },
  { key: "system-permissions", group: "系统", title: "权限", description: "查看系统权限", method: "GET", path: "/api/system/permissions" },
  { key: "system-org-create", group: "系统", title: "创建组织", description: "创建组织节点", method: "POST", path: "/api/system/orgs", fields: simpleCreateFields },
  { key: "connectors", group: "集成", title: "连接器", description: "查看第三方连接器", method: "GET", path: "/api/integrations/connectors", primary: true },
  { key: "webhooks", group: "集成", title: "Webhook事件", description: "查看 Webhook 事件", method: "GET", path: "/api/integrations/webhooks" },
  { key: "webhook-receive", group: "集成", title: "模拟Webhook", description: "模拟接收第三方事件", method: "POST", path: "/api/integrations/webhooks", queryFields: ["connectorId", "eventType"], fields: [
    { name: "connectorId", label: "连接器ID", type: "number", defaultValue: "" },
    { name: "eventType", label: "事件类型", type: "select", options: ["contract.created", "invoice.received", "ticket.created"], defaultValue: "ticket.created" },
    { name: "payload", label: "事件载荷", type: "json", defaultValue: "{\n  \"source\": \"frontend\"\n}" }
  ] },
  { key: "ai-providers", group: "AI治理", title: "模型供应商", description: "查看 AI 模型供应商", method: "GET", path: "/api/ai-governance/providers", primary: true },
  { key: "ai-runtime-config", group: "AI治理", title: "查看真实AI配置", description: "查看 Java AI 网关当前运行模型配置，密钥会脱敏", method: "GET", path: "/api/ai-governance/runtime-config" },
  { key: "ai-runtime-status", group: "AI治理", title: "真实AI运行状态", description: "查看真实模型是否启用、场景开关和最近一次调用状态", method: "GET", path: "/api/ai-governance/runtime-status", primary: true },
  { key: "ai-runtime-save", group: "AI治理", title: "保存真实AI配置", description: "在页面配置豆包、OpenAI兼容模型或本地模型网关", method: "POST", path: "/api/ai-governance/runtime-config", primary: true, fields: [
    { name: "provider", label: "供应商", type: "select", options: ["doubao", "openai", "local", "mock"], defaultValue: "doubao" },
    { name: "apiBase", label: "API地址", type: "text", defaultValue: "https://ark.cn-beijing.volces.com/api/v3", help: "豆包火山方舟 OpenAI 兼容地址通常为 https://ark.cn-beijing.volces.com/api/v3" },
    { name: "apiKey", label: "API Key", type: "text", defaultValue: "", help: "保存后仅在服务端本地配置文件中持久化，页面查询会脱敏。" },
    { name: "model", label: "模型/Endpoint ID", type: "text", defaultValue: "请填写豆包 endpoint-id" },
    { name: "enabled", label: "启用真实模型", type: "switch", defaultValue: true },
    { name: "textGenerationEnabled", label: "文本生成走真实模型", type: "switch", defaultValue: true },
    { name: "fileExtractionMode", label: "文件抽取模式", type: "select", options: ["rules_first", "model_first", "rules_only"], defaultValue: "rules_first", help: "建议发票、合同文件使用 rules_first 或 rules_only，保证金额等字段稳定。" },
    { name: "contractRiskMode", label: "合同风险模式", type: "select", options: ["rules_first", "model_first", "rules_only"], defaultValue: "rules_first" },
    { name: "remark", label: "备注", type: "textarea", defaultValue: "豆包真实模型配置" }
  ] },
  { key: "ai-prompts", group: "AI治理", title: "Prompt模板", description: "查看 Prompt 模板", method: "GET", path: "/api/ai-governance/prompt-templates" },
  { key: "ai-prompt-evaluate", group: "AI治理", title: "Prompt评测", description: "调用 Java 评测 Prompt 质量", method: "POST", path: "/api/ai-tasks/prompts/evaluate", fields: [
    { name: "prompt", label: "Prompt", type: "textarea", defaultValue: "请基于{input}生成结构化经营日报，并列出数据来源。" },
    { name: "scenario", label: "场景", type: "select", options: ["report", "contract", "ticket"], defaultValue: "report" }
  ] },
  { key: "ai-call-logs", group: "AI治理", title: "模型调用日志", description: "查看模型调用日志", method: "GET", path: "/api/ai-governance/model-call-logs" },
  { key: "audit-logs", group: "审计", title: "审计日志", description: "查看关键操作审计", method: "GET", path: "/api/audit/logs", primary: true }
];
