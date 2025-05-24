<p align="center">
    <img src="app/src/main/res/drawable/capybara.png" alt="CapybaraLedger Icon" width="25%"/>
</p>

<p align="center">
    <strong>卡皮记账</strong>
    <br>
    <strong>简单易用的本地记账应用</strong>
</p>

<br>

<p align="center">
    <img src="https://img.shields.io/badge/Android-3DDC84?style=flat-square&logo=android&logoColor=white"/>
    <img src="https://img.shields.io/badge/Kotlin-0095D5?style=flat-square&logo=kotlin&logoColor=white"/>
    <img src="https://img.shields.io/badge/License-MIT-blue.svg?style=flat-square"/>
    <img src="https://img.shields.io/badge/Min%20SDK-23-orange?style=flat-square"/>
    <img src="https://img.shields.io/badge/QQ群-962285162-blue?style=flat-square&logo=tencent-qq"/>
</p>

<br>

## 🎯 项目简介

卡皮记账 是一款基于 Android 平台的现代化记账应用，旨在帮助用户轻松管理个人财务。应用采用直观的界面设计和强大的功能，让记账变得简单高效。

### ✨ 项目特色

- 🎨 **优雅的界面设计**：采用 Material Design 3 设计语言，提供现代化的视觉体验
- 🔒 **数据安全**：本地数据存储，保护用户隐私
- 📊 **丰富的统计分析**：多维度的收支分析，帮助用户了解消费习惯
- 💰 **多账本管理**：支持创建和管理多个账本

## 🚀 主要功能

### 📚 账本管理

- [x] 支持创建多个账本
- [x] 可设置默认账本
- [x] 灵活的账本描述和排序

### 💳 账单记录

- [x] 支持收入和支出记录
- [x] 分类管理
- [x] 详细的交易信息记录

### 📊 统计分析

- [x] 收支趋势分析
- [x] 分类统计
- [x] 月度/年度报表

## 📥 安装说明

### 环境要求

- Android 6.0 (API 23) 或更高版本
- 至少 50MB 可用存储空间

### 快速下载

1. [卡皮本地记账软件分发](https://www.pgyer.com/cpledger)
2. 腾讯应用宝搜索"卡皮本地记账软件"

### 开发环境配置

1. 安装 Android Studio | 2023.3.1 或更高版本
2. 安装 JDK 17 或更高版本
3. 克隆项目代码：
   ```bash
   git clone https://github.com/Light5-star/CapybaraLedger1.git
   ```
4. 在 Android Studio 中打开项目
5. 等待 Gradle 同步完成
6. 连接 Android 设备或启动模拟器
7. 点击运行按钮开始调试

## 🛠️ 技术架构

### 开发环境

- 开发语言：Kotlin 1.9.23
- 最低支持 Android 版本：Android 6.0 (API 23)
- 目标 Android 版本：Android 14 (API 34)
- 构建工具：Gradle (Kotlin DSL)
- IDE：Android Studio Jellyfish | 2023.3.1

### 核心技术

- **Room 数据库 2.6.1**：用于本地数据持久化
- **MVVM 架构**：采用 ViewModel 和 LiveData
- **Kotlin Coroutines 1.7.3**：处理异步操作
- **Navigation Component 2.6.0**：实现应用内导航
- **Material Design 3 1.3.1**：现代化 UI 设计
- **MPAndroidChart 3.1.0**：数据可视化

## 🤝 贡献指南

我们非常欢迎您为 卡皮本地记账软件 做出贡献！

### 提交 Issue

- 报告 Bug：请详细描述问题，包括复现步骤、期望行为和实际行为
- 提出新功能：描述功能需求，说明使用场景和预期效果

### 提交 Pull Request

1. Fork 本仓库
2. 创建您的特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交您的修改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开一个 Pull Request

## 📝 开发规范

### 代码风格

- 遵循 [Kotlin 官方编码规范](https://kotlinlang.org/docs/coding-conventions.html)
- 使用 4 个空格进行缩进
- 类名采用 PascalCase，函数名和变量名采用 camelCase
- 常量使用全大写，单词间用下划线分隔
- 每行代码不超过 100 个字符

### 注释规范

- 为公共 API 添加 KDoc 注释
- 复杂的业务逻辑需要添加详细注释
- 使用 TODO 标记待完成的任务

### Git 提交规范

提交信息格式：

```
<type>(<scope>): <subject>

<body>
```

- type: feat, fix, docs, style, refactor, test, chore
- scope: 影响范围
- subject: 简短描述
- body: 详细描述

## 📄 许可证

本项目采用 [MIT 许可证](LICENSE) 许可证，部分图标资源来自阿里巴巴矢量图标库，使用时请遵守相关授权协议。

## 👥 维护者

- 主要维护者：[Light5-star](https://github.com/Light5-star)
- 邮箱：xhhcode@qq.com

## 💬 社区交流

- 提交 Issue：[GitHub Issues](https://github.com/Light5-star/capybaraledger1/issues)
- 讨论：[GitHub Discussions](https://github.com/Light5-star/capybaraledger1/discussions)
- QQ 群：[962285162](http://qm.qq.com/cgi-bin/qm/qr?_wv=1027&k=K0gSvdTlO47PADHVtNb359_k4-8cQWdJ&authKey=RsLmfgHmJGak64CeoXo22gnLhaUBPyohrpkS7%2FrylfrGSqZZ82BNE9hl6r4BDfAu&noverify=0&group_code=962285162)
