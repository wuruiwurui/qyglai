# IDEA 导入 Java 后端配置

如果 IDEA 打开 `backend-java` 后所有 Maven 依赖飘红，请按下面配置。命令行已验证该项目依赖可正常解析，飘红通常是 IDEA 没使用正确的 JDK、Maven 或本地仓库。

## 1. 用 Maven 项目方式打开

推荐直接打开：

```text
E:\xm\qyglai\backend-java\pom.xml
```

或者打开根目录后，在 IDEA 右侧 Maven 面板中手动添加：

```text
E:\xm\qyglai\backend-java\pom.xml
```

## 2. 配置 Project SDK

路径：

```text
File -> Project Structure -> Project
```

设置：

```text
SDK: D:\kfhj\jdk\jdk-21.0.11
Language level: 21
```

## 3. 配置 Maven

路径：

```text
File -> Settings -> Build, Execution, Deployment -> Build Tools -> Maven
```

设置：

```text
Maven home path:
Bundled (Maven 3)

User settings file:
D:\kfhj\maven\apache-maven-3.9.16\conf\settings.xml

Local repository:
D:\kfhj\maven\mavenqiye
```

如果 IDEA 使用 `D:\kfhj\maven\apache-maven-3.9.16` 报下面异常：

```text
NoSuchMethodError: DefaultModelValidator: method 'void <init>()' not found
```

说明 IDEA 自带 Maven 导入插件与该 Maven Core 版本不兼容。IDEA 内请使用 `Bundled (Maven 3)`；命令行仍然可以继续使用 `D:\kfhj\maven\apache-maven-3.9.16`。

## 4. 配置 Maven Runner

路径：

```text
File -> Settings -> Build, Execution, Deployment -> Build Tools -> Maven -> Runner
```

设置：

```text
JRE: D:\kfhj\jdk\jdk-21.0.11
```

## 5. 重新加载 Maven

在 IDEA 右侧 Maven 面板点击：

```text
Reload All Maven Projects
```

如果仍然飘红，依次执行：

```text
File -> Invalidate Caches...
Invalidate and Restart
```

重启后再次 Reload Maven。

## 6. IDEA 内运行后端

Maven 面板中运行：

```text
automation-backend -> Plugins -> spring-boot -> spring-boot:run
```

或创建 Spring Boot 运行配置：

```text
Main class:
com.qyglai.automation.AutomationBackendApplication

JRE:
D:\kfhj\jdk\jdk-21.0.11
```

当前后端默认连接本地 MySQL：

```text
jdbc:mysql://localhost:3306/qyglai
username: root
password: 123456
```
