#!/usr/bin/env python3
"""
AI Code Review Script for Payment Gateway Service
Uses Alibaba Cloud Coding Plan API (OpenAI-compatible) with Qwen/Kimi/Minimax models
"""

import os
import json
import requests
import subprocess
from typing import List, Dict, Any, Optional
from dataclasses import dataclass
from enum import Enum


class ReviewType(Enum):
    BUG = "bug"
    QUALITY = "quality"
    SECURITY = "security"
    TESTS = "tests"


class Severity(Enum):
    CRITICAL = "CRITICAL"
    HIGH = "HIGH"
    MEDIUM = "MEDIUM"
    LOW = "LOW"


@dataclass
class Issue:
    file: str
    line: int
    severity: Severity
    category: str
    description: str
    suggestion: str


@dataclass
class FileDiff:
    filename: str
    status: str  # added, modified, deleted
    additions: List[str]
    deletions: List[str]
    diff_text: str


class AlibabaCloudClient:
    """Client for Alibaba Cloud Coding Plan API (OpenAI-compatible)"""

    def __init__(self, api_key: str, base_url: str, model: str):
        self.api_key = api_key
        self.base_url = base_url.rstrip("/")
        self.model = model
        self.headers = {
            "Authorization": f"Bearer {api_key}",
            "Content-Type": "application/json",
        }

    def chat(
        self, messages: List[Dict], max_tokens: int = 4000, temperature: float = 0.3
    ) -> str:
        """Send chat completion request"""
        url = f"{self.base_url}/chat/completions"

        payload = {
            "model": self.model,
            "messages": messages,
            "max_tokens": max_tokens,
            "temperature": temperature,
        }

        response = requests.post(url, headers=self.headers, json=payload, timeout=60)
        response.raise_for_status()

        result = response.json()
        return result["choices"][0]["message"]["content"]


class GitHubClient:
    """Client for GitHub API"""

    def __init__(self, token: str, repository: str, pr_number: int):
        self.token = token
        self.repository = repository
        self.pr_number = pr_number
        self.headers = {
            "Authorization": f"token {token}",
            "Accept": "application/vnd.github.v3+json",
        }
        self.base_url = f"https://api.github.com/repos/{repository}"

    def get_pr_diff(self) -> str:
        """Get PR diff"""
        url = f"{self.base_url}/pulls/{self.pr_number}"
        headers = {**self.headers, "Accept": "application/vnd.github.v3.diff"}
        response = requests.get(url, headers=headers)
        response.raise_for_status()
        return response.text

    def get_pr_files(self) -> List[Dict]:
        """Get list of changed files in PR"""
        url = f"{self.base_url}/pulls/{self.pr_number}/files"
        response = requests.get(url, headers=self.headers)
        response.raise_for_status()
        return response.json()

    def create_review_comment(self, body: str, commit_id: str, path: str, line: int):
        """Create a review comment on a specific line"""
        url = f"{self.base_url}/pulls/{self.pr_number}/comments"
        payload = {
            "body": body,
            "commit_id": commit_id,
            "path": path,
            "line": line,
            "side": "RIGHT",
        }
        response = requests.post(url, headers=self.headers, json=payload)
        if response.status_code != 201:
            print(f"Warning: Could not create comment: {response.text}")
        return response

    def create_issue_comment(self, body: str):
        """Create a comment on the PR"""
        url = f"{self.base_url}/issues/{self.pr_number}/comments"
        payload = {"body": body}
        response = requests.post(url, headers=self.headers, json=payload)
        response.raise_for_status()
        return response

    def get_pr_commit_id(self) -> str:
        """Get the latest commit ID for the PR"""
        url = f"{self.base_url}/pulls/{self.pr_number}"
        response = requests.get(url, headers=self.headers)
        response.raise_for_status()
        return response.json()["head"]["sha"]


