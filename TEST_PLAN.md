# 🧪 测试计划 - 确保模块化生成稳定

## 目标
在添加高级功能前，确保模块化代码生成完全稳定可靠。

---

## 📋 测试清单

### Phase 1: 基础功能测试（今天）

#### 测试 1: Java 项目生成
**Issue 内容**：
```markdown
构建用户管理系统：
- 用户注册/登录
- JWT Token 认证
- 用户信息管理

使用 Spring Boot + MySQL
```

**预期结果**：
- [ ] 生成 .java 文件（不是 .txt）
- [ ] 文件在 src/main/java/... 目录下
- [ ] 包含 Controller/Service/Repository
- [ ] 代码语法正确（无编译错误）

**验证命令**：
```bash
# 检查生成的文件
ls -R auto_impl/issue-*/src/main/java/

# 统计代码文件数量
find auto_impl/issue-* -name "*.java" | wc -l
# 应该 > 0

# 确保没有代码写入 txt
find auto_impl/issue-* -name "*.txt" -exec grep -l "public class" {} \;
# 应该返回空（没有包含代码的txt文件）
```

---

#### 测试 2: Python 项目生成
**Issue 内容**：
```markdown
创建数据分析工具：
- 数据加载（CSV/Excel）
- 数据清洗
- 统计分析

使用 Python + Pandas + FastAPI
```

**预期结果**：
- [ ] 生成 .py 文件
- [ ] 文件在 src/ 目录下
- [ ] 包含 __init__.py
- [ ] 可执行（无语法错误）

**验证命令**：
```bash
# 检查 Python 语法
find auto_impl/issue-* -name "*.py" -exec python -m py_compile {} \;
# 应该无错误
```

---

#### 测试 3: 简单需求（不应模块化）
**Issue 内容**：
```markdown
创建一个计算斐波那契数列的函数
```

**当前行为**：
- 仍然会生成模块化结构（因为我们强制了）
- 这是预期的，因为我们要测试稳定性

**后续优化**：
- 可以添加复杂度检测
- 简单需求生成单文件（但仍是 .java/.py，不是 .txt）

---

#### 测试 4: 中文需求
**Issue 内容**：
```markdown
构建电商订单系统：
- 用户管理模块
- 商品管理模块  
- 订单管理模块
- 支付模块

技术栈：Java + Spring Boot
```

**预期结果**：
- [ ] 正确理解中文需求
- [ ] 生成 Java 代码（不是 Python）
- [ ] 模块划分合理

---

#### 测试 5: 边界情况

##### 5.1 超长需求
**Issue 内容**：
（超过 5000 字符的详细需求文档）

**预期**：
- [ ] Token 截断处理正确
- [ ] 仍生成代码文件

##### 5.2 模糊需求
**Issue 内容**：
```markdown
优化一下代码
```

**预期**：
- [ ] 生成占位符或基础结构
- [ ] 不报错

##### 5.3 特殊字符
**Issue 内容**：
```markdown
创建用户管理（包含 emoji 🎉 和特殊符号 <>&"）
```

**预期**：
- [ ] 正确处理特殊字符
- [ ] 不破坏代码生成

---

### Phase 2: 错误处理测试（明天）

#### 测试 6: API 失败处理
**场景**：模拟 DeepSeek API 超时

**预期**：
- [ ] 优雅降级
- [ ] 生成占位符 README
- [ ] 记录错误日志
- [ ] 不阻塞 PR 创建

#### 测试 7: 无 API Key
**场景**：不配置 DEEPSEEK_API_KEY

**预期**：
- [ ] 生成占位符 README.md
- [ ] 不报错退出
- [ ] PR 仍能创建

#### 测试 8: 脚本不存在
**场景**：删除 opencode_generate_modular.js

**预期**：
- [ ] 报错信息清晰
- [ ] 优雅降级

---

### Phase 3: 性能测试（本周内）

#### 测试 9: 生成速度
**指标**：
- [ ] 模块化生成 < 2 分钟
- [ ] 最多 6 个模块
- [ ] 每个模块 2-4 个文件

