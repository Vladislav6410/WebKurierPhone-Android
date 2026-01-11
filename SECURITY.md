# Security Policy — WebKurierPhone-Android

WebKurierPhone-Android is a Level 3 client repository in the WebKurier ecosystem.
It must remain a thin client and must not bypass security or integrity controls enforced by:
- WebKurierSecurity (validation/scanning)
- WebKurierChain (integrity/ledger)
- WebKurierHybrid (orchestration/CI/CD)
- WebKurierCore (routing/auth)

## Supported Versions

| Version | Supported |
|--------:|:---------:|
| 0.x     | ✅ Yes     |

## Reporting a Vulnerability

Please report security issues responsibly.

### Do NOT open a public issue for:
- credential leakage
- token/session issues
- auth bypass
- remote code execution
- signing/keystore compromise
- supply chain issues

### Preferred reporting method
- Use GitHub Security Advisories (private) if enabled in this repository.
- If not enabled, report via a private channel to the maintainer.

### What to include
- affected component (file/module/screen)
- reproduction steps
- expected vs actual behavior
- logs/screenshot (without secrets)
- severity assessment (low/medium/high/critical)

## Security Guarantees (Client-Side)

This repository enforces:
- No secrets committed (keys, tokens, passwords)
- Encrypted local storage only (SecureStore)
- Network calls via CoreGateway / PhoneCoreAPI only
- No direct calls to domain hubs (Chain/Security/VehicleHub) from Android

## Coordinated Disclosure

We aim to acknowledge within 7 days and provide a remediation plan as soon as feasible.