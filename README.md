# AI 企业流程自动化系统骨架

本仓库包含三端基础架构：

- `backend-java`：Java 21 + Spring Boot 3，负责企业业务、权限审计、流程编排和 AI 服务编排。
- Java 持久层使用 MyBatis-Plus，默认 H2 内存库便于开发启动，也预留 MySQL profile。
- `ai-python`：Python + FastAPI，负责 OCR、抽取、分类、RAG、报表生成等 AI 能力。
- `frontend-web`：React + Vite + TypeScript，PC 管理端和老板智能经营助手入口。

## 端口

| 服务 | 端口 | 说明 |
|---|---:|---|
| Java 后端 | 8080 | `/api/health`、`/api/boss-assistant/ask`、`/api/modules` |
| Python AI 服务 | 8000 | `/health`、`/api/v1/boss/query`、`/api/v1/documents/extract` |
| 前端 | 5173 | Vite 开发服务，代理 `/api` 到 Java 后端 |

## 启动方式

### 1. Java 后端

需要 JDK 21。当前机器推荐使用：

- JDK：`D:\kfhj\jdk\jdk-21.0.11`
- Maven：`D:\kfhj\maven\apache-maven-3.9.16`
- Maven 本地仓库：`D:\kfhj\maven\mavenqiye`

```powershell
cd backend-java
$env:JAVA_HOME='D:\kfhj\jdk\jdk-21.0.11'
$env:Path="$env:JAVA_HOME\bin;D:\kfhj\maven\apache-maven-3.9.16\bin;$env:Path"
mvn "-Dmaven.repo.local=D:\kfhj\maven\mavenqiye" spring-boot:run
```

打包验证：

```powershell
mvn "-Dmaven.repo.local=D:\kfhj\maven\mavenqiye" -DskipTests package
```

默认会使用 H2 内存库并自动执行 `schema.sql`、`data.sql`。H2 控制台：

```text
http://localhost:8080/h2-console
```

默认连接信息：

```text
JDBC URL: jdbc:h2:mem:qyglai
User: sa
Password: 空
```

如果要切换到 MySQL：

```powershell
cd backend-java
mvn "-Dmaven.repo.local=D:\kfhj\maven\mavenqiye" spring-boot:run
```

当前默认已接入本地 MySQL：

```text
数据库：qyglai
地址：localhost:3306
用户名：root
密码：123456
```

如果临时需要切回 H2：

```powershell
mvn "-Dmaven.repo.local=D:\kfhj\maven\mavenqiye" spring-boot:run -Dspring-boot.run.profiles=h2
```

正式 MySQL 建表脚本：

```text
database/mysql/qyglai_schema.sql
```

该脚本覆盖 37 张核心表，包含组织用户权限、自动化模块、文件解析、合同风险、发票对账、客服工单、销售跟进、知识库、日报周报、流程编排、人工复核、消息通知、第三方集成、AI 模型治理和审计日志。

### 2. Python AI 服务

```powershell
cd ai-python
python -m venv .venv
.\.venv\Scripts\Activate.ps1
pip install -r requirements.txt
uvicorn app.main:app --reload --host 0.0.0.0 --port 8000
```

### 3. 前端

```powershell
cd frontend-web
npm install
npm run dev
```

浏览器打开：

```text
http://localhost:5173
```

## 当前已搭建能力

- 老板可通过对话查询经营摘要。
- Java 后端会优先调用 Python AI 服务，失败时返回本地 mock 数据，方便独立开发。
- 前端包含经营指标、对话助手、自动化模块列表、建议动作。
- Python AI 服务预留文档抽取、工单分类、报表生成接口。
- Java 后端已拆出合同、发票、工单、销售、知识库、报表、流程、人工复核等模块接口。
- Java 后端已接入 MyBatis-Plus，`/api/modules` 已改为从 `automation_module` 表查询。
- Java 后端已加入 Redis/Kafka 客户端封装，默认关闭实际读写，生产环境可通过配置打开。
- Java 后端已实现轻量工作流能力，基于 `workflow_definition`、`workflow_instance`、`workflow_task` 三张表，不强制依赖外部流程引擎。
- Java 后端已实现文件资产、合同、发票、工单、销售、知识库、报表、流程、复核、消息、AI治理、审计的企业级基础闭环。

