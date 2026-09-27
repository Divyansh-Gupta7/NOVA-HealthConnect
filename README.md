# NOVA — Health Connect

[![Platform](https://img.shields.io/badge/Platform-Android%20(API%2026%2B)-teal.svg)](https://developer.android.com)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2F%20Material%203-blue.svg)](https://developer.android.com/jetpack/compose)
[![Health Connect](https://img.shields.io/badge/Integration-Android%20Health%20Connect-green.svg)](https://developer.android.com/guide/health-and-fitness/health-connect)
[![Architecture](https://img.shields.io/badge/Architecture-Clean%20MVVM%20%2B%20Coroutines%20Flow-purple.svg)](https://developer.android.com/topic/architecture)

> **Autonomous Wellness, Focus Flow, & Health Connect Integration**  
> A native Android client for the NOVA cognitive wellness platform, connecting directly with Android Health Connect to synchronize biometric signals, drive autonomous recovery insights, and power flow-state focus sprints.

---

## 1. Repository Architecture & Separation

`NOVA - Health Connect` is developed as an **independent native Android repository**, fully separated from the NOVA web project and backend:

```text
GitHub
│
├── NOVA
│   ├── Web Frontend (React / Tailwind)
│   ├── Node / Express Backend API
│   ├── Firebase Auth & Firestore
│   └── Gemini Cognitive Processing Service
│
└── NOVA - Health Connect (This Repository)
    ├── Native Android App (Kotlin / Jetpack Compose / Material 3)
    ├── Health Connect Manager & Permission Controller
    ├── WorkManager Background Synchronization Engine
    ├── Retrofit / OkHttp API Client
    └── Autonomous Wellness & Reflex Calibration UI
```

---

## 2. Core Features & Capabilities

### ✦ Visual Identity & Design System
* **Bespoke NOVA Theme**: Exact replication of the web platform's aesthetic using `#00685F` (Teal), `#D8F5EF` (Mint), `#712AE2` (Violet), and `#FAF8FF` (Soft Light Background).
* **Concentric Equilibrium Rings**: Custom Canvas-rendered multi-ring graphic tracking Readiness, Autonomic Recovery, and Focus Capacity with live animation.
* **Component Library**: Tailored `NovaCard`, `NovaButton`, `NovaMetricCard`, `NovaTopBar`, and `NovaSectionHeader` matching the design system.

### ✦ Android Health Connect Integration
* **Direct Health Connect Client**: Interfaces with Android's native `HealthConnectClient` to read:
  * `StepsRecord` (Daily locomotion)
  * `SleepSessionRecord` (Sleep duration, deep sleep stages)
  * `HeartRateRecord` (Resting and average BPM)
  * `TotalCaloriesBurnedRecord` (Active energy expenditure)
  * `ExerciseSessionRecord` (Physical training minutes)
* **Granular Permission Flow**: In-app permission onboarding using Android's `PermissionController.createRequestPermissionResultContract()`.
* **Sync Pipeline**:
  * **Manual Sync**: Instant "Sync Now" action with real-time feedback and timestamp tracking.
  * **Autonomous Background Sync**: `WorkManager` `PeriodicWorkRequest` running hourly under network constraints.

### ✦ Overview Dashboard
* Daily wellness summary with dynamic AI Recovery Insight.
* Concentric rings representing cognitive state and neural equilibrium.
* Real-time biometric cards (Steps, Sleep, Resting HR, Active Burn).
* Daily habit protocol checklist with interactive state toggling.
* Quick-action triggers to launch focus sprints or converse with the AI companion.

### ✦ Deep Work Sprint Terminal
* Configurable sprint durations (15m, 25m, 45m, 60m).
* Circular countdown timer with real-time progress stroke.
* **Binaural & Acoustic Immersion**: Audio presets (Binaural 40Hz Gamma, Pink Noise, Alpha Waves 10Hz, Silence).
* **Distraction Friction Counter**: Log internal or external interruptions with a single tap.
* Automatic sprint recording and streak accumulation sent to the NOVA backend.

### ✦ Daily Neuro-Calibration & Reflex Test
* Subjective somatic ratings: Sleep Quality (0–100%), Energy Index (1–5), and Cognitive Load / Stress (1–5).
* **Interactive Neuro-Reflex Calibration**: A reaction-latency measurement tool. Screen transitions from Waiting (Amber) to Click Now (Bright Green); calculates neural reaction time in milliseconds (e.g., 248 ms) to track central nervous system readiness.
* Daily neuro-habit checklist with instant backend submission.

### ✦ NOVA AI Companion
* Conversational AI interface replicating the NOVA web assistant.
* Context-grounded queries: Automatically bundles recent Health Connect metrics (sleep hours, resting heart rate, recovery score) into the backend payload.
* **Zero Client Secret Exposure**: All Gemini requests proxy through the authenticated NOVA Node/Express backend (`/api/ai/chat`); no `GEMINI_API_KEY` is ever bundled inside the APK.

### ✦ Dynamic Settings & Network Configuration
* **Configurable Backend Endpoint**: Easily switch between Android Emulator (`http://10.0.2.2:5000/api/`), local LAN IP (`http://192.168.x.x:5000/api/`), or remote production server.
* Background sync toggle via Android `WorkManager`.
* User session details and sign-out controls.

---

## 3. Technology Stack

* **Language**: Kotlin 1.9.0
* **UI Toolkit**: Jetpack Compose + Material 3
* **Navigation**: Jetpack Navigation Compose
* **Architecture**: Clean MVVM (Model-View-ViewModel) + Android Architecture Components
* **Asynchronous Streams**: Kotlin Coroutines + `StateFlow`
* **Health API**: Android Health Connect SDK (`androidx.health.connect:connect-client:1.1.0-alpha11`)
* **Background Tasks**: Android Jetpack `WorkManager` (`androidx.work:work-runtime-ktx:2.9.1`)
* **Networking**: Retrofit 2.11.0 + OkHttp 4.12.0 + Gson
* **Authentication**: Firebase Authentication KTX + Session Persistence + Demo Fallback

---

## 4. Project Structure

```text
app/src/main/java/com/nova/healthconnect/
│
├── NovaApplication.kt            # App initialization, Retrofit setup, WorkManager scheduling
├── MainActivity.kt               # Navigation host, authentication router, bottom navigation scaffold
│
├── auth/
│   └── AuthManager.kt            # Firebase auth handler, persistent session store, demo mode
│
├── data/
│   ├── api/
│   │   ├── NovaApiService.kt     # Retrofit endpoints (dashboard, checkin, sessions, sync, ai/chat)
│   │   ├── AuthInterceptor.kt    # Injects Bearer token and platform headers
│   │   └── RetrofitClient.kt     # Dynamic base URL management & OkHttpClient builder
│   ├── models/                   # Type-safe DTOs for dashboard, biometrics, habits, sessions, chat
│   └── repository/
│       └── NovaRepository.kt     # Local caching, Health Connect metric merging, fallback responses
│
├── health/
│   ├── HealthConnectManager.kt   # Permission checks and record readers (Steps, Sleep, HR, Cal)
│   ├── HealthSyncManager.kt      # Manual and scheduled sync orchestration
│   └── HealthSyncWorker.kt       # WorkManager periodic background sync worker
│
└── ui/
    ├── components/               # Custom UI: Concentric rings, cards, buttons, headers, metric cards
    ├── navigation/               # Screen definitions and styled bottom navigation bar
    ├── screens/                  # Splash, Login, Overview, Health, Focus, CheckIn, AiChat, Settings
    ├── theme/                    # Color palette, Material3 type scales, shapes, dimensions
    └── viewmodels/               # StateFlow ViewModels for each primary screen
```

---

## 5. Building & Running the Project

### Prerequisites
* **JDK**: OpenJDK 17 or higher
* **Android SDK**: API 35 (Android 15) with Build Tools 35.0.0
* **Min SDK**: API 26 (Android 8.0)
* **Target SDK**: API 35 (Android 15)

### Command-Line Build
To compile the Kotlin sources and assemble the debug APK:

```powershell
# Set Java 17 environment
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

# Assemble Debug APK
.\gradlew.bat assembleDebug
```

The generated APK will be output to:
```text
app/build/outputs/apk/debug/app-debug.apk
```

### Installing on Device / Emulator
```powershell
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

---

## 6. Backend Integration

The app connects to the existing NOVA Node/Express backend. By default, when running on the Android Emulator, it points to:
```text
http://10.0.2.2:5000/api/
```
To point to a physical device on your local Wi-Fi, open **Settings** inside the app and enter your host machine's IP (e.g., `http://192.168.1.50:5000/api/`).

### Supported API Endpoints
* `GET /api/dashboard?date=YYYY-MM-DD` — Fetches daily wellness, focus, and habit data
* `POST /api/checkin` — Records somatic ratings, reaction test latency, and habits completed
* `POST /api/sessions` — Logs completed deep work sprint sessions and distraction count
* `POST /api/health-connect/sync` — Ingests Health Connect normalized biometrics
* `POST /api/ai/chat` — Proxies conversational queries to Gemini with biometric context

---

## 7. Privacy & Security

* **Sandboxed Biometrics**: All Health Connect read operations occur strictly on-device.
* **No Direct Third-Party Ad Networks**: Data is only transferred to your own authenticated NOVA backend instance.
* **Zero Client-Side AI Keys**: Gemini API keys are never stored on Android devices; all AI queries are brokered securely via backend authentication.
