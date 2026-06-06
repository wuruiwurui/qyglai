<template>
  <section class="knowledge-workbench">
    <aside class="knowledge-library">
      <header class="knowledge-section-head">
        <div>
          <span class="knowledge-eyebrow">资料空间</span>
          <h2>企业知识库</h2>
        </div>
        <button class="knowledge-icon-button" type="button" title="新建知识库空间" @click="spaceDialogOpen = !spaceDialogOpen">
          <Plus :size="17" />
        </button>
      </header>

      <form v-if="spaceDialogOpen" class="knowledge-inline-form" @submit.prevent="saveSpace">
        <input v-model="spaceForm.name" placeholder="空间名称" required />
        <div>
          <select v-model="spaceForm.scope">
            <option value="company">全公司</option>
            <option value="legal">法务</option>
            <option value="finance">财务</option>
            <option value="sales">销售</option>
          </select>
          <button type="submit" :disabled="busy"><Check :size="16" /></button>
        </div>
      </form>

      <nav class="knowledge-space-list">
        <button
          v-for="space in spaces"
          :key="space.id"
          type="button"
          :class="{ active: selectedSpace?.id === space.id }"
          @click="selectSpace(space)"
        >
          <span class="knowledge-space-mark"><Library :size="16" /></span>
          <span>
            <strong>{{ space.spaceName }}</strong>
            <small>{{ scopeLabel(space.permissionScope) }} · {{ documentCount(space.id) }} 份资料</small>
          </span>
          <ChevronRight :size="15" />
        </button>
      </nav>

      <div class="knowledge-library-footer">
        <Database :size="16" />
        <span>向量索引由 Java 服务维护</span>
      </div>
    </aside>

    <section class="knowledge-conversation">
      <header class="knowledge-chat-head">
        <div>
          <span class="knowledge-eyebrow">知识问答</span>
          <h2>{{ selectedSpace?.spaceName ?? "选择一个知识空间" }}</h2>
          <p>回答仅基于已索引资料，并附带可核验引用。</p>
        </div>
        <div class="knowledge-head-actions">
          <button type="button" title="清空对话" @click="resetChat"><RotateCcw :size="16" /></button>
          <button type="button" title="刷新资料" @click="loadAll"><RefreshCw :size="16" /></button>
        </div>
      </header>

      <div ref="chatBody" class="knowledge-chat-body">
        <article v-for="message in messages" :key="message.id" class="knowledge-message" :class="message.role">
          <div class="knowledge-message-avatar">
            <BookOpenCheck v-if="message.role === 'assistant'" :size="17" />
            <UserRound v-else :size="17" />
          </div>
          <div class="knowledge-message-content">
            <div class="knowledge-message-meta">
              <strong>{{ message.role === "assistant" ? "知识助手" : "你" }}</strong>
              <span v-if="message.confidence !== undefined">置信度 {{ confidenceText(message.confidence) }}</span>
            </div>
            <p>{{ message.content }}</p>
            <div v-if="message.citations?.length" class="knowledge-citations">
              <button v-for="citation in message.citations" :key="citation" type="button">
                <Quote :size="13" />{{ citation }}
              </button>
            </div>
            <details v-if="message.hits?.length" class="knowledge-evidence">
              <summary>查看命中的知识片段</summary>
              <article v-for="hit in message.hits" :key="hit.chunkId">
                <div><strong>{{ hit.title }}</strong><span>{{ confidenceText(hit.score) }}</span></div>
                <p>{{ hit.content }}</p>
              </article>
            </details>
          </div>
        </article>
        <article v-if="asking" class="knowledge-message assistant">
          <div class="knowledge-message-avatar"><BookOpenCheck :size="17" /></div>
          <div class="knowledge-message-content knowledge-thinking">
            <Loader2 class="spin" :size="16" />正在检索资料并核对引用...
          </div>
        </article>
      </div>

      <div class="knowledge-prompts">
        <button v-for="prompt in prompts" :key="prompt" type="button" @click="question = prompt">{{ prompt }}</button>
      </div>

      <form class="knowledge-composer" @submit.prevent="ask">
        <textarea v-model="question" rows="2" placeholder="询问制度、流程、合同条款或业务资料..." @keydown.ctrl.enter.prevent="ask" />
        <div>
          <span><Search :size="14" />检索 {{ selectedScopeLabel }} 范围</span>
          <button type="submit" title="发送问题" :disabled="asking || !question.trim() || !selectedSpace">
            <Send :size="17" />
          </button>
        </div>
      </form>
    </section>

    <aside class="knowledge-documents">
      <header class="knowledge-section-head">
        <div>
          <span class="knowledge-eyebrow">索引资料</span>
          <h2>文档目录</h2>
        </div>
        <span class="knowledge-count">{{ visibleDocuments.length }}</span>
      </header>

      <div class="knowledge-ingest-tabs">
        <button type="button" :class="{ active: ingestMode === 'file' }" @click="ingestMode = 'file'"><Upload :size="15" />上传</button>
        <button type="button" :class="{ active: ingestMode === 'text' }" @click="ingestMode = 'text'"><FilePenLine :size="15" />录入</button>
      </div>

      <form v-if="ingestMode === 'file'" class="knowledge-ingest-form" @submit.prevent="uploadDocument">
        <label class="knowledge-dropzone">
          <input type="file" @change="chooseFile" />
          <FileUp :size="22" />
          <strong>{{ selectedFile?.name ?? "选择知识文件" }}</strong>
          <small>PDF、Word、Excel、TXT</small>
        </label>
        <input v-model="documentTitle" placeholder="文档标题（可选）" />
        <button class="knowledge-primary-button" type="submit" :disabled="busy || !selectedFile || !selectedSpace">
          <Loader2 v-if="busy" class="spin" :size="16" /><Upload v-else :size="16" />建立索引
        </button>
      </form>

      <form v-else class="knowledge-ingest-form" @submit.prevent="saveTextDocument">
        <input v-model="textForm.title" placeholder="文档标题" required />
        <textarea v-model="textForm.content" rows="6" placeholder="粘贴制度、流程说明或业务知识..." required />
        <button class="knowledge-primary-button" type="submit" :disabled="busy || !selectedSpace">
          <Loader2 v-if="busy" class="spin" :size="16" /><FilePenLine v-else :size="16" />切片并索引
        </button>
      </form>

      <p class="knowledge-notice" :class="noticeState">{{ notice }}</p>

      <div class="knowledge-document-list">
        <article v-for="document in visibleDocuments" :key="document.id">
          <span class="knowledge-doc-icon"><FileText :size="17" /></span>
          <div>
            <strong>{{ document.title }}</strong>
            <small>{{ document.docType === "file" ? "文件资料" : "文本资料" }} · {{ dateText(document.createdAt) }}</small>
          </div>
          <span class="knowledge-index-state">{{ document.indexingStatus === "indexed" ? "已索引" : document.indexingStatus }}</span>
        </article>
        <div v-if="!visibleDocuments.length" class="knowledge-empty">
          <Files :size="25" />
          <span>这个空间还没有资料</span>
        </div>
      </div>
    </aside>
  </section>
