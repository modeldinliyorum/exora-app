# Exora — Currency Rates

An extremely simple Android app that shows TRY-based exchange rates on a pure
black screen with large white text. USD and EUR are pinned at the very top.

- **1 USD = XX.XX TRY**
- **1 EUR = XX.XX TRY**
- GBP, CHF, JPY, AED, CAD below

## Features

- Black background, white text, full-screen immersive UI.
- Refreshes automatically every **10 minutes**.
- Keeps the **last successful rates** when offline (cached in `SharedPreferences`).
- Free, no-key API with automatic **fallback** to a mirror.
- Only the `INTERNET` permission.
- Minimal dependencies: `androidx.core` + `androidx.appcompat` only. Networking via
  `HttpURLConnection`, JSON via the built-in `org.json`.
- Kotlin, `minSdk 26`, `targetSdk 35`, `compileSdk 35`.

## Data source

Primary: `https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/try.json`
Fallback: `https://latest.currency-api.pages.dev/v1/currencies/try.json`

The API returns "1 TRY = X <currency>"; the app inverts it to show "1 <currency> = Y TRY".

## Building

The APK is built in the cloud — **no local build is needed or expected**.

See `.github/workflows/build-apk.yml`. It runs on `ubuntu-latest`, builds the
**debug** APK and uploads it as the `exora-debug-apk` artifact.

### How to run the workflow

1. Open the repository on GitHub.
2. Go to the **Actions** tab.
3. Select **Build Debug APK** in the left sidebar.
4. Click **Run workflow** → choose the `main` branch → **Run workflow**.
   (It also runs automatically on every push to `main`.)

### How to download the APK

1. Wait for the run to finish (green check).
2. Open the completed run and scroll to the **Artifacts** section at the bottom.
3. Download **exora-debug-apk** — it contains `app-debug.apk`.
4. Install it on a device with "Install unknown apps" enabled.

## Project layout

```
exora-app/
├── .github/workflows/build-apk.yml
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/exora/rates/MainActivity.kt
        └── res/
            ├── layout/activity_main.xml
            ├── values/{strings,colors,themes}.xml
            ├── drawable/ic_launcher_foreground.xml
            └── mipmap-anydpi-v26/ic_launcher.xml
```
