# NVIDIA Reuse Business Plan — WebKurierPhone-Android

Audit date: 2026-09-08

Historical baseline: `archive/pre-nvidia-reuse-audit-2026-09-08`.

## Strategic decision

Android remains a Kotlin/Compose/WebRTC client. NVIDIA runtime functions should normally stay in PhoneCore/Jetson/server services and be consumed through stable APIs.

## NVIDIA candidates

### 1. JPS AI-NVR reference Android application — USE AS REFERENCE

Jetson Platform Services 2.0 includes an Android mobile application and sources/reference call flows for its AI-NVR workflow. This can save design/integration effort if WebKurier later needs an Android client for Jetson camera/video services.

Potential reuse:
- service discovery/control interaction patterns;
- live/recorded video workflow concepts;
- reference API call sequencing.

Do not replace the main WebKurier Android architecture with AI-NVR code.

Status: **REFERENCE / COMPATIBILITY AUDIT IF CAMERA UI IS PRIORITIZED**.

### 2. Riva / Speech NIM through PhoneCore

Use provider-transparent PhoneCore APIs for NVIDIA-backed ASR/TTS/NMT after server/Jetson benchmarks. No direct NGC credentials in the mobile app.

Status: **INDIRECT**.

## Business impact

Reuse useful NVIDIA reference workflows without coupling the mobile app to JetPack/NGC releases. Keep app-store lifecycle independent from edge infrastructure lifecycle.

## Guardrails

- no embedded NVIDIA/NGC secrets;
- no direct Jetson admin bypass;
- no direct flight control;
- one integration → one branch → one PR.

## Official references

- https://docs.nvidia.com/jetson/jps/
- https://docs.nvidia.com/jetson/jps/moj-Releasenotes.html
- https://docs.nvidia.com/nim/speech/latest/nmt/index.html
