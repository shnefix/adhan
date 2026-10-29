# Prayer Time Calculation Engine & Countdown Timing

This document details the astronomical calculation engine and real-time countdown synchronization implemented in Prayer Alarm.

---

## 1. Countdown Discrepancy: Diagnosis & Root Cause

### The Problem
Previously, the countdown timer on the Home Screen displayed inaccurate remaining durations and drifted over time.

### Root Cause
1. **Mock Countdown Loop**: The previous `HomeViewModel` initialized remaining time to an arbitrary sample (`2h 14m 30s`) and ran a `while (isActive) { delay(1000L); ... }` loop that blindly decremented `seconds`, `minutes`, and `hours` as standalone integers.
2. **Timing Drift**: Calling `delay(1000L)` inside a coroutine loop accumulates scheduling latency (drift) on every iteration.
3. **No Reference to True Timestamps**: When a user selected a different location, the countdown did not recalculate from the target prayer's astronomical timestamp.

### The Corrected Solution
1. **Prayer Timestamps as Source of Truth**:
   Prayer times are computed as full `ZonedDateTime` instances anchored to the location's IANA timezone via `com.example.prayeralarm.domain.calculator.PrayerTimeCalculator`.
2. **Instant Difference**:
   Remaining time is calculated using `java.time.Duration.between(now, targetPrayerZonedDateTime.toInstant())`.
3. **Midnight & Post-Isha Rollover**:
   When all five daily prayers for today have passed, `computeNextPrayerAndCountdown()` computes tomorrow's Fajr timestamp so the countdown smoothly targets tomorrow morning's dawn prayer.
4. **Drift-Free Ticker**:
   The ticker ticks once per second, but on each tick it reads the real clock instant `Instant.now(clock)`. It never relies on internal decrement counters.
5. **Cached Day-Boundary Calculation**:
   The next day's timetable is calculated once when the active day's timetable is refreshed. It is reused after Isha instead of recalculating astronomical data on every one-second tick.

---

## 2. Astronomical Engine Architecture

```
┌────────────────────────────────────────────────────────┐
│            SavedLocation (Lat, Lon, TimeZone)          │
└───────────────────────────┬────────────────────────────┘
                            │
                            ▼
┌────────────────────────────────────────────────────────┐
│               PrayerTimeCalculator                     │
│    • Coordinates(latitude, longitude)                  │
│    • DateComponents(year, month, day)                  │
│    • CalculationParameters (18.0° Fajr, 18.0° Isha)   │
│    • Madhab.SHAFI (Maliki m=1 Asr)                     │
└───────────────────────────┬────────────────────────────┘
                            │
              ┌─────────────┴─────────────┐
              ▼                           ▼
┌───────────────────────────┐ ┌───────────────────────────┐
│   CalculatedTimetable     │ │    NextPrayerCountdown    │
│  • List<PrayerScheduleItem│ │  • nextPrayerItem         │
│  • Map<PrayerType, ZonedDT│ │  • remainingHours/Mins/Sec│
└───────────────────────────┘ └───────────────────────────┘
```

---

## 3. Unit Test Verification

Deterministic tests in `PrayerTimeCalculatorTest.kt`:
- `calculateTimetable_producesChronologicalPrayers`: Verifies that Fajr < Dhuhr < Asr < Maghrib < Isha.
- `differentLocations_produceDistinctPrayerTimes`: Verifies that locations with differing longitudes (e.g. Mahdia vs. Ariana) produce distinct prayer times.
- `computeNextPrayerAndCountdown_beforeFajr`: Verifies accurate remaining duration before dawn.
- `computeNextPrayerAndCountdown_betweenDhuhrAndAsr`: Verifies accurate remaining duration between midday prayers.
- `computeNextPrayerAndCountdown_afterIsha_rollsOverToTomorrowFajr`: Verifies midnight / post-Isha transition to tomorrow's Fajr.
- `computeNextPrayerAndCountdown_lessThanOneMinute_preservesSeconds`: Verifies sub-minute precision.
- `computeNextPrayerAndCountdown_atPrayerBoundary_advancesToNextPrayer`: Verifies strict timestamp-boundary selection.

The verified local command was `.\gradlew.bat testDebugUnitTest`; all 18 unit tests passed. The debug APK was also built successfully with `.\gradlew.bat assembleDebug`.
