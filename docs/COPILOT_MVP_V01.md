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
preferences rather than crashing. Authentication is not persisted. Phase 3 stores
separate conversation histories and drafts for days 1, 2 and 3 on this device.

## Architecture and localization

MainActivity now creates PilotDependencies and the Compose PilotApp. The existing
explicit dependency-construction and lightweight navigation approach is retained;
there is no new production framework or runtime dependency. Test-only Robolectric and Compose test libraries exercise Android behavior on the JVM. Controller StateFlows drive UI;
composition-owned coroutines cancel on disposal and reset pending states. The
selected destination is persisted with a validated course fallback; system Back returns to the course. Switching sections or lessons starts at the top rather than reusing another section's scroll position. Navigation exposes tab selection semantics, and status changes use polite accessibility announcements.

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

CopilotService accepts the lesson, instruction, bounded prior conversation and verified project context at a
client boundary. UnconfiguredCopilot always returns Unavailable without network
traffic. Send is disabled while the shipped backend is unconfigured, with the prerequisite explanation beside the editable draft. A service-level Unavailable result also leaves the draft intact and explicitly says it was not sent. No canned
assistant answer, repository edit or deployment is presented as real. Exceptions
map to localized errors instead of exposing diagnostics. Input is capped at 2,000
characters; duplicate pending requests and lesson switches during sending are blocked.

## Phase 3 conversation foundation

The clean starting HEAD was `48ec258816866220d5a347058b3c9b8f1df4dd2c` on
`feat/webkurier-copilot-mvp-v01`. MainActivity still opens PilotApp directly.
Exactly three main destinations remain: Мой курс, Copilot, Мой проект / GitHub.
No communication Home, Translator, Voice, Wallet or generic AI navigation is added.

`PilotController` owns immutable conversation snapshots for each Week 1 lesson.
Entries have a monotonically increasing lesson-local ID and a STUDENT, COPILOT or
SYSTEM role. The list order is authoritative; wall-clock timestamps are not used.
Material 3 cards distinguish roles using both localized labels and container colors.
System messages use localized notice codes and polite live regions. Pending status,
task, lesson, project context, draft, send and local completion remain on the chat screen.

`ConversationStore` separates state from storage. Production uses versioned JSON in
private `pilot_conversations` SharedPreferences, alongside existing progress storage.
Each of days 1–3 has at most **40 entries**, each at most **8,000 UTF-16 characters**,
and a **2,000-character draft**. Oldest entries are dropped. Stored payloads above
2,000,000 characters per lesson are rejected before JSON parsing. Invalid versions,
roles, notice codes, text bounds, duplicate/out-of-order/overflow IDs and malformed
JSON recover to an empty conversation for that lesson. Invalid routes fall back to course.
Storage writes use Android SharedPreferences.apply; normal Activity recreation and
app restart retain state. Abrupt termination before the asynchronous disk flush
can lose the latest write. There is no cloud sync, history export or deletion UI.

Draft edits persist independently per lesson. Success clears the originating draft;
unavailable, exception and cancellation outcomes retain it. Pending sends reject
duplicate submissions, draft edits and lesson switches. Completion is self-reported
course progress only. Composition cancellation clears pending and records a safe
system notice; pending work is never restored or automatically resent after restart.
If a process dies during a request, its saved student entry and draft remain without
a confirmed answer; the client does not infer remote success.

Requests carry the day, trimmed instruction, verified assigned project if provided
by GitHubAuthService, and at most **12 prior non-system entries from that lesson**.
The current instruction is not duplicated in that context. Unavailable/error text
never becomes a fabricated assistant answer. Empty service replies become safe errors;
long replies are bounded locally. No HTTP endpoint or provider SDK was added.
Future real adapters still require Android → WebKurier backend/Core → Training Agent
→ authorized AI/tools → Android. Server responsibilities remain outside Android.

No credentials, session tokens, GitHub identity or project assignment are serialized
by the new store. Conversation text is private app data, not encrypted by this store;
Android backup remains disabled. No dependency, permission or CI policy was changed.

### Phase 3 local verification

Completed on 2026-09-19; final resource-change verification repeated on 2026-09-20.
Exact successful invocation (PowerShell, existing ignored toolchain helper):

```powershell
$env:ANDROID_USER_HOME = Join-Path $PWD '.cache/android-user'
$env:JAVA_TOOL_OPTIONS = '-Duser.home=C:\Users\User'
.\.cache\run-gradle.ps1 :app:testDebugUnitTest :app:testReleaseUnitTest :app:lintDebug :app:check :app:assembleDebug :app:assembleRelease --continue --no-daemon --console=plain
```

The helper invokes gradlew.bat using cached JDK 17, Android SDK 34 and Gradle 8.7.
The first sandboxed attempt was interrupted after Kotlin could not write its daemon
marker under AppData. The retry outside the sandbox completed BUILD SUCCESSFUL in
1m 23s, 106 actionable tasks (34 executed, 72 up-to-date). After correcting the draft
persistence hint, the same command passed again in 1m 16s (28 executed, 78 up-to-date).
Logs: ignored `.cache/phase3-verification-2.log` and `.cache/phase3-final-verification.log`.
No passing claim is made for the first attempt.

| Task | Result |
| --- | --- |
| `:app:testDebugUnitTest` | PASS: 51 tests, 0 failures/errors/skips |
| `:app:testReleaseUnitTest` | PASS: 51 tests, 0 failures/errors/skips |
| `:app:lintDebug` | PASS: 0 errors, 12 warnings |
| `:app:check` | PASS |
| `:app:assembleDebug` | PASS: app-debug.apk, 28,101,613 bytes |
| `:app:assembleRelease` | PASS: app-release-unsigned.apk, 21,299,982 bytes |

Per variant: LocalConversationStoreTest 7, PilotAndroidTest 3, PilotControllerTest 12,
PilotConversationTest 12, PilotModelTest 11, PilotUiTest 6. This adds 22 tests per
variant to the 29-test baseline. Tests cover roles/order, all three lesson histories
and drafts, length limits, cancellation/duplicate sends, bounded request context,
storage corruption and bounds, route/progress restoration, Activity recreation,
three tab semantics, Back, unavailable backend and role labels at 320dp/1.5x font.
Robolectric/Compose tests are JVM smoke tests; no real TalkBack, device, process-kill,
OAuth, AI/backend, repository mutation or deployment test was performed.

Baseline CI for `48ec258816866220d5a347058b3c9b8f1df4dd2c`: Android run
35367800360 passed; CodeQL job 105674354287 passed. Dependency Review job
105674354588 failed with “Dependency review is not supported on this repository”
and asked to enable Dependency graph. This predates Phase 3. Security settings and
workflow enforcement remain unchanged. Final pushed-SHA CI is recorded in Draft PR #2.

Earlier verification sections below describe historical foundation/hardening runs only.

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
