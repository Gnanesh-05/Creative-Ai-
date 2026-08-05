from fastapi import APIRouter, Depends
from sqlalchemy.ext.asyncio import AsyncSession
from backend.database import get_db
from backend.schemas.auth import LoginRequest, RegisterRequest, AuthResponse, PasswordResetRequest, PasswordResetConfirmRequest
from backend.schemas.common import StandardResponse
from backend.services.auth_service import AuthService

router = APIRouter()

@router.post("/login", response_model=StandardResponse[AuthResponse])
async def fun_login(req: LoginRequest, db: AsyncSession = Depends(get_db)):
    auth_service = AuthService(db)
    res = await auth_service.fun_login(req)
    return StandardResponse(success=True, data=res, message="Login successful")

@router.post("/register", response_model=StandardResponse[AuthResponse])
async def fun_register(req: RegisterRequest, db: AsyncSession = Depends(get_db)):
    auth_service = AuthService(db)
    res = await auth_service.fun_register(req)
    return StandardResponse(success=True, data=res, message="Account registered successfully")

@router.post("/forgot-password", response_model=StandardResponse[bool])
@router.post("/password-reset", response_model=StandardResponse[bool])
async def fun_forgot_password(req: PasswordResetRequest, db: AsyncSession = Depends(get_db)):
    auth_service = AuthService(db)
    res = await auth_service.fun_send_password_reset(req.email)
    return StandardResponse(success=True, data=res, message="If an account with that email exists, reset instructions have been sent.")

@router.post("/reset-password", response_model=StandardResponse[bool])
@router.post("/password-reset-confirm", response_model=StandardResponse[bool])
async def fun_confirm_password_reset(req: PasswordResetConfirmRequest, db: AsyncSession = Depends(get_db)):
    auth_service = AuthService(db)
    res = await auth_service.fun_confirm_password_reset(req)
    return StandardResponse(success=True, data=res, message="Password reset successfully.")

