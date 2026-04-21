# PingPath Execution Plan

## Overview
This document outlines the step-by-step execution plan to resolve current issues and implement the requested features for the PingPath application. The tasks focus on replacing hardcoded mock data with a real persistence layer, fixing map UI bugs, ensuring background services work correctly, and revamping the app's navigation structure.

---

## Phase 1: Local Data Persistence & Real Data Integration
**Objective:** Replace dummy data with a real local database (e.g., Room or DataStore) to save alerts and recent locations.

- [x] **Task 1.1:** Setup Room Database / DataStore. Create entities for `Location` and `Alert`.
- [x] **Task 1.2:** Update `AlertRepositoryImpl` to read/write from the local database instead of using `allDestinations`, `allAlarmLocations`, and `InMemoryAlertsDatabase`.
- [x] **Task 1.3:** Implement logic to save searched/selected locations to a "Recent Locations" table.
- [x] **Task 1.4:** Update `HomeScreen` and `AlertDetailsScreen` to load and display actual data based on real item clicks instead of dummy data.

---

## Phase 2: Map & Pin Behavior Fixes
**Objective:** Fix the map coordinates and pin display logic.

- [x] **Task 2.1:** Remove hardcoded default locations (e.g., `19.0760, 72.8777` Mumbai) from `AddAlertScreen.kt` and `OsmMapView.kt`.
- [x] **Task 2.2:** Do not show any default destination/alarm pins on the map before the user explicitly selects one.
- [x] **Task 2.3:** When a user selects a "Final Destination" or "Alarm Location", update the map to center on that coordinate and show the pin accurately.

---

## Phase 3: Distance Calculation
**Objective:** Calculate and display accurate distances after location selection.

- [x] **Task 3.1:** Fetch exact latitude and longitude for selected places from Nominatim (update `LocationModel` to hold coordinates).
- [x] **Task 3.2:** Use `DistanceHelper` (or OSRM for routing) to accurately calculate the distance between the user's current location, the alarm location, and the final destination.
- [x] **Task 3.3:** Bind the calculated distance and ETA to the UI on the `AddAlertScreen` (Review Step) and `AlertDetailsScreen`.

---

## Phase 4: Background Service & Alarm Triggers
**Objective:** Fix the background tracking service to reliably trigger alarms.

- [x] **Task 4.1:** Investigate `AlertForegroundService` to ensure it properly stays alive in the background.
- [x] **Task 4.2:** Make the service read the active alert from the local database (from Phase 1) instead of using temporary intent extras.
- [x] **Task 4.3:** Ensure the location callback accurately checks the distance against the threshold and fires the full-screen alarm notification when conditions are met.

---

## Phase 5: Cancel Alarm Behavior
**Objective:** Handle the "Cancel Alarm" flow on the Alert Details screen.

- [x] **Task 5.1:** Wire up the "Cancel Alarm" button in `AlertDetailsScreen` to call `CancelAlertUseCase`.
- [x] **Task 5.2:** When canceled, stop the `AlertForegroundService`.
- [x] **Task 5.3:** Mark the alert as cancelled/inactive in the database and add it to the "Recent" list.
- [x] **Task 5.4:** Navigate the user back to the Recent/Destination list screen automatically.

---

## Phase 6: Bottom Navigation & Settings Screen
**Objective:** Revamp the app's main navigation flow.

- [x] **Task 6.1:** Create a `BottomNavigationBar` component with 3 tabs:
  1. **Destination:** Shows the current "My Alerts" list (Active alerts).
  2. **Recent:** Shows the list of recent destinations/alerts (Historical data from local DB).
  3. **Settings:** A new screen for configuration.
- [x] **Task 6.2:** Create the `RecentScreen` UI to show past locations.
- [x] **Task 6.3:** Create the `SettingsScreen` UI. Include fields for:
  - Alarm sound selection
  - Country change (for Nominatim search restriction)
  - Default timer before destination
  - Alert volume controls
- [x] **Task 6.4:** Integrate the Bottom Navigation into `AppNavigation.kt` or a main `MainScaffold` wrapper so it persists across the top-level screens.
