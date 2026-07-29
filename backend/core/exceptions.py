from fastapi import HTTPException, status, Request
from fastapi.responses import JSONResponse
from fastapi.exceptions import RequestValidationError

class CreativeAiException(Exception):
    def __init__(self, message: str, code: str = "INTERNAL_ERROR"):
        self.message = message
        self.code = code
        super().__init__(self.message)

class ResourceNotFoundException(CreativeAiException):
    def __init__(self, message: str = "Resource not found"):
        super().__init__(message=message, code="NOT_FOUND")

class AuthenticationException(CreativeAiException):
    def __init__(self, message: str = "Could not validate credentials"):
        super().__init__(message=message, code="UNAUTHORIZED")

class ProviderException(CreativeAiException):
    def __init__(self, message: str = "External AI provider error"):
        super().__init__(message=message, code="PROVIDER_ERROR")

async def creative_ai_exception_handler(request: Request, exc: CreativeAiException):
    status_code = status.HTTP_500_INTERNAL_SERVER_ERROR
    if exc.code == "NOT_FOUND":
        status_code = status.HTTP_404_NOT_FOUND
    elif exc.code == "UNAUTHORIZED":
        status_code = status.HTTP_401_UNAUTHORIZED
    elif exc.code == "PROVIDER_ERROR":
        status_code = status.HTTP_502_BAD_GATEWAY
        
    return JSONResponse(
        status_code=status_code,
        content={
            "success": False,
            "error": {
                "code": exc.code,
                "message": exc.message
            }
        }
    )

async def validation_exception_handler(request: Request, exc: RequestValidationError):
    return JSONResponse(
        status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
        content={
            "success": False,
            "error": {
                "code": "VALIDATION_ERROR",
                "message": "Invalid request payload",
                "details": exc.errors()
            }
        }
    )
