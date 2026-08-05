from typing import List, Dict, Any
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.orm import Session
from backend.database import get_db
from backend.core.dependencies import fun_get_current_user_token
from backend.schemas.auth import TokenData
from backend.schemas.games import (
    StartGameSessionRequest, ChessMoveRequest, TicTacToeMoveRequest, MazeRequest,
    EndGameSessionRequest, LLMGameAnalysisRequest, GameResponse, GameSessionResponse,
    GameStatisticResponse, GamePreferenceRequest, GamePreferenceResponse
)
from backend.schemas.common import StandardResponse
from backend.services.games_service import GamesService

router = APIRouter()
games_service = GamesService()

@router.post("/session/start", response_model=StandardResponse[Dict[str, Any]])
async def start_game_session(
    req: StartGameSessionRequest,
    db: Session = Depends(get_db),
    user_token: TokenData = Depends(fun_get_current_user_token)
):
    user_id = user_token.user_id or "user_101"
    session = await games_service.create_session(db, user_id, req)
    return StandardResponse(success=True, data={"session_id": session.id, "game_type": session.game_type, "status": session.status})

@router.post("/chess/move", response_model=StandardResponse[GameResponse])
async def fun_chess_move(
    req: ChessMoveRequest,
    db: Session = Depends(get_db),
    user_token: TokenData = Depends(fun_get_current_user_token)
):
    user_id = user_token.user_id or "user_101"
    res = await games_service.fun_compute_chess_move(req, db, user_id)
    return StandardResponse(success=True, data=res)

@router.post("/tictactoe/move", response_model=StandardResponse[GameResponse])
async def fun_tictactoe_move(
    req: TicTacToeMoveRequest,
    db: Session = Depends(get_db),
    user_token: TokenData = Depends(fun_get_current_user_token)
):
    user_id = user_token.user_id or "user_101"
    res = await games_service.fun_compute_tictactoe_move(req, db, user_id)
    return StandardResponse(success=True, data=res)

@router.post("/maze/generate", response_model=StandardResponse[GameResponse])
async def fun_generate_maze(
    req: MazeRequest,
    db: Session = Depends(get_db),
    user_token: TokenData = Depends(fun_get_current_user_token)
):
    user_id = user_token.user_id or "user_101"
    res = await games_service.fun_generate_maze(req, db, user_id)
    return StandardResponse(success=True, data=res)

@router.post("/session/end", response_model=StandardResponse[Dict[str, Any]])
async def end_game_session(
    req: EndGameSessionRequest,
    db: Session = Depends(get_db),
    user_token: TokenData = Depends(fun_get_current_user_token)
):
    user_id = user_token.user_id or "user_101"
    res = await games_service.end_session(db, user_id, req)
    return StandardResponse(success=True, data=res)

@router.get("/statistics", response_model=StandardResponse[List[GameStatisticResponse]])
async def get_user_statistics(
    db: Session = Depends(get_db),
    user_token: TokenData = Depends(fun_get_current_user_token)
):
    user_id = user_token.user_id or "user_101"
    stats = await games_service.get_user_statistics(db, user_id)
    return StandardResponse(success=True, data=stats)

@router.get("/preferences", response_model=StandardResponse[GamePreferenceResponse])
async def get_user_preferences(
    db: Session = Depends(get_db),
    user_token: TokenData = Depends(fun_get_current_user_token)
):
    user_id = user_token.user_id or "user_101"
    pref = await games_service.get_user_preferences(db, user_id)
    return StandardResponse(success=True, data=pref)

@router.put("/preferences", response_model=StandardResponse[GamePreferenceResponse])
async def update_user_preferences(
    req: GamePreferenceRequest,
    db: Session = Depends(get_db),
    user_token: TokenData = Depends(fun_get_current_user_token)
):
    user_id = user_token.user_id or "user_101"
    pref = await games_service.update_user_preferences(db, user_id, req)
    return StandardResponse(success=True, data=pref)

@router.post("/llm-analysis", response_model=StandardResponse[Dict[str, str]])
async def analyze_game_with_llm(
    req: LLMGameAnalysisRequest,
    user_token: TokenData = Depends(fun_get_current_user_token)
):
    analysis = await games_service.analyze_game_with_llm(req)
    return StandardResponse(success=True, data={"analysis": analysis})