#### 测试 10: Token 使用
**指标**：
- [ ] 单次生成 < 10000 tokens
- [ ] 成本 < ¥0.05

---

## 🔍 监控指标

### 在 Actions 日志中关注

```bash
# 好的输出
✅ Successfully generated 8 Java files
📦 Total files: 8

# 警告输出
⚠️  No code files generated

# 错误输出
❌ Modular generation failed
```

### 收集数据

创建测试记录表：

| Issue # | 语言 | 模块数 | 文件数 | 耗时 | 状态 |
|---------|------|--------|--------|------|------|
| 1 | Java | 3 | 9 | 45s | ✅ |
| 2 | Python | 2 | 6 | 38s | ✅ |
| 3 | - | - | 0 | - | ❌ |

---

## 🛠️ 调试工具

### 1. 本地测试脚本

```bash
# test_locally.sh
#!/bin/bash

export DEEPSEEK_API_KEY="sk-xxx"

python3 issue_to_branch.py \
  --issue-number 999 \
  --repo "test/test" \
  --base main \
  --token "fake-token" \
  2>&1 | tee test_output.log

# 检查生成的文件
echo "生成的文件："
find auto_impl/issue-999 -type f

# 检查扩展名
echo "代码文件："
find auto_impl/issue-999 -name "*.java" -o -name "*.py" -o -name "*.ts"

echo "避免的 txt 文件："
find auto_impl/issue-999 -name "*.txt" | head -5
```

### 2. 验证代码语法

```bash
# Java
find auto_impl -name "*.java" -exec javac -d /tmp/compiled {} + 2>&1 | head -20

# Python  
find auto_impl -name "*.py" -exec python3 -m py_compile {} \; 2>&1

# TypeScript
find auto_impl -name "*.ts" -exec npx tsc --noEmit {} \; 2>&1 | head -20
```

---

## 🎯 成功标准

### 必须满足（Release Blockers）
- [ ] 100% 生成代码文件（不是 .txt）
- [ ] 代码语法正确（可编译/解析）
- [ ] 文件在正确的包目录中
- [ ] 不报错退出

### 应该满足（High Priority）
- [ ] 语言检测准确率 > 80%
- [ ] 生成速度 < 2 分钟
- [ ] 成本 < ¥0.1/次

### 最好满足（Nice to have）
- [ ] 代码质量可编译运行
- [ ] 注释清晰
- [ ] 包含测试用例

---

## 🚀 开始测试

### 今天任务

1. **提交修复代码**
   ```bash
   git add issue_to_branch.py
   git commit -m "fix: force modular generation, remove single-file .txt"
   git push origin main
   ```

2. **创建 3-5 个测试 Issue**
   - Java 电商系统
   - Python 数据分析
   - TypeScript Web 应用
   - 中文需求
   - 边界情况

3. **记录结果**
   填写测试记录表

4. **修复发现的问题**
   根据测试结果调整

---

## 📊 测试记录模板

```markdown
## 测试 #{N}

**日期**: 2024-XX-XX
**Issue**: #{number}
**需求**: [简述]

### 结果
- 语言: [Java/Python/TS/Go]
- 模块数: [N]
- 文件数: [N]
- 耗时: [N]s
- 状态: [✅/❌]

### 问题
[如果有问题，描述]

### 截图/日志
[关键日志]

### 结论
[通过/需要修复]
```

---

## ⏰ 时间表

| 时间 | 任务 |
|------|------|
| 今天 | 提交修复，创建测试 Issue |
| 明天 | 分析结果，修复问题 |
| 后天 | 第二轮测试 |
| 本周五 | 总结，决定是否稳定 |
| 下周一 | 如果稳定，开始上下文功能 |

---

## ✅ 验收标准

当满足以下条件时，认为基础功能稳定：

1. ✅ 连续 10 次成功生成（无失败）
2. ✅ 100% 生成代码文件（不是 .txt）
3. ✅ 语言检测准确率 > 80%
4. ✅ 无关键错误（API 失败能优雅降级）
5. ✅ 文档完善（使用说明清晰）

**完成后**：开始实现上下文感知重构功能！

---

**准备好了吗？开始测试吧！** 🧪
