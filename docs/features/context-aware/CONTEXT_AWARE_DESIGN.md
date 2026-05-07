# 🔍 基于现有代码的智能重构方案

## 需求理解

你希望实现：**Context-Aware Code Generation（上下文感知代码生成）**

### 场景示例
```
当前项目：已有用户登录功能（UserController.java, UserService.java）

新 Issue：优化用户登录，支持微信/QQ第三方登录

期望行为：
1. 读取现有的 UserController.java 和 UserService.java
2. 理解当前的登录逻辑和接口
3. 基于现有代码结构，添加第三方登录支持
4. 保持现有代码风格和设计模式
```

---

## ✅ 可行性分析

### 可以实现的功能 ✅

1. **读取项目文件** ✅
   - 通过 GitHub API 获取文件树
   - 通过 Git 命令读取本地文件
   - 检索特定关键词相关的文件

2. **代码上下文理解** ✅
   - 提取接口定义、类结构
   - 分析方法签名和依赖关系
   - 理解代码风格和命名规范

3. **增量代码生成** ✅
   - 基于现有代码生成新增方法
   - 生成兼容现有接口的实现
   - 生成适配器/扩展类

4. **代码重构建议** ✅
   - 识别需要修改的文件
   - 生成修改后的代码片段
   - 创建重构文档说明变更

---

## 🎯 实现方案

### 方案一：文件检索模式（推荐）

**工作流程**：
```
Issue 创建
    ↓
[Step 1] 关键词匹配
  - 从 Issue 中提取关键词（用户、登录、微信、第三方）
  - 扫描项目文件，找到相关文件
    
[Step 2] 读取上下文
  - 读取匹配的文件内容（UserController.java, UserService.java）
  - 提取关键接口和方法签名
  - 分析项目结构和技术栈
    
[Step 3] 上下文感知生成
  - 将现有代码作为上下文发送给 AI
  - AI 基于现有代码生成新代码
  - 生成的新代码保持风格一致
    ↓
提交 PR（包含修改后的文件）
```

**示例**：
```
Issue: "优化用户登录，支持微信第三方登录"

系统自动执行：
1. 检索到 UserController.java, UserService.java, User.java
2. 读取文件内容（限制在 3000 tokens 内）
3. 分析出当前使用 JWT + 数据库存储
4. 生成：
   - WechatAuthService.java（新文件）
   - UserController.java（修改：添加 /auth/wechat 端点）
   - application.yml（修改：添加微信配置）
```

---

### 方案二：项目快照模式

**工作流程**：
```
[Step 1] 创建项目快照
  - 扫描整个项目的文件结构
  - 生成项目地图（目录树 + 关键文件摘要）
  - 识别技术栈（Spring Boot? Django?）
    
[Step 2] 语义检索
  - 使用 AI 分析每个文件的用途
  - 建立文件-功能索引
  - 例如：UserController.java → 用户相关
    
[Step 3] 智能匹配
  - Issue 创建时，分析需求语义
  - 匹配最相关的文件
  - 读取相关文件内容
    ↓
[Step 4] 增量生成
  - 基于上下文生成代码
```

**优点**：
- 更智能的文件匹配
- 可以理解文件间的关系

**缺点**：
- 需要更多 API 调用
- 大项目成本高

---

### 方案三：增量更新模式

**适用于**：已有明确需要修改的文件

**工作流程**：
```
Issue: "修改 UserController 添加微信登录"
（明确提到了要修改的文件）

1. 读取 UserController.java
2. 分析现有方法
3. 生成新增的方法
4. 输出完整的修改后文件
5. 提交 PR 替换原文件
```

---

## 🔧 技术实现

### 1. 文件检索模块

```javascript
// 扫描项目文件
async function scanProjectFiles(repo, token) {
  // 使用 GitHub API 获取文件树
  const tree = await github.getFileTree(repo, token);
  
  // 过滤代码文件
  const codeFiles = tree.filter(file => 
    file.path.endsWith('.java') ||
    file.path.endsWith('.py') ||
    file.path.endsWith('.ts')
  );
  
  return codeFiles;
}

// 关键词匹配
function findRelevantFiles(files, keywords) {
  return files.filter(file => {
    const path = file.path.toLowerCase();
    return keywords.some(kw => path.includes(kw.toLowerCase()));
  });
}
```

