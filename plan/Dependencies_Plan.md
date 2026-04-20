# ProximAlert — Dependencies Plan

Based on the `ProximAlert_PRD.md`, this document outlines the required dependencies for the project, structured for a `libs.versions.toml` Version Catalog implementation.

## 1. Core Android & Kotlin
These dependencies provide the foundation for Android development and asynchronous programming.
| Dependency | Purpose |
|---|---|
| `androidx.core:core-ktx` | Kotlin extensions for Android Core |
| `androidx.lifecycle:lifecycle-runtime-ktx` | Lifecycle awareness for coroutines |
| `androidx.activity:activity-compose` | Compose integration with ComponentActivity |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | Asynchronous programming for UI and Services |

## 2. Jetpack Compose (UI)
Jetpack Compose is the declared UI toolkit for ProximAlert. We use a Bill of Materials (BOM) to manage versions.
| Dependency | Purpose |
|---|---|
| `androidx.compose:compose-bom` | Bill of Materials to align Compose versions |
| `androidx.compose.ui:ui` | Core UI components |
| `androidx.compose.ui:ui-graphics` | Graphics, Canvas, etc. |
| `androidx.compose.ui:ui-tooling-preview` | UI Preview in Android Studio |
| `androidx.compose.material3:material3` | Material Design 3 components and theming |
| `androidx.compose.material:material-icons-extended` | Extended Material Icons for Compose |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | ViewModel integration for Compose screens |
| `androidx.navigation:navigation-compose` | Navigation graph for Compose screens |

## 3. Map & Location tracking
Dependencies for rendering the map and getting background/foreground location updates.
| Dependency | Purpose |
|---|---|
| `org.osmdroid:osmdroid-android` | OpenStreetMap integration for map rendering (as specified in PRD) |
| `com.google.android.gms:play-services-location` | FusedLocationProviderClient for highly accurate GPS tracking |

## 4. Networking (Nominatim API & OSRM API)
Dependencies for API calls to search for destinations and fetch ETA routing.
| Dependency | Purpose |
|---|---|
| `com.squareup.retrofit2:retrofit` | Type-safe HTTP client for API requests |
| `com.squareup.retrofit2:converter-gson` | JSON serialization/deserialization for Retrofit |
| `com.squareup.okhttp3:okhttp` | Core networking client |
| `com.squareup.okhttp3:logging-interceptor` | Debugging HTTP requests (useful in development) |

## 5. Dependency Injection (Hilt)
Hilt is specified as the DI framework in the architecture.
| Dependency | Purpose |
|---|---|
| `com.google.dagger:hilt-android` | Core Hilt dependency injection framework |
| `com.google.dagger:hilt-compiler` | KSP compiler for processing Hilt annotations |
| `androidx.hilt:hilt-navigation-compose` | Hilt integration with Compose Navigation (for ViewModels in NavGraph) |

## 6. Local Storage
For saving the active `AlertConfig` state persistently.
| Dependency | Purpose |
|---|---|
| `androidx.datastore:datastore-preferences` | Type-safe, async preference storage (modern alternative to SharedPreferences) |

---

## Example `libs.versions.toml` implementation
This section provides a concrete `libs.versions.toml` structure to easily add these dependencies to the new Gradle project.

```toml
[versions]
agp = "8.3.0"
kotlin = "1.9.22"
coreKtx = "1.12.0"
lifecycleRuntimeKtx = "2.7.0"
activityCompose = "1.8.2"
composeBom = "2024.02.00"
navigationCompose = "2.7.7"
osmdroid = "6.1.18"
playServicesLocation = "21.2.0"
retrofit = "2.9.0"
okhttp = "4.12.0"
hilt = "2.50"
hiltNavigationCompose = "1.2.0"
datastore = "1.0.0"
ksp = "1.9.22-1.0.17"

[libraries]
# Core
androidx-core-ktx = { group = "androidx.core", name = "core-ktx", version.ref = "coreKtx" }
androidx-lifecycle-runtime-ktx = { group = "androidx.lifecycle", name = "lifecycle-runtime-ktx", version.ref = "lifecycleRuntimeKtx" }
androidx-activity-compose = { group = "androidx.activity", name = "activity-compose", version.ref = "activityCompose" }

# Compose
androidx-compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "composeBom" }
androidx-ui = { group = "androidx.compose.ui", name = "ui" }
androidx-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
androidx-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
androidx-material3 = { group = "androidx.compose.material3", name = "material3" }
androidx-material-icons-extended = { group = "androidx.compose.material", name = "material-icons-extended" }
androidx-lifecycle-viewmodel-compose = { group = "androidx.lifecycle", name = "lifecycle-viewmodel-compose", version.ref = "lifecycleRuntimeKtx" }
androidx-navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigationCompose" }

# Location & Maps
osmdroid-android = { group = "org.osmdroid", name = "osmdroid-android", version.ref = "osmdroid" }
play-services-location = { group = "com.google.android.gms", name = "play-services-location", version.ref = "playServicesLocation" }

# Networking
retrofit = { group = "com.squareup.retrofit2", name = "retrofit", version.ref = "retrofit" }
retrofit-converter-gson = { group = "com.squareup.retrofit2", name = "converter-gson", version.ref = "retrofit" }
okhttp-logging-interceptor = { group = "com.squareup.okhttp3", name = "logging-interceptor", version.ref = "okhttp" }

# Dependency Injection
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }
androidx-hilt-navigation-compose = { group = "androidx.hilt", name = "hilt-navigation-compose", version.ref = "hiltNavigationCompose" }

# Storage
androidx-datastore-preferences = { group = "androidx.datastore", name = "datastore-preferences", version.ref = "datastore" }

[plugins]
androidApplication = { id = "com.android.application", version.ref = "agp" }
jetbrainsKotlinAndroid = { id = "org.jetbrains.kotlin.android", version.ref = "kotlin" }
hiltAndroid = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }
```
