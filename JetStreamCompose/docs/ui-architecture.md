# WilTV Android TV Frontend Architecture Document

## Introduction

This document defines the comprehensive frontend architecture for WilTV's Android TV streaming application, covering the existing Jetpack Compose UI system and planned enhancements. It serves as the definitive guide for AI development tools and developers working on WilTV's TV-optimized user interface.

### Document Scope

**Complete Android TV frontend architecture for WilTV streaming application**  
**Covers:** Jetpack Compose UI patterns, TV-specific components, navigation, state management, styling, and enhancement features

### Change Log

| Date | Version | Description | Author |
|------|---------|-------------|--------|
| 2025-08-25 | 1.0 | Initial Android TV frontend architecture | Winston (Architect) |

## Template and Framework Selection

### Framework Analysis

WilTV uses **Jetpack Compose for TV** as its primary UI framework, representing a modern, declarative approach to Android TV interface development.

**Current Framework Stack:**
- **UI Framework**: Jetpack Compose for TV (BOM 2025.02.00)
- **Platform**: Android TV native application (API 28+)
- **Architecture Pattern**: MVVM with Compose UI integration
- **Navigation**: Jetpack Navigation Compose with TV-optimized routing
- **State Management**: StateFlow with ViewModel integration

**Framework Benefits for TV Development:**
- Declarative UI paradigm optimized for reactive state updates
- Built-in TV focus management and navigation support
- Performance optimizations for TV hardware constraints
- Seamless integration with Android TV ecosystem
- Future-proof architecture with active Google support

## Android TV Frontend Tech Stack

### Technology Stack Table

| Category | Technology | Version | Purpose | Rationale |
|----------|-----------|---------|---------|-----------|
| **UI Framework** | Jetpack Compose for TV | BOM 2025.02.00 | Declarative UI framework optimized for TV interfaces | Modern reactive UI with excellent TV focus management and performance |
| **TV Components** | androidx.tv:tv-material | 1.0.0 | TV-specific Material Design components | Purpose-built components for 10-foot TV viewing experience |
| **Architecture** | MVVM + Repository Pattern | N/A | Clean separation of concerns with reactive state | Industry standard for Android with excellent Compose integration |
| **State Management** | StateFlow + ViewModel | Lifecycle 2.8.7 | Reactive state management with lifecycle awareness | Built-in Android solution, perfect for Compose reactive updates |
| **Dependency Injection** | Hilt | 2.54 | Compile-time DI with Android lifecycle integration | Robust DI with excellent testing support and code generation |
| **Navigation** | Navigation Compose | 2.8.8 | Type-safe navigation with TV-optimized transitions | Seamless integration with Compose and TV navigation patterns |
| **Image Loading** | Coil Compose | 2.7.0 | Async image loading with caching and authentication | Kotlin-first with excellent Compose integration and memory management |
| **Theming** | Material3 + Custom TV Theme | 1.3.2 | Consistent design system optimized for TV viewing | Material Design adapted for large screens and remote navigation |
| **Focus Management** | TV Foundation | 1.0.0-alpha10 | Advanced D-pad navigation and focus handling | Essential for professional TV user experience |
| **Animation** | Compose Animation | BOM 2025.02.00 | Smooth transitions and micro-interactions | Built-in animations optimized for TV performance constraints |
| **Testing** | Compose UI Testing + JUnit | BOM 2025.02.00 | Component and integration testing for Compose UI | Standard Android testing with Compose-specific assertions |

## Project Structure

Based on the existing WilTV codebase and Android TV best practices, here's the comprehensive project structure for AI development tools:

