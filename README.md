# WebKurierPhone-Android — Native Android Client for the WebKurier Ecosystem

**WebKurierPhone-Android** is the official native Android application of the WebKurier AI ecosystem.  
It provides a fast, stable, multilingual mobile client tightly integrated with WebKurierPhoneCore, WebKurierCore, and domain agents across all communication, translation, voice, geodesy previews, WebCoin wallet, and DreamMaker features.

Built with:
- Kotlin / Jetpack Compose
- WebRTC (calls & live translation)
- ML-ready audio pipeline
- Secure on-device storage
- REST + WebSocket integration with PhoneCore & Core
- Adaptive UI for phones/tablets (Material You)

---

# 1. Role in the Ecosystem (Hierarchy Level 3)

```text
Level 0 — WebKurierHybrid (orchestrator)
Level 1 — WebKurierCore (gateway & terminal)
Level 2 — WebKurierPhoneCore (communication hub)
Level 2 — WebKurierVehicleHub (transport & geodesy)
Level 2 — WebKurierChain (blockchain & tokens)
Level 2 — WebKurierSecurity (security)
Level 3 — WebKurierPhone-Android (THIS REPOSITORY)
Level 3 — WebKurierPhone-iOS
Level 4 — WebKurierSite
Level 5 — WebKurierX

Android acts as a primary mobile gateway, enabling:

User → Android App → PhoneCore/Core → Domain Hubs → Core → Android App

With full STT/TTS, translation, WebRTC communication, and wallet support.

⸻

2. App Capabilities

2.1. Real-Time Translation
	•	Auto-detection of languages
	•	Bidirectional text/voice translation
	•	Subtitles for voice calls
	•	Image/document translation (OCR via PhoneCore)

2.2. Voice Communication (WebRTC)
	•	Secure peer-to-peer calls
	•	Live bilingual subtitles
	•	Noise reduction, AGC, echo cancellation
	•	Optional offline speech hints

2.3. Lessons (A1–C1)
	•	Vocabulary trainer
	•	Listening/speaking practice
	•	Grammar modules
	•	Pronunciation scoring

2.4. Social/Emotional Agents
	•	Romantic advisor
	•	HR simulator
	•	Marketing notifications

2.5. DreamMaker
	•	Generate images, music, short clips
	•	Save, export, share to Core or Telegram

2.6. Wallet (WebCoin)
	•	View balance
	•	Earn rewards (tasks/events)
	•	Confirm transactions via Chain

2.7. VehicleHub/Geodesy
	•	Mission previews
	•	Flight warnings
	•	Status reports from drones or vehicles

⸻

3. Repository Structure

WebKurierPhone-Android/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/webkurier/android/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── ui/
│   │   │   │   │   ├── HomeScreen.kt
│   │   │   │   │   ├── TranslatorScreen.kt
│   │   │   │   │   ├── VoiceCallScreen.kt
│   │   │   │   │   ├── LessonsScreen.kt
│   │   │   │   │   ├── WalletScreen.kt
│   │   │   │   │   ├── CafeScreen.kt
│   │   │   │   │   ├── RomanticScreen.kt
│   │   │   │   │   ├── HRScreen.kt
│   │   │   │   │   ├── DreamMakerScreen.kt
│   │   │   │   │   └── SettingsScreen.kt
│   │   │   ├── res/
│   │   │   ├── AndroidManifest.xml
│   │   │   └── assets/
│   │   ├── test/
│   │   └── androidTest/
├── core/
│   ├── PhoneCoreAPI.kt
│   ├── CoreGateway.kt
│   ├── WebRTCClient.kt
│   ├── AudioEngine.kt
│   ├── LocalizationManager.kt
│   └── SecureStore.kt
├── config/
│   ├── env.example.json
│   └── endpoints.json
└── docs/
    ├── ARCHITECTURE.md
    ├── BUILD_GUIDE.md
    └── API_REFERENCE.md


⸻

4. Integration Model

With PhoneCore

Provides:
	•	STT/TTS
	•	Translator engine
	•	Voice call bridging
	•	Lessons engine
	•	File/OCR processing
	•	Wallet & rewards API

With Core

Provides:
	•	account/session management
	•	agent registry access
	•	command routing (limited)

With Chain

Indirect:
	•	token balance
	•	transactions
	•	reward logic

With VehicleHub

Receive-only:
	•	Telemetry notifications
	•	Mission summaries
	•	Safety prompts

With Security
	•	URL filtering
	•	File upload scanning
	•	Anti-phishing filters

⸻

5. CI/CD

Managed by WebKurierHybrid through:
	•	GitHub Actions pipelines
	•	Keystore signing (stored in Hybrid)
	•	Play Store release tracks (Alpha/Beta/Production)
	•	GitHub Secrets for environment variables

⸻

6. Agent Glossary (EN + RU)

TranslatorAgent — Переводчик
VoiceAgent — Голосовой ассистент
PhoneAgent — Телефония
LessonsAgent — Уроки A1–C1
RomanticAgent — Романтический собеседник
MemoryAgent — Память
DreamAgent — Генератор медиа
CafeAgent — Меню, кафе, заказы
WalletAgent — Кошелёк WebCoin
MarketingAgent — Маркетолог
HRAgent — HR-агент
SecurityAgent — Агент безопасности


⸻

7. Governance

WebKurierPhone-Android is developed and maintained by:
Vladyslav Hushchyn (VladoExport) — Germany, EU

⸻




