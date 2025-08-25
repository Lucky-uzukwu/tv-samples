# Integration Points and External Dependencies

## External Services

| Service   | Purpose        | Integration Type | Key Files                            |
| --------- | -------------- | ---------------- | ------------------------------------ |
| API Server| Content + Auth | REST API         | `data/network/*Service.kt` (12 files)|
| Pusher    | Real-time      | WebSocket        | Real-time communication              |

**External API Details:**
- **Base URL**: `https://api.nortv.xyz` (release builds only)
- **Authentication**: Token-based via AuthInterceptor
- **Content Types**: Movies, TV Shows, TV Channels, User profiles

## Internal Integration Points

- **Authentication Flow**: AuthScreen → ProfileSelection → Dashboard (see App.kt navigation)
- **State Management**: UserStateHolder manages global user state via StateFlow
- **Database Integration**: Room with Paging 3 RemoteMediator for offline-first experience  
- **Media Playback**: ExoPlayer integration in VideoPlayerScreen with HLS support
- **Image Authentication**: Custom AuthenticatedAsyncImage for secure image loading
