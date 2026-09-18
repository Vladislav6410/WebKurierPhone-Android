# WebKurier Copilot MVP v0.1 — Pilot / Week 1

This Android pilot gives approximately five beginner students a simple course and
project-assistant surface. It is a client foundation, not a working AI/GitHub
automation service: the required external contracts are not present in this repository.

## Student journey

GitHub Connect → Мой курс → День 1 / День 2 / День 3 → Copilot →
Посмотреть результат на сайте. The only navigation destinations are Мой курс,
Copilot, and Мой проект / GitHub. Temporary WK text branding is replaceable in
Android resources; no provider logos or settings are shown.

- Day 1: open the training page and understand project → GitHub → website.
- Day 2: ask for one visible change, such as text, a button, an image or a block.
- Day 3: request a header/navigation and 2–3 pages (Инструкция, О проекте, Контакты).
- Weeks 2–8 are roadmap-only. Every item says «Откроется позже», has no action,
  and has no lesson content or backend workflow.

All three Week 1 lessons can be selected. The selected unfinished lesson is CURRENT;
other unfinished lessons are AVAILABLE. «Я выполнил(а) задание» records a student's
self-reported COMPLETED mark. It never claims that GitHub or the website changed.
Progress is stored on this device in dedicated SharedPreferences through
CourseProgressStore. It survives app recreation, is not synced, and is not server
authority. Restoration filters invalid lesson values and incorrectly typed stored
preferences rather than crashing. Authentication and conversation state are not persisted. Drafts are
in memory per lesson and clear on lesson change or activity recreation.

## Architecture and localization

MainActivity now creates PilotDependencies and the Compose PilotApp. The existing
explicit dependency-construction and lightweight navigation approach is retained;
there is no new production framework or runtime dependency. Test-only Robolectric and Compose test libraries exercise Android behavior on the JVM. Controller StateFlows drive UI;
composition-owned coroutines cancel on disposal and reset pending states. The
selected destination is saveable; system Back returns to the course. Switching sections starts at the top rather than reusing another section's scroll position. Navigation exposes tab selection semantics, and status changes use polite accessibility announcements.

Legacy screens, networking, secure storage, resources and models remain in place.
The pilot does not instantiate or call the legacy placeholder APIs. Small build
compatibility repairs align Java/Kotlin target 17, remove imports of nonexistent
endpoint source classes, use the already installed OkHttp 4 API, and opt the legacy
TopAppBar into its existing Material3 experimental API. No dependency upgrade,
workflow change, release change or server repository change is included.

Russian is the pilot's default resource language in `app/src/main/res/values/strings.xml`.
Future `values-pl`, `values-de`, and `values-en` translations can use the same keys.
Legacy language preferences are not used to choose pilot strings; Android resource
resolution determines the language. There is no new student settings screen.

## GitHub authentication status

GitHubAuthService is a client interface, not a proposed HTTP endpoint. The default
UnconfiguredGitHubAuth returns NOT_CONFIGURED. The UI starts NOT_CONNECTED, exposes
CONNECTING during the suspend call, and displays ERROR with retry/help when no
configuration is available. CONNECTED requires a nonblank identity from the service;
an assigned project is optional and must also come from verified service data.
The immediate unconfigured response may make CONNECTING too brief to see on screen.

Before enabling real login, the WebKurier operator must supply an approved OAuth
application/flow, registered Android redirect configuration, authenticated backend
session exchange/refresh/logout contract, required scopes and a verified per-student
project assignment mechanism. OAuth state/PKCE/callback validation must follow that
approved contract. Secrets and GitHub authorization remain server-controlled; only
appropriate mobile session artifacts may use existing encrypted SecureStore. None
of those external prerequisites is invented or implemented here.

## AI backend status

CopilotService accepts the lesson, instruction and verified project context at a
client boundary. UnconfiguredCopilot always returns Unavailable without network
traffic. Send is disabled while the shipped backend is unconfigured, with the prerequisite explanation beside the editable draft. A service-level Unavailable result also leaves the draft intact and explicitly says it was not sent. No canned
assistant answer, repository edit or deployment is presented as real. Exceptions
map to localized errors instead of exposing diagnostics. Input is capped at 2,000
characters; duplicate pending requests and lesson switches during sending are blocked.

The operator must supply a verified WebKurier-controlled backend contract for
authenticated lesson assistance, project authorization, responses, errors and any
change/deployment approvals. Future direction: Android → WebKurier backend → AI →
authorized student GitHub project. Implement that adapter and replace the current
unavailable banner when integration is actually enabled. No AI-provider key or
provider SDK belongs in this client.

## Website URL status

No appropriate verified student site URL or assignment exists in repository docs
or configuration. Existing Core/PhoneCore URLs are not student result websites.
`app/src/main/res/values/pilot_config.xml` therefore ships an empty
`pilot_website_url`. An operator may package a verified **public** training/result
HTTPS URL there. It is non-secret build configuration, not a student input field.
A shared build URL must only point to a shared public pilot site. Per-student URLs
require the future authenticated assignment contract, not one hard-coded shared value.

Validation rejects malformed URLs, non-HTTPS schemes, missing/dotless hosts, literal
IPv4/IPv6 addresses, localhost subdomains, credentials, non-default ports, queries
and fragments. Queries/fragments are intentionally unsupported to avoid embedding
access material. This is syntactic validation, not phishing detection or proof of
site ownership. The operator must verify the site and any required Security approval;
there is no invented security-scan endpoint or claim that validation replaces it.
A valid configured URL opens ACTION_VIEW/CATEGORY_BROWSABLE in the normal browser;
missing handlers/security errors show a localized message. Empty/invalid configuration
disables the action and explains the problem. The app never fabricates a result URL.

