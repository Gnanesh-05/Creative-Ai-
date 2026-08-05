# Creative AI - Android Frontend Architecture

This folder represents the modular Android/Kotlin application layer for Creative AI.

## Modular Component Structure

1. `ui/splash/` - Splash screen with animated logo reveal and state initialization.
2. `ui/onboarding/` - Interactive multi-slide onboarding flow with feature highlights.
3. `ui/auth/` - Secure Login, Register, Forgot Password, and Reset Password screens with form validation.
4. `ui/home/` - Central dashboard showcasing all 10 core AI features, quick launcher widgets, and statistics.
5. `ui/chat/` - Conversational Chat AI with multi-modal attachments, stream rendering, and prompt presets.
6. `ui/image/` - Realistic AI Image Generator with aspect ratio selectors, style controls, and instant downloads.
7. `ui/music/` - AI Music Composer with mood selectors, tempo controls, waveform visualizations, and playback controls.
8. `ui/games/` - Game Mind AI hub featuring:
   - `chess/` - Interactive Chess engine vs Game Mind AI (Stockfish-inspired evaluation engine).
   - `tictactoe/` - Minimax AI Tic-Tac-Toe with 3 difficulty levels.
   - `maze/` - Procedural AI Maze generator with A* pathfinding visualization & interactive solver.
9. `ui/history/` - Unified history hub with tab filtering, search, detail modals, and export features.
10. `ui/profile/` - User profile, usage stats, tier status, and session controls.
11. `ui/settings/` - Theme preferences, security settings, API connection status, clear cache, and about info.

## Data Layer
- `data/local/` - Room Database (`CreativeAiDatabase`), DAOs, Entities.
- `data/remote/` - Retrofit API service (`CreativeAiApiService`), OkHttp client, JWT auth interceptor.
- `data/repository/` - Centralized Repository interfaces & implementations binding local cache with remote API endpoints.
