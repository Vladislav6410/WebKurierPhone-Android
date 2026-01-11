# WebKurierPhone-Android — Architecture

This document defines the **strict architectural role** of the Android client inside the **WebKurier 10-repository ecosystem**, and the internal module boundaries required for safe evolution.

---

## 1. Position in the WebKurier Ecosystem

**Repository:** WebKurierPhone-Android  
**Hierarchy Level:** 3 (Client Applications)

### 1.1. Global Hierarchy (Reference)

- Level 0 — WebKurierHybrid (orchestrator)
- Level 1 — WebKurierCore (gateway & terminal)
- Level 2 — WebKurierPhoneCore (communication hub)
- Level 2 — WebKurierVehicleHub (transport & geodesy)
- Level 2 — WebKurierChain (blockchain & tokens)
- Level 2 — WebKurierSecurity (security)
- Level 3 — WebKurierPhone-Android (this repository)
- Level 3 — WebKurierPhone-iOS
- Level 4 — WebKurierSite
- Level 5 — WebKurierX

### 1.2. Execution Path

The Android app is a **mobile gateway**:

User → Android App → PhoneCore/Core → Domain Hubs → Core → Android App

The Android app:
- does **not** implement business rules of Core
- does **not** implement integrity/ledger rules of Chain
- does **not** bypass Security validation
- acts as a **secure UI + device capability layer** (audio, camera, storage, notifications)

---

## 2. Core Principles (Non-Negotiable)

### 2.1. Separation of Responsibilities

- **WebKurierCore**: routing, agents, user sessions, UI gateway, command registry  
- **WebKurierPhoneCore**: STT/TTS, translation engines, lesson backend, call bridging  
- **WebKurierSecurity**: URL/file scanning, auth hardening, anti-phishing, validation gates  
- **WebKurierChain**: WebCoin ledger, integrity, licensing, encrypted events  
- **WebKurierPhone-Android**: Android UI client + device runtime

The Android app must remain a **thin client**:
- UI rendering
- input capture
- local secure storage for session tokens (encrypted)
- network transport (REST/WebSocket/WebRTC)
- offline hints/caching (non-authoritative)

### 2.2. Trust Zones

- Trusted: Hybrid, Core logic, Chain blocks  
- Semi-trusted: PhoneCore, VehicleHub  
- Untrusted: mobile devices, browsers, wearables

Android is **untrusted by default** and must assume:
- tokens can leak if not encrypted
- UI can be manipulated
- network can be intercepted (so use TLS + pinning optional)
- server is the source of truth

---

## 3. High-Level Modules (Android Repo)

### 3.1. `app/` (Presentation Layer)

Responsibilities:
- Compose UI screens
- navigation
- view models/state
- permissions & UX flows
- presenting agent features

Must not contain:
- cryptographic ledger logic
- server authority decisions
- security scanning rules

### 3.2. `core/` (Client Runtime Layer)

Responsibilities:
- API clients (Core + PhoneCore)
- WebRTC client
- audio pipeline helpers
- localization management
- secure local storage

### 3.3. `config/` (Non-secret Configuration)

Responsibilities:
- endpoints templates
- environment example files
- build-time flags (non-secret)

No secrets must be committed.

### 3.4. `docs/` (Documentation)

Architecture, build, API notes.

---

## 4. Integration Contracts (Client View)

### 4.1. With WebKurierCore

Core provides:
- account / session management
- agent registry access
- limited command routing

Android must:
- authenticate and store session data securely
- render agent UIs provided by Android screens
- treat Core output as authoritative

### 4.2. With WebKurierPhoneCore

PhoneCore provides:
- STT/TTS
- translator engine
- voice call bridging
- lessons engine
- file/OCR processing
- wallet & rewards API (read-only or proxy)

Android must:
- capture audio reliably
- display subtitles
- handle realtime streams (WebSocket/WebRTC)

### 4.3. With WebKurierChain (Indirect)

Chain provides:
- token balance
- transactions
- reward logic
- integrity proofs

Android must:
- never “confirm” a transaction locally as authoritative
- always display Chain-confirmed status from server responses

### 4.4. With WebKurierSecurity

Security provides:
- URL filtering / scanning gates
- file upload scanning
- anti-phishing checks
- validation of requests

Android must:
- send URLs/files through server gates (when required)
- enforce safe UX warnings from Security responses

### 4.5. With VehicleHub (Receive-only)

VehicleHub provides:
- telemetry notifications
- mission summaries
- safety prompts

Android must:
- show notifications
- never attempt to command VehicleHub directly unless routed via Core

---

## 5. UI Surface (Screens)

Recommended screens mapping (current planned set):

- HomeScreen — navigation hub
- TranslatorScreen — text/voice/doc translation
- VoiceCallScreen — WebRTC calls + subtitles
- LessonsScreen — A1–C1 learning
- WalletScreen — balance, rewards, history (view + confirm via server)
- CafeScreen — menu / orders (optional feature)
- RomanticScreen — social agent UI
- HRScreen — HR simulator UI
- DreamMakerScreen — media generation UI
- SettingsScreen — language, permissions, endpoints (dev)

---

## 6. Non-Functional Requirements

- Reliability first: reconnect & retry policy for WebSocket/WebRTC
- Offline safety: cached data must be marked “cached”
- Privacy: do not log sensitive data
- Performance: audio pipeline must be low latency
- i18n: UI text from localization manager; support RTL where applicable
- Observability: structured logs with redaction

---

## 7. Future Extensions (Controlled)

Allowed:
- optional certificate pinning
- modular feature flags (buildConfig)
- local language packs (non-authoritative)
- telemetry crash reporting (privacy-safe)

Not allowed:
- local ledger forks
- bypassing server validation
- embedding secrets in APK


⸻


