#!/usr/bin/env python3
"""
Issue-to-Branch Automation Script (v1 - Simplified)

This script performs end-to-end automation for turning an Issue into a code implementation:
- Reads the issue body from a given issue number
- Generates placeholder implementation code
- Creates a new feature branch, commits generated code, pushes the branch, and opens a PR to the base branch
Usage:
- python3 issue_to_branch_v1.py --issue-number <N> --repo owner/repo --base <branch> --token <token>
"""
import argparse
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
    args = p.parse_args()

    GITHUB_API = 'https://api.github.com'
    headers = {
        'Authorization': f'token {args.token}',
        'Accept': 'application/vnd.github+json'
    }

    owner, repo = args.repo.split('/')
    
    try:
        issue_num = int(args.issue_number)
    except (ValueError, TypeError):
        print(f"Error: Invalid --issue-number value '{args.issue_number}'. Must be a positive integer.")
        sys.exit(2)
    
    if issue_num <= 0:
        print(f"Error: Invalid --issue-number value '{args.issue_number}'. Must be a positive integer.")
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

    # 2) Generate placeholder implementation
    generated_code_dir = f"auto_impl/issue-{issue_num}"
    os.makedirs(generated_code_dir, exist_ok=True)

    # Generate placeholder code
    code_content = f"""# Auto-generated implementation for Issue #{issue_num}
# Title: {issue_title}
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
"""
    
    output_file = os.path.join(generated_code_dir, 'auto_generated_code.txt')
    with open(output_file, 'w', encoding='utf-8') as f:
        f.write(code_content)
    print(f"Generated placeholder code: {output_file}")

    # 3) Prepare git: create a new branch, commit and push
    branch_name = f"auto/issue-{issue_num}-{int(time.time())}"
    
    # Ensure git user config exists
    run('git config user.name "github-actions[bot]"', check=False)
    run('git config user.email "github-actions[bot]@users.noreply.github.com"', check=False)

    # Fetch and create branch
    run(f'git fetch origin {args.base}', check=False)
    
    checkout_result = run(
        f'git checkout -b {branch_name} origin/{args.base} 2>/dev/null || git checkout -b {branch_name}',
        check=False,
        capture_output=True
    )
    print(f"Branch creation result: {checkout_result}")
    
    # Add and commit
    run(f'git add -A {generated_code_dir}', check=False)
    
    # Check if there are changes to commit
    status_output = run('git status --porcelain', capture_output=True)
    if status_output:
        commit_message = f'auto-impl: issue #{issue_num} - generate implementation'
        run(f'git commit -m "{commit_message}"', check=False)
    else:
        # Empty commit
        commit_message = f'auto-impl: issue #{issue_num} - generate implementation'
        run(f'git commit --allow-empty -m "{commit_message}"', check=False)
    
    # Push
    push_result = run(f'git push -u origin {branch_name}', check=False, capture_output=True)
    print(f"Git push result: {push_result}")

    # 4) Create a PR
    pr_title = f"Auto-implementation for issue #{issue_num}"
    pr_body = f"""This PR was auto-generated to implement issue #{issue_num}.

**Issue Title:** {issue_title}

**Generated Files:**
- `{output_file}`

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
        # Comment on the issue
        issues_comment_url = f"{GITHUB_API}/repos/{owner}/{repo}/issues/{issue_num}/comments"
        comment_payload = {'body': f"Automated PR created: {pr_html_url}"}
        requests.post(issues_comment_url, headers=headers, json=comment_payload)
        print(f"PR URL: {pr_html_url}")
        with open('/tmp/pr_url.txt', 'w') as f:
            f.write(pr_html_url)
    else:
        if r.status_code == 422:
            print("PR creation failed: validation failed. Check that the base branch exists and that the head branch was successfully pushed to origin.")
            print(f"Details: {r.text}")
            sys.exit(4)
        print(f"Failed to create PR: {r.status_code} {r.text}")
        sys.exit(1)

if __name__ == '__main__':
    main()
