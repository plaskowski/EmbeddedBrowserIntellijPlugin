# JCEF Seeded Profile Solution

## Overview

This solution keeps the **embedded JCEF browser inside IntelliJ** as the main day-to-day workspace browser, while using a **real Chromium/Chrome instance only for daily authentication refresh**.

The key idea is to avoid sharing one live profile between Chromium and JCEF. Instead, the system uses a **seeded profile workflow**:

- a **seed profile** owned by real Chromium
- a **runtime profile** owned by JCEF
- a **one-way refresh step** from seed profile to runtime profile

This makes the embedded browser practical even when authentication must happen in a real browser because of **YubiKey / corporate SSO requirements**.

## Motivation

The embedded JCEF browser is attractive because it gives:

- real browser rendering inside IntelliJ
- persistent cookies and session state
- direct embedding without screenshots or window mirroring
- task-related pages next to the code workspace

However, some corporate authentication flows require a **real Chromium/Chrome browser**, especially when daily re-authentication involves **YubiKey or other strong browser/OS-integrated auth mechanisms**.

Using one shared live profile between real Chromium and JCEF is too fragile because it creates profile ownership, locking, and compatibility problems.

The seeded profile model solves this by making the browser state transfer **explicit, one-way, and restart-based**.

## Core model

The solution uses two separate browser profile directories.

### 1. Seed profile

The **seed profile** is a dedicated Chromium/Chrome profile used only for authentication and session seeding.

It is used for:

- logging into Jira
- logging into Confluence
- logging into GitHub
- logging into other relevant work systems
- completing daily YubiKey-backed authentication

This profile is:

- small and dedicated
- never used as the JCEF runtime profile
- the only source of truth for refreshed authenticated browser state

### 2. Runtime profile

The **runtime profile** is the browser profile used by the embedded JCEF browser inside IntelliJ.

It is used for:

- browsing task-related pages during the workday
- keeping Jira, Confluence, GitHub PRs, and similar pages open next to the IDE
- preserving session continuity inside the IntelliJ tool window

This profile is:

- recreated from the seed profile during refresh
- owned only by JCEF
- disposable and replaceable

## Refresh workflow

The refresh workflow is intentionally simple and one-way.

### Daily refresh flow

1. Start real Chromium/Chrome using the **seed profile**.
2. Re-authenticate into all required corporate systems.
3. Close the real browser completely.
4. Stop or reset the embedded JCEF browser runtime.
5. Delete the old **runtime profile**.
6. Copy the entire **seed profile** into the runtime profile location.
7. Start the embedded JCEF browser using the refreshed runtime profile.
8. Reopen the task-related pages in JCEF.

This creates a fresh embedded browser environment that starts the day already authenticated.

## Ownership model

A strict ownership rule keeps the solution safe and predictable.

### Seed profile ownership

Owned only by real Chromium/Chrome.

### Runtime profile ownership

Owned only by JCEF.

### Allowed data flow

Only this direction is allowed:

**seed profile -> runtime profile**

The runtime profile should never be merged back into the seed profile.

This prevents:

- concurrent profile access
- profile corruption
- partial state synchronization problems
- unclear source-of-truth situations

## Why full copy is preferred

Because the seed profile is dedicated and intentionally small, the safest refresh strategy is to **copy the entire profile** rather than try to copy only selected browser files.

That avoids:

- guessing which files are needed
- missing IndexedDB or service worker state
- partial auth breakage
- ongoing maintenance of profile-internals knowledge

The design principle is:

**keep the seed profile small, then copy it whole**

instead of trying to optimize too early.

## Task context model

Task-related browsing context should be managed by the plugin, not by depending on browser-native session restore.

The plugin should store task-related page lists explicitly, for example:

- Jira issue page
- Confluence design page
- GitHub PR page
- internal dashboard
- todo app page

After a runtime profile refresh, the plugin can reopen those pages in JCEF.

This is more reliable than depending on Chromium/JCEF to restore tabs exactly as desired.

## Technical advantages

This solution has several important advantages.

### 1. Works with YubiKey-driven auth

Authentication happens where it is most likely to work: in a real Chromium/Chrome browser.

### 2. Keeps JCEF as the main embedded browser

The day-to-day browsing experience stays inside IntelliJ.

### 3. Avoids live shared-profile problems

Real Chromium and JCEF never actively use the same profile directory.

### 4. Keeps the architecture simple

No screenshot bridge, no remote debugging transport, no system window mirroring, and no browser-state synchronization engine are required.

### 5. Predictable reset model

If something goes wrong, the runtime profile can simply be discarded and recreated from the seed profile.

## Technical limitations

This solution also has boundaries.

### 1. Re-auth still happens outside JCEF

The embedded browser does not solve strong authentication itself. It depends on the refresh step.

### 2. Runtime state is disposable

Any browser-only changes made during the day inside JCEF are lost when the runtime profile is replaced, unless the plugin persists important context separately.

### 3. Midday auth expiry may still happen

If some systems expire or rotate auth aggressively during the day, a fresh seeding cycle may be needed.

### 4. Browser runtime mismatch is still possible

Even though both sides are Chromium-family, the seed browser and JCEF runtime are still different environments, so this remains a pragmatic workflow rather than a formal browser compatibility contract.

## Recommended plugin responsibilities

The plugin should manage the workflow explicitly.

### Profile management

- configure seed profile location
- configure runtime profile location
- reset runtime profile safely
- copy seed profile into runtime profile

### Embedded browser lifecycle

- stop JCEF runtime before profile replacement
- start JCEF runtime after refresh
- ensure JCEF always uses the runtime profile only

### Task restoration

- persist task-related URLs or page descriptors
- reopen them after refresh
- optionally organize them by project or task context

### Operational commands

Useful commands could include:

- Refresh embedded browser from seed profile
- Reopen task tabs
- Reset runtime profile
- Open seed profile in real Chromium

## Recommended design principles

This solution should follow a few strict rules.

### Rule 1

Never let real Chromium and JCEF use the same live profile directory at the same time.

### Rule 2

Treat the seed profile as the only source of truth for refreshed authentication state.

### Rule 3

Treat the runtime profile as disposable.

### Rule 4

Keep task context in plugin-managed state, not only inside browser session restore.

### Rule 5

Optimize for predictability first, not for clever partial synchronization.

## Summary

The seeded profile solution is a pragmatic architecture for combining:

- **real-browser authentication** where required
- **embedded IntelliJ browsing** during the workday
- **task-related web context** kept next to the code workspace

In short, the solution is:

**authenticate once per day in a real Chromium/Chrome seed profile, copy that profile into a JCEF-owned runtime profile, then use the embedded browser inside IntelliJ for the rest of the workflow.**

This avoids the fragility of live shared profiles while preserving the main benefits of an embedded browser workspace.

