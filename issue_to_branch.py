#!/usr/bin/env python3
"""
Issue-to-Branch Automation Script with Multiple Code Generation Strategies

Strategies (in order of preference):
1. DeepSeek API (if DEEPSEEK_API_KEY available) - Great for code, cost-effective
2. OpenAI/Anthropic API (if API key available) - Best AI quality
3. OpenCode SDK (if installed) - Good for local OpenCode users
4. Pure Node.js (fallback) - Always works, template-based

Usage:
- python3 issue_to_branch.py --issue-number <N> --repo owner/repo --base <branch> --token <token> [--opencode-key <key>]

Environment Variables:
- DEEPSEEK_API_KEY: For DeepSeek API code generation (recommended for Chinese users)
- OPENAI_API_KEY: For OpenAI GPT code generation
- ANTHROPIC_API_KEY: For Anthropic Claude code generation
- OPENCODE_API_KEY: Legacy, kept for compatibility
"""
import argparse
import shlex
import json
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
    p = argparse.ArgumentParser(description=(
        'Issue-to-Branch Automation: reads an issue, generates implementation code using AI or templates, creates a branch, opens a PR.'
    ))
    p.add_argument('--issue-number', required=True, dest='issue_number', help='Issue number to process')
    p.add_argument('--repo', required=True, help='Owner/Repo, e.g. octo-org/sample-repo')
    p.add_argument('--base', default='main', help='Base branch to merge into (default: main)')
    p.add_argument('--token', required=True, help='GitHub token for authentication')
    p.add_argument('--opencode-key', required=False, help='OpenCode API key (optional, legacy)')
    p.add_argument('--strategy', required=False, choices=['auto', 'deepseek', 'openai', 'anthropic', 'opencode', 'template'], 
                   default='auto', help='Code generation strategy (default: auto)')
    args = p.parse_args()

    GITHUB_API = 'https://api.github.com'
    headers = {
        'Authorization': f'token {args.token}',
        'Accept': 'application/vnd.github+json'
    }

    owner, repo = args.repo.split('/')
    
    # Validate issue_number input
    if args.issue_number is None or str(args.issue_number).strip() == "":
        print("Error: --issue-number must be provided and non-empty.")
        sys.exit(2)
    try:
        issue_num = int(args.issue_number)
    except (ValueError, TypeError):
        print(f"Error: Invalid --issue-number value '{args.issue_number}'. Must be a positive integer.")
        sys.exit(2)
    if issue_num <= 0:
        print(f"Error: Invalid --issue-number value '{args.issue_number}'. Must be a positive integer.")
        sys.exit(2)

    # 1) Pre-check repository accessibility
    repo_url = f"{GITHUB_API}/repos/{owner}/{repo}"
    r_repo = requests.get(repo_url, headers=headers)
    if not r_repo.ok:
        print(f"Error: Unable to access repository {owner}/{repo}. Status: {r_repo.status_code}. Response: {r_repo.text}")
        sys.exit(2)

    # 1) Fetch issue body
    issue_url = f"{GITHUB_API}/repos/{owner}/{repo}/issues/{issue_num}"
    r = requests.get(issue_url, headers=headers)
    try:
        r.raise_for_status()
    except requests.HTTPError as e:
        if r.status_code == 404:
            print(f"Error: Issue #{issue_num} not found in {owner}/{repo}. Check that the issue exists and is accessible. (HTTP 404)")
            sys.exit(2)
        else:
            print(f"HTTP error when fetching issue: {e}")
            sys.exit(1)
    issue = r.json()
    issue_body = issue.get('body', '') or ''
    issue_title = issue.get('title', f'Issue #{issue_num}')
    print(f"Fetched issue #{issue_num}: {len(issue_body)} chars in body")
    print(f"Issue title: {issue_title}")

    # Prepare output directory for generated code
    generated_code_dir = f"auto_impl/issue-{issue_num}"
    os.makedirs(generated_code_dir, exist_ok=True)
    
    # Determine which strategy to use
    strategy = args.strategy
    deepseek_key = os.environ.get('DEEPSEEK_API_KEY')
    openai_key = os.environ.get('OPENAI_API_KEY')
    anthropic_key = os.environ.get('ANTHROPIC_API_KEY')
    opencode_key = os.environ.get('OPENCODE_API_KEY') or args.opencode_key
    
    # Auto-select strategy based on available keys (DeepSeek first for code generation)
    if strategy == 'auto':
        if deepseek_key:
            strategy = 'deepseek'
            print(f"[Strategy] Auto-selected: DEEPSEEK API (API key found)")
        elif openai_key or anthropic_key:
            strategy = 'openai' if openai_key else 'anthropic'
            print(f"[Strategy] Auto-selected: {strategy.upper()} API (API key found)")
        else:
            strategy = 'template'
            print("[Strategy] Auto-selected: TEMPLATE (no API keys found)")
    else:
        print(f"[Strategy] User selected: {strategy.upper()}")

    code_generated = False
    generation_method = ""

    # Strategy 1: DeepSeek API (优先，对中文友好且性价比高)
    if strategy == 'deepseek' and deepseek_key:
        print("[DeepSeek] Attempting to use DeepSeek API...")
        node_script = 'scripts/opencode_generate_with_deepseek.js'
        if os.path.isfile(node_script):
            try:
                env_vars = f"DEEPSEEK_API_KEY={shlex.quote(deepseek_key)}"
                sdk_cmd = f"{env_vars} node {node_script} --issue-number {issue_num} --issue-body {shlex.quote(issue_body)} --outdir {shlex.quote(generated_code_dir)} --model coder"
                result = subprocess.run(sdk_cmd, shell=True, capture_output=True, text=True)
                print(f"DeepSeek stdout: {result.stdout}")
                if result.stderr:
                    print(f"DeepSeek stderr: {result.stderr}")
                
                # Check for output file
                candidate = os.path.join(generated_code_dir, 'generated_by_deepseek.txt')
                if os.path.exists(candidate):
                    with open(candidate, 'r', encoding='utf-8') as f_in:
                        content = f_in.read()
                    if content and len(content) > 50 and not content.startswith('# DeepSeek generation failed'):
                        with open(os.path.join(generated_code_dir, 'auto_generated_code.txt'), 'w', encoding='utf-8') as f_out:
                            f_out.write(content)
                        code_generated = True
                        generation_method = "DeepSeek API (deepseek-coder)"
                        print("[DeepSeek] ✅ Successfully generated code via DeepSeek API.")
            except Exception as e:
                print(f"[DeepSeek] ❌ Failed: {e}")
        else:
            print(f"[DeepSeek] ⚠️ Script not found: {node_script}")

    # Strategy 2: OpenAI/Anthropic API
    if not code_generated and strategy in ['openai', 'anthropic'] and (openai_key or anthropic_key):
        print(f"[AI Generation] Attempting to use {strategy.upper()} API...")
        node_script = 'scripts/opencode_generate_with_ai_api.js'
        if os.path.isfile(node_script):
            try:
                env_vars = f"OPENAI_API_KEY={shlex.quote(openai_key or '')} ANTHROPIC_API_KEY={shlex.quote(anthropic_key or '')}"
                sdk_cmd = f"{env_vars} node {node_script} --issue-number {issue_num} --issue-body {shlex.quote(issue_body)} --outdir {shlex.quote(generated_code_dir)}"
                result = subprocess.run(sdk_cmd, shell=True, capture_output=True, text=True)
                print(f"AI API stdout: {result.stdout}")
                if result.stderr:
                    print(f"AI API stderr: {result.stderr}")
                
                # Check for output file
                candidate = os.path.join(generated_code_dir, 'generated_by_ai.txt')
                if os.path.exists(candidate):
                    with open(candidate, 'r', encoding='utf-8') as f_in:
                        content = f_in.read()
                    if content and len(content) > 50 and not content.startswith('# AI generation failed'):
                        with open(os.path.join(generated_code_dir, 'auto_generated_code.txt'), 'w', encoding='utf-8') as f_out:
                            f_out.write(content)
                        code_generated = True
                        generation_method = f"{strategy.upper()} API"
                        print(f"[AI Generation] Successfully generated code via {strategy.upper()} API.")
            except Exception as e:
                print(f"[AI Generation] {strategy.upper()} API failed: {e}")
        else:
            print(f"[AI Generation] Script not found: {node_script}")

    # Strategy 2: OpenCode SDK (if selected or as fallback)
    if not code_generated and strategy == 'opencode':
        print("[OpenCode SDK] Attempting to use OpenCode SDK...")
        node_script = 'scripts/opencode_generate_with_sdk.js'
        if os.path.isfile(node_script):
            try:
                sdk_cmd = f"node {node_script} --issue-number {issue_num} --issue-body {shlex.quote(issue_body)} --outdir {shlex.quote(generated_code_dir)}"
                result = subprocess.run(sdk_cmd, shell=True, capture_output=True, text=True)
                print(f"OpenCode SDK stdout: {result.stdout}")
                if result.stderr:
                    print(f"OpenCode SDK stderr: {result.stderr}")
                
                candidate = os.path.join(generated_code_dir, 'generated_by_sdk.txt')
                if os.path.exists(candidate):
                    with open(candidate, 'r', encoding='utf-8') as f_in:
                        content = f_in.read()
                    if content and len(content) > 50 and not content.startswith('# OpenCode SDK generation failed'):
                        with open(os.path.join(generated_code_dir, 'auto_generated_code.txt'), 'w', encoding='utf-8') as f_out:
                            f_out.write(content)
                        code_generated = True
                        generation_method = "OpenCode SDK"
                        print("[OpenCode SDK] Successfully generated code via SDK.")
            except Exception as e:
                print(f"[OpenCode SDK] Failed: {e}")
        else:
            print(f"[OpenCode SDK] Script not found: {node_script}")

    # Strategy 3: Template-based (fallback)
    if not code_generated:
        print("[Template] Using Node.js template generator as fallback...")
        node_script = 'scripts/opencode_generate.js'
        if os.path.isfile(node_script):
            try:
                sdk_cmd = f"node {node_script} --issue-number {issue_num} --issue-body {shlex.quote(issue_body)} --outdir {shlex.quote(generated_code_dir)}"
                result = subprocess.run(sdk_cmd, shell=True, capture_output=True, text=True)
                print(f"Template generator stdout: {result.stdout}")
                if result.stderr:
                    print(f"Template generator stderr: {result.stderr}")
                
                candidate = os.path.join(generated_code_dir, 'generated_by_sdk.txt')
                if os.path.exists(candidate):
                    with open(candidate, 'r', encoding='utf-8') as f_in:
                        content = f_in.read()
                    with open(os.path.join(generated_code_dir, 'auto_generated_code.txt'), 'w', encoding='utf-8') as f_out:
                        f_out.write(content)
                    code_generated = True
                    generation_method = "Template (Node.js)"
                    print("[Template] Successfully generated code via template.")
            except Exception as e:
                print(f"[Template] Node.js template failed: {e}")
        
        # Ultimate fallback: Python template generator
        if not code_generated:
            print("[Template] Using Python fallback generator...")
            try:
                code_content = f"""# Auto-generated implementation for Issue #{issue_num}
# Title: {issue_title}
# Generation Method: Python Template Fallback
# Generated at: {time.strftime('%Y-%m-%d %H:%M:%S')}

# Issue Requirements:
"""
                for line in issue_body.split('\n'):
                    code_content += f"# {line}\n"
                
                code_content += f"""
# Placeholder implementation
def implement_issue_{issue_num}():
    \"\"\"Implementation for issue #{issue_num}: {issue_title}\"\"\"
    # TODO: Implement the feature based on the above requirements
    raise NotImplementedError("Please implement based on issue requirements")

# Example usage:
if __name__ == "__main__":
    try:
        result = implement_issue_{issue_num}()
        print(f"Result: {{result}}")
    except NotImplementedError as e:
        print(f"Not implemented yet: {{e}}")
"""
                
                output_file = os.path.join(generated_code_dir, 'auto_generated_code.txt')
                with open(output_file, 'w', encoding='utf-8') as f:
                    f.write(code_content)
                code_generated = True
                generation_method = "Template (Python)"
                print(f"[Template] Generated placeholder code: {output_file}")
            except Exception as e:
                print(f"[Template] Python fallback failed: {e}")

    # Verify files exist before committing
    generated_files = []
    if os.path.exists(generated_code_dir):
        for f in os.listdir(generated_code_dir):
            file_path = os.path.join(generated_code_dir, f)
            if os.path.isfile(file_path):
                generated_files.append(file_path)
                print(f"[Verify] Found generated file: {file_path}")
    
    if not generated_files:
        print("Error: No files were generated. Cannot create PR.")
        sys.exit(1)

    # 3) Prepare git: create a new branch, commit and push
    branch_name = f"auto/issue-{issue_num}-{int(time.time())}"
    
    # Ensure git user config exists
    run('git config user.name "github-actions[bot]"', check=False)
    run('git config user.email "github-actions[bot]@users.noreply.github.com"', check=False)

    # Fetch latest changes and create branch from base
    run(f'git fetch origin {args.base}', check=False)
    
    # Create new branch from base
    checkout_result = run(
        f'git checkout -b {branch_name} origin/{args.base} 2>/dev/null || git checkout -b {branch_name}',
        check=False,
        capture_output=True
    )
    print(f"Branch creation result: {checkout_result}")

    # Add generated files
    add_result = run(f'git add -A {generated_code_dir}', check=False, capture_output=True)
    print(f"Git add result: {add_result}")
    
    # Check if there are staged changes
    status_output = run('git status --porcelain', capture_output=True)
    print(f"Git status: {status_output}")
    
    if not status_output:
        print("Warning: No changes to commit. Adding files explicitly...")
        # Force add all files in the directory
        for f in generated_files:
            run(f'git add -f "{f}"', check=False)
        status_output = run('git status --porcelain', capture_output=True)
        print(f"Git status after force add: {status_output}")
    
    # Commit with explicit file list if needed
    commit_message = f'auto-impl: issue #{issue_num} - generate implementation [{generation_method or "fallback"}]'
    if status_output:
        commit_result = run(f'git commit -m "{commit_message}"', check=False, capture_output=True)
        print(f"Git commit result: {commit_result}")
    else:
        # Create an empty commit if somehow there are no changes
        commit_result = run(f'git commit --allow-empty -m "{commit_message}"', check=False, capture_output=True)
        print(f"Git commit (empty) result: {commit_result}")
    
    # Push branch
    push_result = run(f'git push -u origin {branch_name}', check=False, capture_output=True)
    print(f"Git push result: {push_result}")

    # 4) Create a PR from branch to base
    pr_title = f"Auto-implementation for issue #{issue_num}"
    pr_body = f"""This PR was auto-generated to implement issue #{issue_num}.

**Issue Title:** {issue_title}

**Generation Method:** {generation_method or "Fallback Template"}

**Generated Files:**
"""
    for f in generated_files:
        pr_body += f"- `{f}`\n"
    
    pr_body += f"""
**Note:** This is an automated implementation. Please review and modify as needed before merging.

---
*Generated by GitHub Actions* ⭐
"""

    pr_url = f"{GITHUB_API}/repos/{owner}/{repo}/pulls"
    payload = {
        'title': pr_title,
        'head': branch_name,
        'base': args.base,
        'body': pr_body,
    }
    r = requests.post(pr_url, headers=headers, json=payload)
    if r.ok:
        pr = r.json()
        pr_number = pr.get('number')
        pr_html_url = pr.get('html_url')
        print(f"✅ Created PR #{pr_number}: {pr_html_url}")
        # Comment on the issue with the PR URL for traceability
        issues_comment_url = f"{GITHUB_API}/repos/{owner}/{repo}/issues/{issue_num}/comments"
        comment_payload = {'body': f"🤖 Automated PR created: {pr_html_url}\n\nGeneration method: {generation_method or 'Template'}"}
        requests.post(issues_comment_url, headers=headers, json=comment_payload)
        # Persist PR URL for downstream steps if needed
        print(f"PR URL: {pr_html_url}")
        with open('/tmp/pr_url.txt', 'w') as f:
            f.write(pr_html_url)
    else:
        if r.status_code == 403:
            print("Error: GitHub Actions is not permitted to create pull requests with the provided token.")
            print("Solution: use a Personal Access Token with repo scope or enable workflow PR creation in repository settings.")
            print("PR was not created. You can create it manually using the URL shown in logs or by visiting the PR UI.")
            sys.exit(3)
        if r.status_code == 422:
            print("PR creation failed: validation failed. Check that the base branch exists and that the head branch was successfully pushed to origin.")
            print(f"Details: {r.text}")
            print(f"Branch attempted: {branch_name}")
            print(f"Base branch: {args.base}")
            sys.exit(4)
        print(f"Failed to create PR: {r.status_code} {r.text}")
        sys.exit(1)

if __name__ == '__main__':
    main()
