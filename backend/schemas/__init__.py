from backend.schemas.common import StandardResponse, HealthResponse, ReadinessResponse
from backend.schemas.auth import LoginRequest, RegisterRequest, AuthResponse, PasswordResetRequest, PasswordResetConfirmRequest
from backend.schemas.user import UserRead, UserUpdate, UserProfileResponse, UserSettings
from backend.schemas.chat import ChatRequest, ChatResponse, ChatMessageDto
from backend.schemas.image import ImageGenRequest, ImageGenResponse
from backend.schemas.music import MusicGenRequest, MusicTrackResponse
from backend.schemas.games import ChessMoveRequest, TicTacToeMoveRequest, MazeRequest, GameResponse
from backend.schemas.history import HistoryItemCreate, HistoryItemRead, HistoryListResponse

__all__ = [
    "StandardResponse", "HealthResponse", "ReadinessResponse",
    "LoginRequest", "RegisterRequest", "AuthResponse", "PasswordResetRequest", "PasswordResetConfirmRequest",
    "UserRead", "UserUpdate", "UserProfileResponse", "UserSettings",
    "ChatRequest", "ChatResponse", "ChatMessageDto",
    "ImageGenRequest", "ImageGenResponse",
    "MusicGenRequest", "MusicTrackResponse",
    "ChessMoveRequest", "TicTacToeMoveRequest", "MazeRequest", "GameResponse",
    "HistoryItemCreate", "HistoryItemRead", "HistoryListResponse"
]
