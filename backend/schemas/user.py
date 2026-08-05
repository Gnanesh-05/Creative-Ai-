from typing import Optional
from datetime import datetime
from pydantic import BaseModel, EmailStr, Field

class UserRead(BaseModel):
    id: str
    username: str
    email: EmailStr
    full_name: Optional[str] = None
    is_active: bool
    created_at: datetime

    class Config:
        from_attributes = True

class UserUpdate(BaseModel):
    full_name: Optional[str] = None
    email: Optional[EmailStr] = None

class UserProfileResponse(BaseModel):
    username: str
    email: str
    full_name: Optional[str] = "Creative Master"
    avatar_url: Optional[str] = "https://picsum.photos/seed/useravatar/200"
    bio: Optional[str] = "AI Enthusiast & Game Creator"
    tier: str = "Pro Creator Tier"
    dailyGenerationsUsed: int = 18
    dailyGenerationsMax: int = 100
    accountCreated: str = "2026-01-15"

class UserProfileUpdate(BaseModel):
    full_name: Optional[str] = None
    username: Optional[str] = None
    avatar_url: Optional[str] = None
    bio: Optional[str] = None

class AiPreferences(BaseModel):
    chat_response_style: str = "Detailed & Creative"
    image_generation_model: str = "imagen-3.0-generate-002"
    image_aspect_ratio: str = "1:1"
    music_generation_genre: str = "Ambient Synthwave"
    content_filter_level: str = "Standard"

class GamePreferencesSchema(BaseModel):
    chess_difficulty: str = "Grandmaster Mind"
    tictactoe_difficulty: str = "Unbeatable"
    maze_size: int = 15
    ai_coaching_enabled: bool = True
    sound_effects_enabled: bool = True
    no_spoiler_mode: bool = False

class UserSettings(BaseModel):
    theme: str = "system"  # light, dark, system
    darkMode: bool = True
    notificationsEnabled: bool = True
    language: str = "English"
    autoSaveHistory: bool = True
    highQualityRendering: bool = True
    modelTemperature: float = 0.7
    ai_preferences: AiPreferences = Field(default_factory=AiPreferences)
    game_preferences: GamePreferencesSchema = Field(default_factory=GamePreferencesSchema)

class UserSettingsUpdate(BaseModel):
    theme: Optional[str] = None
    darkMode: Optional[bool] = None
    notificationsEnabled: Optional[bool] = None
    language: Optional[str] = None
    autoSaveHistory: Optional[bool] = None
    highQualityRendering: Optional[bool] = None
    modelTemperature: Optional[float] = None
    ai_preferences: Optional[AiPreferences] = None
    game_preferences: Optional[GamePreferencesSchema] = None

class ChangePasswordRequest(BaseModel):
    current_password: str
    new_password: str

class DeleteAccountRequest(BaseModel):
    password_confirmation: str

