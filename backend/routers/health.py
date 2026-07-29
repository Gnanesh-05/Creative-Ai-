from fastapi import APIRouter
from backend.schemas.common import HealthResponse, ReadinessResponse
from backend.config import settings

router = APIRouter()

@router.get("/health", response_model=HealthResponse, tags=["Health"])
async def fun_health_check():
    return HealthResponse(
        status="healthy",
        service=settings.PROJECT_NAME,
        version="1.0.0",
        environment=settings.ENVIRONMENT
    )

@router.get("/readiness", response_model=ReadinessResponse, tags=["Health"])
async def fun_readiness_check():
    return ReadinessResponse(
        status="ready",
        database=True,
        ai_providers=True
    )
