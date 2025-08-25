# WilTV Brownfield Architecture Document

## Introduction

This document captures the CURRENT STATE of the WilTV Android TV streaming application codebase, including technical debt, workarounds, and real-world patterns. It serves as a reference for AI agents working on enhancements.

### Document Scope

**Comprehensive documentation of the entire WilTV Android TV streaming application**  
**Covers:** Complete architecture, data layer, UI components, technical debt, and development patterns

### Change Log

| Date       | Version | Description                 | Author |
| ---------- | ------- | --------------------------- | ------ |
| 2025-08-25 | 1.0     | Initial brownfield analysis | Claude |

## Quick Reference - Key Files and Entry Points

### Critical Files for Understanding the System

- **Main Entry**: `wiltv/src/main/java/com/google/wiltv/WilTvApplication.kt` (Hilt DI setup)
- **App Entry**: `wiltv/src/main/java/com/google/wiltv/presentation/App.kt` (Navigation)
- **Database**: `wiltv/src/main/java/com/google/wiltv/AppDatabase.kt` (Room database)
- **User Management**: `wiltv/src/main/java/com/google/wiltv/data/repositories/UserRepository.kt`
- **User Entity**: `wiltv/src/main/java/com/google/wiltv/data/entities/User.kt`
- **Movie Details**: `wiltv/src/main/java/com/google/wiltv/presentation/screens/moviedetails/MovieDetailsScreen.kt`
- **Build Config**: `wiltv/build.gradle.kts`

### Key Integration Points for AI Agents

**Critical files for understanding system integration:**
- `User.kt` - Core user data model with DataStore persistence
- `UserRepository.kt` - User data management patterns and DataStore usage
- `AppDatabase.kt` - Room database schema and entity relationships
- `MovieDetailsScreen.kt` - Detail screen patterns and UI components
- `TvShowDetailsScreen.kt` - TV show detail implementation
- `DashboardScreen.kt` - Main content hub and navigation patterns

## High Level Architecture

### Technical Summary

WilTV is a modern Android TV streaming application built with Jetpack Compose for TV, following MVVM + Repository pattern with comprehensive dependency injection via Hilt.

### Actual Tech Stack (from build.gradle.kts and libs.versions.toml)

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

### Repository Structure Reality Check

- **Type:** Single module Android application
- **Package Manager:** Gradle with version catalogs (libs.versions.toml)
- **Notable:** TV-specific focus, mock implementations for offline development

## Source Tree and Module Organization

### Project Structure (Actual)

```text
wiltv/
├── src/main/java/com/google/wiltv/
│   ├── WilTvApplication.kt          # Hilt application + DI modules
│   ├── AppDatabase.kt               # Room database configuration
│   ├── MainActivity.kt              # Single activity entry point
│   ├── data/                        # Data layer - well organized
│   │   ├── dao/                     # Room DAOs (MoviesDao, RemoteKeysDao)
│   │   ├── entities/                # Room entities (User, MovieEntity, Profile)
│   │   ├── models/                  # API response models + converters
│   │   ├── network/                 # Retrofit services (12+ service interfaces)
│   │   ├── repositories/            # Repository implementations + mocks
│   │   ├── paging/                  # Paging 3 sources for large datasets
│   │   └── util/                    # Data utilities and constants
│   ├── presentation/                # UI layer - Compose screens
│   │   ├── App.kt                   # Navigation host and routing
│   │   ├── screens/                 # Feature screens (auth, movies, etc.)
│   │   ├── common/                  # Reusable UI components
│   │   ├── theme/                   # Material theme and styling
│   │   └── utils/                   # UI utilities and extensions
│   ├── state/                       # Global state management
│   │   ├── UserStateHolder.kt       # User authentication state
│   │   └── UserState.kt             # User state data class
│   └── domain/                      # Domain layer (minimal)
│       ├── ApiResult.kt             # Network result wrapper
│       └── Error.kt                 # Error handling types
├── src/main/assets/                 # Local JSON files for mock data
└── src/main/res/                    # Android resources (themes, fonts, images)
```

