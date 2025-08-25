# High Level Architecture

## Technical Summary

WilTV is a modern Android TV streaming application built with Jetpack Compose for TV, following MVVM + Repository pattern with comprehensive dependency injection via Hilt.

## Actual Tech Stack (from build.gradle.kts and libs.versions.toml)

| Category           | Technology          | Version    | Notes                                    |
| ------------------ | ------------------- | ---------- | ---------------------------------------- |
| Language           | Kotlin              | 2.1.0      | JVM Toolchain 17                         |
| UI Framework       | Jetpack Compose TV  | 2025.02.00 | TV-specific material components          |
| DI Framework       | Hilt                | 2.54       | Comprehensive DI setup                   |
| Database           | Room + Paging 3     | 2.7.2      | Local caching with remote mediator      |
| Networking         | Retrofit + Gson     | 2.9.0      | REST API communication                   |
| Media Player       | Media3 ExoPlayer    | 1.6.0      | Video streaming with HLS support         |
| State Management   | DataStore           | 1.1.6      | User preferences and auth state          |
| Navigation         | Navigation Compose  | 2.8.8      | Screen-based routing                     |
| Image Loading      | Coil                | 2.7.0      | Async image loading with authentication  |
| Real-time          | Pusher              | 2.2.1      | WebSocket communication                  |
| QR Code            | ZXing               | 3.5.3      | QR code generation for auth              |
| Build System       | Gradle              | 8.8.2      | Kotlin DSL, baseline profiles enabled    |

## Repository Structure Reality Check

- **Type:** Single module Android application
- **Package Manager:** Gradle with version catalogs (libs.versions.toml)
- **Notable:** TV-specific focus, mock implementations for offline development
