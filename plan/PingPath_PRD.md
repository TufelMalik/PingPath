# ProximAlert — Product Requirements Document

> **Version:** 1.0 — MVP  
> **Platform:** Android (Jetpack Compose + Kotlin)  
> **Status:** Ready for Development  
> **Last Updated:** April 2026

---

## Table of Contents

1. [Project Prompt](#project-prompt)
2. [Overview](#overview)
3. [Goals & Non-Goals](#goals--non-goals)
4. [Tech Stack](#tech-stack)
5. [Architecture](#architecture)
6. [Screens & User Flow](#screens--user-flow)
7. [Permissions](#permissions)
8. [API Reference](#api-reference)
9. [Development Plan & Checklists](#development-plan--checklists)
10. [Known Constraints & Risks](#known-constraints--risks)
11. [Future Scope (V2+)](#future-scope-v2)

---

## Project Prompt

> Build an Android application in **Jetpack Compose + Kotlin** that allows a user to search for a destination on an **OpenStreetMap** map, configure a location-based alert (either by proximity radius OR ETA in minutes), and receive a **full-screen alarm-style notification** when the alert condition is met — even when the app is running in the background or the screen is locked.
>
> The app requires **GPS location** and an **active internet connection** at all times. No login or user account is needed. Only **one active alert** is allowed at a time in V1.

---

## Overview

**ProximAlert** solves a common travel problem: missing your stop or destination when travelling by bus, train, or car.

The user sets a destination, chooses how they want to be alerted (proximity or ETA), and the app monitors their location silently in the background via a Foreground Service. When the condition is met, a full-screen alarm fires — sound, vibration, and a dismissible overlay — regardless of whether the app is open or the screen is locked.

---

## Goals & Non-Goals

### Goals (V1)
- User can search and select any destination via OpenStreetMap / Nominatim
- User can choose between **Proximity mode** (X metres away) or **ETA mode** (X minutes away)
- Only one alert active at a time
- Background location tracking via Foreground Service
- Full-screen alarm with sound + vibration on alert trigger
- Works on locked screen
- No login, no account — fully offline-capable except map tiles & ETA

### Non-Goals (V1)
- Multiple simultaneous alerts
- Turn-by-turn navigation
- Offline map tile caching
- Alert history / trip logs
- Repeat / scheduled alerts
- Custom ringtone selection
- iOS / KMP support
- User accounts or cloud sync

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| UI | Jetpack Compose |
| Architecture | MVVM + Clean Architecture |
| Map | OSMDroid (OpenStreetMap) |
| Location Search | Nominatim REST API |
| ETA Routing | OSRM Public API |
| Location Tracking | FusedLocationProviderClient |
| Background Service | Foreground Service |
| Alarm Delivery | Full-screen Intent + NotificationManager |
| Dependency Injection | Hilt |
| Networking | Retrofit + OkHttp |
| Local Storage | DataStore Preferences |
| Min SDK | API 26 (Android 8.0) |
| Target SDK | API 35 |

---

## Architecture

```
app/
└── src/
    └── main/
        ├── kotlin/
        │   └── com/bilty/generator/
        │       ├── App.kt                          # @HiltAndroidApp Application class
        │       ├── AppNavigation.kt                # Root NavGraph + route definitions
        │       │
        │       ├── di/                             # Hilt dependency injection
        │       │   ├── AppModule.kt                # Retrofit, OkHttp, DataStore, FusedLocation
        │       │   ├── helperModules/
        │       │   │   └── HelperModule.kt         # NotificationHelper, PermissionHelper, DistanceUtils
        │       │   ├── repositoryModules/
        │       │   │   └── RepositoryModule.kt     # Binds repo interfaces to implementations
        │       │   └── viewModelModules/
        │       │       └── ViewModelModule.kt      # ViewModel-scoped bindings (if needed)
        │       │
        │       ├── model/                          # Shared data layer
        │       │   ├── constants/
        │       │   │   └── AppConstants.kt         # Poll intervals, default thresholds, API base URLs
        │       │   ├── data/
        │       │   │   ├── Destination.kt          # lat, lon, displayName
        │       │   │   ├── AlertConfig.kt          # mode, threshold, destination
        │       │   │   └── EtaResult.kt            # durationSeconds, distanceMetres
        │       │   ├── enums/
        │       │   │   └── AlertMode.kt            # PROXIMITY, ETA
        │       │   ├── interfaces/
        │       │   │   ├── LocationRepository.kt   # getUserLocation(): Flow<Location>
        │       │   │   ├── SearchRepository.kt     # searchDestination(query): List<Destination>
        │       │   │   └── EtaRepository.kt        # getEta(from, to): EtaResult
        │       │   └── response/
        │       │       ├── NominatimResponse.kt    # API response model for search
        │       │       └── OsrmResponse.kt         # API response model for ETA routing
        │       │
        │       ├── repository/                     # Interface implementations
        │       │   ├── LocationRepositoryImpl.kt   # FusedLocationProviderClient wrapper
        │       │   ├── SearchRepositoryImpl.kt     # Nominatim API calls via Retrofit
        │       │   └── EtaRepositoryImpl.kt        # OSRM API calls via Retrofit
        │       │
        │       ├── modules/                        # Feature modules
        │       │   │
        │       │   ├── permission/                 # Permission onboarding (first launch)
        │       │   │   ├── components/
        │       │   │   │   └── PermissionItemRow.kt
        │       │   │   ├── navigation/
        │       │   │   │   └── PermissionNavigation.kt
        │       │   │   ├── ui/
        │       │   │   │   └── PermissionScreen.kt
        │       │   │   └── viewModel/
        │       │   │       └── PermissionViewModel.kt
        │       │   │
        │       │   ├── home/                       # Map screen + active alert state
        │       │   │   ├── components/
        │       │   │   │   ├── OsmMapView.kt       # AndroidView wrapper for OSMDroid
        │       │   │   │   ├── SearchBar.kt        # Top search input
        │       │   │   │   ├── ActiveAlertSheet.kt # Bottom sheet shown while alert is running
        │       │   │   │   └── NoInternetBanner.kt
        │       │   │   ├── navigation/
        │       │   │   │   └── HomeNavigation.kt
        │       │   │   ├── ui/
        │       │   │   │   └── HomeScreen.kt
        │       │   │   └── viewModel/
        │       │   │       └── HomeViewModel.kt    # Holds map state, active alert state, location
        │       │   │
        │       │   ├── search/                     # Destination search results
        │       │   │   ├── components/
        │       │   │   │   └── SearchResultItem.kt
        │       │   │   ├── navigation/
        │       │   │   │   └── SearchNavigation.kt
        │       │   │   ├── ui/
        │       │   │   │   └── SearchScreen.kt
        │       │   │   └── viewModel/
        │       │   │       └── SearchViewModel.kt  # Debounced search, result list state
        │       │   │
        │       │   ├── setup/                      # Alert configuration screen
        │       │   │   ├── components/
        │       │   │   │   ├── AlertModeToggle.kt  # Proximity / ETA chip selector
        │       │   │   │   ├── ProximityOptions.kt # 500m / 1km / 2km / Custom
        │       │   │   │   └── EtaOptions.kt       # 5 / 10 / 15 / 20 min / Custom
        │       │   │   ├── navigation/
        │       │   │   │   └── SetupNavigation.kt
        │       │   │   ├── ui/
        │       │   │   │   └── SetupScreen.kt
        │       │   │   └── viewModel/
        │       │   │       └── SetupViewModel.kt   # Builds AlertConfig, triggers service start
        │       │   │
        │       │   └── alarm/                      # Full-screen alarm (separate Activity)
        │       │       ├── components/
        │       │       │   └── PulseAnimation.kt   # Animated icon on alarm screen
        │       │       ├── ui/
        │       │       │   └── AlarmScreen.kt      # Compose UI inside AlarmActivity
        │       │       └── AlarmActivity.kt        # showWhenLocked + turnScreenOn Activity
        │       │
        │       ├── service/
        │       │   └── AlertForegroundService.kt   # Location polling + alarm trigger logic
        │       │
        │       ├── theme/
        │       │   ├── Color.kt
        │       │   ├── Theme.kt
        │       │   └── Type.kt
        │       │
        │       ├── uiToolKit/                      # Reusable generic UI components
        │       │   ├── PrimaryButton.kt
        │       │   ├── ChipGroup.kt
        │       │   └── LoadingOverlay.kt
        │       │
        │       └── utils/
        │           ├── extensions/
        │           │   ├── ContextExtensions.kt    # showToast, isInternetAvailable, etc.
        │           │   └── LocationExtensions.kt   # Location.toLatLng(), etc.
        │           └── helpers/
        │               ├── DistanceHelper.kt       # Haversine distance calculation
        │               ├── NotificationHelper.kt   # Builds persistent + alarm notifications
        │               └── PermissionHelper.kt     # Permission check + rationale logic
        │
        └── res/
            ├── drawable/                           # Icons, pin marker, alert ring assets
            ├── font/                               # App font files
            ├── raw/                                # Alarm sound file (.mp3 / .ogg)
            └── values/
                ├── strings.xml
                ├── colors.xml
                └── themes.xml
```

---

## Screens & User Flow

### Screens

| # | Screen | Description |
|---|---|---|
| 01 | Home / Map | OSMDroid map centred on user. Search bar at top. FAB to start alert setup. |
| 02 | Search Destination | Nominatim-powered search with results list. Tap to pin destination on map. |
| 03 | Alert Setup | Choose mode (Proximity / ETA), configure value, preview ring on map. |
| 04 | Active Alert | Persistent bottom sheet showing destination, mode, and live distance. Cancel button. |
| 05 | Full-screen Alarm | Alarm-style overlay with sound + vibration. Dismiss or Snooze (5 min). |
| 06 | Permission Gate | First-launch permission request with rationale for location + notifications. |

### User Flow

```
App Launch
   │
   ├─► [Permission Gate] ─► Grant location + notification permissions
   │
   └─► [Home / Map]
          │
          ├─► Tap Search Bar
          │       └─► [Search Destination] ─► Type → Nominatim results
          │                                        └─► Tap result → pin on map
          │
          ├─► Tap FAB (after destination set)
          │       └─► [Alert Setup]
          │               ├─► Choose: Proximity or ETA
          │               ├─► Set value (radius / minutes)
          │               └─► Tap "Set Alert"
          │                       └─► Foreground Service starts
          │                               └─► [Active Alert] bottom sheet shown
          │
          └─► [While monitoring...]
                  ├─► User minimises app → Service keeps running
                  ├─► Location polled every 15–30s
                  └─► Condition met
                          └─► [Full-screen Alarm]
                                  ├─► Dismiss → Service stops, alert cleared
                                  └─► Snooze → Re-alerts in 5 minutes
```

---

## Permissions

| Permission | Why Needed |
|---|---|
| `ACCESS_FINE_LOCATION` | Real-time GPS while app is in foreground |
| `ACCESS_BACKGROUND_LOCATION` | Continue tracking when app is minimised |
| `FOREGROUND_SERVICE` | Run location monitoring service in background |
| `FOREGROUND_SERVICE_LOCATION` | Required for location-type foreground services (API 34+) |
| `POST_NOTIFICATIONS` | Show persistent service notification + alarm (API 33+) |
| `USE_FULL_SCREEN_INTENT` | Launch alarm screen over lock screen (API 34+ restricted) |
| `INTERNET` | Map tiles, Nominatim search, OSRM routing |
| `VIBRATE` | Vibration on alarm trigger |
| `WAKE_LOCK` | Wake screen when alarm fires on locked device |

> ⚠️ `ACCESS_BACKGROUND_LOCATION` must be requested separately after `ACCESS_FINE_LOCATION` is granted. Show a rationale screen before directing user to system settings.  
> ⚠️ `USE_FULL_SCREEN_INTENT` requires a Special App Access setting on Android 14+. Show a one-time guide.

---

## API Reference

### Nominatim (Search)
```
GET https://nominatim.openstreetmap.org/search
  ?q={query}
  &format=json
  &limit=5
  &addressdetails=1

Response: [ { display_name, lat, lon, ... } ]
```
> Add `User-Agent: ProximAlert/1.0` header — Nominatim policy requires it.

### OSRM (ETA Routing)
```
GET https://router.project-osrm.org/route/v1/driving/{userLon},{userLat};{destLon},{destLat}
  ?overview=false
  &annotations=false

Response: { routes: [ { duration, distance } ] }
```
> `duration` is in seconds. Poll every 30–60 seconds from the Foreground Service.

---

## Development Plan & Checklists

### Phase 0 — Project Setup

- [x] Create new Android project with Compose + Kotlin
  - [x] Set `minSdk = 26`, `targetSdk = 35` in `build.gradle`
  - [x] Enable `buildFeatures { compose = true }`
  - [x] Add Kotlin Symbol Processing (KSP) for Hilt
- [x] Add all dependencies to `libs.versions.toml`
  - [x] Jetpack Compose BOM
  - [x] Hilt + Hilt Navigation Compose
  - [x] OSMDroid (`org.osmdroid:osmdroid-android`)
  - [x] Retrofit + OkHttp + Gson converter
  - [x] DataStore Preferences
  - [x] Google Play Services Location (`play-services-location`)
  - [x] Coroutines + Flow
  - [x] Lifecycle ViewModel Compose
- [x] Set up `AndroidManifest.xml`
  - [x] Declare all permissions (see Permissions section)
  - [x] Declare `AlertForegroundService` with `foregroundServiceType="location"`
  - [ ] Declare `AlarmActivity` with `showWhenLocked` and `turnScreenOn` flags
- [x] Set up project package structure (as per Architecture section)
- [x] Set up Hilt `@HiltAndroidApp` in `Application` class
- [x] Set up NavGraph with all screen routes defined
- [x] Set up Material3 theme (colors, typography, shapes)

---

### Phase 1 — Permission Handling

- [x] Build `PermissionHelper` utility
  - [x] Check `ACCESS_FINE_LOCATION` granted status
  - [x] Check `ACCESS_BACKGROUND_LOCATION` granted status
  - [x] Check `POST_NOTIFICATIONS` granted status (API 33+)
  - [x] Check `USE_FULL_SCREEN_INTENT` special permission (API 34+)
- [x] Build Permission Gate screen (Compose)
  - [x] Show rationale for each permission with plain-language explanation
  - [x] Request `ACCESS_FINE_LOCATION` first
  - [x] After fine location granted, show rationale then request `ACCESS_BACKGROUND_LOCATION`
  - [x] Request `POST_NOTIFICATIONS` (API 33+)
  - [x] Show guide for `USE_FULL_SCREEN_INTENT` → deep-link to system Special App Access
  - [x] Handle "denied permanently" case with settings deep-link
- [x] Gate entry to Home screen behind all required permissions

---

### Phase 2 — Map & Search

- [x] Integrate OSMDroid map
  - [x] Add `osmdroid-android` dependency
  - [x] Create `OsmMapView` Compose wrapper using `AndroidView`
  - [x] Set tile source to `TileSourceFactory.MAPNIK`
  - [x] Set default zoom level (15) and centre on user's last known location
  - [x] Add user location marker (animated dot)
  - [x] Add destination marker (pin icon) — shown after search selection
  - [x] Add alert zone circle overlay (shown during setup and active alert)
  - [x] Handle map lifecycle (onResume / onPause) correctly
- [x] Build Home screen UI
  - [x] Search bar at top (non-functional placeholder in this phase, wired in next)
  - [x] FAB button — disabled until destination is selected
  - [x] "No internet" banner shown when connectivity is lost
  - [x] "Location unavailable" banner when GPS is off
- [x] Implement Nominatim search
  - [x] Create `NominatimService` Retrofit interface
  - [x] Add `User-Agent` OkHttp interceptor
  - [x] Create `SearchDestinationUseCase`
  - [ ] Debounce search input (300ms)
  - [ ] Show results list below search bar
  - [ ] On result tap: drop pin on map, store selected `Destination` in ViewModel, enable FAB
  - [ ] Handle empty results and network error states

---

### Phase 3 — Alert Setup

- [ ] Build Alert Setup screen (Compose bottom sheet or full screen)
  - [ ] Show selected destination name and coordinates
  - [ ] Toggle between Proximity mode and ETA mode
  - [ ] **Proximity mode options:**
    - [ ] Radio/chip selection: 500m / 1km / 2km / Custom
    - [ ] Custom input: text field accepting integer metres
    - [ ] Show radius ring preview on map as user changes value
  - [ ] **ETA mode options:**
    - [ ] Radio/chip selection: 5 min / 10 min / 15 min / 20 min / Custom
    - [ ] Custom input: text field accepting integer minutes
    - [ ] Note: "ETA is approximate — updates every 30s"
  - [ ] Validate inputs (non-zero, reasonable range)
  - [ ] "Set Alert" button — triggers alert start
- [x] Create `AlertConfig` domain model
  - [x] Fields: `destination`, `mode (PROXIMITY | ETA)`, `thresholdMetres` or `thresholdMinutes`
- [ ] Persist `AlertConfig` to DataStore on alert start
  - [ ] Save alert active state, destination, mode, threshold

---

### Phase 4 — Foreground Service & Location Monitoring

- [x] Build `AlertForegroundService`
  - [x] Extend `Service`, annotate with `@AndroidEntryPoint`
  - [x] Start as foreground service with a persistent notification
    - [x] Notification: title "ProximAlert active", body "Heading to {destination}"
    - [ ] Add "Cancel Alert" action button to notification
  - [x] Inject `FusedLocationProviderClient` via Hilt
  - [x] Request location updates every 15 seconds (`LocationRequest`)
    - [x] Priority: `PRIORITY_HIGH_ACCURACY`
    - [x] Min update interval: 10 seconds
  - [x] On each location update, check alert condition:
    - [x] **Proximity mode:** calculate Haversine distance to destination
      - [x] If `distance <= thresholdMetres` → trigger alarm
    - [x] **ETA mode:** call OSRM API with current location + destination
      - [x] Poll OSRM every 30 seconds (not every location update)
      - [x] If `etaSeconds <= thresholdMinutes * 60` → trigger alarm
      - [ ] Handle OSRM network failure gracefully (skip poll, retry next cycle)
  - [x] On alarm trigger: call `NotificationHelper.fireAlarm()`
  - [ ] Stop service cleanly when:
    - [ ] Alarm is dismissed
    - [ ] User cancels from notification action or Active Alert screen
  - [ ] Restore alert state from DataStore on service restart (killed by OS)
- [ ] Create `StartAlertUseCase` — starts the Foreground Service
- [ ] Create `StopAlertUseCase` — stops the service and clears DataStore state
- [ ] Broadcast `cancel` intent receiver inside service for notification action

---

### Phase 5 — Full-Screen Alarm

- [ ] Create `NotificationHelper`
  - [ ] Create high-priority notification channel (`IMPORTANCE_HIGH`)
  - [ ] Build alarm notification with `fullScreenIntent` pointing to `AlarmActivity`
  - [ ] Set `Notification.FLAG_INSISTENT` for repeating sound
- [ ] Create `AlarmActivity` (NOT a Compose NavGraph destination — separate Activity)
  - [ ] Declare in manifest with:
    - [ ] `android:showWhenLocked="true"`
    - [ ] `android:turnScreenOn="true"`
    - [ ] `android:excludeFromRecents="true"`
  - [ ] Set window flags: `FLAG_KEEP_SCREEN_ON`, `FLAG_DISMISS_KEYGUARD`
  - [ ] Build alarm UI (Compose `setContent`)
    - [ ] Full-screen dark overlay
    - [ ] Destination name (large text)
    - [ ] Animated pulse icon
    - [ ] **Dismiss button** → calls `StopAlertUseCase`, finishes activity
    - [ ] **Snooze button** → schedules re-trigger in 5 minutes via `AlarmManager`, finishes activity
  - [ ] Play alarm sound using `MediaPlayer` or `RingtoneManager`
  - [ ] Start vibration pattern using `Vibrator` / `VibrationEffect`
  - [ ] Stop sound + vibration on Dismiss or Snooze
- [ ] Test alarm fires correctly when:
  - [ ] App is in foreground
  - [ ] App is in background (minimised)
  - [ ] Screen is locked
  - [ ] App process is killed (OS restart of service)

---

### Phase 6 — Active Alert UI

- [ ] Build Active Alert bottom sheet (shown on Home screen while alert is running)
  - [ ] Show destination name
  - [ ] Show selected mode (Proximity / ETA)
  - [ ] Show live distance remaining (updated every 15s from ViewModel)
  - [ ] Show "Cancel Alert" button → calls `StopAlertUseCase`
- [ ] Connect Home screen ViewModel to Foreground Service state
  - [ ] Observe DataStore for `isAlertActive` flag
  - [ ] Show/hide bottom sheet accordingly
  - [ ] Show alert zone circle on map when active

---

### Phase 7 — Error States & Edge Cases

- [ ] Handle no internet connection
  - [ ] Show banner on Home screen
  - [ ] Disable ETA mode option in Alert Setup when offline
  - [ ] Show error in search if Nominatim unreachable
- [ ] Handle GPS disabled
  - [ ] Show banner and deep-link to Location Settings
- [ ] Handle `ACCESS_BACKGROUND_LOCATION` revoked mid-session
  - [ ] Detect in service, show notification prompting re-grant
- [ ] Handle `USE_FULL_SCREEN_INTENT` permission not granted
  - [ ] Fall back to heads-up notification with sound + vibration
- [ ] Handle service killed by OS (low memory)
  - [ ] Use `START_STICKY` return in `onStartCommand`
  - [ ] Re-read alert config from DataStore on restart
- [ ] Handle device reboot (V1 optional — show "Alert was cleared" message on next launch)

---

### Phase 8 — Polish & Testing

- [ ] UI polish
  - [ ] Add loading states for search and ETA fetch
  - [ ] Add haptic feedback on "Set Alert" tap
  - [ ] Smooth map camera animation to destination on search selection
  - [ ] Animate alert zone ring appearing on map
- [ ] Unit tests
  - [ ] `DistanceUtils` — Haversine calculation accuracy
  - [ ] `SearchDestinationUseCase` — mock Nominatim responses
  - [ ] `MonitorProximityUseCase` — trigger condition logic
  - [ ] `MonitorEtaUseCase` — OSRM response parsing + trigger logic
- [ ] Integration tests
  - [ ] Foreground service starts and stops correctly
  - [ ] DataStore reads/writes alert state correctly
- [ ] Manual device testing
  - [ ] Test on Android 8.0 (API 26) — min SDK
  - [ ] Test on Android 13 (API 33) — POST_NOTIFICATIONS
  - [ ] Test on Android 14 (API 34) — USE_FULL_SCREEN_INTENT + FOREGROUND_SERVICE_LOCATION
  - [ ] Test background alarm on a locked device (real device, not emulator)
  - [ ] Test OSRM polling doesn't drain battery excessively

---

### Phase 9 — Release Prep

- [ ] Add `ProGuard` / R8 rules for Retrofit and OSMDroid
- [ ] Add Privacy Policy URL (required for Play Store — location app)
- [ ] Write Play Store listing
  - [ ] Short description (80 chars)
  - [ ] Full description explaining location usage
  - [ ] Screenshots of all 6 screens
- [ ] Complete Play Store Data Safety form
  - [ ] Declare: Location data collected, not shared, used for app functionality only
- [ ] Submit for review — note: background location requires Play Store approval form

---

## Known Constraints & Risks

| Risk | Severity | Mitigation |
|---|---|---|
| `ACCESS_BACKGROUND_LOCATION` Play Store review | High | Submit Data Safety form + background location justification form |
| `USE_FULL_SCREEN_INTENT` restricted on API 34+ | High | Fallback to heads-up notification + guide user to special access settings |
| OSRM public API rate limits | Medium | Poll every 30–60s, add caching of last ETA result |
| Foreground Service killed by aggressive OEMs (Xiaomi, Oppo, Realme) | Medium | Document battery optimisation exemption step in onboarding |
| Nominatim usage policy | Low | Add `User-Agent` header, respect 1 req/sec limit with debounce |
| Map tiles unavailable offline | Low | Show "No internet" banner — map is non-functional without connectivity |

---

## Future Scope (V2+)

- **Multiple alerts** — track multiple destinations simultaneously
- **Alert history** — log of past trips, trigger times, and destinations
- **Custom alert zones** — draw polygon on map instead of circular radius
- **Repeat alerts** — recurring alert for daily commute (same destination, same time)
- **Offline tile caching** — download map region for offline use
- **Custom ringtone** — let user pick alarm sound from device
- **Home screen widget** — show active alert status and distance
- **KMP expansion** — share domain + data layers with iOS target
- **Share trip** — send live location + ETA to a contact

---

*ProximAlert PRD — V1.0 — Internal use only*
