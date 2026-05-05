#!/usr/bin/env python3
"""
Wrapper to locate issue_to_branch.py in repo and forward arguments.
This ensures that downstream GitHub Actions workflows can invoke the root-scope script
even if the workflow is executed from a subdirectory like demo/.
"""
import os
import sys
import subprocess

def main():
    # Potential locations of the real script
    candidates = [
        'issue_to_branch.py',               # repo root
        'demo/issue_to_branch.py',          # alternative location
        'demo/demo/issue_to_branch.py',     # another possibility
    ]
    script = None
    for c in candidates:
        if os.path.isfile(os.path.join(os.getcwd(), c)):
            script = c
            break
    if script is None:
        print('Error: could not locate issue_to_branch.py in any known location: ', candidates)
        sys.exit(1)

    # Build the command: call the real script with all original arguments.
    cmd = [sys.executable, script] + sys.argv[1:]
    print(f'Running: {" ".join(cmd)}')
    return subprocess.call(cmd)

if __name__ == '__main__':
    sys.exit(main())
