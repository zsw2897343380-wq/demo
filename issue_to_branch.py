#!/usr/bin/env python3
"""
Issue-to-Branch Automation - Modular Code Generation Only
强制模块化代码生成，只生成多文件包结构，永不生成单文件.txt
"""
import argparse
import shlex
import os
import subprocess
import sys
import time
import requests

def run(cmd, check=False, capture_output=False):
    """Execute a shell command."""
    result = subprocess.run(cmd, shell=True, check=check, capture_output=capture_output, text=True)
    if capture_output:
        return result.stdout.strip()
    return ""

def main():
    p = argparse.ArgumentParser(description='Issue-to-Branch Automation with Modular Code Generation')
    p.add_argument('--issue-number', required=True, dest='issue_number', help='Issue number to process')
    p.add_argument('--repo', required=True, help='Owner/Repo')
    p.add_argument('--base', default='main', help='Base branch')
    p.add_argument('--token', required=True, help='GitHub token')
    p.add_argument('--strategy', required=False, default='auto', help='Generation strategy (kept for compatibility, always uses modular)')
    p.add_argument('--opencode-key', required=False, help='Legacy parameter (not used)')
    args = p.parse_args()

    GITHUB_API = 'https://api.github.com'
    headers = {
        'Authorization': f'token {args.token}',
        'Accept': 'application/vnd.github+json'
    }

    owner, repo = args.repo.split('/')
    
    try:
        issue_num = int(args.issue_number)
        if issue_num <= 0:
            raise ValueError()
    except (ValueError, TypeError):
        print(f"Error: Invalid issue number '{args.issue_number}'")
        sys.exit(2)

    # Fetch issue
    issue_url = f"{GITHUB_API}/repos/{owner}/{repo}/issues/{issue_num}"
    r = requests.get(issue_url, headers=headers)
    try:
        r.raise_for_status()
    except requests.HTTPError as e:
        print(f"Error: Issue #{issue_num} not found")
        sys.exit(2)
    
    issue = r.json()
    issue_body = issue.get('body', '') or ''
    issue_title = issue.get('title', f'Issue #{issue_num}')
    
    print(f'Fetched issue #{issue_num}: {len(issue_body)} chars')
    print(f'Title: {issue_title}')

    # Prepare directory
    generated_code_dir = f"auto_impl/issue-{issue_num}"
    os.makedirs(generated_code_dir, exist_ok=True)
    
    # Check for API keys
    deepseek_key = os.environ.get('DEEPSEEK_API_KEY')
    code_generated = False
    generation_method = ""
    generated_files = []
    
    # Detect if this is a context-aware request (refactoring based on existing code)
    context_keywords = ['优化', '重构', '修改', '添加', '更新', '改进', '基于', '现有', '已有', 
                        'optimize', 'refactor', 'modify', 'update', 'improve', 'existing', 'current', 'based on']
    is_context_mode = any(kw in issue_body.lower() for kw in context_keywords)
    
    if deepseek_key and is_context_mode:
        # Context-aware generation based on existing code
        print("\n[Strategy] Using CONTEXTUAL code generation (based on existing codebase)")
        node_script = 'scripts/opencode_generate_contextual.js'
        
        if os.path.isfile(node_script):
            try:
                env_vars = f"DEEPSEEK_API_KEY={shlex.quote(deepseek_key)}"
                sdk_cmd = f"{env_vars} node {node_script} --issue-number {issue_num} --issue-body {shlex.quote(issue_body)} --repo {shlex.quote(args.repo)} --token {shlex.quote(args.token)} --base {args.base} --outdir {shlex.quote(generated_code_dir)}"
                
                print("\n🧠 Analyzing existing code and generating contextual changes...")
                result = subprocess.run(sdk_cmd, shell=True, capture_output=True, text=True)
                print(result.stdout)
                if result.stderr:
                    print(result.stderr)
                
                # Check for generated files
                code_extensions = ['.java', '.py', '.ts', '.js', '.go', '.rs']
                
                if os.path.exists(generated_code_dir):
                    for root, dirs, files in os.walk(generated_code_dir):
                        for file in files:
                            if any(file.endswith(ext) for ext in code_extensions):
                                file_path = os.path.join(root, file)
                                generated_files.append(file_path)
                                rel_path = os.path.relpath(file_path, generated_code_dir)
                                print(f"  ✅ {rel_path}")
                
                if len(generated_files) > 0:
                    code_generated = True
                    generation_method = "Contextual (Based on existing code)"
                    print(f"\n✅ Successfully generated {len(generated_files)} contextual changes")
                else:
                    print("\n⚠️  No code files generated, falling back to modular mode")
                    
            except Exception as e:
                print(f"\n⚠️  Contextual generation failed: {e}, falling back to modular")
        else:
            print(f"\n⚠️  Contextual generator not found, using modular mode")
    
    if not code_generated and deepseek_key:
        # Modular generation (from scratch)
        print("\n[Strategy] Using MODULAR code generation (multi-file package structure)")
        node_script = 'scripts/opencode_generate_modular.js'
        
        if os.path.isfile(node_script):
            try:
                env_vars = f"DEEPSEEK_API_KEY={shlex.quote(deepseek_key)}"
                sdk_cmd = f"{env_vars} node {node_script} --issue-number {issue_num} --issue-body {shlex.quote(issue_body)} --outdir {shlex.quote(generated_code_dir)}"
                
                print("\n🚀 Generating modular code...")
                result = subprocess.run(sdk_cmd, shell=True, capture_output=True, text=True)
                print(result.stdout)
                if result.stderr:
                    print(result.stderr)
                
                # Check for generated code files (not .txt)
                code_extensions = ['.java', '.py', '.ts', '.js', '.go', '.rs', '.c', '.cpp', '.h']
                
                if os.path.exists(generated_code_dir):
                    for root, dirs, files in os.walk(generated_code_dir):
                        for file in files:
                            if any(file.endswith(ext) for ext in code_extensions):
                                file_path = os.path.join(root, file)
                                generated_files.append(file_path)
                                rel_path = os.path.relpath(file_path, generated_code_dir)
                                print(f"  ✅ {rel_path}")
                
                if len(generated_files) > 0:
                    # Detect language from first file
                    first_file = generated_files[0]
                    if first_file.endswith('.java'): detected_lang = 'Java'
                    elif first_file.endswith('.py'): detected_lang = 'Python'
                    elif first_file.endswith('.ts'): detected_lang = 'TypeScript'
                    elif first_file.endswith('.js'): detected_lang = 'JavaScript'
                    elif first_file.endswith('.go'): detected_lang = 'Go'
                    elif first_file.endswith('.rs'): detected_lang = 'Rust'
                    else: detected_lang = 'Code'
                    
                    code_generated = True
                    generation_method = f"Modular ({detected_lang})"
                    print(f"\n✅ Successfully generated {len(generated_files)} {detected_lang} files")
                else:
                    print("\n⚠️  No code files generated")
                    
            except Exception as e:
                print(f"\n❌ Modular generation failed: {e}")
        else:
            print(f"\n❌ Modular generator not found: {node_script}")
    
    # Fallback: Create placeholder structure
    if not code_generated:
        print("\n[Strategy] Creating placeholder structure (no API key)")
        
        # Create a simple structure
        placeholder_content = f"""# Placeholder for Issue #{issue_num}
# Title: {issue_title}
# 
# Please implement based on the requirements:
#
"""
        for line in issue_body.split('\n'):
            placeholder_content += f"# {line}\n"
        
        # Save as README instead of code
        readme_path = os.path.join(generated_code_dir, 'README.md')
        with open(readme_path, 'w', encoding='utf-8') as f:
            f.write(placeholder_content)
        
        generated_files.append(readme_path)
        generation_method = "Placeholder"
        code_generated = True
        print(f"  ✅ Created placeholder: README.md")
    
    # Git operations
    if not generated_files:
        print("\n❌ Error: No files to commit")
        sys.exit(1)
    
    print(f"\n📦 Total files: {len(generated_files)}")
    
    # Git setup
    branch_name = f"auto/issue-{issue_num}-{int(time.time())}"
    run('git config user.name "github-actions[bot]"')
    run('git config user.email "github-actions[bot]@users.noreply.github.com"')
    run(f'git fetch origin {args.base}')
    run(f'git checkout -b {branch_name} origin/{args.base} 2>/dev/null || git checkout -b {branch_name}')
    
    # Add files
    run(f'git add {generated_code_dir}')
    
    # Commit
    commit_msg = f'auto-impl: issue #{issue_num} - {generation_method}'
    run(f'git commit -m "{commit_msg}" || git commit --allow-empty -m "{commit_msg}"')
    
    # Push
    run(f'git push -u origin {branch_name}')
    
    # Create PR
    pr_title = f"Auto-implementation for issue #{issue_num}"
    pr_body = f"This PR implements issue #{issue_num}.\n\n**Method:** {generation_method}\n\n**Generated Files:**\n"
    for f in generated_files:
        rel_path = os.path.relpath(f, generated_code_dir)
        pr_body += f"- `{rel_path}`\n"
    
    pr_url = f"{GITHUB_API}/repos/{owner}/{repo}/pulls"
    payload = {'title': pr_title, 'head': branch_name, 'base': args.base, 'body': pr_body}
    r = requests.post(pr_url, headers=headers, json=payload)
    
    if r.ok:
        pr = r.json()
        print(f"\n✅ Created PR #{pr['number']}: {pr['html_url']}")
        
        # Comment on issue
        comment_url = f"{GITHUB_API}/repos/{owner}/{repo}/issues/{issue_num}/comments"
        requests.post(comment_url, headers=headers, json={'body': f"🤖 PR created: {pr['html_url']}"})
    else:
        print(f"\n❌ Failed to create PR: {r.status_code}")
        sys.exit(1)

if __name__ == '__main__':
    main()
