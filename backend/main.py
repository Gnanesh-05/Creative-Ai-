from contextlib import asynccontextmanager
from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.exceptions import RequestValidationError

from backend.config import settings
from backend.core.logging import setup_logging, logger
from backend.core.exceptions import (
    CreativeAiException,
    creative_ai_exception_handler,
    validation_exception_handler
)
from backend.routers import (
    auth, user, profile, settings as settings_router,
    chat, image, music, games, history, health
)

@asynccontextmanager
async def fun_lifespan(app: FastAPI):
    # Startup sequence
    setup_logging()
    logger.info(f"Starting {settings.PROJECT_NAME} in [{settings.ENVIRONMENT}] mode...")
    yield
    # Shutdown sequence
    logger.info(f"Shutting down {settings.PROJECT_NAME}...")

app = FastAPI(
    title=settings.PROJECT_NAME,
    description="Scalable FastAPI backend proxy & orchestrator for Creative AI Android client application.",
    version="1.0.0",
    lifespan=fun_lifespan
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Custom Exception Handlers
app.add_exception_handler(CreativeAiException, creative_ai_exception_handler)
app.add_exception_handler(RequestValidationError, validation_exception_handler)

# Include API v1 Routers
app.include_router(auth.router, prefix="/api/v1/auth", tags=["Authentication"])
app.include_router(user.router, prefix="/api/v1/user", tags=["User Profile & Settings"])
app.include_router(profile.router, prefix="/api/v1/profile", tags=["Profile Direct"])
app.include_router(settings_router.router, prefix="/api/v1/settings", tags=["Settings Direct"])
app.include_router(chat.router, prefix="/api/v1/chat", tags=["Conversational Chat AI"])
app.include_router(image.router, prefix="/api/v1/image", tags=["AI Image Generator"])
app.include_router(music.router, prefix="/api/v1/music", tags=["AI Music Composer"])
app.include_router(games.router, prefix="/api/v1/games", tags=["Game Mind AI"])
app.include_router(history.router, prefix="/api/v1/history", tags=["Unified History"])

# Health & Readiness
app.include_router(health.router, prefix="/api/v1", tags=["Health Checks"])
app.include_router(health.router, prefix="", tags=["Root Health Checks"])

@app.get("/", tags=["Root"])
async def fun_root():
    return {
        "status": "online",
        "app": settings.PROJECT_NAME,
        "version": "1.0.0",
        "docs_url": "/docs"
    }
