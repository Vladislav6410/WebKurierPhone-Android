# WebKurierPhone-Android — Pull Request

## Summary
Describe what this PR changes in 2–5 bullet points.

- 
- 
- 

## Scope
Select all that apply:
- [ ] UI (Jetpack Compose)
- [ ] Core integration (CoreGateway / PhoneCoreAPI)
- [ ] WebRTC / Call UI
- [ ] Lessons
- [ ] Wallet (WebCoin)
- [ ] DreamMaker
- [ ] Security / SecureStore
- [ ] i18n / Localization
- [ ] Build / CI

## Architecture Rules (must hold)
- [ ] Android remains a thin client (no business logic)
- [ ] Routing goes through Core / PhoneCore (no direct domain hub bypass)
- [ ] Tokens are stored only in SecureStore (encrypted)
- [ ] No secrets committed (keys/tokens/passwords)

## Testing
- [ ] CI passed (lint + unit tests + assembleDebug)
- [ ] Manual smoke test performed (if UI changed)

## Screenshots (if UI changed)
Attach screenshots or a short screen recording.

## Notes / Follow-ups
List any TODOs or follow-up work.