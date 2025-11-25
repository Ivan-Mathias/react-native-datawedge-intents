# React Native DataWedge Intents (Modern Fork)

A modernized, TypeScript-first fork of `react-native-datawedge-intents` designed to support **Android 14+ (API 34)** and Zebra devices running the latest DataWedge versions.

### Key Features

- **Android 14 Support:** Fixed `Context.RECEIVER_EXPORTED` crashes on newer Android versions.
- **TypeScript:** Written in TS with full type definitions included.
- **Batteries Included:** Includes a high-level `DataWedgeService` that handles Profile creation, configuration, and intent parsing automatically.
- **Modern Build:** Updated to Gradle 8 and compiles against SDK 34.
- **Compatibility:** Works with Expo SDK 54 with edge-to-edge

---

### 📦 Installation

Since this is a fork, install it directly from GitHub:

```bash
npm install github:IvanMathias/react-native-datawedge-intents
```

> **⚠️ Important:** Since this library contains native Android code changes, you must rebuild your native app:
> `npx expo run:android` or `cd android && ./gradlew clean && cd .. && npx react-native run-android`

---

### 🛠 Usage: DataWedgeService (Recommended)

This fork includes a helper service that simplifies the integration. It automatically registers the receiver, creates the DataWedge profile on the device, and configures the scanner plugins.

#### 1\. Initialize the Service

Call this once when your app mounts (e.g., in a `useEffect` inside your root layout or App.tsx).

```typescript
import { useEffect } from 'react'
import { DeviceEventEmitter } from 'react-native'
import { DataWedgeService } from 'react-native-datawedge-intents'
import Constants from 'expo-constants' // or react-native-device-info

// ... inside your component
useEffect(() => {
  try {
    DataWedgeService.initialize({
      // Your app's package name (required to filter intents correctly)
      packageName: Constants.expoConfig?.android?.package ?? 'com.your.app.package',

      // Optional: Disable the keyboard output to prevent double-typing
      disableKeystroke: true
    })

    // Listener for scans
    const subscription = DeviceEventEmitter.addListener('datawedge_broadcast_intent', (intent) => {
      handleScan(intent)
    })

    return () => {
      subscription.remove()
    }
  } catch (e) {
    console.error(e)
  }
}, [])
```

#### 2\. Handle Scans

The service provides a helper to parse the raw Android Intent into a clean object.

```typescript
const handleScan = (intent: any) => {
  const scan = DataWedgeService.parseIntentToScan(intent)

  if (scan) {
    console.log('Data:', scan.data) // e.g. "789123456"
    console.log('Type:', scan.decoder) // e.g. "ean13"
  }
}
```

#### 3\. Control Scanner (Illumination)

Toggle the torch/flashlight mode of the scanner.

```typescript
// Turn light ON
DataWedgeService.setIllumination(true)

// Turn light OFF
DataWedgeService.setIllumination(false)
```

---

### ⚙️ Usage: Low-Level API

If you prefer to manually manage filters and profiles, you can use the low-level primitives.

> **Note:** Deprecated methods like `registerReceiver` (string arg) and `sendIntent` have been removed in favor of the more robust `registerBroadcastReceiver` and `sendBroadcastWithExtras`.

```typescript
import DataWedgeIntents from 'react-native-datawedge-intents'
import { Platform } from 'react-native'

// 1. Register Receiver
DataWedgeIntents.registerBroadcastReceiver({
  filterActions: ['com.symbol.datawedge.api.RESULT_ACTION', 'com.your.custom.action'],
  filterCategories: ['android.intent.category.DEFAULT']
})

// 2. Send Command (Example: Soft Trigger)
const extras = {
  'com.symbol.datawedge.api.SOFT_SCAN_TRIGGER': 'TOGGLE_SCANNING'
}

DataWedgeIntents.sendBroadcastWithExtras({
  action: 'com.symbol.datawedge.api.ACTION',
  extras: extras
})
```

---

### Migrations from darryncampbell/react-native-datawedge-intents

If you are migrating from the original `darryncampbell` library:

1.  **Removed `ObservableObject`:** The library no longer maintains internal state observers. Use `DeviceEventEmitter` directly.
2.  **Removed `registerReceiver(action, category)`:** Use `registerBroadcastReceiver(filterObj)` instead.
3.  **Removed `sendIntent(action, value)`:** Use `sendBroadcastWithExtras(obj)` instead.
4.  **Constants:** Java constants are no longer exported via NativeModules to improve performance. Use the string literals or the provided Service.

---

### License

MIT