class CodeReviewer:
    """AI Code Reviewer using Alibaba Cloud models"""

    REVIEW_PROMPTS = {
        ReviewType.BUG: """You are an expert Java code reviewer specializing in bug detection.

Analyze the following code changes and identify potential bugs:

CODE CHANGES:
{diff}

Look for:
1. Null pointer exceptions
2. Resource leaks (unclosed streams, connections)
3. Concurrency issues (race conditions, deadlocks)
4. Off-by-one errors
5. Incorrect exception handling
6. Logic errors
7. Type mismatches
8. Missing null checks
9. Incorrect return values
10. Potential infinite loops

For each issue, respond in this JSON format:
{{"issues": [{{"file": "filename.java", "line": 42, "severity": "HIGH", "type": "NULL_POINTER", "description": "desc", "suggestion": "fix"}}], "summary": "brief summary"}}

If no issues found, return: {{"issues": [], "summary": "No bug issues detected"}}""",
        ReviewType.QUALITY: """You are an expert Java code reviewer specializing in code quality.

Analyze the following code changes for quality improvements:

CODE CHANGES:
{diff}

Evaluate:
1. SOLID principles compliance
2. Design patterns usage
3. Code duplication (DRY)
4. Naming conventions
5. Method/function length
6. Class size and responsibility
7. Code complexity
8. Comments and documentation
9. Error messages clarity
10. Code organization

For each issue, respond in this JSON format:
{{"issues": [{{"file": "filename.java", "line": 15, "category": "READABILITY", "description": "desc", "suggestion": "improvement"}}], "summary": "brief summary"}}

If no issues found, return: {{"issues": [], "summary": "No quality issues detected"}}""",
        ReviewType.SECURITY: """You are a security expert reviewing Java code for vulnerabilities.

Analyze the following code changes for security issues:

CODE CHANGES:
{diff}

Check for:
1. SQL injection vulnerabilities
2. XSS vulnerabilities
3. Insecure deserialization
4. Path traversal
5. Command injection
6. Sensitive data exposure
7. Insecure random number generation
8. Missing input validation
9. Authentication/authorization bypasses
10. Improper error handling exposing internals

For each issue, respond in this JSON format:
{{"issues": [{{"file": "filename.java", "line": 25, "severity": "CRITICAL", "owasp": "A03:2021", "type": "SQL_INJECTION", "description": "desc", "remediation": "fix"}}], "summary": "brief summary"}}

If no issues found, return: {{"issues": [], "summary": "No security issues detected"}}""",
        ReviewType.TESTS: """You are a test coverage expert reviewing Java code.

Analyze the following code changes for test coverage:

CODE CHANGES:
{diff}

Evaluate:
1. Are there corresponding test files for new code?
2. Are edge cases tested?
3. Are exception paths tested?
4. Are business logic paths covered?
5. Are integration tests needed?
6. Missing assertions
7. Mock setup correctness
8. Test naming conventions

For each issue, respond in this JSON format:
{{"issues": [{{"sourceFile": "PaymentService.java", "method": "methodName", "type": "EDGE_CASE", "description": "desc", "suggestedTest": "test method"}}], "summary": "brief summary"}}

If no issues found, return: {{"issues": [], "summary": "No test coverage issues detected"}}""",
    }

    def __init__(self, ai_client: AlibabaCloudClient):
        self.ai_client = ai_client

    def review(self, diff: str, review_type: ReviewType) -> Dict[str, Any]:
        """Perform code review for a specific type"""
        prompt = self.REVIEW_PROMPTS[review_type].format(
            diff=diff[:8000]
        )  # Limit diff size

        messages = [
            {
                "role": "system",
                "content": "You are a helpful code reviewer. Always respond with valid JSON only, no markdown formatting.",
            },
            {"role": "user", "content": prompt},
        ]

        response = self.ai_client.chat(messages)

        # Parse JSON response
        try:
            # Remove potential markdown code blocks
            clean_response = response.strip()
            if clean_response.startswith("```"):
                clean_response = clean_response.split("```")[1]
                if clean_response.startswith("json"):
                    clean_response = clean_response[4:]
            clean_response = clean_response.strip()

            return json.loads(clean_response)
        except json.JSONDecodeError:
            return {
                "issues": [],
                "summary": f"Could not parse AI response for {review_type.value}",
            }


def parse_diff(diff_text: str) -> List[FileDiff]:
    """Parse unified diff into structured format"""
    files = []
    current_file = None
    current_additions = []
    current_deletions = []
    current_diff = []

    for line in diff_text.split("\n"):
        if line.startswith("diff --git"):
            if current_file:
                files.append(
                    FileDiff(
                        filename=current_file,
                        status="modified",
                        additions=current_additions,
                        deletions=current_deletions,
                        diff_text="\n".join(current_diff),
                    )
                )
            current_file = line.split(" ")[2].replace("b/", "")
            current_additions = []
            current_deletions = []
            current_diff = [line]
        elif current_file:
            current_diff.append(line)
            if line.startswith("+") and not line.startswith("+++"):
                current_additions.append(line[1:])
            elif line.startswith("-") and not line.startswith("---"):
                current_deletions.append(line[1:])

    if current_file:
        files.append(
            FileDiff(
                filename=current_file,
                status="modified",
                additions=current_additions,
                deletions=current_deletions,
                diff_text="\n".join(current_diff),
            )
        )

    return files


def filter_java_files(files: List[FileDiff]) -> List[FileDiff]:
    """Filter to only include Java files"""
    return [
        f
        for f in files
        if f.filename.endswith(".java") and not f.filename.endswith("Test.java")
    ]