```
wiltv/src/main/java/com/google/wiltv/
├── WilTvApplication.kt                    # Application class with Hilt setup
├── MainActivity.kt                        # Single Activity entry point
├── AppDatabase.kt                         # Room database configuration
│
├── data/                                  # Data Layer
│   ├── dao/                              # Database Access Objects
│   │   ├── MovieDao.kt
│   │   ├── WatchlistDao.kt               # New: Watchlist database operations
│   │   └── RemoteKeysDao.kt
│   ├── entities/                         # Room database entities
│   │   ├── User.kt
│   │   ├── MovieEntity.kt
│   │   ├── WatchlistItem.kt              # New: Watchlist persistence
│   │   └── WatchProgress.kt              # New: Continue watching data
│   ├── models/                           # API response models & DTOs
│   │   ├── MovieNew.kt
│   │   ├── TvShow.kt
│   │   ├── converters/                   # Room type converters
│   │   └── responses/                    # API response wrappers
│   ├── network/                          # Retrofit API services
│   │   ├── MovieService.kt
│   │   ├── SearchService.kt
│   │   ├── SportsChannelService.kt       # New: Sports content API
│   │   └── AuthInterceptor.kt
│   ├── repositories/                     # Repository implementations
│   │   ├── MovieRepository.kt + Impl
│   │   ├── UserRepository.kt
│   │   ├── WatchlistRepository.kt        # New: Watchlist operations
│   │   └── mock/                         # Mock implementations for development
│   └── paging/                           # Paging 3 sources
│       ├── MoviesPagingSource.kt
│       └── SearchPagingSource.kt
│
├── domain/                               # Domain/Business Logic Layer
│   ├── ApiResult.kt                      # Result wrapper for network operations
│   ├── Error.kt                          # Error handling types
│   └── usecases/                         # Business logic use cases
│       ├── WatchlistUseCase.kt           # New: Watchlist business logic
│       └── ContinueWatchingUseCase.kt    # New: Progress tracking logic
│
├── presentation/                         # UI Layer
│   ├── App.kt                           # Navigation host and routing
│   ├── common/                          # Reusable UI components
│   │   ├── cards/                       # Content display components
│   │   │   ├── MovieCard.kt
│   │   │   ├── TvShowCard.kt
│   │   │   └── SportChannelCard.kt      # New: Sports channel display
│   │   ├── buttons/                     # Interactive components
│   │   │   ├── CustomFillButton.kt
│   │   │   ├── PlayButton.kt
│   │   │   └── WatchlistButton.kt       # New: Watchlist toggle
│   │   ├── indicators/                  # Progress and status indicators
│   │   │   ├── ProgressBar.kt           # New: Continue watching progress
│   │   │   └── WatchlistIndicator.kt    # New: Watchlist status icon
│   │   ├── layouts/                     # Layout components
│   │   │   ├── CatalogLayout.kt
│   │   │   └── TvCatalogLayout.kt
│   │   ├── images/                      # Image handling components
│   │   │   ├── AuthenticatedAsyncImage.kt
│   │   │   └── PosterImage.kt
│   │   ├── navigation/                  # Navigation utilities
│   │   │   ├── FocusUtils.kt
│   │   │   └── BringIntoViewIfChildrenAreFocused.kt
│   │   └── errors/                      # Error state components
│   │       ├── Error.kt
│   │       ├── NetworkErrorScreen.kt    # New: Enhanced error handling
│   │       └── RetryButton.kt           # New: Error recovery actions
│   │
│   ├── screens/                         # Feature screens organized by domain
│   │   ├── auth/                        # Authentication screens
│   │   │   ├── AuthScreen.kt + ViewModel
│   │   │   └── components/              # Auth-specific components
│   │   ├── dashboard/                   # Main navigation and home
│   │   │   ├── DashboardScreen.kt + ViewModel
│   │   │   ├── DashboardSideBar.kt
│   │   │   └── navigation/
│   │   │       └── drawer/HomeDrawer.kt
│   │   ├── movies/                      # Movie-related screens
│   │   │   ├── MoviesScreen.kt + ViewModel
│   │   │   ├── MovieDetailsScreen.kt + ViewModel
│   │   │   └── components/              # Movie-specific components
│   │   ├── tvshows/                     # TV show screens
│   │   │   ├── TvShowsScreen.kt + ViewModel
│   │   │   ├── TvShowDetailsScreen.kt + ViewModel
│   │   │   └── components/
│   │   ├── search/                      # Search functionality
│   │   │   ├── SearchScreen.kt + ViewModel
│   │   │   ├── ModernSearchScreen.kt    # New: Enhanced search UX
│   │   │   └── components/
│   │   │       ├── SearchFilters.kt     # New: Advanced filtering
│   │   │       └── SearchSuggestions.kt # New: Autocomplete
│   │   ├── watchlist/                   # New: Watchlist management
│   │   │   ├── WatchlistScreen.kt + ViewModel
│   │   │   └── components/
│   │   │       └── WatchlistGrid.kt
│   │   ├── sports/                      # New: Live sports channels
│   │   │   ├── LiveSportsScreen.kt + ViewModel
│   │   │   └── components/
│   │   │       └── SportsChannelGrid.kt
│   │   ├── profile/                     # User settings and preferences
│   │   │   ├── ProfileScreen.kt + ViewModel
│   │   │   ├── LanguageSettingsScreen.kt # New: i18n language selection
│   │   │   └── components/
│   │   ├── categories/                  # Content categorization
│   │   │   ├── CategoriesScreen.kt + ViewModel
│   │   │   └── CategoryMovieListScreen.kt + ViewModel
│   │   └── videoplayer/                 # Video playback
│   │       ├── VideoPlayerScreen.kt + ViewModel
│   │       └── components/
│   │           ├── VideoPlayerControls.kt
│   │           ├── VideoPlayerSeeker.kt
│   │           └── ContinueWatchingOverlay.kt # New: Resume functionality
│   │
│   ├── theme/                           # Design system and theming
│   │   ├── Color.kt                     # Color palette definitions
│   │   ├── Theme.kt                     # Material theme configuration
│   │   ├── Type.kt                      # Typography scale
│   │   ├── Shape.kt                     # Shape definitions
│   │   └── WilTvFocusTheme.kt          # TV-specific focus theming
│   │
│   └── utils/                           # UI utilities and extensions
│       ├── Extensions.kt                # Kotlin extensions for UI
│       ├── ModifierUtils.kt            # Custom Compose modifiers
│       ├── Padding.kt                  # Consistent spacing definitions
│       └── GradientBg.kt               # Background styling utilities
│
├── state/                               # Global state management
│   ├── UserStateHolder.kt              # User authentication state
│   ├── UserState.kt                    # User state data class
│   ├── WatchlistStateHolder.kt         # New: Global watchlist state
│   └── AppStateHolder.kt               # New: App-wide state coordination
│
└── util/                               # Cross-cutting utilities
    ├── DeviceNetworkInfo.kt            # Network status utilities
    └── Constants.kt                    # App-wide constants

# Resources Structure
wiltv/src/main/res/
├── values/                             # Default values
│   ├── strings.xml                     # Base language strings
│   ├── colors.xml                      # Color resources
│   ├── themes.xml                      # Material theme definitions
│   └── dimens.xml                      # Dimension resources for TV spacing
├── values-es/                          # New: Spanish translations
│   └── strings.xml
├── values-fr/                          # New: French translations
│   └── strings.xml
├── drawable/                           # Vector drawables and icons
│   ├── ic_watchlist_outlined.xml       # New: Watchlist icons
│   ├── ic_watchlist_filled.xml
│   ├── ic_sports.xml                   # New: Sports channel icons
│   └── ic_continue_watching.xml        # New: Continue watching icon
└── font/                               # Custom fonts for TV readability
    ├── inter_regular.ttf
    └── lexend_exa_medium.ttf
```

