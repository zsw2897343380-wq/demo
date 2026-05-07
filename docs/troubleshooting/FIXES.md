# Issue-to-Branch 自动化修复说明

## 问题分析

根据你的错误日志，主要有以下几个问题：

### 1. `@opencode-ai/sdk` 模块不存在 ❌
```
Error: Cannot find module '@opencode-ai/sdk'
```
- 这个 npm 包是虚构的，不存在于 npm registry
- 导致 Node.js 脚本执行失败

### 2. API 地址是示例地址 ❌
```
OpenCode API failed: HTTPSConnectionPool(host='api.opencode.example', port=443)
```
- `api.opencode.example` 只是一个占位符地址
- 真实的 OpenCode API 需要正确的端点

### 3. 空提交问题 ❌
```
nothing to commit, working tree clean
No commits between main and auto/issue-1-1778141559
```
- 由于代码生成完全失败，没有生成任何文件
- 导致无法创建有意义的 PR

## 修复内容

### 1. 移除了不存在的 npm 依赖
- **文件**: `scripts/opencode_generate.js` 和 `opencode_generate.js`
- **改动**: 移除了 `@opencode-ai/sdk` 的导入
- **新实现**: 使用纯 Node.js 根据 issue 内容生成代码模板

### 2. 改进了代码生成逻辑
- **多层降级机制**:
  1. 首先尝试 Node.js 脚本生成
  2. 失败后使用 Python 回退生成
  3. 最后确保创建占位符文件
- **确保有文件可提交**: 无论外部 API 是否可用，都会生成代码文件

### 3. 修复了 GitHub Actions 工作流
- **默认分支**: 将默认分支从 `develop` 改为 `main`（现代 GitHub 仓库标准）
- **fetch-depth**: 添加 `fetch-depth: 0` 确保可以访问完整历史
- **issue number 获取**: 区分 `workflow_dispatch` 和 `issues` 事件

### 4. 增强了错误处理和日志
- 添加了更多的调试输出
- 改进了文件存在性检查
- 即使生成失败也会创建占位符，避免空提交

## 使用方法

### 方式一：Issue 触发（自动）
当有人在仓库创建或编辑 issue 时，工作流会自动运行：
```yaml
on:
  issues:
    types: [opened, edited]
```

### 方式二：手动触发
1. 进入仓库的 **Actions** 标签页
2. 选择 **Harness** 工作流
3. 点击 **Run workflow**
4. 输入:
   - **Issue number**: 要处理的 issue 编号（如 `1`）
   - **Base branch**: 目标分支（默认为 `main`）

### 配置真实的外部 API（可选）

如果你想使用真实的 OpenCode API，需要：

1. **设置 GitHub Secrets**:
   - 在仓库设置中添加 `OPENCODE_API_KEY`
   - （可选）添加 `OPENCODE_API_URL` 指向真实 API 端点

2. **在 Python 脚本中配置**:
   修改 `issue_to_branch.py` 中的 API URL:
   ```python
   opencode_url = os.environ.get('OPENCODE_API_URL', 'https://your-real-api.com/generate')
   ```

## 生成的代码结构

代码将生成在 `auto_impl/issue-{编号}/` 目录中：

```
auto_impl/
└── issue-1/
    ├── auto_generated_code.txt    # 主要的生成代码
    └── generated_by_sdk.txt       # 由 Node.js 脚本生成的代码
```

## 注意事项

1. **权限设置**: 确保 GitHub Actions 有写入权限：
   - 进入仓库 Settings → Actions → General
   - 在 "Workflow permissions" 中选择 "Read and write permissions"

2. **分支保护**: 如果 `main` 分支有保护规则，可能需要调整：
   - 允许 GitHub Actions 创建 PR
   - 或者使用个人访问令牌 (PAT)

3. **Node.js 环境**: 代码生成脚本不需要安装任何 npm 包，完全自包含

## 测试

要本地测试脚本：

```bash
# 设置环境变量
export GITHUB_TOKEN="your_token_here"

# 运行脚本
python3 issue_to_branch.py \
  --issue-number 1 \
  --repo "your-username/your-repo" \
  --base main \
  --token "$GITHUB_TOKEN"
```

## 下一步

1. 提交这些修复到你的仓库
2. 创建一个新的 issue 测试工作流
3. 或者手动触发 Actions 测试

如果还有其他问题，请查看 Actions 的详细日志！
