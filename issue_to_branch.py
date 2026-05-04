#!/usr/bin/env python3
- Issue to Branch Automation Script
This script performs end-to-end automation for turning an Issue into a code implementation:
- Reads the issue body from a given issue number.
- Optionally calls an external code generator (OpenCode) to produce implementation code.
- Creates a new feature branch, commits generated code, pushes the branch, and opens a PR to the base branch.
- Comments on the issue with the PR URL for traceability.
"""
"""Issue-to-Branch Automation Script

This script provides a practical, end-to-end workflow:
- Input: issue_number, repo, base, token, opencode-key
- Output: creates a new branch, commits generated code, pushes the branch, opens a PR, and comments back with the PR URL.

Usage:
- python3 issue_to_branch.py --issue-number <N> --repo owner/repo --base <branch> --token <token> [--opencode-key <key>]

Key behaviors and guarantees
- If OpenCode API is unavailable, the script writes a placeholder implementation to ensure the pipeline continues and you can verify flow.
- The script uses a deterministic naming scheme for branches and stores generated code under auto_impl/issue-<N>/<files>.
- PR creation is performed via GitHub REST API using the provided token.
- On success, the PR URL is written to /tmp/pr_url.txt for downstream steps to reference.
"""
- Fetches the issue body for a given issue number
- Sends the issue content to an external code generator (OpenCode) to produce code
- Creates a new feature branch, commits generated code, pushes branch
- Creates a PR from the new branch to the base branch (default: develop)
- Comments on the issue with the PR URL
"""
import argparse
import json
import os
import subprocess
import sys
import time
from urllib import request

import requests

def run(cmd, check=False, capture_output=False):
    """Execute a shell command.

    Args:
      cmd: Command string to execute.
      check: If True, raise CalledProcessError on non-zero exit.
      capture_output: If True, return stdout as string; otherwise return empty string.
    Returns:
      Captured stdout (str) when capture_output is True, else empty string.
    """
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
    p.add_argument('--base', default='develop', help='Base branch to merge into (default: develop)')
    p.add_argument('--token', required=True, help='GitHub token for authentication')
    p.add_argument('--opencode-key', required=False, help='OpenCode API key (optional)')
    args = p.parse_args()

    GITHUB_API = 'https://api.github.com'
    headers = {
        'Authorization': f'token {args.token}',
        'Accept': 'application/vnd.github+json'
    }

    owner, repo = args.repo.split('/')
    issue_num = int(args.issue_number)

    # 1) Fetch issue body
    issue_url = f"{GITHUB_API}/repos/{owner}/{repo}/issues/{issue_num}"
    r = requests.get(issue_url, headers=headers)
    r.raise_for_status()
    issue = r.json()
    issue_body = issue.get('body', '') or ''
    print(f"Fetched issue #{issue_num}: {len(issue_body)} chars in body")

    # 2) Call OpenCode API to generate implementation (placeholder)
    generated_code_dir = f"auto_impl/issue-{issue_num}"
    os.makedirs(generated_code_dir, exist_ok=True)

    # Simple placeholder: if opencode-key provided, call API; otherwise, create a dummy file
    if os.environ.get('OPENCODE_API_KEY') or args.opencode_key:
        opencode_url = os.environ.get('OPENCODE_API_URL', 'https://api.opencode.example/generate')
        payload = {
            'repository': f"{owner}/{repo}",
            'issue_number': issue_num,
            'requirements': issue_body,
        }
        if os.environ.get('OPENCODE_API_KEY'):
            headers_op = {'Authorization': f'Bearer {os.environ["OPENCODE_API_KEY"]}'}
        else:
            headers_op = {}
        try:
            resp = requests.post(opencode_url, json=payload, headers=headers_op, timeout=60)
            resp.raise_for_status()
            codezip = resp.content
            # In a real scenario, unzip and place into generated_code_dir. Here we mock a file.
            with open(os.path.join(generated_code_dir, 'auto_generated_code.txt'), 'wb') as f:
                f.write(codezip or b'generated-code-placeholder')
        except Exception as e:
            print(f"OpenCode API failed: {e}")
            # Fallback: create a placeholder file
            with open(os.path.join(generated_code_dir, 'auto_generated_code.txt'), 'w') as f:
                f.write('# placeholder generated code due to generator failure\n')
    else:
        with open(os.path.join(generated_code_dir, 'auto_generated_code.txt'), 'w') as f:
            f.write('# placeholder generated code (no OpenCode API key provided)\n')

    # 3) Prepare git: create a new branch, commit and push
    branch_name = f"auto/issue-{issue_num}-{int(time.time())}"
    # Ensure git user config exists
    run('git config user.name "github-actions[bot]"', check=False)
    run('git config user.email "github-actions[bot]@users.noreply.github.com"', check=False)

    # Create new branch from base (default develop) and switch to it
    run(f'git checkout -b {branch_name} origin/{args.base}', check=False)
    # Add generated files
    run(f'git add -A {generated_code_dir}', check=False)
    commit_message = f'auto-impl: issue #{issue_num} - generate implementation'
    # Commit the changes (only if there are staged changes)
    run(f'git commit -m "{commit_message}"', check=False)
    # Push branch
    run(f'git push -u origin {branch_name}', check=False)

    # 4) Create a PR from branch to base (develop by default)
    pr_title = f"Auto-implementation for issue #{issue_num}"
    pr_body = f"This PR auto-generated by harness to implement issue #{issue_num}."
    pr_url = f"{GITHUB_API}/repos/{owner}/{repo}/pulls"
    payload = {
        'title': pr_title,
        'head': branch_name,
        'base': args.base if args.base else 'develop',
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
        print(f"Failed to create PR: {r.status_code} {r.text}")
        sys.exit(1)

if __name__ == '__main__':
    main()
