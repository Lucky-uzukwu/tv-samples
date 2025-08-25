# Architecture Patterns for Feature Development

## Database Layer Patterns

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

## UI Layer Patterns

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

## State Management Patterns

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