### Key Modules and Their Purpose

- **Data Layer**: Comprehensive repository pattern with Room + Retrofit + Paging
- **Presentation Layer**: Pure Compose UI with MVVM ViewModels
- **Dependency Injection**: Hilt modules in WilTvApplication.kt with mock/real switching
- **State Management**: Centralized user state via UserStateHolder
- **Navigation**: Screen-based navigation with parameter passing

## Data Models and APIs

### Data Models

**Core Entities (Room Database):**
- **User**: `data/entities/User.kt` - DataStore-persisted user info (identifier, name, email, token, device info)
- **MovieEntity**: `data/entities/MovieEntity.kt` - Complete movie data with embedded video, genres, cast
- **MovieRemoteKey**: Paging 3 remote keys for pagination

**API Models:**
- **MovieNew**: `data/models/Film.kt` - API response model for movies
- **TvShow**: API response model for TV shows
- **Person**: Cast and crew information
- **Genre, Country, Language**: Metadata models
- **StreamingProvider**: Available platforms

**Key Relationships:**
- User ↔ Profile (1:many relationship via ProfileRepository)
- MovieEntity contains embedded VideoEntity and lists of related data
- Paging models separate from display models

### API Specifications

**Network Services (12+ Retrofit interfaces):**
- `AuthRepository` - User authentication via UserService
- `MovieRepository` - Movie data via MovieService + CatalogService  
- `TvShowsRepository` - TV show data via TvShowsService
- `SearchRepository` - Search functionality across content types
- `ProfileRepository` - User profile management

**Mock vs Real Data:**
- Mock implementations available for offline development
- Controlled by `@Named("isMock")` boolean in DI
- Currently: `BuildConfig.DEBUG == false` (NOTE: inverse logic)

## Technical Debt and Known Issues

### Critical Technical Debt

1. **Mock Implementation Toggle**: `@Named("isMock")` has inverse logic (`BuildConfig.DEBUG == false`) - confusing
2. **Database Schema**: Only movies cached locally, no TV shows or user watchlist persistence
3. **User Repository**: Uses individual DataStore keys instead of single User object - verbose and error-prone
4. **Navigation State**: `selectedMovie` and `selectedTvShow` stored in App.kt as mutable state - could be lost on process death
5. **Inconsistent Naming**: MoviesDao vs MovieDao, mixed singular/plural naming conventions

### Workarounds and Gotchas

- **Debug Token**: UserRepository hardcodes `"debug_token_123"` as fallback token for development
- **URL Encoding**: Video player requires manual URL encoding for TV channel links (see App.kt:284-291)
- **Focus Management**: TV focus requires careful state management - existing patterns in `FocusUtils.kt`
- **Profile Selection**: User must select profile after authentication - no automatic profile assignment

### Development Considerations

1. **Database Extensions**: Room database designed for easy schema extensions via migrations
2. **User Data Model**: Simple User entity with DataStore persistence - no complex relationships
3. **DataStore Pattern**: Individual key-based approach for user preferences and settings
4. **State Management**: Navigation state handled in App.kt - consider StateHolder patterns for complex state

## Integration Points and External Dependencies

### External Services

| Service   | Purpose        | Integration Type | Key Files                            |
| --------- | -------------- | ---------------- | ------------------------------------ |
| API Server| Content + Auth | REST API         | `data/network/*Service.kt` (12 files)|
| Pusher    | Real-time      | WebSocket        | Real-time communication              |

**External API Details:**
- **Base URL**: `https://api.nortv.xyz` (release builds only)
- **Authentication**: Token-based via AuthInterceptor
- **Content Types**: Movies, TV Shows, TV Channels, User profiles

### Internal Integration Points

- **Authentication Flow**: AuthScreen → ProfileSelection → Dashboard (see App.kt navigation)
- **State Management**: UserStateHolder manages global user state via StateFlow
- **Database Integration**: Room with Paging 3 RemoteMediator for offline-first experience  
- **Media Playback**: ExoPlayer integration in VideoPlayerScreen with HLS support
- **Image Authentication**: Custom AuthenticatedAsyncImage for secure image loading

