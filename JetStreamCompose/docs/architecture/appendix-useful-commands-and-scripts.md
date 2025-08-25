# Appendix - Useful Commands and Scripts

## Frequently Used Commands

```bash
./gradlew installDebug        # Install debug build on connected device
./gradlew build               # Full project build  
./gradlew spotlessApply       # Auto-format code with ktlint
./gradlew clean build         # Clean build from scratch
```

## Debugging and Troubleshooting

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