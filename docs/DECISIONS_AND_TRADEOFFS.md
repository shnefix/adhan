# Engineering Decisions & Tradeoffs

Every architecture involves tradeoffs. This document records critical design choices made in Prayer Alarm, the alternatives considered, and the technical justification.

---

## 1. No GPS Permission vs. Automatic Geolocation

- **Decision**: Zero GPS permission. Manual coordinate entry and preloaded Tunisian city catalog.
- **Alternatives Considered**: Using Google Play Services `FusedLocationProviderClient` to detect user coordinates automatically.
- **Reasoning**:
  - *Battery Life*: Continuous location tracking drains battery unnecessarily.
  - *Privacy*: Users should not have to share their physical coordinates with any app to know when to pray.
  - *Determinism*: Cities do not move. Selecting a city once from a list is completely reliable and works indoors or in airplane mode.
- **Tradeoff**: When traveling to a new city, the user must select it manually.

---

## 2. On-Device Astronomical Math vs. Cloud Prayer APIs (e.g. Aladhan API)

- **Decision**: 100% on-device astronomical calculation.
- **Alternatives Considered**: Fetching daily timings from REST APIs.
- **Reasoning**:
  - Cloud APIs introduce network latency, require internet access, can suffer outages, and can change their backend algorithms without notice.
  - An alarm must wake the user up even if their phone has no internet connection or is in flight mode during the night.
- **Tradeoff**: We must implement, verify, and unit-test the spherical trigonometry algorithms directly in Kotlin.

---

## 3. Jetpack Compose vs. Legacy XML Views

- **Decision**: 100% Jetpack Compose with Material 3.
- **Alternatives Considered**: XML layouts with `ConstraintLayout`, `RecyclerView`, and `ViewBinding`.
- **Reasoning**:
  - Compose is Google's modern, recommended declarative framework.
  - Far fewer lines of code, seamless dynamic theme switching, eliminates view-binding boilerplate, and provides first-class state observation with Kotlin `StateFlow`.
- **Tradeoff**: Requires team familiarity with declarative UI and recomposition lifecycle.

---

## 4. Native `AlarmManager` vs. Android `WorkManager` for Wakeups

- **Decision**: Use `AlarmManager.setExactAndAllowWhileIdle()` for waking alarms.
- **Alternatives Considered**: `WorkManager.enqueueUniquePeriodicWork()`.
- **Reasoning**:
  - Google's official documentation explicitly specifies that `WorkManager` is for deferrable background jobs. In deep Doze mode, periodic workers are batched and delayed by up to 30+ minutes.
  - A Fajr alarm scheduled for 05:10 must not ring at 05:42.
- **Tradeoff**: Requires exact alarm permissions and explicit handling of phone reboots via `RECEIVE_BOOT_COMPLETED`.

---

## 5. Decision Revision History

| Date | Topic | Decision | Status |
| :---: | :--- | :--- | :---: |
| 2026-10-29 | Build System | Use Gradle 8.11.1 + AGP 8.7.3 with Kotlin 2.0.21 | **Verified & Passing** |
| 2026-10-29 | Java Compatibility | Target Java 17 bytecode | **Verified & Passing** |
| 2026-10-29 | SDK Targets | compileSdk 35, minSdk 26, targetSdk 35 | **Verified & Passing** |

