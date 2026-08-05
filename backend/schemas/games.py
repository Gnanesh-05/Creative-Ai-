from typing import List, Optional, Any, Dict
from pydantic import BaseModel, Field
from datetime import datetime

class StartGameSessionRequest(BaseModel):
    game_type: str = Field(..., description="CHESS, TICTACTOE, or MAZE")
    difficulty: str = "MEDIUM" # EASY, MEDIUM, HARD / UNBEATABLE
    initial_config: Optional[Dict[str, Any]] = None

class ChessMoveRequest(BaseModel):
    session_id: Optional[str] = None
    moveFrom: str = "e2"
    moveTo: str = "e4"
    fen: str = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
    difficulty: str = "MEDIUM"

class TicTacToeMoveRequest(BaseModel):
    session_id: Optional[str] = None
    board: List[str] # List of 9 strings ("X", "O", "")
    difficulty: str = "Unbeatable"
    ai_symbol: str = "O"

class MazeRequest(BaseModel):
    session_id: Optional[str] = None
    rows: int = 15
    cols: int = 15
    difficulty: str = "MEDIUM"

class EndGameSessionRequest(BaseModel):
    session_id: str
    winner: str # human, ai, draw
    score: int = 0
    total_moves: int = 0
    duration_seconds: int = 0

class LLMGameAnalysisRequest(BaseModel):
    game_type: str # CHESS, TICTACTOE, MAZE
    game_state_description: str
    user_query: Optional[str] = "Provide strategic advice and move analysis."

class GameSessionResponse(BaseModel):
    id: str
    user_id: str
    game_type: str
    difficulty: str
    current_state_json: Dict[str, Any]
    status: str
    created_at: datetime
    updated_at: datetime

class GameResponse(BaseModel):
    status: str
    boardState: Optional[Any] = None
    aiMove: Optional[Any] = None
    fen: Optional[str] = None
    evaluation: Optional[float] = None
    winner: Optional[str] = None
    session_id: Optional[str] = None

class GameStatisticResponse(BaseModel):
    game_type: str
    games_played: int
    wins: int
    losses: int
    draws: int
    win_rate: float

class GamePreferenceRequest(BaseModel):
    default_chess_difficulty: str = "Medium"
    default_tictactoe_difficulty: str = "Unbeatable"
    maze_size: int = 15
    sound_effects: bool = True

class GamePreferenceResponse(BaseModel):
    default_chess_difficulty: str
    default_tictactoe_difficulty: str
    maze_size: int
    sound_effects: bool
