# Gradle wrapper provenance

The wrapper is Gradle **8.7**, compatible with the unchanged Android Gradle Plugin
8.6.1 and JDK 17. Both `gradlew` and `gradlew.bat` are generated scripts.

The original `gradle-wrapper.jar` was not a ZIP/JAR: it contained 611 bytes of
ProGuard text, SHA-256 `8e870c33d4dabf25199f5e3efcb720807efc7475ef29e0bb420b2b6abd5cf19f`.
That text is preserved at the already configured `app/proguard-rules.pro` path.

On 2026-09-18 the wrapper was regenerated in an empty bootstrap project using the
official `https://services.gradle.org/distributions/gradle-8.7-bin.zip`, after
checking its SHA-256 against the official `.sha256` companion file:

```text
544c35d6bd849ae8a5ed0bcea39ba677dc40f49df7d1835561582da2009b961d
```

Exact generation task (run with that verified Gradle binary and Temurin JDK 17):

```text
gradle -p .cache/wrapper-bootstrap wrapper --gradle-version 8.7 --distribution-type bin --gradle-distribution-sha256-sum 544c35d6bd849ae8a5ed0bcea39ba677dc40f49df7d1835561582da2009b961d --no-daemon
```

Result: BUILD SUCCESSFUL, one executed task. Generated wrapper JAR SHA-256:
`cb0da6751c2b753a16ac168bb354870ebb1e162e9083f116729cec9c781156b8`.
The generated JAR, scripts and properties were copied without hand-editing their
implementation. The distribution checksum is committed in the wrapper properties.
No arbitrary wrapper binary or dependency upgrade was used.