## Component Standards

### Component Template

Standard component template for WilTV Android TV components:

```kotlin
// ABOUTME: Standard WilTV component template following TV-optimized Compose patterns
// ABOUTME: Includes focus management, state handling, and accessibility for 10-foot UI

package com.google.wiltv.presentation.common.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.focusable
import androidx.compose.foundation.clickable
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.google.wiltv.presentation.theme.WilTvTheme

/**
 * Standard WilTV component following TV-optimized patterns.
 * 
 * @param modifier Compose modifier for layout and styling
 * @param focusRequester Optional focus requester for programmatic focus control
 * @param onAction Primary action callback (click/select)
 * @param onFocusChanged Callback for focus state changes
 * @param enabled Whether the component is interactive
 * @param contentDescription Accessibility description for screen readers
 */
@Composable
fun WilTvComponentTemplate(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    onAction: () -> Unit = {},
    onFocusChanged: (Boolean) -> Unit = {},
    enabled: Boolean = true,
    contentDescription: String? = null,
    // Component-specific parameters
    title: String,
    subtitle: String? = null,
    isSelected: Boolean = false,
) {
    var isFocused by remember { mutableStateOf(false) }
    
    Surface(
        modifier = modifier
            .let { if (focusRequester != null) it.focusRequester(focusRequester) else it }
            .onFocusChanged { focusState ->
                isFocused = focusState.isFocused
                onFocusChanged(focusState.isFocused)
            }
            .focusable(enabled)
            .clickable(enabled = enabled) { onAction() }
            .semantics {
                if (contentDescription != null) {
                    this.contentDescription = contentDescription
                }
            },
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.colors(
            containerColor = if (isFocused) {
                MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        border = CardDefaults.border(
            focusedBorder = androidx.tv.material3.Border(
                border = androidx.compose.foundation.BorderStroke(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            )
        ),
        scale = CardDefaults.scale(focusedScale = 1.05f)
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = if (enabled) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                }
            )
            
            subtitle?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
```

