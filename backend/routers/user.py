from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from backend.database import get_db
from backend.core.dependencies import fun_get_current_user_token
from backend.schemas.auth import TokenData
from backend.schemas.user import (
    UserProfileResponse, UserProfileUpdate, UserSettings, UserSettingsUpdate,
    ChangePasswordRequest, DeleteAccountRequest
)
from backend.schemas.common import StandardResponse
from backend.services.user_service import UserService

router = APIRouter()

@router.get("/profile", response_model=StandardResponse[UserProfileResponse])
async def fun_get_profile(
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    svc = UserService(db)
    profile = await svc.fun_get_profile(user_token.user_id or "user_101", user_token.email or "")
    return StandardResponse(success=True, data=profile)

@router.put("/profile", response_model=StandardResponse[UserProfileResponse])
async def fun_update_profile(
    req: UserProfileUpdate,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    svc = UserService(db)
    profile = await svc.fun_update_profile(user_token.user_id or "user_101", req)
    return StandardResponse(success=True, data=profile)

@router.get("/settings", response_model=StandardResponse[UserSettings])
async def fun_get_settings(
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    svc = UserService(db)
    settings_data = await svc.fun_get_settings(user_token.user_id or "user_101")
    return StandardResponse(success=True, data=settings_data)

@router.put("/settings", response_model=StandardResponse[UserSettings])
async def fun_update_settings(
    req: UserSettingsUpdate,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    svc = UserService(db)
    settings_data = await svc.fun_update_settings(user_token.user_id or "user_101", req)
    return StandardResponse(success=True, data=settings_data)

@router.post("/change-password", response_model=StandardResponse[bool])
async def fun_change_password(
    req: ChangePasswordRequest,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    svc = UserService(db)
    result = await svc.fun_change_password(user_token.user_id or "user_101", req)
    return StandardResponse(success=True, data=result, message="Password updated successfully")

@router.delete("/account", response_model=StandardResponse[bool])
async def fun_delete_account(
    req: DeleteAccountRequest,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    svc = UserService(db)
    result = await svc.fun_delete_account(user_token.user_id or "user_101", req)
    return StandardResponse(success=True, data=result, message="Account deleted successfully")

