# Changelog — WebKurierPhone-Android

All notable changes to this repository will be documented in this file.

Format:
- Keep sections in reverse chronological order.
- Use tracks: alpha / beta / production.
- Client is thin: major behavior changes must be routed via Core/PhoneCore contracts.

---

## [Unreleased]
### Added
- 

### Changed
- 

### Fixed
- 

### Security
- 

---

## [0.1.0] — 2026-01-11
### Added
- Initial architecture scaffolding (Compose UI shells)
- Core integration stubs (CoreGateway / PhoneCoreAPI)
- SecureStore (encrypted token storage)
- Basic CI (lint + tests + debug APK artifacts)
- Release workflows (signing + track build) and security workflows

### Notes
- Release artifacts are published as GitHub Actions artifacts.
- Play Console upload will be enabled after service-account secrets are configured.