### Naming Conventions

**Component Files and Classes:**
- **Components**: PascalCase with descriptive names - `MovieCard.kt`, `WatchlistButton.kt`, `SearchFilters.kt`
- **Screens**: `[Feature]Screen.kt` + `[Feature]ScreenViewModel.kt`
- **State Classes**: `[Feature]ScreenUiState` with sealed class hierarchy

**Resource Naming:**
- **Strings**: snake_case with feature prefix - `watchlist_add_button`, `continue_watching_progress`
- **Drawables**: snake_case with ic_ prefix - `ic_watchlist_outlined.xml`, `ic_continue_watching.xml`
- **Colors**: snake_case descriptive names - `primary_color`, `focus_border_color`

**TV-Specific Conventions:**
- **Focus Management**: All interactive components handle focus states
- **Accessibility**: ContentDescription required for all interactive elements
- **Navigation**: `on[Action]` pattern for remote control actions

## State Management

### Store Structure

```
state/
├── global/                              # App-wide state holders
│   ├── UserStateHolder.kt              # Existing: User authentication state
│   ├── WatchlistStateHolder.kt         # New: Global watchlist state management
│   ├── ContinueWatchingStateHolder.kt  # New: Continue watching progress state
│   └── AppPreferencesStateHolder.kt    # New: Language, theme, and user preferences
│
├── screen/                              # Screen-specific state management
│   ├── dashboard/DashboardScreenViewModel.kt
│   ├── watchlist/WatchlistScreenViewModel.kt
│   ├── search/ModernSearchViewModel.kt
│   └── sports/LiveSportsScreenViewModel.kt
│
├── data/                               # Data state classes
│   ├── WatchlistState.kt
│   ├── ContinueWatchingState.kt
│   └── SearchState.kt
│
└── di/                                # State management DI modules
    ├── StateModule.kt
    └── ViewModelModule.kt
```

### State Management Template

```kotlin
@Singleton
@HiltViewModel
class WatchlistStateHolder @Inject constructor(
    private val watchlistRepository: WatchlistRepository,
    private val userStateHolder: UserStateHolder
) : ViewModel() {

    private val _watchlistState = MutableStateFlow(WatchlistState())
    val watchlistState: StateFlow<WatchlistState> = _watchlistState.asStateFlow()
    
    val watchlistItems: StateFlow<List<WatchlistItemUi>> = combine(
        _watchlistState,
        userStateHolder.userState
    ) { watchlist, user ->
        if (user.user?.token != null) {
            watchlist.items.map { it.toUiModel() }
        } else {
            emptyList()
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addToWatchlist(contentId: String, contentType: ContentType) {
        viewModelScope.launch {
            try {
                // Optimistic update
                updateWatchlistState { currentState ->
                    // Update logic
                }
                watchlistRepository.addToWatchlist(contentId, contentType)
            } catch (error: Exception) {
                handleError(error)
                loadWatchlist()
            }
        }
    }
}
```

## API Integration

### Service Template

```kotlin
@Singleton
interface WatchlistService {
    @GET("user/watchlist")
    suspend fun getWatchlist(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20
    ): Response<WatchlistResponse>

    @POST("user/watchlist")
    suspend fun addToWatchlist(
        @Body request: AddToWatchlistRequest
    ): Response<WatchlistActionResponse>

    @DELETE("user/watchlist/{contentId}")
    suspend fun removeFromWatchlist(
        @Path("contentId") contentId: String
    ): Response<WatchlistActionResponse>
}
```