## Java API 概览

| 模块 | 接口 | 说明 |
|---|---|---|
| 健康检查 | `GET /api/health` | 后端服务状态 |
| 老板助手 | `POST /api/boss-assistant/ask` | 对话式经营查询 |
| 模块列表 | `GET /api/modules` | 获取自动化能力模块 |
| 合同 | `POST /api/contracts/extract` | 合同字段抽取和风险识别 |
| 发票 | `POST /api/invoices/parse` | 发票解析和校验 |
| 工单 | `POST /api/tickets/classify` | 客服工单分类 |
| 销售 | `GET /api/sales/followups` | 销售跟进任务 |
| 知识库 | `POST /api/kb/query` | 企业知识库问答 |
| 报表 | `POST /api/reports/generate` | 日报周报生成 |
| 流程 | `POST /api/workflows/start` | 启动自动化流程 |
| 复核 | `GET /api/review/tasks` | 人工复核任务 |
| 复核 | `PUT /api/review/tasks/{id}/complete` | 完成人工复核 |
| 文件 | `POST /api/files/upload` | 上传文件并登记资产 |
| 文件 | `POST /api/files/parse-results` | 保存文档解析结果 |
| 消息 | `POST /api/notifications` | 创建消息通知 |
| 消息 | `GET /api/notifications` | 查询消息通知 |
| AI治理 | `GET /api/ai-governance/model-call-logs` | 查询模型调用日志 |
| 系统 | `GET /api/system/orgs` | 查询组织 |
| 系统 | `POST /api/system/orgs` | 创建组织 |
| 系统 | `GET /api/system/users` | 查询用户 |
| 系统 | `GET /api/system/roles` | 查询角色 |
| 系统 | `GET /api/system/permissions` | 查询权限 |
| 对账 | `POST /api/reconciliation/batches` | 创建对账批次 |
| 对账 | `GET /api/reconciliation/batches` | 查询对账批次 |
| 对账 | `GET /api/reconciliation/items` | 查询对账明细 |
| 集成 | `POST /api/integrations/connectors` | 创建第三方连接器 |
| 集成 | `GET /api/integrations/connectors` | 查询第三方连接器 |
| 集成 | `POST /api/integrations/webhooks` | 接收Webhook事件 |
| 集成 | `GET /api/integrations/webhooks` | 查询Webhook事件 |
| 审计 | `GET /api/audit/logs` | 查询审计日志 |
| AI治理 | `GET /api/ai-governance/providers` | 查询模型供应商 |
| AI治理 | `POST /api/ai-governance/prompt-templates` | 创建提示词模板 |
| AI治理 | `GET /api/ai-governance/prompt-templates` | 查询提示词模板 |
| AI治理 | `GET /api/ai-governance/evaluation-samples` | 查询评测样本 |

## Java 工程化说明

当前 Java 端采用以下工程设计：

| 能力 | 实现 |
|---|---|
| 数据访问 | MyBatis-Plus + MySQL |
| 接口文档 | Springdoc OpenAPI + Knife4j |
| 缓存 | Spring Data Redis，默认 `automation.cache.redis-enabled=false` |
| 消息 | Spring Kafka，默认 `automation.messaging.kafka-enabled=false` |
| 工作流 | 内置轻量工作流表模型 |
| 审计 | `audit_log` 表统一记录关键业务动作 |
| 事件 | `BusinessEventService` 统一封装业务事件发布 |
| 文件 | 本地 `storage/uploads` 存储，后续可替换 MinIO/OSS |

当前 Java 端已为 MySQL 37 张核心表生成对应实体对象，实体字段均带中文注释，并提供 MyBatis-Plus Mapper。

## 下一步建议

1. 接入数据库：PostgreSQL/MySQL、Redis、MinIO。
2. 增加权限认证：JWT、RBAC、组织和数据范围。
3. 接入流程引擎：Flowable/Camunda。
4. 接入真实 OCR、Embedding、向量库和大模型网关。
5. 建立合同、发票、工单、销售、知识库、报告等业务表。
# qyglai
