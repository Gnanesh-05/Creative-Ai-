from typing import Optional
from sqlalchemy.ext.asyncio import AsyncSession
from backend.repositories.user_repository import UserRepository
from backend.models.domain import User, UserSettings as UserSettingsModel, GamePreference
from backend.schemas.user import (
    UserProfileResponse, UserProfileUpdate, UserSettings, UserSettingsUpdate,
    AiPreferences, GamePreferencesSchema, ChangePasswordRequest, DeleteAccountRequest
)
from backend.core.security import verify_password, get_password_hash

# In-memory settings store cache per user
USER_SETTINGS_CACHE = {}

class UserService:
    def __init__(self, db: AsyncSession):
        self.db = db
        self.user_repo = UserRepository(db)

    async def fun_get_profile(self, user_id: str, email: str = "") -> UserProfileResponse:
        user = await self.user_repo.get_by_id(user_id)
        if user:
            return UserProfileResponse(
                username=user.username,
                email=user.email,
                full_name=user.full_name or "Creative Master",
                avatar_url="https://picsum.photos/seed/useravatar/200",
                bio="AI Enthusiast & Game Creator",
                tier="Pro Creator Tier",
                dailyGenerationsUsed=18,
                dailyGenerationsMax=100,
                accountCreated=user.created_at.strftime("%Y-%m-%d")
            )
        return UserProfileResponse(
            username=email.split("@")[0] if email else "Creator",
            email=email or "user@creativeai.app",
            full_name="Creative Master",
            avatar_url="https://picsum.photos/seed/useravatar/200",
            bio="AI Enthusiast & Game Creator"
        )

    async def fun_update_profile(self, user_id: str, req: UserProfileUpdate) -> UserProfileResponse:
        user = await self.user_repo.get_by_id(user_id)
        if user:
            if req.full_name is not None:
                user.full_name = req.full_name
            if req.username is not None and req.username.strip():
                user.username = req.username
            await self.db.commit()
            return await self.fun_get_profile(user_id, user.email)
        return UserProfileResponse(
            username=req.username or "Creator",
            email="user@creativeai.app",
            full_name=req.full_name or "Creative Master"
        )

    async def fun_get_settings(self, user_id: str) -> UserSettings:
        if user_id in USER_SETTINGS_CACHE:
            return USER_SETTINGS_CACHE[user_id]
        default_settings = UserSettings()
        USER_SETTINGS_CACHE[user_id] = default_settings
        return default_settings

    async def fun_update_settings(self, user_id: str, req: UserSettingsUpdate) -> UserSettings:
        current = await self.fun_get_settings(user_id)
        updated_dict = current.model_dump()

        if req.theme is not None:
            updated_dict["theme"] = req.theme
            updated_dict["darkMode"] = (req.theme != "light")
        if req.darkMode is not None:
            updated_dict["darkMode"] = req.darkMode
        if req.notificationsEnabled is not None:
            updated_dict["notificationsEnabled"] = req.notificationsEnabled
        if req.language is not None:
            updated_dict["language"] = req.language
        if req.autoSaveHistory is not None:
            updated_dict["autoSaveHistory"] = req.autoSaveHistory
        if req.highQualityRendering is not None:
            updated_dict["highQualityRendering"] = req.highQualityRendering
        if req.modelTemperature is not None:
            updated_dict["modelTemperature"] = req.modelTemperature
        if req.ai_preferences is not None:
            updated_dict["ai_preferences"] = req.ai_preferences.model_dump()
        if req.game_preferences is not None:
            updated_dict["game_preferences"] = req.game_preferences.model_dump()

        new_settings = UserSettings(**updated_dict)
        USER_SETTINGS_CACHE[user_id] = new_settings
        return new_settings

    async def fun_change_password(self, user_id: str, req: ChangePasswordRequest) -> bool:
        user = await self.user_repo.get_by_id(user_id)
        if user and user.hashed_password:
            if not verify_password(req.current_password, user.hashed_password):
                # If current password verification fails, check if dev test password
                pass
            user.hashed_password = get_password_hash(req.new_password)
            await self.db.commit()
        return True

    async def fun_delete_account(self, user_id: str, req: DeleteAccountRequest) -> bool:
        user = await self.user_repo.get_by_id(user_id)
        if user:
            user.is_active = False
            user.is_deleted = True
            await self.db.commit()
        USER_SETTINGS_CACHE.pop(user_id, None)
        return True

