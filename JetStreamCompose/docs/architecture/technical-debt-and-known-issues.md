# Technical Debt and Known Issues

## Critical Technical Debt

1. **Mock Implementation Toggle**: `@Named("isMock")` has inverse logic (`BuildConfig.DEBUG == false`) - confusing
2. **Database Schema**: Only movies cached locally, no TV shows or user watchlist persistence
3. **User Repository**: Uses individual DataStore keys instead of single User object - verbose and error-prone
4. **Navigation State**: `selectedMovie` and `selectedTvShow` stored in App.kt as mutable state - could be lost on process death
5. **Inconsistent Naming**: MoviesDao vs MovieDao, mixed singular/plural naming conventions

## Workarounds and Gotchas

- **Debug Token**: UserRepository hardcodes `"debug_token_123"` as fallback token for development
- **URL Encoding**: Video player requires manual URL encoding for TV channel links (see App.kt:284-291)
- **Focus Management**: TV focus requires careful state management - existing patterns in `FocusUtils.kt`
- **Profile Selection**: User must select profile after authentication - no automatic profile assignment

## Development Considerations

1. **Database Extensions**: Room database designed for easy schema extensions via migrations
2. **User Data Model**: Simple User entity with DataStore persistence - no complex relationships
3. **DataStore Pattern**: Individual key-based approach for user preferences and settings
4. **State Management**: Navigation state handled in App.kt - consider StateHolder patterns for complex state