### API Client Configuration

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object EnhancedNetworkModule {

    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor,
        cacheInterceptor: CacheInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(cacheInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideWatchlistService(retrofit: Retrofit): WatchlistService =
        retrofit.create(WatchlistService::class.java)
}
```

## Routing

### Route Configuration

```kotlin
object Screens {
    // Existing screens
    fun Dashboard() = "dashboard"
    fun MovieDetails() = "movie_details/{movieId}"
    fun VideoPlayer() = "video_player/{contentId}"
    
    // New enhancement screens
    fun WatchlistScreen() = "watchlist"
    fun ModernSearchScreen() = "modern_search"
    fun LiveSportsScreen() = "live_sports"
    fun LanguageSettingsScreen() = "language_settings"
    
    object MovieDetails {
        const val MovieIdBundleKey = "movieId"
        fun withArgs(movieId: String) = "movie_details/$movieId"
    }
}

@Composable
fun EnhancedWilTvNavigation(
    navController: NavHostController,
    userStateHolder: UserStateHolder,
    onBackPressed: () -> Unit
) {
    NavHost(
        navController = navController,
        startDestination = if (userStateHolder.userState.collectAsState().value.user?.token != null) {
            Screens.Dashboard()
        } else {
            Screens.AuthScreen()
        }
    ) {
        composable(route = Screens.Dashboard()) {
            DashboardScreen(
                onOpenWatchlist = { navController.navigate(Screens.WatchlistScreen()) },
                onOpenLiveSports = { navController.navigate(Screens.LiveSportsScreen()) }
            )
        }
        
        composable(route = Screens.WatchlistScreen()) {
            WatchlistScreen(
                onMovieSelected = { movieId ->
                    navController.navigate(Screens.MovieDetails.withArgs(movieId))
                },
                onBackPressed = { navController.navigateUp() }
            )
        }
    }
}
```

## Styling Guidelines

### Styling Approach

WilTV uses **Jetpack Compose with Material3 theming** specifically optimized for Android TV interfaces, combining Material Design principles with TV-specific adaptations for 10-foot viewing experiences.

**Core Styling Philosophy:**
- **TV-First Design**: All styling optimized for large screen viewing distances
- **Focus-Driven Interactions**: Visual hierarchy emphasizes focusable elements
- **High Contrast**: Colors and typography ensure readability on TV displays
- **Performance-Optimized**: Styling patterns for smooth TV hardware performance

### Global Theme Variables

```css
:root {
  /* TV-Optimized Color System */
  --wiltv-primary: #1976D2;
  --wiltv-background: #121212;
  --wiltv-surface: #1E1E1E;
  --wiltv-on-background: #E0E0E0;
  
  /* Focus and Interaction */
  --wiltv-focus-ring: #2196F3;
  --wiltv-focus-ring-width: 3px;
  --wiltv-scale-focus: 1.05;
  
  /* TV-Optimized Spacing */
  --wiltv-space-xs: 4px;
  --wiltv-space-sm: 8px;
  --wiltv-space-md: 16px;
  --wiltv-space-lg: 24px;
  --wiltv-space-xl: 32px;
  --wiltv-space-xxl: 48px;
  
  /* Typography for 10-foot viewing */
  --wiltv-font-size-sm: 16px;
  --wiltv-font-size-md: 18px;
  --wiltv-font-size-lg: 22px;
  --wiltv-font-size-xl: 28px;
  --wiltv-font-size-xxl: 36px;
  
  /* Animation and Transitions */
  --wiltv-transition-fast: 150ms ease-out;
  --wiltv-transition-normal: 250ms ease-out;
  --wiltv-focus-transition: all var(--wiltv-transition-fast);
}
```

## Testing Requirements

### Component Test Template

```kotlin
@RunWith(AndroidJUnit4::class)
class WatchlistButtonTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun watchlistButton_onClick_triggersCallback() {
        val mockOnWatchlistToggle = mock<(Boolean) -> Unit>()
        
        composeTestRule.setContent {
            WilTvTheme {
                WatchlistButton(
                    isInWatchlist = false,
                    onWatchlistToggle = mockOnWatchlistToggle,
                    contentDescription = "Add to watchlist"
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription("Add to watchlist")
            .performClick()

        verify(mockOnWatchlistToggle).invoke(true)
    }

    @Test
    fun watchlistButton_focusManagement_worksCorrectly() {
        val focusRequester = FocusRequester()
        
        composeTestRule.setContent {
            WilTvTheme {
                WatchlistButton(
                    focusRequester = focusRequester,
                    contentDescription = "Add to watchlist"
                )
            }
        }

        composeTestRule.runOnIdle {
            focusRequester.requestFocus()
        }

        composeTestRule
            .onNodeWithContentDescription("Add to watchlist")
            .assertIsFocused()
    }
}
```

### Testing Best Practices

1. **Focus Management Testing**: Test D-pad navigation and focus states
2. **Remote Control Simulation**: Test TV remote control inputs
3. **Accessibility Compliance**: Verify content descriptions and screen reader support
4. **Performance Testing**: Test with large datasets and smooth animations
5. **State Management Testing**: Test optimistic updates and error handling
6. **TV-Specific Scenarios**: Test 10-foot UI and navigation patterns

## Environment Configuration

### Required Environment Variables

```bash
# Core Application Configuration
WILTV_SERVER_URL=https://api.nortv.xyz
WILTV_API_TIMEOUT=30000

# Enhanced Features
WILTV_WATCHLIST_MAX_ITEMS=1000
WILTV_CONTINUE_WATCHING_MAX_ITEMS=50
WILTV_SEARCH_SUGGESTIONS_COUNT=8

# Internationalization
WILTV_DEFAULT_LANGUAGE=en
WILTV_SUPPORTED_LANGUAGES=en,es,fr,de,it,pt,zh,ja

# Performance Configuration
WILTV_IMAGE_CACHE_SIZE=100
WILTV_NETWORK_RETRY_COUNT=3

# Feature Flags
WILTV_ENABLE_MOCK_DATA=false
WILTV_SPORTS_CHANNELS_ENABLED=true
WILTV_ENABLE_ANALYTICS=true
```

### Configuration Management

```kotlin
@Singleton
class ConfigurationManager @Inject constructor() {
    val serverUrl: String get() = BuildConfig.SERVER_URL
    val watchlistMaxItems: Int get() = BuildConfig.WATCHLIST_MAX_ITEMS
    val supportedLanguages: List<String> get() = BuildConfig.SUPPORTED_LANGUAGES.split(",")
    
    fun isLanguageSupported(languageCode: String): Boolean {
        return languageCode in supportedLanguages
    }
}
```

## Frontend Developer Standards

### Critical Coding Rules

**Universal Android TV Development Rules:**

1. **Focus Management is Mandatory**: Every interactive component must handle focus states
2. **Accessibility is Non-Negotiable**: ContentDescription required for all interactive elements
3. **TV-Optimized Sizing**: Use TV-specific spacing and font sizes for 10-foot viewing
4. **State Management Consistency**: Use StateFlow with optimistic UI updates
5. **Performance Requirements**: Lazy loading and proper caching for TV hardware

**Jetpack Compose Specific Rules:**

6. **Composition Optimization**: Use `remember` for expensive calculations
7. **State Hoisting**: Pass callbacks down, never pass ViewModels to leaf components
8. **Resource Management**: Dispose resources properly in DisposableEffect

**WilTV-Specific Rules:**

9. **Repository Pattern**: Always inject through Hilt with mock/real data switching
10. **Authentication Integration**: Check user state before accessing protected features

### Quick Reference

**Essential Commands:**
```bash
./gradlew installDebug        # Install on connected TV
./gradlew spotlessApply       # Format code
./gradlew testDebugUnitTest   # Run tests
```

**Key Import Patterns:**
```kotlin
import androidx.compose.runtime.*
import androidx.tv.material3.*
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.wiltv.presentation.theme.WilTvTheme
```

**Component Pattern:**
```kotlin
@Composable
fun WilTvComponent(
    modifier: Modifier = Modifier,
    focusRequester: FocusRequester? = null,
    onAction: () -> Unit = {},
    contentDescription: String? = null
) {
    // TV-optimized component implementation
}
```

---

**This comprehensive Android TV Frontend Architecture document provides the complete foundation for implementing all WilTV enhancement features while maintaining TV-specific best practices, performance optimization, and accessibility compliance. The architecture ensures consistent, high-quality development that meets modern Android TV standards and user expectations.**