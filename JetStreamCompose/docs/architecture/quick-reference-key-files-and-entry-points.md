# Quick Reference - Key Files and Entry Points

## Critical Files for Understanding the System

- **Main Entry**: `wiltv/src/main/java/com/google/wiltv/WilTvApplication.kt` (Hilt DI setup)
- **App Entry**: `wiltv/src/main/java/com/google/wiltv/presentation/App.kt` (Navigation)
- **Database**: `wiltv/src/main/java/com/google/wiltv/AppDatabase.kt` (Room database)
- **User Management**: `wiltv/src/main/java/com/google/wiltv/data/repositories/UserRepository.kt`
- **User Entity**: `wiltv/src/main/java/com/google/wiltv/data/entities/User.kt`
- **Movie Details**: `wiltv/src/main/java/com/google/wiltv/presentation/screens/moviedetails/MovieDetailsScreen.kt`
- **Build Config**: `wiltv/build.gradle.kts`

## Key Integration Points for AI Agents

**Critical files for understanding system integration:**
- `User.kt` - Core user data model with DataStore persistence
- `UserRepository.kt` - User data management patterns and DataStore usage
- `AppDatabase.kt` - Room database schema and entity relationships
- `MovieDetailsScreen.kt` - Detail screen patterns and UI components
- `TvShowDetailsScreen.kt` - TV show detail implementation
- `DashboardScreen.kt` - Main content hub and navigation patterns
