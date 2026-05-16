# ⚡ Grama-Urja — Crowdsourced Power Monitor
### MindMatrix VTU Internship Program — Project #60

---

## 📱 What This App Does

Grama-Urja is a community-powered "Smart Grid" app for rural areas.  
Farmers can report whether power is ON or OFF in their transformer zone.  
Everyone in that zone gets an instant push notification when power returns.

---

## 🗂 Project Structure

```
GramaUrja/
├── app/
│   ├── src/main/
│   │   ├── java/com/gramaUrja/
│   │   │   ├── model/          Models.kt         (PowerStatus, Zone, CropType)
│   │   │   ├── repository/     PowerRepository.kt (Firebase Realtime DB)
│   │   │   ├── viewmodel/      MainViewModel.kt, ZoneViewModel.kt
│   │   │   ├── ui/             Activities + ZoneAdapter
│   │   │   ├── service/        GramaUrjaFirebaseMessagingService.kt
│   │   │   └── utils/          TimeUtils, PrefsUtils, NotificationUtils
│   │   └── res/
│   │       ├── layout/         5 XML layout files
│   │       ├── drawable/       Icons and backgrounds
│   │       └── values/         colors, strings, themes
│   └── google-services.json    ← Replace with YOUR file
└── build.gradle
```

---

## ✅ Features Implemented

| Requirement | Status |
|---|---|
| Zone Selection (village/transformer) | ✅ |
| Power ON / OFF toggle (community reported) | ✅ |
| Real-time sync — updates within 2 seconds | ✅ Firebase Realtime DB |
| "Last Seen" freshness text | ✅ Updates every 30s |
| Push Notifications on Power ON | ✅ FCM |
| Pump Timer (crop-based calculator) | ✅ |
| High-contrast UI for outdoor use | ✅ Dark theme, large text |
| Data freshness indicator | ✅ "Updated 2 mins ago" style |

---

## 🔧 SETUP INSTRUCTIONS (Required Before Building)

### Step 1 — Create Firebase Project

1. Go to [https://console.firebase.google.com](https://console.firebase.google.com)
2. Click **"Add Project"** → Name it `GramaUrja`
3. Enable **Google Analytics** (optional)

### Step 2 — Add Android App to Firebase

1. In Firebase Console → **Project Settings** → **Add App** → Android
2. Enter package name: `com.gramaUrja`
3. Download **`google-services.json`**
4. Replace the placeholder file at `app/google-services.json` with your downloaded file

### Step 3 — Enable Firebase Realtime Database

1. In Firebase Console → **Realtime Database** → **Create Database**
2. Choose your region (Asia South for India)
3. Start in **test mode** for development
4. Set Database Rules (for production):
```json
{
  "rules": {
    "power_status": {
      ".read": true,
      ".write": true
    },
    "zones": {
      ".read": true,
      ".write": true
    }
  }
}
```

### Step 4 — Enable Firebase Cloud Messaging

1. In Firebase Console → **Cloud Messaging** tab
2. No extra setup needed for Android — it works with `google-services.json`

### Step 5 — Open in Android Studio

1. Open Android Studio → **Open** → Select the `GramaUrja` folder
2. Wait for Gradle sync to complete
3. Connect your Android device (API 24+) or use an emulator
4. Click **Run ▶**

---

## 🏗 Architecture

```
UI Layer (Activities)
    ↓
ViewModel (LiveData/StateFlow)
    ↓
Repository (PowerRepository)
    ↓
Firebase Realtime Database
    ↑
FCM Push Notifications (GramaUrjaFirebaseMessagingService)
```

**Pattern:** MVVM (Model-View-ViewModel)  
**Real-time:** Kotlin Coroutines + Flow with Firebase listeners  
**Persistence:** SharedPreferences for zone selection  

---

## 📋 Success Criteria — Verified

- ✅ Status updates on home screen for all users **within 2 seconds** (Firebase Realtime DB)
- ✅ App shows **"Freshness"** of data (e.g., "Updated 2 mins ago") — refreshes every 30s
- ✅ **High-contrast UI** — dark background (#0D1117), bright green (#2ECC40) and red (#FF4136)

---

## 🌾 Pump Timer — Crop Durations

| Crop | Min | Max |
|---|---|---|
| Rice / Paddy | 60 min | 120 min |
| Wheat | 30 min | 60 min |
| Sugarcane | 45 min | 90 min |
| Vegetables | 20 min | 40 min |
| Cotton | 30 min | 60 min |
| Maize / Corn | 25 min | 50 min |
| Groundnut | 20 min | 40 min |
| Soybean | 25 min | 45 min |

---

## 📦 Dependencies

- Firebase Realtime Database `32.7.0`
- Firebase Cloud Messaging `32.7.0`
- Kotlin Coroutines `1.7.3`
- AndroidX Lifecycle / ViewModel `2.7.0`
- Material Components `1.11.0`
- CardView `1.0.0`
- RecyclerView `1.3.2`

---

*Built for MindMatrix VTU Internship Program — Grama-Urja, Project #60*
