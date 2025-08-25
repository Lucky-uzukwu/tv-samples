# Development and Deployment

## Local Development Setup

**Required Steps:**
1. Clone repository and open in Android Studio
2. Use Android TV emulator (API 33+ recommended) or physical Android TV device
3. No API keys required - app includes mock data in `assets/` folder
4. Build and run: `./gradlew installDebug`

**Mock Data Available:**
- Movies: `assets/movies.json`
- Categories: `assets/movieCategories.json`  
- Cast: `assets/movieCast.json`

## Build and Deployment Process

**Build Commands:**
- `./gradlew build` - Standard build
- `./gradlew installDebug` - Install debug APK
- `./gradlew spotlessApply` - Code formatting (ktlint)
- `./gradlew :benchmark:pixel6Api33BenchmarkAndroidTest` - Generate baseline profile

**Deployment Environments:**
- Debug: Mock data enabled, localhost development
- Release: Production API, minified with ProGuard
