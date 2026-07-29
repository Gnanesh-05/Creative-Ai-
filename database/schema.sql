-- ================================================================
-- CREATIVE AI PRODUCTION POSTGRESQL DATABASE SCHEMA (DDL)
-- ================================================================

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Auto-update updated_at timestamp trigger function
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = CURRENT_TIMESTAMP;
   RETURN NEW;
END;
$$ language 'plpgsql';

-- 1. Users Table
CREATE TABLE IF NOT EXISTS users (
    id VARCHAR(36) PRIMARY KEY,
    email VARCHAR(255) UNIQUE NOT NULL,
    username VARCHAR(100) UNIQUE NOT NULL,
    hashed_password VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TRIGGER update_users_modtime BEFORE UPDATE ON users FOR EACH ROW EXECUTE PROCEDURE update_updated_at_column();

-- 2. Password Reset Tokens Table
CREATE TABLE IF NOT EXISTS password_reset_tokens (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_used BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 3. User Sessions Table (OAuth / Refresh Token management)
CREATE TABLE IF NOT EXISTS user_sessions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token_hash VARCHAR(255) NOT NULL,
    user_agent VARCHAR(512),
    ip_address VARCHAR(45),
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    is_revoked BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 4. User Settings Table
CREATE TABLE IF NOT EXISTS user_settings (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    dark_mode BOOLEAN DEFAULT TRUE,
    notifications_enabled BOOLEAN DEFAULT TRUE,
    auto_save_history BOOLEAN DEFAULT TRUE,
    high_quality_rendering BOOLEAN DEFAULT TRUE,
    model_temperature DOUBLE PRECISION DEFAULT 0.7,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 5. Chat Conversations Table
CREATE TABLE IF NOT EXISTS chat_conversations (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL DEFAULT 'New Conversation',
    system_instruction TEXT,
    model_name VARCHAR(100) DEFAULT 'gemini-2.0-flash',
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 6. Chat Messages Table
CREATE TABLE IF NOT EXISTS chat_messages (
    id VARCHAR(36) PRIMARY KEY,
    conversation_id VARCHAR(36) NOT NULL REFERENCES chat_conversations(id) ON DELETE CASCADE,
    sender VARCHAR(20) NOT NULL CHECK (sender IN ('user', 'model', 'system')),
    content TEXT NOT NULL,
    tokens_used INT DEFAULT 0,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 7. Generated Images Table (Stores Object Storage Key & URLs - NO BLOBS)
CREATE TABLE IF NOT EXISTS image_generations (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    prompt TEXT NOT NULL,
    style_preset VARCHAR(50) DEFAULT 'Cyberpunk',
    aspect_ratio VARCHAR(20) DEFAULT '1:1',
    image_url TEXT NOT NULL,
    storage_key VARCHAR(512),
    seed BIGINT,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 8. Generated Music Table (Stores Audio URLs & Synthetic Notes Metadata)
CREATE TABLE IF NOT EXISTS music_generations (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    prompt TEXT NOT NULL,
    mood VARCHAR(50) NOT NULL,
    genre VARCHAR(50) DEFAULT 'Lo-Fi Beats',
    tempo_bpm INT DEFAULT 90,
    duration_seconds INT DEFAULT 30,
    audio_url TEXT NOT NULL,
    storage_key VARCHAR(512),
    synthetic_notes TEXT,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 9. Game Sessions Table (Chess, Tic-Tac-Toe, Maze)
CREATE TABLE IF NOT EXISTS game_sessions (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    game_type VARCHAR(50) NOT NULL CHECK (game_type IN ('CHESS', 'TICTACTOE', 'MAZE')),
    difficulty VARCHAR(50) DEFAULT 'MEDIUM',
    current_state_json JSONB NOT NULL,
    status VARCHAR(20) DEFAULT 'IN_PROGRESS' CHECK (status IN ('IN_PROGRESS', 'WON', 'LOST', 'DRAW')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 10. Game Moves Table
CREATE TABLE IF NOT EXISTS game_moves (
    id VARCHAR(36) PRIMARY KEY,
    session_id VARCHAR(36) NOT NULL REFERENCES game_sessions(id) ON DELETE CASCADE,
    move_number INT NOT NULL,
    player VARCHAR(20) NOT NULL CHECK (player IN ('human', 'ai')),
    move_data_json JSONB NOT NULL,
    fen_after VARCHAR(255),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 11. Game Results Table
CREATE TABLE IF NOT EXISTS game_results (
    id VARCHAR(36) PRIMARY KEY,
    session_id VARCHAR(36) UNIQUE NOT NULL REFERENCES game_sessions(id) ON DELETE CASCADE,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    game_type VARCHAR(50) NOT NULL,
    winner VARCHAR(20) NOT NULL CHECK (winner IN ('human', 'ai', 'draw')),
    score INT DEFAULT 0,
    total_moves INT DEFAULT 0,
    duration_seconds INT DEFAULT 0,
    ended_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 12. Game Statistics Table
CREATE TABLE IF NOT EXISTS game_statistics (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    game_type VARCHAR(50) NOT NULL CHECK (game_type IN ('CHESS', 'TICTACTOE', 'MAZE')),
    games_played INT DEFAULT 0,
    wins INT DEFAULT 0,
    losses INT DEFAULT 0,
    draws INT DEFAULT 0,
    win_rate DOUBLE PRECISION DEFAULT 0.0,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_game_type UNIQUE(user_id, game_type)
);

-- 13. Game Preferences Table
CREATE TABLE IF NOT EXISTS game_preferences (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) UNIQUE NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    default_chess_difficulty VARCHAR(50) DEFAULT 'Grandmaster Mind',
    default_tictactoe_difficulty VARCHAR(50) DEFAULT 'Unbeatable',
    maze_size INT DEFAULT 15,
    sound_effects BOOLEAN DEFAULT TRUE,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 14. Unified History Table
CREATE TABLE IF NOT EXISTS unified_history (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    module_type VARCHAR(50) NOT NULL CHECK (module_type IN ('CHAT', 'IMAGE', 'MUSIC', 'GAME_CHESS', 'GAME_TICTACTOE', 'GAME_MAZE')),
    title VARCHAR(255) NOT NULL,
    summary TEXT,
    payload JSONB,
    is_deleted BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

-- 15. Daily Usage Tracking Table
CREATE TABLE IF NOT EXISTS usage_tracking (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    usage_date VARCHAR(10) NOT NULL, -- YYYY-MM-DD
    chat_tokens INT DEFAULT 0,
    images_generated INT DEFAULT 0,
    music_generated INT DEFAULT 0,
    games_played INT DEFAULT 0,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT unique_user_usage_date UNIQUE(user_id, usage_date)
);

-- Indexes for Query Performance & Lookups
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);
CREATE INDEX IF NOT EXISTS idx_pw_reset_token ON password_reset_tokens(token_hash);
CREATE INDEX IF NOT EXISTS idx_sessions_refresh ON user_sessions(refresh_token_hash);
CREATE INDEX IF NOT EXISTS idx_chat_conv_user ON chat_conversations(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_chat_msg_conv ON chat_messages(conversation_id, created_at ASC);
CREATE INDEX IF NOT EXISTS idx_images_user ON image_generations(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_music_user ON music_generations(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_game_sess_user ON game_sessions(user_id, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_game_moves_sess ON game_moves(session_id, move_number ASC);
CREATE INDEX IF NOT EXISTS idx_history_user ON unified_history(user_id, module_type, created_at DESC);
CREATE INDEX IF NOT EXISTS idx_usage_user_date ON usage_tracking(user_id, usage_date);
