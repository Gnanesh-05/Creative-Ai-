import uuid
from datetime import datetime
from sqlalchemy import (
    Column, String, DateTime, ForeignKey, Integer, Text, Boolean, JSON, Float, BigInteger, UniqueConstraint
)
from sqlalchemy.orm import relationship
from backend.database import Base

def generate_uuid():
    return str(uuid.uuid4())

class User(Base):
    __tablename__ = "users"
    
    id = Column(String, primary_key=True, default=generate_uuid)
    email = Column(String, unique=True, index=True, nullable=False)
    username = Column(String, unique=True, index=True, nullable=False)
    hashed_password = Column(String, nullable=False)
    full_name = Column(String, nullable=True)
    is_active = Column(Boolean, default=True)
    is_deleted = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    # Relationships
    settings = relationship("UserSettings", back_populates="user", uselist=False, cascade="all, delete-orphan")
    password_reset_tokens = relationship("PasswordResetToken", back_populates="user", cascade="all, delete-orphan")
    sessions = relationship("UserSession", back_populates="user", cascade="all, delete-orphan")
    chat_conversations = relationship("ChatConversation", back_populates="user", cascade="all, delete-orphan")
    image_generations = relationship("ImageGeneration", back_populates="user", cascade="all, delete-orphan")
    music_generations = relationship("MusicGeneration", back_populates="user", cascade="all, delete-orphan")
    game_sessions = relationship("GameSession", back_populates="user", cascade="all, delete-orphan")
    game_results = relationship("GameResult", back_populates="user", cascade="all, delete-orphan")
    game_statistics = relationship("GameStatistic", back_populates="user", cascade="all, delete-orphan")
    game_preferences = relationship("GamePreference", back_populates="user", uselist=False, cascade="all, delete-orphan")
    history_items = relationship("UnifiedHistoryItem", back_populates="user", cascade="all, delete-orphan")
    usage_tracking = relationship("UsageTracking", back_populates="user", cascade="all, delete-orphan")


class PasswordResetToken(Base):
    __tablename__ = "password_reset_tokens"
    
    id = Column(String, primary_key=True, default=generate_uuid)
    user_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    token_hash = Column(String, nullable=False, index=True)
    expires_at = Column(DateTime, nullable=False)
    is_used = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)
    
    user = relationship("User", back_populates="password_reset_tokens")


class UserSession(Base):
    __tablename__ = "user_sessions"
    
    id = Column(String, primary_key=True, default=generate_uuid)
    user_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    refresh_token_hash = Column(String, nullable=False, index=True)
    user_agent = Column(String, nullable=True)
    ip_address = Column(String, nullable=True)
    expires_at = Column(DateTime, nullable=False)
    is_revoked = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    user = relationship("User", back_populates="sessions")


class UserSettings(Base):
    __tablename__ = "user_settings"
    
    id = Column(String, primary_key=True, default=generate_uuid)
    user_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), unique=True, nullable=False)
    dark_mode = Column(Boolean, default=True)
    notifications_enabled = Column(Boolean, default=True)
    auto_save_history = Column(Boolean, default=True)
    high_quality_rendering = Column(Boolean, default=True)
    model_temperature = Column(Float, default=0.7)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    user = relationship("User", back_populates="settings")


class ChatConversation(Base):
    __tablename__ = "chat_conversations"
    
    id = Column(String, primary_key=True, default=generate_uuid)
    user_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    title = Column(String, nullable=False, default="New Conversation")
    system_instruction = Column(Text, nullable=True)
    model_name = Column(String, default="gemini-2.0-flash")
    is_deleted = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    user = relationship("User", back_populates="chat_conversations")
    messages = relationship("ChatMessage", back_populates="conversation", cascade="all, delete-orphan")


class ChatMessage(Base):
    __tablename__ = "chat_messages"
    
    id = Column(String, primary_key=True, default=generate_uuid)
    conversation_id = Column(String, ForeignKey("chat_conversations.id", ondelete="CASCADE"), nullable=False)
    sender = Column(String, nullable=False) # user, model, system
    content = Column(Text, nullable=False)
    tokens_used = Column(Integer, default=0)
    created_at = Column(DateTime, default=datetime.utcnow)
    
    conversation = relationship("ChatConversation", back_populates="messages")


