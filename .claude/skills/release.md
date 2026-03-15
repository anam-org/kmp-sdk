---
name: release
description: Use when performing a release of the KMP SDK - user specifies major, minor, or patch to determine version increment
---

# Release

## Overview

Automates the KMP SDK release process following RELEASING.md: strip SNAPSHOT, commit, push, tag, bump to next SNAPSHOT.

## Usage

Invoke with release type as argument: `/release major`, `/release minor`, or `/release patch`.

## Pre-flight Checks

Before starting, verify:
1. `VERSION_NAME` in `gradle.properties` ends with `-SNAPSHOT` (abort if not)
2. Working tree is clean — no uncommitted changes (abort if dirty)
3. Current branch is `main` (warn if not)

## Process

1. Read current `VERSION_NAME` from `gradle.properties` (format: `X.Y.Z-SNAPSHOT`)
2. Strip `-SNAPSHOT` to get release version
3. **Confirm with user**: show release version and next snapshot version, ask for approval before proceeding
4. Update `VERSION_NAME` in `gradle.properties` to release version
5. Commit: `Release: vX.Y.Z`
6. Push commit to GitHub
7. Create tag `vX.Y.Z` and push it (CI publishes to Maven Central + creates GitHub Release)
8. Calculate next SNAPSHOT version based on release type:
   - `patch` or `minor`: increment Y, reset Z (e.g., `1.2.3` → `1.3.0-SNAPSHOT`)
   - `major`: increment X, reset Y and Z (e.g., `1.2.3` → `2.0.0-SNAPSHOT`)
9. Update `VERSION_NAME` in `gradle.properties` to next SNAPSHOT version
10. Commit: `Build: Bump version to X.Y.Z-SNAPSHOT`
11. Push commit to GitHub

## Commit Messages

Per CLAUDE.md conventions:
- Release commit: `Release: vX.Y.Z`
- Snapshot bump commit: `Build: Bump version to X.Y.Z-SNAPSHOT`
- Do NOT add co-author lines (per CLAUDE.md git conventions)

## Version Arithmetic Examples

| Current SNAPSHOT | Type  | Release | Next SNAPSHOT   |
|-----------------|-------|---------|-----------------|
| 0.2.0-SNAPSHOT  | patch | 0.2.0   | 0.3.0-SNAPSHOT  |
| 0.2.0-SNAPSHOT  | minor | 0.2.0   | 0.3.0-SNAPSHOT  |
| 0.2.0-SNAPSHOT  | major | 0.2.0   | 1.0.0-SNAPSHOT  |
| 1.3.5-SNAPSHOT  | patch | 1.3.5   | 1.4.0-SNAPSHOT  |
| 1.3.5-SNAPSHOT  | minor | 1.3.5   | 1.4.0-SNAPSHOT  |
| 1.3.5-SNAPSHOT  | major | 1.3.5   | 2.0.0-SNAPSHOT  |
