import os
from pydantic_settings import BaseSettings

class Settings(BaseSettings):
    ENVIRONMENT: str = "development"
    PROJECT_NAME: str = "Creative AI API"
    LOG_LEVEL: str = "INFO"
    
    HOST: str = "0.0.0.0"
    PORT: int = 8000
    
    SECRET_KEY: str = "super-secret-creative-ai-jwt-signing-key-change-in-prod"
    ALGORITHM: str = "HS256"
    ACCESS_TOKEN_EXPIRE_MINUTES: int = 60 * 24 * 7  # 7 days
    REFRESH_TOKEN_EXPIRE_DAYS: int = 30
    
    # Database Connection String
    DATABASE_URL: str = "postgresql+asyncpg://creative_user:creative_pass@localhost:5432/creative_ai_db"
    
    # External AI Provider Credentials (SERVER-SIDE ONLY)
    GEMINI_API_KEY: str = ""
    IMAGE_GEN_API_KEY: str = ""
    MUSIC_GEN_API_KEY: str = ""
    
    # Storage Settings
    STORAGE_PROVIDER: str = "local"
    STORAGE_BUCKET_NAME: str = "creative-ai-assets"
    
    # Email Settings
    EMAIL_PROVIDER: str = "mock"
    SMTP_HOST: str = "smtp.mailtrap.io"
    SMTP_PORT: int = 587
    SMTP_USER: str = ""
    SMTP_PASSWORD: str = ""
    EMAILS_FROM_EMAIL: str = "noreply@creativeai.app"

    @property
    def is_production(self) -> bool:
        return self.ENVIRONMENT.lower() == "production"

    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"
        extra = "ignore"

settings = Settings()
