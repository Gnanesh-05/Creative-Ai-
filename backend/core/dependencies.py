from typing import Optional
from fastapi import Depends, HTTPException, status
from fastapi.security import HTTPBearer, HTTPAuthorizationCredentials
from sqlalchemy.ext.asyncio import AsyncSession
from backend.database import get_db
from backend.core.security import decode_token
from backend.schemas.auth import TokenData

security_bearer = HTTPBearer(auto_error=False)

async def fun_get_current_user_token(
    credentials: Optional[HTTPAuthorizationCredentials] = Depends(security_bearer)
) -> TokenData:
    if not credentials:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Authentication credentials are required",
            headers={"WWW-Authenticate": "Bearer"},
        )
        
    token = credentials.credentials
    payload = decode_token(token)
    if not payload:
        raise HTTPException(
            status_code=status.HTTP_401_UNAUTHORIZED,
            detail="Invalid authentication token",
            headers={"WWW-Authenticate": "Bearer"},
        )
    return TokenData(user_id=payload.get("sub"), email=payload.get("email"))
