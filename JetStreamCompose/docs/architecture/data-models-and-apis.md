# Data Models and APIs

## Data Models

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

## API Specifications

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