class ImageGeneration(Base):
    __tablename__ = "image_generations"
    
    id = Column(String, primary_key=True, default=generate_uuid)
    user_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    prompt = Column(Text, nullable=False)
    enhanced_prompt = Column(Text, nullable=True)
    negative_prompt = Column(Text, nullable=True)
    model = Column(String, default="imagen-3.0-generate-002")
    style_preset = Column(String, default="Photorealistic")
    aspect_ratio = Column(String, default="1:1")
    resolution = Column(String, default="1024x1024")
    generation_status = Column(String, default="COMPLETED")
    image_url = Column(Text, nullable=False)
    storage_reference = Column(String, nullable=True) # Storage key / path
    seed = Column(BigInteger, nullable=True)
    is_deleted = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)
    
    user = relationship("User", back_populates="image_generations")


class ImageGenerationJob(Base):
    __tablename__ = "image_generation_jobs"

    id = Column(String, primary_key=True, default=generate_uuid)
    user_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    prompt = Column(Text, nullable=False)
    enhanced_prompt = Column(Text, nullable=True)
    negative_prompt = Column(Text, nullable=True)
    model = Column(String, default="imagen-3.0-generate-002")
    style_preset = Column(String, default="Photorealistic")
    aspect_ratio = Column(String, default="1:1")
    resolution = Column(String, default="1024x1024")
    num_images = Column(Integer, default=1)
    status = Column(String, default="PENDING") # PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
    progress = Column(Integer, default=0)
    error_message = Column(Text, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


class MusicGeneration(Base):
    __tablename__ = "music_generations"
    
    id = Column(String, primary_key=True, default=generate_uuid)
    user_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    prompt = Column(Text, nullable=False)
    enhanced_prompt = Column(Text, nullable=True)
    mood = Column(String, nullable=False, default="Relaxing")
    genre = Column(String, default="Lo-Fi Beats")
    tempo_bpm = Column(Integer, default=90)
    duration_seconds = Column(Integer, default=30)
    key_signature = Column(String, default="C Major")
    instruments = Column(String, default="Piano, Strings")
    energy_level = Column(String, default="Medium")
    is_instrumental = Column(Boolean, default=True)
    lyrics = Column(Text, nullable=True)
    model = Column(String, default="musicgen-stereo-large")
    generation_status = Column(String, default="COMPLETED")
    audio_url = Column(Text, nullable=False)
    audio_storage_reference = Column(String, nullable=True) # References object storage key
    synthetic_notes = Column(Text, nullable=True)
    metadata_json = Column(JSON, nullable=True)
    is_saved = Column(Boolean, default=False)
    is_deleted = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow)
    
    user = relationship("User", back_populates="music_generations")


class MusicGenerationJob(Base):
    __tablename__ = "music_generation_jobs"

    id = Column(String, primary_key=True, default=generate_uuid)
    user_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    prompt = Column(Text, nullable=False)
    enhanced_prompt = Column(Text, nullable=True)
    mood = Column(String, default="Relaxing")
    genre = Column(String, default="Lo-Fi Beats")
    tempo_bpm = Column(Integer, default=90)
    duration_seconds = Column(Integer, default=30)
    key_signature = Column(String, default="C Major")
    instruments = Column(String, default="Piano, Strings")
    energy_level = Column(String, default="Medium")
    is_instrumental = Column(Boolean, default=True)
    lyrics = Column(Text, nullable=True)
    model = Column(String, default="musicgen-stereo-large")
    status = Column(String, default="PENDING") # PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
    progress = Column(Integer, default=0)
    error_message = Column(Text, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)


