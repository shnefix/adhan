# Prayer Time Calculation Engine & Tunisian Standards

This document specifies the exact astronomical and juristic conventions governing Islamic prayer time calculation in Prayer Alarm, with focused verification of Tunisian standards.

---

## 1. Astronomical Foundations

Prayer times are governed by the position of the Sun relative to an observer at latitude $\phi$, longitude $\lambda$, and elevation $h$:

1. **Fajr (Dawn)**: Occurs when the morning twilight begins, defined by the Sun's center descending to a specific depression angle $\alpha_{\text{fajr}}$ below the eastern horizon.
2. **Sunrise (Shuruq)**: The instant the upper limb of the Sun touches the eastern astronomical horizon (considering atmospheric refraction of $0.5667^\circ$ and solar semi-diameter of $0.2667^\circ$, totalling $0.833^\circ$).
3. **Dhuhr (Noon)**: The instant the Sun crosses the local celestial meridian (solar transit). At this moment, shadows reach their minimum length for the day ($s_{\min}$).
4. **Asr (Afternoon)**: Occurs when the shadow of a vertical gnomon equals its minimum shadow plus a multiple $m$ of the gnomon's height ($s = s_{\min} + m \cdot h$).
   - Standard / Maliki / Shafi'i / Hanbali: $m = 1$.
   - Hanafi: $m = 2$.
5. **Maghrib (Sunset)**: The instant the upper limb of the Sun descends below the western horizon (same geometric refraction correction of $0.833^\circ$).
6. **Isha (Nightfall)**: Occurs when evening astronomical twilight disappears completely, defined by the Sun's center descending to a depression angle $\alpha_{\text{isha}}$ below the western horizon.

---

## 2. Verification of the Official Tunisian Method

### Authoritative Bodies
In the Republic of Tunisia, official Islamic prayer timetables are computed by the **Institut National de la Météorologie (INM)** (Service de l'Astronomie et des Éphémérides) in coordination with the **Ministère des Affaires Religieuses** ([meteo.tn](https://www.meteo.tn)).

### Conventions & Parameters
- **Fajr Angle**: **$18.0^\circ$**
- **Isha Angle**: **$18.0^\circ$**
- **Asr Juristic Convention**: **Maliki ($m = 1$)**, identical geometrically to the Shafi'i convention.
- **Dhuhr Transit Buffer**: Solar transit plus a safety offset of $1$ to $2$ minutes after true solar noon.
- **Maghrib**: Sunset ($0.833^\circ$ depression of Sun's center) with official 1-minute buffer.
- **Timezone**: `Africa/Tunis` (UTC+1 year-round; Tunisia abolished daylight saving time in 2008).

### Isolated Configuration Design
Because calculation parameters may vary in regional micro-conventions or future ministerial decrees, calculation settings are isolated behind a clean interface:
```kotlin
data class CalculationSettings(
    val fajrAngle: Double = 18.0,
    val ishaAngle: Double = 18.0,
    val asrJuristicMethod: AsrMethod = AsrMethod.STANDARD, // m = 1 (Maliki)
    val dhuhrAdjustmentMinutes: Int = 1,
    val maghribAdjustmentMinutes: Int = 1
)
```
This ensures zero coupling between the UI and astronomical calculation routines.

---

## 3. Library Selection: `com.batoulapps.adhan:adhan:1.2.1`

Instead of reimplementing Jean Meeus's spherical trigonometry and solar ephemeris algorithms from scratch—which risks numerical edge cases during equinoxes or leap years—we select **Batoul Apps' Adhan library**:
- **Artifact**: `com.batoulapps.adhan:adhan:1.2.1` on Maven Central.
- **Pedigree**: Implements astronomical algorithms recommended by the U.S. Naval Observatory. Zero external runtime dependencies.
- **Customizable**: Allows direct configuration of arbitrary Fajr ($18.0^\circ$) and Isha ($18.0^\circ$) angles with `Madhab.SHAFI` ($m=1$).
- **Offline & Autonomous**: Runs entirely on-device with zero network access, tracking, or battery overhead.

---

## 4. Verification of Default Coordinates

The three default locations for Prayer Alarm are verified against Open-Meteo / GeoNames geodetic databases:

| Location | Latitude (Decimal) | Longitude (Decimal) | IANA Timezone | Elevation | Verification Source |
| :--- | :---: | :---: | :---: | :---: | :--- |
| **Mahdia, Tunisia** | `35.5047` | `11.0622` | `Africa/Tunis` | ~8m | Open-Meteo / GeoNames (`35.50472, 11.06222`) |
| **Kelibia, Tunisia** | `36.8476` | `11.0939` | `Africa/Tunis` | ~29m | Open-Meteo / GeoNames (`36.8476, 11.0939`) |
| **Ariana, Tunisia** | `36.8625` | `10.1956` | `Africa/Tunis` | ~16m | Open-Meteo / GeoNames (`36.8601 - 36.8625, 10.1956`) |

*Note: While all three locations share `Africa/Tunis` (UTC+1), their distinct longitudes and latitudes result in genuine astronomical offsets of several minutes between them (e.g. solar transit occurs earlier in Kelibia than in Ariana).*

---

## 5. Safe Database Migration Policy

To transition from legacy pre-populated cities (Tunis, Sfax, Sousse, etc.) to the new defaults without deleting user-created locations:
1. **Room Database Version**: Increment version from `1` to `2`.
2. **Migration Plan**:
   - Provide a safe `Migration(1, 2)`.
   - Insert Mahdia, Kelibia, and Ariana if their names do not already exist.
   - Set the active selection in DataStore to Mahdia only if no active selection exists or if the existing active selection was the legacy Tunis default.
   - Preserve all user-created records untouched.