def generate_summary_comment(results: Dict[str, Dict]) -> str:
    """Generate a summary comment for the PR"""

    # Count issues by type and severity
    bug_issues = results.get("bug", {}).get("issues", [])
    quality_issues = results.get("quality", {}).get("issues", [])
    security_issues = results.get("security", {}).get("issues", [])
    test_issues = results.get("tests", {}).get("issues", [])

    # Count severities
    def count_severity(issues, severity):
        return sum(
            1
            for i in issues
            if i.get("severity") == severity
            or i.get("severity", "").upper() == severity
        )

    comment = """## 🤖 AI Code Review Results

**Model:** Qwen-Max | **Review Types:** Bug, Quality, Security, Tests

### 📊 Summary

| Type | Critical | High | Medium | Low |
|------|----------|------|--------|-----|
"""

    # Add bug stats
    comment += f"| 🐛 Bugs | {count_severity(bug_issues, 'CRITICAL')} | {count_severity(bug_issues, 'HIGH')} | {count_severity(bug_issues, 'MEDIUM')} | {count_severity(bug_issues, 'LOW')} |\n"
    comment += f"| 📝 Quality | - | {count_severity(quality_issues, 'HIGH')} | {count_severity(quality_issues, 'MEDIUM')} | {count_severity(quality_issues, 'LOW')} |\n"
    comment += f"| 🔒 Security | {count_severity(security_issues, 'CRITICAL')} | {count_severity(security_issues, 'HIGH')} | {count_severity(security_issues, 'MEDIUM')} | {count_severity(security_issues, 'LOW')} |\n"
    comment += f"| 🧪 Tests | - | {count_severity(test_issues, 'HIGH')} | {count_severity(test_issues, 'MEDIUM')} | {count_severity(test_issues, 'LOW')} |\n"

    # Add critical/high issues
    critical_issues = [i for i in security_issues if i.get("severity") == "CRITICAL"]
    if critical_issues:
        comment += "\n### 🔴 Critical Security Issues\n\n"
        for issue in critical_issues[:5]:  # Limit to 5
            comment += f"- **{issue.get('type', 'Unknown')}** in `{issue.get('file', 'unknown')}`: {issue.get('description', '')}\n"

    # Add summaries
    comment += "\n### 📋 Review Summaries\n\n"
    for review_type, result in results.items():
        if result.get("summary"):
            comment += f"**{review_type.capitalize()}:** {result['summary']}\n\n"

    comment += (
        "\n---\n*Generated by AI Code Review powered by Alibaba Cloud Coding Plan*"
    )

    return comment


def main():
    # Get environment variables
    api_key = os.environ.get("ALIBABA_API_KEY")
    base_url = os.environ.get(
        "ALIBABA_BASE_URL", "https://coding-intl.dashscope.aliyuncs.com/v1"
    )
    model = os.environ.get("AI_MODEL", "qwen-max")
    github_token = os.environ.get("GITHUB_TOKEN")
    repository = os.environ.get("GITHUB_REPOSITORY")
    pr_number = int(os.environ.get("PR_NUMBER", "0"))
    review_types_str = os.environ.get("REVIEW_TYPES", "bug,quality,security,tests")

    # Validate required variables
    if not api_key:
        print("Error: ALIBABA_API_KEY not set")
        return
    if not github_token:
        print("Error: GITHUB_TOKEN not set")
        return
    if not repository or not pr_number:
        print("Error: GITHUB_REPOSITORY or PR_NUMBER not set")
        return

    # Initialize clients
    ai_client = AlibabaCloudClient(api_key, base_url, model)
    github_client = GitHubClient(github_token, repository, pr_number)
    reviewer = CodeReviewer(ai_client)

    # Get PR diff
    print(f"Getting diff for PR #{pr_number}...")
    diff_text = github_client.get_pr_diff()

    # Parse diff
    files = parse_diff(diff_text)
    java_files = filter_java_files(files)

    print(f"Found {len(java_files)} Java files to review")

    if not java_files:
        print("No Java files to review")
        return

    # Perform reviews
    review_types = [ReviewType(rt.strip()) for rt in review_types_str.split(",")]
    results = {}

    # Combine all Java diffs for review
    combined_diff = "\n".join(f.diff_text for f in java_files[:10])  # Limit to 10 files

    for review_type in review_types:
        print(f"Running {review_type.value} review...")
        try:
            results[review_type.value] = reviewer.review(combined_diff, review_type)
        except Exception as e:
            print(f"Error during {review_type.value} review: {e}")
            results[review_type.value] = {
                "issues": [],
                "summary": f"Review failed: {str(e)}",
            }

    # Generate and post summary comment
    summary_comment = generate_summary_comment(results)
    github_client.create_issue_comment(summary_comment)

    print("AI Code Review completed!")


if __name__ == "__main__":
    main()
