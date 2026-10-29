# Jetpack Compose & Material 3 Design System

Jetpack Compose is Android's modern declarative toolkit for building native UI. It replaces the legacy XML-based view system (`setContentView(R.layout.activity_main)` and `findViewById`).

---

## 1. Declarative UI vs. Imperative UI

- **Legacy Imperative (XML Views)**:
  You define a static XML layout and then imperatively mutate it in code:
  `myTextView.text = "04:30"`, `myTextView.visibility = View.VISIBLE`.
  *Problem*: Prone to state sync bugs, race conditions, and null view references.
- **Modern Declarative (Compose)**:
  UI is a mathematical function of current state: `UI = f(State)`.
  When state changes, Compose re-executes the composable function (**Recomposition**) to reflect the new state.

```
State (e.g. countdown = "02:14:30")
        │
        ▼
   Composable Function ────► Screen Canvas
```

---

## 2. Real Stage 2 Implementation Breakdown

In **Stage 2**, we implemented the entire Home Screen presentation layer using small, focused, reusable composables.

### Component Hierarchy:
```
MainScreen (Scaffold with Bottom NavigationBar)
   └── NavHost (startDestination = "home")
         └── HomeScreen (Scrollable Column)
               ├── HomeHeader (Location Pill, Gregorian & Hijri Dates, Settings Action)
               ├── NextPrayerCard (Highlighted Next Prayer, Arabic Subtitle, Live Countdown Boxes)
               └── PrayerTimetable (List of 5 Prayers: Fajr, Dhuhr, Asr, Maghrib, Isha)
                     └── PrayerTimetableRow (Prayer Icon, Names, Time, Alert Notification Toggle)
```

---

## 3. Detailed Walkthrough of Stage 2 Composables

### A. `HomeHeader` (`ui/home/components/HomeHeader.kt`)
- **Role**: Displays the top app context.
- **Features**:
  - A tactile Location Selector Pill (`Surface` with `clip(RoundedCornerShape(20.dp))` and `clickable`) showing the active city and a dropdown chevron.
  - A Settings icon button.
  - Formatted Gregorian date (`EEEE, d MMMM yyyy`) and traditional Hijri date.
- **Parameters**:
  ```kotlin
  @Composable
  fun HomeHeader(
      locationName: String,
      currentDate: LocalDate,
      hijriDate: String,
      onLocationClick: () -> Unit,
      onSettingsClick: () -> Unit,
      modifier: Modifier = Modifier
  )
  ```

### B. `NextPrayerCard` (`ui/home/components/NextPrayerCard.kt`)
- **Role**: The hero focal card of the application.
- **Visual Design**:
  - Vertical gradient background (`NavyCard` to `NavySurface`).
  - Subtle gold border stroke (`border(1.dp, GoldPrimary.copy(alpha = 0.35f))`).
  - Labeled "Sample Mock" badge when `isSampleData == true` so preview data is never confused with real astronomical calculations.
  - Dual Arabic and English title (`Dhuhr` / `الظهر`).
  - Three distinct countdown unit cards (`HOURS`, `MINS`, `SECS`) styled in dark navy boxes with bold gold numerals.

### C. `PrayerTimetable` & `PrayerTimetableRow` (`ui/home/components/PrayerTimetable.kt`)
- **Role**: Displays the 5 daily prayers in chronological order.
- **Dynamic Styling**:
  - The upcoming prayer (`item.isNextPrayer == true`) is dynamically elevated:
    - Card container becomes `NavyCard` instead of `NavySurface`.
    - Border turns to glowing `GoldPrimary`.
    - Distinct gold `NEXT` badge appears beside the name.
    - Icon container background shifts to `GoldContainer`.
  - Notification toggle button switches between `Icons.Default.Notifications` and `NotificationsOff`.

### D. `HomeScreen` (`ui/home/HomeScreen.kt`)
- **Role**: Root container for the home tab.
- **State Handling**: Accepts immutable `HomeUiState` and provides a `verticalScroll(rememberScrollState())` wrapper so the screen adapts cleanly to any phone display height or landscape orientation.

### E. `MainScreen` (`ui/navigation/MainScreen.kt`)
- **Role**: Coordinates top-level navigation using `Scaffold` and `NavigationBar`.
- **Navigation Graph**:
  - `home` -> `HomeScreen`
  - `locations` -> `PlaceholderScreen` (Stage 3 Room integration)
  - `alarms` -> `PlaceholderScreen` (Stage 7 Fajr alarm system)
  - `settings` -> `PlaceholderScreen` (Stage 10 settings)
- **Smooth Navigation**: Uses `popUpTo(findStartDestination().id) { saveState = true }` and `launchSingleTop = true` to preserve user scroll state between tabs.

---

## 4. Recomposition & State Management Explained

Inside `HomeViewModel`:
```kotlin
private val _uiState = MutableStateFlow(createInitialSampleState())
val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
```
When `startMockCountdown()` decrements seconds:
1. `_uiState.update { current -> current.copy(remainingSeconds = ...) }` creates a new instance of `HomeUiState`.
2. Jetpack Compose observes `val uiState by homeViewModel.uiState.collectAsState()`.
3. Because Compose uses smart skipping, **only** the `NextPrayerCard` and `CountdownTimeUnit` composables are recomposed! The rest of the screen (Header, NavigationBar, other rows) does not waste CPU cycles re-rendering.

---

## 5. Compose Previews in Android Studio

You can inspect the UI without deploying to a phone using `@Preview`:
```kotlin
@Preview(name = "Home Screen - Dark Islamic Theme", showBackground = true)
@Composable
fun HomeScreenPreview() {
    PrayerAlarmTheme(darkTheme = true) {
        HomeScreen(
            uiState = HomeViewModel.createInitialSampleState(),
            onLocationClick = {},
            onSettingsClick = {},
            onToggleNotification = {}
        )
    }
}
```

### How to use Compose Preview in Android Studio:
1. Open [`HomeScreen.kt`](file:///c:/Users/foura/adhan/app/src/main/java/com/example/prayeralarm/ui/home/HomeScreen.kt).
2. At the top right of the editor window, click the **Design** or **Split** view icon (`Split` shows code + preview side-by-side).
3. Android Studio will render the interactive preview with our dark navy and gold theme instantly.

---

## 6. Revision Questions

1. Why do we separate `PrayerTimetableRow` into its own composable instead of inlining a `Column` loop inside `HomeScreen`?
2. How does `launchSingleTop = true` improve user experience in bottom navigation?
3. What is the difference between `mutableStateOf` and `StateFlow`?
4. Why should countdown state produce a new immutable copy via `.copy()` rather than mutating a property directly?
