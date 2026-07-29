from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from backend.database import get_db
from backend.core.dependencies import fun_get_current_user_token
from backend.schemas.auth import TokenData
from backend.schemas.user import UserProfileResponse, UserProfileUpdate
from backend.schemas.common import StandardResponse
from backend.services.user_service import UserService

router = APIRouter()

@router.get("", response_model=StandardResponse[UserProfileResponse])
async def fun_get_profile(
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    svc = UserService(db)
    profile = await svc.fun_get_profile(user_token.user_id or "user_101", user_token.email or "")
    return StandardResponse(success=True, data=profile)

@router.put("", response_model=StandardResponse[UserProfileResponse])
async def fun_update_profile(
    req: UserProfileUpdate,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    svc = UserService(db)
    profile = await svc.fun_update_profile(user_token.user_id or "user_101", req)
    return StandardResponse(success=True, data=profile)

