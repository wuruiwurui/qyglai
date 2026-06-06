# 企业流程自动化与 AI 经营助手

这是一个面向企业日常运营的 Java + Vue 自动化平台。系统不是通用聊天机器人，而是围绕合同、发票、对账、客服、销售、知识库、报表、审批与审计等业务流程，帮助企业直接减少人工处理时间。

## 当前架构

项目当前由两个应用组成：

- `backend-java`：Java 21、Spring Boot 3、MyBatis-Plus，负责业务接口、权限、审批工作流、文件解析、AI 调用、知识库 RAG、审计与数据持久化。
- `frontend-web`：Vue 3、Vite、TypeScript，提供企业管理后台、老板经营助手和各业务模块专属工作台。

原 `ai-python` 服务能力已经全部迁移到 Java 并删除。系统运行不再依赖 Python 服务或 8000 端口。

| 服务 | 端口 | 说明 |
|---|---:|---|
| Java 后端 | 8080 | 业务、AI、RAG、权限和工作流接口 |
| Vue 前端 | 5173 | 企业管理界面，`/api` 代理至 Java 后端 |

## 最近完成功能

### Java AI 能力迁移

- Java 直接调用豆包等 OpenAI 兼容大模型。
- AI 配置可在前端 AI 治理页面维护。
- 老板经营问答先查询 MySQL，再由模型组织答案。
- 文件解析、字段抽取、销售建议、对账分析、复核建议、知识问答和报表生成均由 Java 完成。
- 模型不可用时自动降级，并记录 AI 调用日志与失败原因。

### 文件处理闭环

- 支持 PDF、Word、Excel、JSON、CSV 和文本文件解析。
- 支持合同、发票、对账单等业务字段抽取。
- 文件处理结果统一展示，重新查询后保持相同详情结构。
- 支持重复上传同一张发票。
- 支持字段人工修正、修正原因、修正历史和审计日志。
- 文件处理后可自动创建复核任务和审批流程。

### 权限与审批

- JWT 登录认证。
- 用户、角色、权限、组织和 RBAC 管理。
- 按模块控制接口与菜单访问权限。
- 支持审批流程定义、流程实例、任务处理、通过、驳回、转交和审批历史。
- 审批结果可回写合同、发票等业务记录。

### 前端专属工作台

- 老板经营助手使用连续对话界面。
- 文件、AI 治理、权限管理、审批流程和知识库均拥有专属页面。
- 各业务字段使用中文标签展示。
- 前端仅调用真实 Java 后端接口。

## 知识库 RAG

知识库已经实现完整 Java RAG 闭环，不再返回固定答案。

### 知识入库

- 创建带权限范围的知识库空间，支持全公司、法务、财务和销售范围。
- 支持直接录入制度、流程说明和业务知识文本。
- 支持上传 PDF、Word、Excel、TXT 等知识文件。
- Java 自动解析文本、生成重叠切片并建立向量索引。

### 向量检索

- 使用 Java 确定性稀疏哈希 Embedding。
- 向量与切片元数据持久化在 MySQL `kb_chunk` 表。
- 使用余弦相似度进行 Top-K 检索。
- 可查看命中文档、原始知识片段和相似度。
- 当前方案无需额外向量数据库，后续可替换为 Milvus 或 OpenSearch。

### 知识问答

- 前端知识库页面使用真正的连续对话界面。
- 每次问答先检索当前权限范围内的知识切片。
- 豆包仅根据检索资料组织答案，资料不足时提示人工确认。
- 回答返回 `[1]` 格式引用、置信度和命中知识片段。
- 切换知识空间后自动重置对话上下文。

### 知识库接口

| 接口 | 功能 |
|---|---|
| `GET /api/kb/spaces` | 查询知识空间 |
| `POST /api/kb/spaces` | 创建知识空间 |
| `GET /api/kb/documents` | 查询已索引文档 |
| `POST /api/kb/documents/text` | 录入文本并建立索引 |
| `POST /api/kb/documents/file` | 上传文件并建立索引 |
| `GET /api/kb/search` | 查看 Top-K 知识切片 |
| `POST /api/kb/query` | 基于检索上下文进行知识问答 |

## 环境与启动

- JDK：`D:\kfhj\jdk\jdk-21.0.11`
- Maven：`D:\kfhj\maven\apache-maven-3.9.16`
- Maven 本地仓库：`D:\kfhj\maven\mavenqiye`
- MySQL：`localhost:3306/qyglai`

启动 Java 后端：

```powershell
cd backend-java
$env:JAVA_HOME='D:\kfhj\jdk\jdk-21.0.11'
$env:Path="$env:JAVA_HOME\bin;D:\kfhj\maven\apache-maven-3.9.16\bin;$env:Path"
mvn "-Dmaven.repo.local=D:\kfhj\maven\mavenqiye" spring-boot:run
```

启动 Vue 前端：

```powershell
cd frontend-web
npm install
npm run dev
```

访问地址：`http://localhost:5173`

默认管理员账号：`admin / 123456`

## 数据与配置

- MySQL 初始化脚本：`database/mysql/qyglai.sql`
- 后端配置：`backend-java/src/main/resources/application.yml`
- AI 模型配置：`backend-java/runtime/model_config.json`
- 上传文件目录：`backend-java/storage/uploads`

AI 模型配置文件已加入 `.gitignore`，不要提交真实 API Key。

## 基础设施

- MyBatis-Plus：业务数据持久化。
- Redis：客户端已集成，默认关闭实际读写。
- Kafka：客户端已集成，默认关闭事件投递。
- Spring Security：JWT 与 RBAC 权限控制。
- Springdoc / Knife4j：接口文档。
- MySQL：业务数据、知识切片和向量元数据存储。