## Security boundaries

No secrets were committed. No OAuth client secret, GitHub token, AI-provider key,
production endpoint, assigned repository or student identity is configured. Test
identities and example.org URLs exist only as test fixtures. Progress stores only
lesson numbers; draft messages and authentication data are not written to disk by
the pilot. No network requests are made by the shipped pilot adapters. No extra
Android permissions were added. Existing legacy permissions remain declared but
the pilot does not request camera, microphone or notification permission.

## Verification and known limitations

Starting SHA: `a1b797b199fd2892745a0ab526d3a9db818825d1`.
Repository/branch and a clean starting worktree were verified, then `git fetch origin`
succeeded. Existing draft NVIDIA documentation PR #1 was inspected and left untouched.

Local commands attempted on 2026-09-17:

| Command | Result |
| --- | --- |
| `.\gradlew.bat :app:tasks --all` | Exit 1: JAVA_HOME unset; java not found |
| `.\gradlew.bat :app:testDebugUnitTest` | Exit 1: JAVA_HOME unset; java not found |
| `.\gradlew.bat :app:check` | Exit 1: JAVA_HOME unset; java not found |
| `.\gradlew.bat :app:lintDebug` | Exit 1: JAVA_HOME unset; java not found |
| `.\gradlew.bat :app:assembleDebug` | Exit 1: JAVA_HOME unset; java not found |

These were blocked attempts, not passing Android checks. On 2026-09-18 the
hardening pass verified the same baseline and restored the wrapper with official
Gradle 8.7; see `gradle/wrapper/README.md` for its exact command and SHA-256 evidence.
Temurin JDK 17 and Android SDK 34 were installed in ignored local cache directories.
The wrapper generation task completed successfully. Command-line tools 13.0 installed
SDK 34 successfully after the current SDK CLI failed to parse legacy package names.

Draft PR #2 was created. Initial Android CI run 35304928928 on the foundation commit
compiled `:app:compileDebugKotlin` successfully but `gradle :app:lintDebug` failed
with `PermissionImpliesUnsupportedChromeOsHardware`: the legacy CAMERA permission
had no optional camera feature declaration. The fix declares that hardware optional;
it adds no permission and suppresses no lint rule. Unit tests and assembleDebug were
skipped in that initial run, so they were not passes.

The repaired `.github/workflows/android-ci.yml` invokes `./gradlew --version`,
`./gradlew :app:lintDebug`, `./gradlew :app:testDebugUnitTest`,
`./gradlew :app:assembleDebug` and `./gradlew :app:check` on JDK 17. Test/lint reports
are uploaded even on failure. Security and Play release workflows are unchanged.
The local hardening verification used these process-only environment settings:
`JAVA_HOME=.cache/toolchain/jdk/jdk-17.0.20.1+1`, `ANDROID_HOME=.cache/android-sdk`,
and `GRADLE_USER_HOME=.cache/gradle-home` (all resolved to absolute paths).

| Command executed on 2026-09-18 | Result |
| --- | --- |
| `.\gradlew.bat :app:tasks --all --console=plain` | Exit 0, BUILD SUCCESSFUL in 2m 3s |
| `.\gradlew.bat :app:testDebugUnitTest :app:lintDebug :app:check :app:assembleDebug --continue --console=plain` | Exit 0, BUILD SUCCESSFUL in 3m 44s; 81 tasks executed |

The combined invocation compiled debug and release Kotlin, executed **29 tests in
each variant** (58 executions; zero failures/errors/skips), completed lint and check,
and assembled `app/build/outputs/apk/debug/app-debug.apk` (27,995,277 bytes).
`--continue` collected independent task outcomes; it did not suppress any failure.
Lint reported **0 errors and 12 warnings**: dependency-update notices, the existing
backup-configuration notice and missing launcher icon. Those were not suppressed
or used to justify unrelated upgrades. Legacy PhoneCoreAPI retains its deprecation
warning. No runtime source was excluded from compilation.

Final GitHub CI verification is still in progress; consult Draft PR #2 for the
exact tested head. Local success alone is not a READY verdict.

Added JUnit tests cover Week 1, locked roadmap, selection/completion/restoration,
URL validation, connection transitions/retry/cancellation, identity invariants,
request context, pending-request guards, unavailable/error states and cancellation.
Resource XML parsing and diff whitespace checks are performed separately; they
do not establish Android compilation or runtime correctness.

Robolectric tests exercise actual Compose navigation/back, disabled integration
actions, a draft, a 320dp-wide screen at 1.5x font scale, Android preference
restoration and browser intent success/missing-handler/security-error paths.
They are JVM smoke tests, not emulator/device or screenshot tests.

Not exercised: real GitHub login, AI replies, actual repository changes,
deployment, a real browser, TalkBack, OS process death, physical keyboard/IME
behavior or device smoke tests. Real integration cannot work until the listed prerequisites
are supplied. No signed release or Play upload is part of this task.

## Out of scope

Slack, Google Drive, Dropbox, Google Calendar, Gmail, payments, subscriptions,
wallet, voice speed, multiple AI voices, student LLM configuration/API-key entry,
provider/model selection, advanced AI/developer settings and Weeks 2–8 implementation.
No changes to WebKurierPhoneCore or any other repository. No merge.
