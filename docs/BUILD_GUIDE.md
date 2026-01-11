# WebKurierPhone-Android — Build & Run Guide

This document defines the **canonical way** to build, run, and maintain the Android client of the WebKurier ecosystem.

This repository follows **strict separation of responsibilities** and is fully managed by **WebKurierHybrid CI/CD**.

---

## 1. Supported Environment

### 1.1. Required Software

- Android Studio (latest stable)
- Android SDK 34+
- Kotlin 1.9+
- Gradle (wrapper-managed)
- JDK 17 (recommended)

### 1.2. Supported Devices

- Android 10+ (API 29+)
- Phones and tablets
- ARM64 preferred
- Microphone, camera, storage permissions required for full feature set

---

## 2. Repository Preparation

Clone the repository:

```bash
git clone https://github.com/<org>/WebKurierPhone-Android.git
cd WebKurierPhone-Android

⚠️ This repository does not contain secrets.
All environment values are injected via CI/CD or local config files.

⸻

3. Configuration Files

3.1. Environment Template

File:

config/env.example.json

Purpose:
	•	shows required variables
	•	must be copied locally (never committed with secrets)

Example workflow:

cp config/env.example.json config/env.local.json

env.local.json must be excluded via .gitignore

⸻

3.2. Endpoints Configuration

File:

config/endpoints.json

Defines:
	•	WebKurierCore base URL
	•	WebKurierPhoneCore base URL
	•	WebSocket endpoints
	•	WebRTC signaling endpoint

This file is non-secret and may differ between environments (dev / staging / prod).

⸻

4. Build Variants

Recommended variants:
	•	debug — local development
	•	staging — internal testing
	•	release — production (Play Store)

Keystore handling:
	•	Debug keystore: local
	•	Release keystore: stored only in WebKurierHybrid

⸻

5. Local Build (Developer)

From Android Studio:
	1.	Open project
	2.	Sync Gradle
	3.	Select debug
	4.	Run on emulator or device

From CLI (optional):

./gradlew assembleDebug

APK output:

app/build/outputs/apk/debug/


⸻

6. CI/CD Build (Authoritative)

All official builds are executed by WebKurierHybrid:
	•	GitHub Actions
	•	Secure keystore injection
	•	Versioning & signing
	•	Play Store upload

Local builds are non-authoritative and must not be distributed.

⸻

7. Permissions Model

The app requests permissions dynamically:
	•	Microphone — voice calls, STT
	•	Camera — document/image translation
	•	Storage — temporary media files
	•	Notifications — calls, missions, alerts

All permissions must:
	•	be user-approved
	•	degrade gracefully if denied

⸻

8. Debug & Logging

Rules:
	•	No sensitive data in logs
	•	Tokens must be redacted
	•	Audio data must never be logged

Recommended:
	•	Use structured logs
	•	Wrap logs behind build flags

⸻

9. Common Errors

Gradle Sync Fails
	•	Check JDK version
	•	Invalidate caches
	•	Update Android Studio

Network Errors
	•	Verify endpoints.json
	•	Check TLS certificates
	•	Confirm Core/PhoneCore availability

WebRTC Issues
	•	Check microphone permission
	•	Verify signaling endpoint
	•	Test on real device (emulator limitations)

⸻

10. Update Policy
	•	API changes come from Core / PhoneCore
	•	Android client adapts, never dictates protocol
	•	Breaking changes require version bump

⸻

11. Golden Rule

Android is a client, not an authority.
If logic feels “server-like” — it does not belong here.

---