</template>

<script setup lang="ts">
import { computed, nextTick, onMounted, ref } from "vue";
import {
  BookOpenCheck, Check, ChevronRight, Database, FilePenLine, Files, FileText, FileUp,
  Library, Loader2, Plus, Quote, RefreshCw, RotateCcw, Search, Send, Upload, UserRound
} from "lucide-vue-next";
import {
  createKnowledgeSpace, fetchKnowledgeDocuments, fetchKnowledgeSpaces, indexKnowledgeFile,
  indexKnowledgeText, queryKnowledge, searchKnowledge, type KnowledgeDocument, type KnowledgeSearchHit,
  type KnowledgeSpace
} from "../services/api";

type KnowledgeMessage = {
  id: string;
  role: "user" | "assistant";
  content: string;
  confidence?: number;
  citations?: string[];
  hits?: KnowledgeSearchHit[];
};

const spaces = ref<KnowledgeSpace[]>([]);
const documents = ref<KnowledgeDocument[]>([]);
const selectedSpace = ref<KnowledgeSpace | null>(null);
const spaceDialogOpen = ref(false);
const spaceForm = ref({ name: "", scope: "company" });
const ingestMode = ref<"file" | "text">("file");
const selectedFile = ref<File | null>(null);
const documentTitle = ref("");
const textForm = ref({ title: "", content: "" });
const question = ref("");
const asking = ref(false);
const busy = ref(false);
const notice = ref("选择空间后，可上传文件或直接录入知识。");
const noticeState = ref<"idle" | "success" | "error">("idle");
const chatBody = ref<HTMLElement | null>(null);
const prompts = ["这份制度的审批条件是什么？", "资料里有哪些风险或例外？", "请总结执行步骤和责任人"];
const messages = ref<KnowledgeMessage[]>([
  { id: "welcome", role: "assistant", content: "你好。我会先检索当前知识空间，再依据命中资料回答，并把引用一起交给你核验。" }
]);

