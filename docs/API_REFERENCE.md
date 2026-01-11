# WebKurierPhone-Android — API Reference (Client View)

This document describes the **client-side integration contracts** for the Android app.

Important:
- Android is a **thin client**
- WebKurierCore and WebKurierPhoneCore are **authoritative**
- WebKurierSecurity and WebKurierChain enforce validation & integrity

This reference is intentionally **high-level** and stable.
Detailed endpoint specs live in the server repositories.

---

## 1. Endpoints Configuration

Client loads endpoints from:

```text
config/endpoints.json

Required endpoint groups:
	•	Core REST base URL
	•	PhoneCore REST base URL
	•	Core WebSocket URL
	•	PhoneCore WebSocket URL
	•	WebRTC Signaling URL (if used)

⸻

2. Authentication (via Core)

2.1. Sessions

Core is responsible for:
	•	login / logout
	•	token issuance and refresh policy
	•	session status

Android responsibilities:
	•	store tokens encrypted (SecureStore)
	•	attach tokens to requests
	•	never hardcode tokens

2.2. Recommended Headers

Typical headers (example, names may vary by server):
	•	Authorization: Bearer <token>
	•	Accept-Language: <lang>
	•	X-Client: android
	•	X-App-Version: <version>

⸻

3. Core APIs (Client View)

3.1. Agent Registry

Purpose:
	•	list available agents/features for user
	•	feature flags and availability

Android uses this to:
	•	render navigation tiles
	•	enable/disable screens

3.2. Command Routing (Limited)

Purpose:
	•	send user intent to Core routing layer
	•	receive responses and agent instructions

Android must:
	•	treat responses as display payload
	•	avoid embedding business rules locally

⸻

4. PhoneCore APIs (Client View)

4.1. Translation

Features:
	•	language auto-detection
	•	text translation
	•	voice translation (streaming)
	•	document/image translation (OCR)

Client responsibilities:
	•	send text/audio/blob
	•	display original + translated output
	•	show “detected language” if provided

4.2. STT/TTS

STT:
	•	low-latency streaming preferred for calls
	•	chunked upload supported for recordings

TTS:
	•	request text + voice profile
	•	stream or download audio output

4.3. Lessons (A1–C1)

PhoneCore provides:
	•	course structure
	•	units/lessons
	•	exercises
	•	scoring feedback (if implemented)

Android provides:
	•	UI, progress screens
	•	local caching (non-authoritative)

⸻

5. WebSocket Realtime Channels

Used for:
	•	live translation streams
	•	call subtitles
	•	lesson live scoring
	•	notifications (optionally)

Client must:
	•	implement reconnect policy
	•	apply backoff
	•	show connection status

⸻

6. WebRTC (Calls)

6.1. Signaling

Signaling is coordinated by PhoneCore (or a dedicated signaling service),
while media is peer-to-peer where possible.

Client handles:
	•	permission checks (mic)
	•	device audio routing
	•	echo cancellation / AGC config (AudioEngine)
	•	subtitle overlay synced with translation stream

6.2. Call Events (Client View)

Typical events:
	•	call invite
	•	accept/decline
	•	connected/disconnected
	•	mute/unmute
	•	subtitle update (from PhoneCore)

⸻

7. Wallet (WebCoin) — Client View

Authoritative sources:
	•	Chain integrity (via server)
	•	Core/PhoneCore APIs as proxy

Client responsibilities:
	•	display balance
	•	display history (if provided)
	•	request transaction confirmation flows
	•	display transaction state (pending/confirmed/failed)

Client must never:
	•	mint coins locally
	•	confirm ledger changes without server response

⸻

8. VehicleHub / Geodesy — Receive-Only

Client receives:
	•	mission previews
	•	telemetry notifications
	•	safety prompts

Client must:
	•	treat as read-only unless routed via Core
	•	provide safe UX for warnings

⸻

9. Security Gates

Security-related actions may require:
	•	URL scan request before opening
	•	file scan request before upload/processing
	•	phishing risk prompts

Client must:
	•	present warnings from Security as blocking UX when required
	•	never bypass scan requirements

⸻

10. Error Handling Standard (Client)

All server errors should be mapped into:
	•	user_message (safe, localized)
	•	error_code (stable identifier)
	•	trace_id (for support)
	•	retryable boolean (if provided)

Client UX rules:
	•	show actionable message
	•	allow retry when safe
	•	never leak raw stack traces to UI

---

### Айфон-ориентированное действие (быстро)
1) Открой репозиторий в GitHub (Safari/приложение).  
2) **Add file → Create new file**  
3) Введи путь: `docs/API_REFERENCE.md`  
4) Вставь текст → **Commit**.

---

