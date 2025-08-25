# Source Tree and Module Organization

## Project Structure (Actual)

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

## Key Modules and Their Purpose

- **Data Layer**: Comprehensive repository pattern with Room + Retrofit + Paging
- **Presentation Layer**: Pure Compose UI with MVVM ViewModels
- **Dependency Injection**: Hilt modules in WilTvApplication.kt with mock/real switching
- **State Management**: Centralized user state via UserStateHolder
- **Navigation**: Screen-based navigation with parameter passing
