# 企业流程自动化与 AI 经营助手

本项目当前由两个可独立部署的应用组成：

- `backend-java`：Java 21、Spring Boot 3、MyBatis-Plus，负责业务接口、权限、审批工作流、文件解析、AI 模型调用、审计和数据持久化。
- `frontend-web`：Vue 3、Vite、TypeScript，提供企业管理后台和老板对话式经营助手。

## 服务端口

| 服务 | 端口 | 说明 |
|---|---:|---|
| Java 后端 | 8080 | 业务接口、AI 接口、Swagger/Knife4j |
| Vue 前端 | 5173 | 开发服务器，`/api` 代理到 Java 后端 |

## 环境

- JDK：`D:\kfhj\jdk\jdk-21.0.11`
- Maven：`D:\kfhj\maven\apache-maven-3.9.16`
- Maven 本地仓库：`D:\kfhj\maven\mavenqiye`
- MySQL：`localhost:3306/qyglai`

## 启动 Java 后端

```powershell
cd backend-java
$env:JAVA_HOME='D:\kfhj\jdk\jdk-21.0.11'
$env:Path="$env:JAVA_HOME\bin;D:\kfhj\maven\apache-maven-3.9.16\bin;$env:Path"
mvn "-Dmaven.repo.local=D:\kfhj\maven\mavenqiye" spring-boot:run
```

构建验证：

```powershell
mvn "-Dmaven.repo.local=D:\kfhj\maven\mavenqiye" -DskipTests package
```

## 启动 Vue 前端

```powershell
cd frontend-web
npm install
npm run dev
```

访问地址：`http://localhost:5173`

## 数据库

- 完整初始化脚本：`database/mysql/qyglai.sql`
- 数据访问：MyBatis-Plus
- 默认数据库连接配置：`backend-java/src/main/resources/application.yml`

## Java AI 能力

Java 后端直接承担以下 AI 能力：

- 豆包等 OpenAI 兼容大模型的配置与调用
- 老板对话式经营问答，先查询 MySQL，再由模型组织答案
- PDF、Word、Excel、文本和 JSON 文件解析
- 合同、发票、对账单等业务字段抽取
- 销售跟进建议、财务对账分析、人工复核建议
- 知识库问答、日报周报生成、提示词评测
- AI 调用日志、失败降级和审计

真实模型配置保存在 `backend-java/runtime/model_config.json`，也可通过前端 AI 治理页面维护。该文件已加入 `.gitignore`，不要提交密钥。

## 基础设施

- Redis 和 Kafka 客户端已集成，默认关闭实际读写，可通过 `application.yml` 开启。
- 审批工作流使用内置流程定义、流程实例和流程任务表。
- 上传文件默认存储在 `backend-java/storage/uploads`。
- API 文档可通过后端 Swagger/Knife4j 页面查看。