### 2. 代码读取模块

```javascript
// 读取文件内容
async function readFileContent(filePath, token) {
  // 使用 GitHub API 读取文件
  const content = await github.getFileContent(filePath, token);
  return Buffer.from(content, 'base64').toString('utf8');
}

// 提取关键信息（接口、类、方法）
function extractCodeContext(content, language) {
  if (language === 'java') {
    // 提取 public class, interface, 方法签名
    const classMatch = content.match(/public\s+class\s+(\w+)/);
    const interfaceMatch = content.match(/public\s+interface\s+(\w+)/);
    const methods = content.match(/public\s+\w+\s+\w+\([^)]*\)/g);
    
    return { className: classMatch?.[1], interfaceName: interfaceMatch?.[1], methods };
  }
  // ... 其他语言
}
```

### 3. 上下文生成模块

```javascript
// 构建 AI 提示词
function buildContextualPrompt(issueBody, existingFiles, language) {
  let prompt = `请基于以下现有代码进行开发：\n\n`;
  prompt += `## 需求\n${issueBody}\n\n`;
  prompt += `## 现有代码上下文\n\n`;
  
  for (const file of existingFiles) {
    prompt += `### ${file.path}\n`;
    prompt += `\`\`\`${language}\n${file.content}\n\`\`\`\n\n`;
  }
  
  prompt += `## 要求\n`;
  prompt += `1. 保持现有代码风格和命名规范\n`;
  prompt += `2. 复用现有接口和工具类\n`;
  prompt += `3. 新代码与现有代码兼容\n`;
  prompt += `4. 生成完整的、可编译的代码文件\n`;
  prompt += `5. 如果修改现有文件，输出完整的修改后内容\n`;
  
  return prompt;
}
```

---

## ⚠️ 难点与挑战

### 难点 1：Token 限制 ⭐⭐⭐

**问题**：DeepSeek/OpenAI 有输入长度限制（通常 4K-8K tokens）

**影响**：
- 无法一次性读取大文件
- 无法读取整个项目代码

**解决方案**：
1. **文件截断**：只读取文件的关键部分（接口定义、方法签名）
2. **分层读取**：
   - 第一层：读取文件结构和文件名
   - 第二层：读取匹配文件的摘要
   - 第三层：只读取最相关文件的完整内容
3. **代码压缩**：
   - 删除注释（保留关键注释）
   - 压缩空行
   - 只保留 public 方法和接口

**实现示例**：
```javascript
function compressCode(content, language) {
  // 删除注释
  content = content.replace(/\/\*[\s\S]*?\*\//g, '');
  content = content.replace(/\/\/.*$/gm, '');
  
  // 删除空行
  content = content.replace(/\n\s*\n/g, '\n');
  
  // 只保留 public 方法（Java）
  if (language === 'java') {
    const publicMethods = content.match(/public\s+[\w<>\[\]]+\s+\w+\s*\([^)]*\)\s*\{[^}]*\}/g);
    if (publicMethods) {
      return publicMethods.join('\n\n');
    }
  }
  
  return content;
}
```

---

### 难点 2：代码相关性判断 ⭐⭐

**问题**：如何确定哪些文件与 Issue 相关？

**挑战**：
- 文件命名不规范
- 功能分散在多个文件中
- 难以识别依赖关系

**解决方案**：
1. **多维度匹配**：
   - 文件名匹配（user → UserController.java）
   - 内容关键词匹配（扫描文件内容）
   - 文件路径匹配（user/ 目录下的文件）

2. **语义理解**：
   - 使用 AI 分析每个文件的功能描述
   - 建立文件-功能索引

3. **用户辅助**：
   - Issue 中可以通过标签指定相关文件
   - 例如：`@file:UserController.java`

**实现示例**：
```javascript
async function findRelevantFilesSmart(issueBody, allFiles, apiKey) {
  // 先通过关键词快速筛选
  const keywords = extractKeywords(issueBody);
  const candidates = findRelevantFiles(allFiles, keywords);
  
  // 如果候选太多，使用 AI 进一步筛选
  if (candidates.length > 5) {
    const prompt = `以下文件列表中，哪些与需求最相关？\n\n需求：${issueBody}\n\n文件列表：\n${candidates.map(f => f.path).join('\n')}\n\n请返回最相关的 3-5 个文件路径。`;
    
    const response = await callAI(prompt, apiKey);
    const selectedPaths = parseAIResponse(response);
    return candidates.filter(f => selectedPaths.includes(f.path));
  }
  
  return candidates;
}
```

---

### 难点 3：项目规模限制 ⭐⭐⭐

**问题**：大项目有成千上万个文件，无法全部扫描

**解决方案**：
1. **分层扫描**：
   - 只扫描 src/ 目录，忽略 test/, node_modules/, .git/
   - 只扫描特定类型的文件（.java, .py）

2. **缓存机制**：
   - 首次扫描后缓存文件树
   - 后续使用缓存，定期更新

3. **按需加载**：
   - 不预读取所有文件内容
   - 先读取文件名，匹配后再读取内容

---

### 难点 4：准确性与安全性 ⭐⭐

**问题**：
- AI 可能误解现有代码逻辑
- 生成的代码可能破坏现有功能
- 难以保证代码质量

**解决方案**：
1. **生成测试用例**：
   - 基于现有代码生成单元测试
   - 确保新代码通过测试

2. **代码审查标记**：
   - 在 PR 中标记 AI 生成的部分
   - 建议人工审查关键逻辑

3. **增量修改**：
   - 优先创建新文件，不修改现有文件
   - 减少对现有代码的破坏风险

---

## 💰 成本分析

### Token 消耗估算

**场景**：优化用户登录功能

**消耗**：
1. 扫描文件树：100 tokens（API 调用）
2. 读取 3 个相关文件：约 3000 tokens
3. 生成代码：约 2000 tokens
4. **总计**：约 5100 tokens

**成本**（DeepSeek）：
- 约 0.015 元（1.5分钱）

**结论**：成本可接受！

---

## 🚀 推荐实现路径

### Phase 1: 基础功能（1-2天）
1. ✅ 文件扫描和读取
2. ✅ 关键词匹配
3. ✅ 上下文提示词构建
4. ✅ 增量代码生成

### Phase 2: 优化（3-5天）
1. 代码压缩和截断
2. 智能文件选择
3. 缓存机制
4. 多文件修改支持

### Phase 3: 高级功能（1-2周）
1. 语义理解
2. 自动测试生成
3. 代码质量检查
4. 重构建议

---

## 📊 对比：从零生成 vs 基于上下文生成

| 特性 | 从零生成 | 基于上下文生成 |
|------|---------|--------------|
| **适用场景** | 新项目、原型 | 现有项目迭代、重构 |
| **代码一致性** | ⭐⭐ | ⭐⭐⭐⭐⭐ |
| **集成难度** | 低 | 中 |
| **Token 消耗** | 低 | 高 |
| **准确性** | 一般 | 高 |
| **实用性** | ⭐⭐⭐ | ⭐⭐⭐⭐⭐ |

---

## ✅ 建议

**建议实现方案一（文件检索模式）**：
- 实现简单，性价比高
- 满足 80% 的使用场景
- 易于逐步优化

**下一步行动**：
1. 我可以帮你实现 Phase 1 的基础功能
2. 先测试简单场景（如添加方法到现有类）
3. 根据反馈逐步优化

**你希望我**：
- ✅ 立即实现基础版本？
- 📋 先设计详细的实现方案？
- 🎯 针对特定场景（如 Spring Boot 项目）定制？

---

## 📚 参考实现

类似的工具：
- **GitHub Copilot**：基于上下文的代码补全
- **Sourcegraph Cody**：代码智能问答
- **Tabnine**：项目感知的代码生成

这些工具证明了技术可行性！
