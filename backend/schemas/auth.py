from typing import Optional
from pydantic import BaseModel, EmailStr

class LoginRequest(BaseModel):
    email: EmailStr
    password: str

class RegisterRequest(BaseModel):
    username: str
    email: EmailStr
    password: str

class AuthResponse(BaseModel):
    token: str
    userId: str
    username: str
    email: str

class PasswordResetRequest(BaseModel):
    email: EmailStr

class PasswordResetConfirmRequest(BaseModel):
    token: str
    newPassword: str

class TokenData(BaseModel):
    user_id: Optional[str] = None
    email: Optional[str] = None