const selectedScopeLabel = computed(() => scopeLabel(selectedSpace.value?.permissionScope ?? "company"));
const visibleDocuments = computed(() => selectedSpace.value
  ? documents.value.filter((item) => item.spaceId === selectedSpace.value?.id)
  : []);

onMounted(loadAll);

async function loadAll() {
  try {
    spaces.value = await fetchKnowledgeSpaces();
    if (!selectedSpace.value && spaces.value.length) selectedSpace.value = spaces.value[0];
    documents.value = await fetchKnowledgeDocuments();
  } catch (error) {
    setNotice(error instanceof Error ? error.message : "知识库加载失败", "error");
  }
}

function selectSpace(space: KnowledgeSpace) {
  selectedSpace.value = space;
  resetChat();
}

async function saveSpace() {
  busy.value = true;
  try {
    const space = await createKnowledgeSpace({ name: spaceForm.value.name, scope: spaceForm.value.scope });
    spaces.value.unshift(space);
    selectedSpace.value = space;
    spaceForm.value.name = "";
    spaceDialogOpen.value = false;
    setNotice("知识库空间已创建", "success");
  } catch (error) {
    setNotice(error instanceof Error ? error.message : "空间创建失败", "error");
  } finally {
    busy.value = false;
  }
}

function chooseFile(event: Event) {
  selectedFile.value = (event.target as HTMLInputElement).files?.[0] ?? null;
}

async function uploadDocument() {
  if (!selectedFile.value || !selectedSpace.value) return;
  busy.value = true;
  try {
    const result = await indexKnowledgeFile(selectedFile.value, selectedSpace.value.id, documentTitle.value);
    setNotice(`“${result.title}”已完成 ${result.chunkCount} 个知识切片索引`, "success");
    selectedFile.value = null;
    documentTitle.value = "";
    documents.value = await fetchKnowledgeDocuments();
  } catch (error) {
    setNotice(error instanceof Error ? error.message : "文件索引失败", "error");
  } finally {
    busy.value = false;
  }
}

async function saveTextDocument() {
  if (!selectedSpace.value) return;
  busy.value = true;
  try {
    const result = await indexKnowledgeText({ spaceId: selectedSpace.value.id, ...textForm.value });
    setNotice(`“${result.title}”已完成 ${result.chunkCount} 个知识切片索引`, "success");
    textForm.value = { title: "", content: "" };
    documents.value = await fetchKnowledgeDocuments();
  } catch (error) {
    setNotice(error instanceof Error ? error.message : "文本索引失败", "error");
  } finally {
    busy.value = false;
  }
}

async function ask() {
  const text = question.value.trim();
  if (!text || asking.value || !selectedSpace.value) return;
  messages.value.push({ id: `user-${Date.now()}`, role: "user", content: text });
  question.value = "";
  asking.value = true;
  await scrollBottom();
  try {
    const [answer, hits] = await Promise.all([
      queryKnowledge(text, selectedSpace.value.permissionScope),
      searchKnowledge(text, selectedSpace.value.permissionScope, 5)
    ]);
    messages.value.push({
      id: `assistant-${Date.now()}`,
      role: "assistant",
      content: answer.answer,
      confidence: answer.confidence,
      citations: answer.citations,
      hits
    });
  } catch (error) {
    messages.value.push({
      id: `assistant-error-${Date.now()}`,
      role: "assistant",
      content: error instanceof Error ? error.message : "知识库问答失败"
    });
  } finally {
    asking.value = false;
    await scrollBottom();
  }
}

function resetChat() {
  messages.value = [{ id: `welcome-${Date.now()}`, role: "assistant", content: "已切换知识空间。你可以继续提问，我会重新检索当前范围内的资料。" }];
}

function documentCount(spaceId: string) {
  return documents.value.filter((item) => item.spaceId === spaceId).length;
}

function scopeLabel(scope: string) {
  return ({ company: "全公司", legal: "法务", finance: "财务", sales: "销售" } as Record<string, string>)[scope] ?? scope;
}

function confidenceText(value: number) {
  return `${Math.round(value * 100)}%`;
}

function dateText(value?: string) {
  return value ? new Date(value).toLocaleDateString("zh-CN") : "刚刚";
}

function setNotice(message: string, state: "idle" | "success" | "error") {
  notice.value = message;
  noticeState.value = state;
}

async function scrollBottom() {
  await nextTick();
  if (chatBody.value) chatBody.value.scrollTop = chatBody.value.scrollHeight;
}
</script>