## Development and Deployment

### Local Development Setup

**Required Steps:**
1. Clone repository and open in Android Studio
2. Use Android TV emulator (API 33+ recommended) or physical Android TV device
3. No API keys required - app includes mock data in `assets/` folder
4. Build and run: `./gradlew installDebug`

**Mock Data Available:**
- Movies: `assets/movies.json`
- Categories: `assets/movieCategories.json`  
- Cast: `assets/movieCast.json`

### Build and Deployment Process

**Build Commands:**
- `./gradlew build` - Standard build
- `./gradlew installDebug` - Install debug APK
- `./gradlew spotlessApply` - Code formatting (ktlint)
- `./gradlew :benchmark:pixel6Api33BenchmarkAndroidTest` - Generate baseline profile

**Deployment Environments:**
- Debug: Mock data enabled, localhost development
- Release: Production API, minified with ProGuard

## Testing Reality

### Current Test Coverage

**Unit Tests:** Limited coverage in `wiltv/src/test/java/`
- NetworkModule tests
- AuthInterceptor tests  
- Authentication edge case tests
- Image authentication tests

**Integration Tests:** None currently implemented
**UI Tests:** None currently implemented  
**Manual Testing:** Primary QA method via Android TV emulator

### Running Tests

```bash
./gradlew test           # Run unit tests
./gradlew connectedAndroidTest  # Would run instrumented tests (none exist)
```

## Architecture Patterns for Feature Development

### Database Layer Patterns

**Room Database Structure:**
- Simple entity design with `@Entity` annotations
- TypeConverters for complex data types (List<Genre>, List<Person>, etc.)
- Paging 3 integration via RemoteMediator pattern
- Version management with migration support

**Repository Pattern:**
- Interface + Implementation separation
- Mock implementations for offline development
- DataStore for simple key-value persistence
- Retrofit services for API communication
- Dependency injection via Hilt modules

### UI Layer Patterns

**Compose Screen Architecture:**
- Screen composables with ViewModel integration
- State management via StateFlow and collectAsStateWithLifecycle
- TV-specific focus handling and navigation
- Reusable UI components in `common/` package
- Custom theming for TV displays

**Navigation Patterns:**
- Centralized navigation in `App.kt` with NavHost
- Parameter passing via navigation arguments
- Deep linking support for content
- Back stack management for TV UX

### State Management Patterns

**Global State:**
- UserStateHolder for authentication state
- StateFlow for reactive state updates
- DataStore for persistent user preferences
- Hilt injection for state holders

**Screen State:**
- ViewModel state management with sealed classes
- Loading/Error/Success state patterns
- Paging state with collectAsLazyPagingItems
- Focus state management for TV navigation

## Appendix - Useful Commands and Scripts

### Frequently Used Commands

```bash
./gradlew installDebug        # Install debug build on connected device
./gradlew build               # Full project build  
./gradlew spotlessApply       # Auto-format code with ktlint
./gradlew clean build         # Clean build from scratch
```

### Debugging and Troubleshooting

**TV Focus Issues:**
- Use `BringIntoViewIfChildrenAreFocused.kt` modifier for scroll containers
- Check existing focus patterns in movie/TV show detail screens

**Database Issues:**
- Room schema changes require version increment and migration
- Test migrations with existing user data in DataStore

**Mock vs Real Data:**
- Toggle `@Named("isMock")` logic in WilTvApplication.kt
- Mock implementations in `data/repositories/Mock*RepositoryImpl.kt`

**Common Development Issues:**
- TV emulator focus navigation requires D-pad or arrow keys
- Image loading requires authentication interceptor for user content
- Navigation state can be lost - use SavedStateHandle for critical data

---

**This document reflects the actual state of WilTV as of 2025-08-25, including technical debt and implementation patterns that must be followed for successful integration of new features.**