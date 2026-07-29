-- ================================================================
-- CREATIVE AI POSTGRESQL SEED DATA
-- ================================================================

-- Seed User
INSERT INTO users (id, email, username, hashed_password, full_name, is_active)
VALUES (
    '11111111-1111-1111-1111-111111111111',
    'creator@example.com',
    'creator',
    '$2b$12$EixZaYVK1fsbw1ZfbX3OXePaWxn96p36WQoeg6Lruj3vjPGga31lW', -- password: password123
    'Pro Creator',
    TRUE
) ON CONFLICT (email) DO NOTHING;

-- Seed User Settings
INSERT INTO user_settings (id, user_id, dark_mode, notifications_enabled, auto_save_history, high_quality_rendering, model_temperature)
VALUES (
    '22222222-2222-2222-2222-222222222222',
    '11111111-1111-1111-1111-111111111111',
    TRUE,
    TRUE,
    TRUE,
    TRUE,
    0.7
) ON CONFLICT (user_id) DO NOTHING;

-- Seed Game Preferences
INSERT INTO game_preferences (id, user_id, default_chess_difficulty, default_tictactoe_difficulty, maze_size, sound_effects)
VALUES (
    '33333333-3333-3333-3333-333333333333',
    '11111111-1111-1111-1111-111111111111',
    'Grandmaster Mind',
    'Unbeatable',
    15,
    TRUE
) ON CONFLICT (user_id) DO NOTHING;

-- Seed Chat Conversation & Messages
INSERT INTO chat_conversations (id, user_id, title, system_instruction, model_name)
VALUES (
    '44444444-4444-4444-4444-444444444444',
    '11111111-1111-1111-1111-111111111111',
    'Kotlin Jetpack Compose Strategy',
    'You are an expert Android Kotlin software engineer.',
    'gemini-2.0-flash'
) ON CONFLICT DO NOTHING;

INSERT INTO chat_messages (id, conversation_id, sender, content, tokens_used)
VALUES 
(
    '55555555-5555-5555-5555-555555555551',
    '44444444-4444-4444-4444-444444444444',
    'user',
    'How do I handle state management in Jetpack Compose using StateFlow?',
    24
),
(
    '55555555-5555-5555-5555-555555555552',
    '44444444-4444-4444-4444-444444444444',
    'model',
    'Use StateFlow inside your ViewModel and collect state in Composables using collectAsStateWithLifecycle().',
    48
) ON CONFLICT DO NOTHING;

-- Seed Image Generations
INSERT INTO image_generations (id, user_id, prompt, style_preset, aspect_ratio, image_url, storage_key, seed)
VALUES (
    '66666666-6666-6666-6666-666666666666',
    '11111111-1111-1111-1111-111111111111',
    'Futuristic neon android studio workspace with glowing purple aura',
    'Cyberpunk',
    '16:9',
    'https://picsum.photos/seed/cyberpunk_art/800/800',
    'images/2026/07/cyberpunk_art.jpg',
    428912
) ON CONFLICT DO NOTHING;

-- Seed Music Generations
INSERT INTO music_generations (id, user_id, prompt, mood, genre, tempo_bpm, duration_seconds, audio_url, storage_key, synthetic_notes)
VALUES (
    '77777777-7777-7777-7777-777777777777',
    '11111111-1111-1111-1111-111111111111',
    'Chilled lo-fi piano beat for deep focused code creation',
    'Chill',
    'Lo-Fi Beats',
    85,
    30,
    'https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3',
    'audio/2026/07/lofi_piano.mp3',
    'C4 E4 G4 B4 | A4 F4 C4 G3'
) ON CONFLICT DO NOTHING;

-- Seed Unified History
INSERT INTO unified_history (id, user_id, module_type, title, summary, payload)
VALUES 
(
    '88888888-8888-8888-8888-888888888881',
    '11111111-1111-1111-1111-111111111111',
    'CHAT',
    'Kotlin Jetpack Compose Strategy',
    'Discussion on StateFlow pattern for Compose architecture',
    '{"model": "gemini-2.0-flash"}'
),
(
    '88888888-8888-8888-8888-888888888882',
    '11111111-1111-1111-1111-111111111111',
    'IMAGE',
    'Futuristic neon android studio',
    'Generated 16:9 Cyberpunk visual asset',
    '{"imageUrl": "https://picsum.photos/seed/cyberpunk_art/800/800"}'
) ON CONFLICT DO NOTHING;

-- Seed Usage Tracking
INSERT INTO usage_tracking (id, user_id, usage_date, chat_tokens, images_generated, music_generated, games_played)
VALUES (
    '99999999-9999-9999-9999-999999999999',
    '11111111-1111-1111-1111-111111111111',
    '2026-07-28',
    72,
    1,
    1,
    0
) ON CONFLICT (user_id, usage_date) DO NOTHING;