class GameSession(Base):
    __tablename__ = "game_sessions"
    
    id = Column(String, primary_key=True, default=generate_uuid)
    user_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    game_type = Column(String, nullable=False) # CHESS, TICTACTOE, MAZE
    difficulty = Column(String, default="MEDIUM")
    current_state_json = Column(JSON, nullable=False)
    status = Column(String, default="IN_PROGRESS") # IN_PROGRESS, WON, LOST, DRAW
    created_at = Column(DateTime, default=datetime.utcnow)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    user = relationship("User", back_populates="game_sessions")
    moves = relationship("GameMove", back_populates="session", cascade="all, delete-orphan")
    result = relationship("GameResult", back_populates="session", uselist=False, cascade="all, delete-orphan")


class GameMove(Base):
    __tablename__ = "game_moves"
    
    id = Column(String, primary_key=True, default=generate_uuid)
    session_id = Column(String, ForeignKey("game_sessions.id", ondelete="CASCADE"), nullable=False)
    move_number = Column(Integer, nullable=False)
    player = Column(String, nullable=False) # human, ai
    move_data_json = Column(JSON, nullable=False)
    fen_after = Column(String, nullable=True)
    created_at = Column(DateTime, default=datetime.utcnow)
    
    session = relationship("GameSession", back_populates="moves")


class GameResult(Base):
    __tablename__ = "game_results"
    
    id = Column(String, primary_key=True, default=generate_uuid)
    session_id = Column(String, ForeignKey("game_sessions.id", ondelete="CASCADE"), unique=True, nullable=False)
    user_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    game_type = Column(String, nullable=False)
    winner = Column(String, nullable=False) # human, ai, draw
    score = Column(Integer, default=0)
    total_moves = Column(Integer, default=0)
    duration_seconds = Column(Integer, default=0)
    ended_at = Column(DateTime, default=datetime.utcnow)
    
    session = relationship("GameSession", back_populates="result")
    user = relationship("User", back_populates="game_results")


class GameStatistic(Base):
    __tablename__ = "game_statistics"
    
    id = Column(String, primary_key=True, default=generate_uuid)
    user_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    game_type = Column(String, nullable=False) # CHESS, TICTACTOE, MAZE
    games_played = Column(Integer, default=0)
    wins = Column(Integer, default=0)
    losses = Column(Integer, default=0)
    draws = Column(Integer, default=0)
    win_rate = Column(Float, default=0.0)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    user = relationship("User", back_populates="game_statistics")
    __table_args__ = (UniqueConstraint('user_id', 'game_type', name='_user_game_type_uc'),)


class GamePreference(Base):
    __tablename__ = "game_preferences"
    
    id = Column(String, primary_key=True, default=generate_uuid)
    user_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), unique=True, nullable=False)
    default_chess_difficulty = Column(String, default="Grandmaster Mind")
    default_tictactoe_difficulty = Column(String, default="Unbeatable")
    maze_size = Column(Integer, default=15)
    sound_effects = Column(Boolean, default=True)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    user = relationship("User", back_populates="game_preferences")


class UnifiedHistoryItem(Base):
    __tablename__ = "unified_history"
    
    id = Column(String, primary_key=True, default=generate_uuid)
    user_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    module_type = Column(String, index=True, nullable=False) # CHAT, IMAGE, MUSIC, GAME_CHESS, GAME_TICTACTOE, GAME_MAZE
    title = Column(String, nullable=False)
    summary = Column(Text, nullable=True)
    payload = Column(JSON, nullable=True)
    is_deleted = Column(Boolean, default=False)
    created_at = Column(DateTime, default=datetime.utcnow, index=True)
    
    user = relationship("User", back_populates="history_items")


class UsageTracking(Base):
    __tablename__ = "usage_tracking"
    
    id = Column(String, primary_key=True, default=generate_uuid)
    user_id = Column(String, ForeignKey("users.id", ondelete="CASCADE"), nullable=False)
    usage_date = Column(String, nullable=False) # YYYY-MM-DD
    chat_tokens = Column(Integer, default=0)
    images_generated = Column(Integer, default=0)
    music_generated = Column(Integer, default=0)
    games_played = Column(Integer, default=0)
    updated_at = Column(DateTime, default=datetime.utcnow, onupdate=datetime.utcnow)
    
    user = relationship("User", back_populates="usage_tracking")
    __table_args__ = (UniqueConstraint('user_id', 'usage_date', name='_user_usage_date_uc'),)
