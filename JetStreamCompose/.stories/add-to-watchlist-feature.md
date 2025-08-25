# Add to Watchlist Feature - Brownfield Addition

**Story ID:** WILTV-001  
**Created:** 2025-08-25  
**Status:** Ready for Implementation  
**Complexity:** Single Development Session (4-6 hours)

## User Story

As a **WilTV user**,  
I want **to add movies and TV shows to my personal watchlist and have it saved permanently**,  
So that **I can easily find and track content I want to watch later, even after closing and reopening the app**.

## Story Context

**Existing System Integration:**
- **Integrates with:** User profile system, MovieDetails/TvShowDetails screens, Room database, UserRepository
- **Technology:** Room database, Hilt DI, Compose UI, existing User/Profile entities  
- **Follows pattern:** Existing repository pattern (UserRepository, ProfileRepository), database entity patterns
- **Touch points:** User state management, movie detail screens, TV show detail screens, database schema

## Acceptance Criteria

**Functional Requirements:**
1. Users can add movies and TV shows to their watchlist from detail screens
2. Users can remove items from their watchlist  
3. Watchlist data persists across app restarts and user sessions
4. Watchlist items display with visual indication (filled/unfilled bookmark icon)

**Integration Requirements:**
5. Existing user profile system continues to work unchanged
6. New functionality follows existing repository and database entity patterns
7. Integration with MovieDetailsScreen and TvShowDetailsScreen maintains current behavior and navigation

**Quality Requirements:**
8. Changes are covered by appropriate tests
9. Database migrations handle schema updates properly
10. No regression in existing user management functionality verified

## UI/UX Design

### Watchlist Toggle Button
**Location:** Movie/TV Show details screens alongside existing "Play" button
**Visual States:**
- **Not in Watchlist:** Outline bookmark icon + "Add to Watchlist" text
- **In Watchlist:** Filled bookmark icon + "In Watchlist" text  
- **Loading:** Spinner while saving/removing

**Button Layout:**
```
┌─────────────────┬──────────────────┐
│   ▶ PLAY       │  🔖 WATCHLIST   │
│                │                  │
└─────────────────┴──────────────────┘
```

### Watchlist Access Points
1. **Primary:** "My Watchlist" row on Home screen (follows existing MoviesRow pattern)
2. **Secondary:** "Watchlist" option in Profile screen menu
3. **Dedicated:** Full watchlist screen following existing MoviesScreen layout

### Visual Integration
- Use existing `CustomFillButton.kt` component style
- Follow color scheme from `theme/Color.kt`
- Maintain TV focus handling from `FocusUtils.kt`
- Use existing font styles from `theme/Type.kt`

## Technical Implementation

### Database Changes
- Create `WatchlistItem` entity linked to User and content (movies/TV shows)
- Extend User entity with watchlist relationship
- Add Room migration for schema updates

### Repository Layer
- Add watchlist methods to `UserRepository.kt` following existing patterns
- Implement add/remove/check watchlist functionality
- Cache watchlist state in memory for performance

### UI Components
**Files to Modify:**
- `MovieDetailsScreen.kt` - Add watchlist toggle button
- `TvShowDetailsScreen.kt` - Add watchlist toggle button
- `HomeScreen.kt` - Add "My Watchlist" content row
- `ProfileScreen.kt` - Add watchlist menu option

**New Components:**
- `WatchlistButton.kt` - Reusable toggle component
- `WatchlistScreen.kt` - Dedicated watchlist view
- Extend existing card components for watchlist context

### Integration Pattern
Follow existing patterns from:
- `UserRepository.kt` and `ProfileRepository.kt` for data operations
- `User.kt`, `Movie.kt` entities for database structure
- `MovieDetailsScreen.kt` and `TvShowDetailsScreen.kt` for UI integration

## Technical Notes

**Integration Approach:** 
- Extend existing User/Profile entity with watchlist relationship
- Create WatchlistItem entity linked to user and content (movies/TV shows)
- Add watchlist methods to UserRepository following existing patterns
- Update detail screens with watchlist toggle buttons

**Key Constraints:** 
- Must work with existing user authentication system
- Database changes must be backward compatible
- UI integration should not disrupt existing detail screen layouts

## Risk Assessment

**Primary Risk:** Database schema changes could affect existing user data or cause migration issues
**Mitigation:** Use Room migrations, test thoroughly with existing data, make additive-only changes
**Rollback:** Remove watchlist UI elements, database migration can revert schema changes

## Compatibility Verification

- ✅ No breaking changes to existing APIs (additive repository methods only)
- ✅ Database changes are additive only (new entity + relationship)
- ✅ UI changes follow existing design patterns
- ✅ Performance impact is negligible (simple database queries, cached in memory)

## Definition of Done

- [ ] Users can add/remove movies and TV shows to/from watchlist
- [ ] Watchlist state persists across app restarts  
- [ ] Visual indicators show watchlist status on detail screens
- [ ] Existing user profile functionality remains unchanged
- [ ] Database migration successfully handles schema updates
- [ ] Code follows existing repository and entity patterns
- [ ] Tests pass (existing and new)
- [ ] No UI regressions in movie/TV show detail screens

## Implementation Checklist

### Phase 1: Database Layer
- [ ] Create `WatchlistItem` entity
- [ ] Add Room migration for schema update
- [ ] Extend UserRepository with watchlist methods
- [ ] Add unit tests for repository methods

### Phase 2: UI Components  
- [ ] Create `WatchlistButton.kt` component
- [ ] Integrate button into MovieDetailsScreen
- [ ] Integrate button into TvShowDetailsScreen
- [ ] Add proper focus handling for TV navigation

### Phase 3: Watchlist Access
- [ ] Add "My Watchlist" row to HomeScreen
- [ ] Add watchlist option to ProfileScreen
- [ ] Create dedicated WatchlistScreen
- [ ] Implement proper navigation routing

### Phase 4: Testing & Polish
- [ ] Test persistence across app restarts
- [ ] Verify no regressions in existing functionality
- [ ] Test TV navigation and focus management
- [ ] Add integration tests

---

**Ready for Implementation:** This story is scoped for a single development session and follows all existing WilTV patterns and architecture.