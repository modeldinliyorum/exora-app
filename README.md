# Exora — Currency Rates

A modern, edge-to-edge dark Android app that shows exchange rates against a
**base currency you choose**. Pick any base from the chip bar at the top and every
rate below is recalculated against it.

- Base **TRY** → `1 USD = 49.37 TRY`, `1 EUR = 55.17 TRY`, …
- Base **EUR** → `1 USD = 0.89 EUR`, `1 TRY = 0.018 EUR`, …

## Features

- **Pure-black, Material 3 dark theme** with raised cards and hairline strokes.
- **Base-currency chip bar** — 23 currencies (TRY, USD, EUR, GBP, CHF, JPY, CAD,
  AUD, CNY, AED, SAR, INR, RUB, SEK, NOK, DKK, PLN, MXN, BRL, ZAR, KRW, SGD, HKD).
  The selected base is a filled white pill.
- **Animated rate list** — `RecyclerView` + `DiffUtil`; switching base or refreshing
  animates the rows smoothly.
- **Pull-to-refresh** — swipe down to reload.
- **Auto-refresh every 10 minutes** in the background.
- **Last update time** shown in the top bar, with a live/offline indicator dot.
- **Offline-friendly** — the last successful rates are cached and shown.
- Free, no-key API with automatic **fallback** to a mirror.
- **Only the `INTERNET`** permission.
- Edge-to-edge with transparent status/navigation bars and correct insets.
- Kotlin, `minSdk 26`, `targetSdk 35`, `compileSdk 35`.

## Data source

Primary: `https://cdn.jsdelivr.net/npm/@fawazahmed0/currency-api@latest/v1/currencies/try.json`
Fallback: `https://latest.currency-api.pages.dev/v1/currencies/try.json`

The API returns "1 TRY = X <currency>". The app fetches this once and derives any
base with cross rates: `1 CODE = (perTry[base] / perTry[code]) BASE`. So one network
call powers every base selection, and switching base is instant and offline-safe.

## Dependencies (kept small)

`androidx.core`, `androidx.appcompat`, `com.google.android.material`,
`androidx.recyclerview`, `androidx.swiperefreshlayout`. Networking via
`HttpURLConnection`, JSON via the built-in `org.json`.

## Building

The APK is built in the cloud — **no local build is needed or expected**.
See `.github/workflows/build-apk.yml`: runs on `ubuntu-latest`, builds the **debug**
APK and uploads it as the `exora-debug-apk` artifact.

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
├── build.gradle.kts / settings.gradle.kts / gradle.properties
├── PRODUCT.md / DESIGN.md / README.md
└── app/
    ├── build.gradle.kts
    └── src/main/
        ├── AndroidManifest.xml
        ├── java/com/exora/rates/
        │   ├── MainActivity.kt
        │   ├── RateAdapter.kt
        │   └── Currencies.kt
        └── res/
            ├── layout/{activity_main,item_rate}.xml
            ├── color/{chip_bg,chip_text,chip_stroke}.xml
            ├── drawable/{dot,ic_launcher_foreground}.xml
            ├── values/{strings,colors,themes}.xml
            └── mipmap-anydpi-v26/ic_launcher.xml
```
