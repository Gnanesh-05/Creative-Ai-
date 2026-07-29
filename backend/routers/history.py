from typing import Optional
from fastapi import APIRouter, Depends, Query, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from backend.database import get_db
from backend.core.dependencies import fun_get_current_user_token
from backend.schemas.auth import TokenData
from backend.schemas.history import HistoryItemCreate, HistoryItemRead, HistoryListResponse
from backend.schemas.common import StandardResponse
from backend.services.history_service import HistoryService

router = APIRouter()

@router.get("", response_model=StandardResponse[HistoryListResponse])
async def fun_get_history(
    category: Optional[str] = Query(None, description="Filter category: ALL, CHAT, IMAGE, MUSIC, GAME_MIND, GAME_CHESS, GAME_TICTACTOE, GAME_MAZE"),
    query: Optional[str] = Query(None, description="Search keyword in title or summary"),
    sort: str = Query("newest", description="Sort order: newest, oldest, title"),
    page: int = Query(1, ge=1, description="Page number"),
    page_size: int = Query(20, ge=1, le=100, description="Items per page"),
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    svc = HistoryService(db)
    res = await svc.fun_get_user_history(
        user_id=user_token.user_id or "user_101",
        category=category,
        query=query,
        sort=sort,
        page=page,
        page_size=page_size
    )
    return StandardResponse(success=True, data=res)

@router.post("", response_model=StandardResponse[HistoryItemRead])
async def fun_add_history_item(
    item: HistoryItemCreate,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    svc = HistoryService(db)
    res = await svc.fun_create_history_entry(user_token.user_id or "user_101", item)
    return StandardResponse(success=True, data=res, message="History item logged successfully")

@router.delete("/{history_id}", response_model=StandardResponse[bool])
async def fun_delete_history_item(
    history_id: str,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    svc = HistoryService(db)
    success = await svc.fun_delete_history_item(user_token.user_id or "user_101", history_id)
    if not success:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="History item not found or unauthorized access"
        )
    return StandardResponse(success=True, data=True, message="History item deleted successfully")

@router.delete("", response_model=StandardResponse[int])
async def fun_clear_all_history(
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    svc = HistoryService(db)
    count = await svc.fun_clear_user_history(user_token.user_id or "user_101")
    return StandardResponse(success=True, data=count, message=f"Cleared {count} history records")
