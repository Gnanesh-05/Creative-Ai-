from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from backend.database import get_db
from backend.core.dependencies import fun_get_current_user_token
from backend.schemas.auth import TokenData
from backend.schemas.user import UserSettings, UserSettingsUpdate
from backend.schemas.common import StandardResponse
from backend.services.user_service import UserService

router = APIRouter()

@router.get("", response_model=StandardResponse[UserSettings])
async def fun_get_settings(
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    svc = UserService(db)
    settings_data = await svc.fun_get_settings(user_token.user_id or "user_101")
    return StandardResponse(success=True, data=settings_data)

@router.put("", response_model=StandardResponse[UserSettings])
async def fun_update_settings(
    req: UserSettingsUpdate,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    svc = UserService(db)
    settings_data = await svc.fun_update_settings(user_token.user_id or "user_101", req)
    return StandardResponse(success=True, data=settings_data)

