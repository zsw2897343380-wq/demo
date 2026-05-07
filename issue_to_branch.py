#!/usr/bin/env python3
"""
Issue-to-Branch Automation Script

This script performs end-to-end automation for turning an Issue into a code implementation:
- Reads the issue body from a given issue number
- Optionally calls an external code generator (OpenCode) to produce implementation code
- Creates a new feature branch, commits generated code, pushes the branch, and opens a PR to the base branch
- Comments on the issue with the PR URL for traceability
Usage:
- python3 issue_to_branch.py --issue-number <N> --repo owner/repo --base <branch> --token <token> [--opencode-key <key>]
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
        'Issue-to-Branch Automation: reads an issue, generates implementation code, creates a branch, opens a PR.'
    ))
    p.add_argument('--issue-number', required=True, dest='issue_number', help='Issue number to process')
    p.add_argument('--repo', required=True, help='Owner/Repo, e.g. octo-org/sample-repo')
    p.add_argument('--base', default='main', help='Base branch to merge into (default: main)')
    p.add_argument('--token', required=True, help='GitHub token for authentication')
    p.add_argument('--opencode-key', required=False, help='OpenCode API key (optional)')
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
    
    # Track if we successfully generated code
    code_generated = False
    
    # Try to use the Node.js code generator first
    node_script_paths = [
        'scripts/opencode_generate.js',
        'opencode_generate.js',
    ]
    
    for node_script in node_script_paths:
        if os.path.isfile(node_script):
            print(f"[Code Generator] Found Node.js script: {node_script}")
            try:
                sdk_cmd = f"node {node_script} --issue-number {issue_num} --issue-body {shlex.quote(issue_body)} --outdir {shlex.quote(generated_code_dir)}"
                result = subprocess.run(sdk_cmd, shell=True, capture_output=True, text=True)
                print(f"Node.js generator output: {result.stdout}")
                if result.stderr:
                    print(f"Node.js generator stderr: {result.stderr}")
                
                # Check if the output file was created
                candidate = os.path.join(generated_code_dir, 'generated_by_sdk.txt')
                if os.path.exists(candidate):
                    with open(candidate, 'r') as f_in, open(os.path.join(generated_code_dir, 'auto_generated_code.txt'), 'w') as f_out:
                        f_out.write(f_in.read())
                    code_generated = True
                    print("[Code Generator] Successfully generated code via Node.js script.")
                    break
            except Exception as e:
                print(f"[Code Generator] Node.js script failed: {e}")
                continue
    
    # If Node.js generator didn't work, generate code directly in Python
    if not code_generated:
        print("[Code Generator] Using Python fallback for code generation.")
        try:
            # Generate code based on issue content
            code_content = f"""# Auto-generated implementation for Issue #{issue_num}
# Title: {issue_title}
# Generated at: {time.strftime('%Y-%m-%d %H:%M:%S')}

"""
            # Add the issue body as comments
            code_content += f"""# Issue Requirements:
"""
            for line in issue_body.split('\n'):
                code_content += f"# {line}\n"
            
            code_content += f"""
# Placeholder implementation
def implement_issue_{issue_num}():
    \"\"\"Implementation for issue #{issue_num}: {issue_title}\"\"\"
    # TODO: Implement the feature based on the above requirements
    raise NotImplementedError("Please implement based on issue requirements")
"""
            
            output_file = os.path.join(generated_code_dir, 'auto_generated_code.txt')
            with open(output_file, 'w', encoding='utf-8') as f:
                f.write(code_content)
            code_generated = True
            print(f"[Code Generator] Generated placeholder code: {output_file}")
        except Exception as e:
            print(f"[Code Generator] Python fallback failed: {e}")
    
    # Try OpenCode API if key is provided
    api_key = os.environ.get('OPENCODE_API_KEY') or args.opencode_key
    if api_key and not code_generated:
        print("[Code Generator] Attempting to use OpenCode API...")
        # Use a configurable API URL or default to None (disabled by default)
        opencode_url = os.environ.get('OPENCODE_API_URL', '')
        if opencode_url and opencode_url != 'https://api.opencode.example/generate':
            try:
                payload = {
                    'repository': f"{owner}/{repo}",
                    'issue_number': issue_num,
                    'requirements': issue_body,
                }
                headers_op = {'Authorization': f'Bearer {api_key}'}
                resp = requests.post(opencode_url, json=payload, headers=headers_op, timeout=60)
                resp.raise_for_status()
                content_type = resp.headers.get('Content-Type', '')
                if content_type.startswith('application/zip') or content_type.startswith('application/octet-stream'):
                    with open(os.path.join(generated_code_dir, 'auto_generated_code.zip'), 'wb') as f:
                        f.write(resp.content)
                elif content_type.startswith('application/json'):
                    data = resp.json()
                    if isinstance(data, dict) and 'code' in data:
                        with open(os.path.join(generated_code_dir, 'auto_generated_code.txt'), 'w') as f:
                            f.write(data['code'] or '')
                    else:
                        with open(os.path.join(generated_code_dir, 'auto_generated_code.json'), 'w') as f:
                            json.dump(data, f, indent=2)
                else:
                    with open(os.path.join(generated_code_dir, 'auto_generated_code.txt'), 'wb') as f:
                        f.write(resp.content)
                code_generated = True
                print("[Code Generator] Successfully generated code via OpenCode API.")
            except Exception as e:
                print(f"[Code Generator] OpenCode API failed: {e}")
        else:
            print("[Code Generator] OpenCode API URL not configured or using placeholder. Skipping API call.")

    # Ensure we have at least a placeholder file
    placeholder_file = os.path.join(generated_code_dir, 'auto_generated_code.txt')
    if not os.path.exists(placeholder_file):
        print("[Code Generator] Creating fallback placeholder file.")
        with open(placeholder_file, 'w', encoding='utf-8') as f:
            f.write(f"""# Placeholder generated code for Issue #{issue_num}
# Title: {issue_title}
# 
# No external code generator was available.
# Please implement based on the issue requirements.

# Issue body:
""")
            for line in issue_body.split('\n'):
                f.write(f"# {line}\n")
            f.write(f"""
def placeholder_implementation():
    raise NotImplementedError("Implement based on issue #{issue_num} requirements")
""")

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
    commit_message = f'auto-impl: issue #{issue_num} - generate implementation'
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

**Generated Files:**
"""
    for f in generated_files:
        pr_body += f"- `{f}`\n"
    
    pr_body += f"""
**Note:** This is an automated implementation. Please review and modify as needed before merging.
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
        print(f"Created PR #{pr_number}: {pr_html_url}")
        # Comment on the issue with the PR URL for traceability
        issues_comment_url = f"{GITHUB_API}/repos/{owner}/{repo}/issues/{issue_num}/comments"
        comment_payload = {'body': f"Automated PR created: {pr_html_url}"}
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
