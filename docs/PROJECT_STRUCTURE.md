# Project Directory & File Structure

Complete directory layout through **Stage 3: Location Management with Room**.

```
adhan/
├── build.gradle.kts                          # Root build script
├── settings.gradle.kts                       # Root project settings & repos
├── gradle.properties                         # JVM arguments & AndroidX flags
├── gradle/
│   ├── libs.versions.toml                    # Version catalog (Room, KSP, Compose)
│   └── wrapper/
│       ├── gradle-wrapper.jar
│       └── gradle-wrapper.properties         # Gradle 8.11.1 distribution
├── app/
│   ├── build.gradle.kts                      # Module build script with Room & KSP
│   ├── src/
│   │   ├── main/
│   │   │   ├── AndroidManifest.xml           # Manifest declarations
│   │   │   ├── java/com/example/prayeralarm/
│   │   │   │   ├── MainActivity.kt           # Single activity entry point
│   │   │   │   ├── PrayerAlarmApplication.kt # Application class & Service Locator DI
│   │   │   │   ├── data/
│   │   │   │   │   ├── local/
│   │   │   │   │   │   ├── PrayerAlarmDatabase.kt # Room database definition
│   │   │   │   │   │   ├── dao/
│   │   │   │   │   │   │   └── LocationDao.kt     # Room DAO with Flow & suspend CRUD
│   │   │   │   │   │   └── entity/
│   │   │   │   │   │       └── LocationEntity.kt  # Room SQLite table mapping
│   │   │   │   │   ├── preferences/
│   │   │   │   │   │   └── UserPreferencesManager.kt # DataStore for active location ID
│   │   │   │   │   └── repository/
│   │   │   │   │       ├── LocationRepository.kt     # Repository interface
│   │   │   │   │       └── LocationRepositoryImpl.kt # Room + DataStore integration
│   │   │   │   ├── domain/
│   │   │   │   │   └── model/
│   │   │   │   │       ├── PrayerType.kt             # Fajr, Dhuhr, Asr, Maghrib, Isha
│   │   │   │   │       └── SavedLocation.kt          # Domain model with coordinate properties
│   │   │   │   ├── ui/
│   │   │   │   │   ├── home/
│   │   │   │   │   │   ├── HomeScreen.kt             # Prayer times screen
│   │   │   │   │   │   ├── HomeUiState.kt            # Immutable state model
│   │   │   │   │   │   ├── HomeViewModel.kt          # Manages countdown & active location observation
│   │   │   │   │   │   ├── PrayerScheduleItem.kt     # Timetable item model
│   │   │   │   │   │   └── components/
│   │   │   │   │   │       ├── HomeHeader.kt         # Location pill header
│   │   │   │   │   │       ├── NextPrayerCard.kt     # Hero countdown card
│   │   │   │   │   │       └── PrayerTimetable.kt    # Daily prayer timetable
│   │   │   │   │   ├── locations/
│   │   │   │   │   │   ├── LocationsScreen.kt        # Saved locations list & empty state
│   │   │   │   │   │   ├── LocationsUiState.kt       # Immutable locations UI state
│   │   │   │   │   │   ├── LocationsViewModel.kt     # Handles CRUD, selection & validation
│   │   │   │   │   │   └── components/
│   │   │   │   │   │       ├── AddEditLocationDialog.kt   # Input dialog with validation
│   │   │   │   │   │       └── LocationItemRow.kt         # Location item with radio & actions
│   │   │   │   │   ├── navigation/
│   │   │   │   │   │   ├── MainScreen.kt             # Navigation container & ViewModel wiring
│   │   │   │   │   │   └── Screen.kt                 # Sealed routes (Home, Locations, Alarms, Settings)
│   │   │   │   │   └── theme/
│   │   │   │   │       ├── Color.kt                  # Dark navy & Islamic gold colors
│   │   │   │   │       ├── Theme.kt                  # Material 3 dark theme setup
│   │   │   │   │       └── Type.kt                   # Typography definitions
│   │   │   │   └── util/
│   │   │   │       └── LocationValidator.kt          # Validates lat, lon, name, and IANA timezone
│   │   │   └── res/
│   │   │       └── values/
│   │   │           ├── colors.xml
│   │   │           ├── strings.xml
│   │   │           └── themes.xml
│   │   └── test/
│   │       └── java/com/example/prayeralarm/
│   │           ├── data/repository/
│   │           │   └── LocationRepositoryTest.kt     # Repository unit tests (Fake DAO & Flow)
│   │           └── util/
│   │               └── LocationValidatorTest.kt      # Validation logic unit tests
└── docs/                                             # Technical documentation suite
    ├── ARCHITECTURE.md
    ├── CHANGELOG.md
    ├── DATA_AND_PERSISTENCE.md
    ├── DECISIONS_AND_TRADEOFFS.md
    ├── JETPACK_COMPOSE.md
    ├── LEARNING_NOTES.md
    ├── PROJECT_STRUCTURE.md
    ├── README.md
    └── TESTING_AND_DEBUGGING.md
```
