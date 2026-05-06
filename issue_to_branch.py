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
    print(f"Fetched issue #{issue_num}: {len(issue_body)} chars in body")

    # Prepare output directory for generated code and flag for SDK success
    generated_code_dir = f"auto_impl/issue-{issue_num}"
    sdk_generated = False
    # OpenCode SDK path by default (disable only if explicitly overridden)
    use_sdk = os.environ.get('USE_OPENCODE_SDK', 'true').lower() in ('1', 'true', 'yes')
    if use_sdk:
        print("[OpenCode SDK] Using local OpenCode SDK path for code generation (default).")
        try:
            sdk_outdir = generated_code_dir
            sdk_cmd = f"node scripts/opencode_generate.js --issue-number {issue_num} --issue-body {shlex.quote(issue_body)} --outdir {shlex.quote(sdk_outdir)}"
            run(sdk_cmd, check=False)
            candidate = os.path.join(sdk_outdir, 'generated_by_sdk.txt')
            if os.path.exists(candidate):
                with open(candidate, 'r') as f_in, open(os.path.join(generated_code_dir, 'auto_generated_code.txt'), 'w') as f_out:
                    f_out.write(f_in.read())
                sdk_generated = True
                print("[OpenCode SDK] Successfully generated code via SDK; auto_generated_code.txt overwritten.")
        except Exception:
            pass

    # 2) Call OpenCode API to generate implementation (preferred path) or fall back to placeholder
    generated_code_dir = f"auto_impl/issue-{issue_num}"
    os.makedirs(generated_code_dir, exist_ok=True)
    api_key = os.environ.get('OPENCODE_API_KEY') or (args.opencode_key)
    opencode_url = os.environ.get('OPENCODE_API_URL', 'https://api.opencode.example/generate')
    payload = {
        'repository': f"{owner}/{repo}",
        'issue_number': issue_num,
        'requirements': issue_body,
    }
    headers_op = {'Authorization': f'Bearer {api_key}'} if api_key else {}
    if api_key and not sdk_generated:
        try:
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
        except Exception as e:
            print(f"OpenCode API failed: {e}")
            with open(os.path.join(generated_code_dir, 'auto_generated_code.txt'), 'w') as f:
                f.write('# placeholder generated code due to generator failure\n')
    else:
        if not sdk_generated:
            with open(os.path.join(generated_code_dir, 'auto_generated_code.txt'), 'w') as f:
                f.write('# placeholder generated code (no OpenCode API key provided)\n')
            # Lightweight skeleton to help local testing when no API key is provided
            try:
                with open(os.path.join(generated_code_dir, 'auto_generated_code.py'), 'w') as f_py:
                    f_py.write('"""Placeholder skeleton for issue #{0} implementation."""\n\n'.format(issue_num))
                    f_py.write('def implement_issue():\n')
                    f_py.write('    """Auto-generated placeholder function."""\n')
                    f_py.write('    raise NotImplementedError("OpenCode generator unavailable; replace with real implementation.")\n')
            except Exception:
                pass
        else:
            print("[OpenCode SDK] Code generated; skipping REST API placeholder.")

    # 3) Prepare git: create a new branch, commit and push
    branch_name = f"auto/issue-{issue_num}-{int(time.time())}"
    # Ensure git user config exists
    run('git config user.name "github-actions[bot]"', check=False)
    run('git config user.email "github-actions[bot]@users.noreply.github.com"', check=False)

    # Create new branch from base (default develop) and switch to it with robust fallback
    run(
        'BRANCH_OK=$(git show-ref --verify --quiet "refs/remotes/origin/' + f'{args.base}' + '"; echo $?); '
        'if [ "${BRANCH_OK}" -eq 0 ]; then '
        f'git checkout -b {branch_name} origin/{args.base}; '
        'else '
        f'git checkout -b {branch_name}; '
        'fi',
        check=False
    )
    # Add generated files
    run(f'git add -A {generated_code_dir}', check=False)
    commit_message = f'auto-impl: issue #{issue_num} - generate implementation'
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
        if r.status_code == 403:
            print("Error: GitHub Actions is not permitted to create pull requests with the provided token.")
            print("Solution: use a Personal Access Token with repo scope or enable workflow PR creation in repository settings.")
            print("PR was not created. You can create it manually using the URL shown in logs or by visiting the PR UI.")
            sys.exit(3)
        if r.status_code == 422:
            print("PR creation failed: validation failed. Check that the base branch exists and that the head branch was successfully pushed to origin.")
            print(f"Details: {r.text}")
            sys.exit(4)
        print(f"Failed to create PR: {r.status_code} {r.text}")
        sys.exit(1)

if __name__ == '__main__':
    main()
