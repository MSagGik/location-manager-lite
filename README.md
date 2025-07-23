# 🌎 🌍 🌏 location-manager-lite (LocationManagerLite)

**LocationManagerLite** is a lightweight library for Android written in Kotlin for safe, configurable, and coroutine-friendly location retrieval using the Android LocationManager.

[![](https://jitpack.io/v/MSagGik/location-manager-lite.svg)](https://jitpack.io/#MSagGik/location-manager-lite)
[![](https://img.shields.io/github/license/MSagGik/location-manager-lite)](LICENSE.txt)
![Beta](https://img.shields.io/badge/status-beta-blue)
---

## 📖 Overview

**LocationManagerLite** offers a simple and modern API for accessing device location in Android apps. It uses Android’s built-in `LocationManager`, with no reliance on proprietary services.

### ✨ Key Features

- Suspend-based `getLocation()` function
- Smart fallback: cached location first, then real-time update
- Configurable timeout and freshness limits
- Safe API using sealed Response (Success/Error)
- Compatible with most Android devices (API 21-35)

---

## 📦 Installation

### Using JitPack

1. In your project `settings.gradle` (or `settings.gradle.kts`):

   ```kotlin
   dependencyResolutionManagement {
       repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
       repositories {
           mavenCentral()
           maven { url = uri("https://jitpack.io") }
       }
   }
   ```

2. In your module `build.gradle` (or `build.gradle.kts`):

   ```kotlin
   dependencies {
       implementation("com.github.MSagGik:location-manager-lite:1.0.0-beta1")
   }
   ```

---

## ✨ Quick Start

```kotlin
val locationApi = LocationManagerLite(context).provide()

val result = locationApi.getLocation()
when (result) {
  is Response.Success -> {
    val location = result.data
    println("Latitude: ${location.latitude}, Longitude: ${location.longitude}")
  }
  is Response.Error -> {
    println("Location error: ${result.message}")
  }
}
```

---

## 🧭 API Overview

### Entry Point

```kotlin
val locationApi: LocationManagerApi = LocationManagerLite(context).provide()
```

### Configuration

```kotlin
LocationManagerLite(
  context = appContext, 
  timeoutMillis = 10_000L,  // how long to wait for fresh location (in milliseconds, default 10_000 ms)
  maxAgeMillis = 60_000L    // use cached location if it's not older than this (in milliseconds, default 60_000 ms)
)
```

### Location Function

```kotlin
interface LocationManagerApi {
  @RequiresPermission(anyOf = [
    Manifest.permission.ACCESS_FINE_LOCATION,
    Manifest.permission.ACCESS_COARSE_LOCATION
  ])
  suspend fun getLocation(): Response<Location>
}
```

Returns:

- ✅ `Response.Success(location)`
- ❌ `Response.Error(message)`

---

## 🛡️ Permissions

You must request runtime permissions before calling `getLocation()`.

Required permissions:
- `ACCESS_FINE_LOCATION`
- `ACCESS_COARSE_LOCATION`

🔒 To check and request permissions:

```kotlin
if (
  ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
  ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
) {
  // Safe to call getLocation()
}
```
> ⚠️ The library does not request permissions on its own.
> 
---

## ⚙️ Internal Logic

- Attempts to retrieve the last known location from all available providers, including GPS, Network, and other satellite-based sources (if supported).
- If no recent data available, waits for a fresh update (via `requestLocationUpdates`)
- Uses `withTimeout(...)` and `suspendCancellableCoroutine` for async cancellation
- All logic wrapped with `runCatching { ... }.fold(...)` and returned via Response

---

## 🧐 Use Cases

- Location tagging for notes, images, reports
- Lightweight background location polling
- Offline fallback when Play Services are unavailable
- Geofencing and contextual behaviors
- Emergency or one-shot location retrieval

---

## 🏗️ Architecture & Design

- Factory: via `LocationManagerLite.provide()`
- Interface: `LocationManagerApi`
- Implementation: `LocationManagerApiImpl`
- Design Patterns: Lazy initialization, Facade, Error-safe wrapper
- Coroutines-first: Kotlin idiomatic suspending API
- Minimal surface: no service binding

---

## ⚠️ Limitations

- Uses Android LocationManager, not fused location provider
- Accuracy and speed depend on enabled providers and environment
- Does not persist or cache location — each call starts fresh
- Not suitable for continuous tracking (but extendable)

> ⚠️ This software is provided “as is” without warranties. Use at your own risk.

---

## 🛡️ Legal & Privacy Disclaimer

- **Privacy & Location Compliance:**  
  This library accesses location data using Android's LocationManager APIs. You, as the integrator or user of this library, are solely responsible for ensuring compliance with all applicable laws and regulations concerning the collection, processing, and storage of location and personal data in your jurisdiction. The original author and contributors of this library assumes no legal responsibility or liability for any misuse or violations of privacy laws.

- **Not for Safety-Critical Use:**  
  This library is not designed or intended for use in safety-critical applications (such as emergency services, autonomous systems, or life-support environments). It provides no guarantees for accuracy, reliability, or fault tolerance. Use at your own risk.

- **Liability Disclaimer:**  
  This software is provided "AS IS", without warranties of any kind. The original author and contributors disclaims any liability for damages, losses, or consequences arising from the use, misuse, or inability to use this library in any form or context.

___

## 🤝 Contributing

Contributions are welcome:

- Report bugs or edge cases
- Suggest improvements
- Add support for additional strategies or providers
- Improve documentation or examples

---

## 📩 Feedback & Contact

- GitHub: [location-manager-lite](https://github.com/MSagGik/location-manager-lite)
- Use GitHub Issues for feedback, bug reports, or suggestions

---

## 📬 Support Policy

This library is provided as-is, without active support or maintenance obligations.  
The maintainers of this project do not offer legal or technical support, and have no obligation to respond to issues, feature requests, or pull requests.

---

## 🏢 Commercial or Enterprise Use

If you plan to use this library as part of a commercial, enterprise, or distributed SDK product, you are solely responsible for ensuring full legal compliance, adequate testing, and proper integration practices.  
No warranties, guarantees, or obligations of support or fitness are expressed or implied by the original author and contributors. Use in production environments is at your own risk.

---

## 📄 License

This project is licensed under the Apache License 2.0.

See:
- [LICENSE](./LICENSE.txt) (LICENSE.txt) for full license terms.
- [NOTICE](./NOTICE.txt) (NOTICE.txt) for legal notices and project-specific disclaimers.
- [THIRD-PARTY-NOTICES](./THIRD-PARTY-NOTICES.txt) (THIRD-PARTY-NOTICES.txt) for third-party components and license references.

Under Section 4(d) of the Apache License 2.0, if you redistribute this library or any derivative works, you **must** include the complete contents of the `NOTICE` file with your distributions.  
This `NOTICE` file **includes** the third-party attributions and license references as documented in its `THIRD-PARTY-NOTICES` section, which is an integral part of the `NOTICE`.  
Failure to include the `NOTICE` file in its entirety constitutes a breach of the license terms and may lead to legal consequences.

By redistributing this project or derivative works without including the `NOTICE` file, you assume full responsibility for any resulting legal or compliance issues.  
The original author and contributors **expressly disclaim** any liability or responsibility arising from your failure to comply with these requirements.

> ⚠️ `LocationManagerLite` is an open-source project and is not affiliated with Google, JetBrains, Oracle, or any other entity. All trademarks and brand names used in this project (such as "Android", "Kotlin", "Java") are the property of their respective owners.
> Use of third-party trademarks is for identification purposes only and does not imply any endorsement by or affiliation with the respective trademark holders.
> 
> This project is provided "as is", without warranty of any kind, either express or implied, including but not limited to warranties of merchantability, fitness for a particular purpose, and noninfringement.