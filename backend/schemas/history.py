from typing import Optional, Any, List
from datetime import datetime
from pydantic import BaseModel, Field

class HistoryItemCreate(BaseModel):
    module_type: str # CHAT, IMAGE, MUSIC, GAME_MIND, GAME_CHESS, GAME_TICTACTOE, GAME_MAZE
    title: str
    summary: Optional[str] = None
    payload: Optional[Any] = None

class HistoryItemRead(BaseModel):
    id: str
    user_id: str
    module_type: str
    title: str
    summary: Optional[str] = None
    payload: Optional[Any] = None
    created_at: datetime

    class Config:
        from_attributes = True

class HistoryListResponse(BaseModel):
    items: List[HistoryItemRead]
    total: int
    page: int = 1
    page_size: int = 20
    has_more: bool = False
