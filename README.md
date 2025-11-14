# WebKurierPhone-Android

Android client for the AI-powered multilingual translator and German A1–C1 learning platform. Built with Kotlin and Jetpack Compose, connected to **WebKurierPhoneCore** and GPT-based dialog assistant.

---

## 🌍 Overview

**Languages (v1):** 🇬🇧 English, 🇩🇪 German, 🇺🇦 Ukrainian, 🇷🇺 Russian  
**Planned:** more EU languages with flag-based selector.

WebKurierPhone-Android lets you:

- translate calls and messages in real time;
- learn German from A1 to C1 with lessons, dialogs and tests;
- practice speaking with AI (GPT / AI Chat);
- track progress and vocabulary directly on your phone.

The app uses **WebKurierPhoneCore** as backend (REST / WebSocket API) and can connect to different AI providers via the core.

---

## 🧱 Features

- 🔁 **Instant translation** — text and (later) voice calls  
- 🎓 **German course A1–C1** — lessons, dialogs, quizzes  
- 🎤 **Speech practice** — repeat after native-like voice, record yourself  
- 💬 **GPT / AI Chat** — smart dialog partner for any topic  
- 🚩 **Flag-based language menu** — quick switch between base languages  
- 📊 **Progress tracking** — scores, streaks, lesson history  
- 📱 **Modern UI** — Jetpack Compose, dark / light theme

---

## 🏗 Architecture

```text
WebKurierPhone-Android
    ├─ app/                 # Android entry module
    │   ├─ ui/              # Screens, components (Jetpack Compose)
    │   ├─ navigation/      # Navigation graph
    │   ├─ viewmodel/       # State & logic (MVVM)
    │   └─ di/              # Dependency injection
    ├─ data/
    │   ├─ api/             # Retrofit / Ktor client for PhoneCore
    │   ├─ model/           # DTOs and domain models
    │   └─ repo/            # Repositories
    └─ core/
        └─ utils/           # Helpers, theming, localization

The app talks to WebKurierPhoneCore:
	•	/translate — text translation
	•	/lessons/{level} — lessons A1–C1
	•	/call/start — prepare translated call
	•	/chat/gpt — AI dialog endpoint (optional)

⸻

🚀 Getting Started

Requirements
	•	Android Studio (latest stable)
	•	Android SDK 24+
	•	Access to a running WebKurierPhoneCore backend

1. Clone repo

git clone https://github.com/<your-username>/WebKurierPhone-Android.git
cd WebKurierPhone-Android

2. Configure backend URL

Create or edit local.properties (or .env file used by your setup):

PHONECORE_BASE_URL=https://phonecore.example.com

Or hard-code a temporary URL in data/api/ApiConfig.kt:

object ApiConfig {
    const val BASE_URL = "https://phonecore.example.com"
}

3. Open in Android Studio
	1.	File → Open… → выбери папку WebKurierPhone-Android
	2.	Подожди, пока Gradle завершит синхронизацию
	3.	Запусти на эмуляторе или реальном устройстве (Run ▶)

⸻

🔐 AI / GPT Integration

All AI calls go through WebKurierPhoneCore, not directly from the app.
	•	The app отправляет текст / голос → в PhoneCore
	•	PhoneCore обращается к GPT / другой нейросети
	•	Ответ возвращается в приложение как обычный JSON

Преимущества:
	•	нет утечки API-ключей в мобильный клиент;
	•	можно менять провайдера (OpenAI, локальная модель и др.) без обновления приложения;
	•	единая логика лимитов и логирования.

⸻

🗺 Roadmap
	•	Voice call translation (WebRTC integration)
	•	Offline phrasebook & cached lessons
	•	Push-уведомления о занятиях и прогрессе
	•	Больше языков интерфейса (по флагам)
	•	Интеграция с Jobcenter-friendly отчётами через PhoneCore

⸻

📄 License

Made in Germany 🇩🇪
© 2025 Vladyslav Hushchyn — WebKurier Project.
License will be defined in LICENSE.

