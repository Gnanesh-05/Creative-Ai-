import pytest
import pytest_asyncio
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker
from backend.database import Base
from backend.services.user_service import UserService
from backend.schemas.user import UserProfileUpdate, UserSettingsUpdate, AiPreferences, GamePreferencesSchema, ChangePasswordRequest, DeleteAccountRequest
from backend.models.domain import User
from backend.core.security import get_password_hash

@pytest_asyncio.fixture
async def async_db():
    engine = create_async_engine("sqlite+aiosqlite:///:memory:", echo=False)
    async with engine.begin() as conn:
        await conn.run_sync(Base.metadata.create_all)
    
    async_session = async_sessionmaker(engine, expire_on_commit=False, class_=AsyncSession)
    async with async_session() as session:
        yield session
    await engine.dispose()

@pytest.mark.asyncio
async def test_user_profile_get_and_update(async_db):
    svc = UserService(async_db)
    user_id = "user_test_profile"

    # Seed test user
    user = User(
        id=user_id,
        email="creator@test.com",
        username="CreatorOriginal",
        hashed_password=get_password_hash("Secret123"),
        full_name="Original Name"
    )
    async_db.add(user)
    await async_db.commit()

    # Get profile
    profile = await svc.fun_get_profile(user_id, "creator@test.com")
    assert profile.username == "CreatorOriginal"
    assert profile.full_name == "Original Name"

    # Update profile
    upd_req = UserProfileUpdate(full_name="Updated Creative Master", username="CreativeNinja")
    updated_profile = await svc.fun_update_profile(user_id, upd_req)
    assert updated_profile.full_name == "Updated Creative Master"
    assert updated_profile.username == "CreativeNinja"

@pytest.mark.asyncio
async def test_user_settings_and_preferences(async_db):
    svc = UserService(async_db)
    user_id = "user_test_settings"

    # Get default settings
    settings = await svc.fun_get_settings(user_id)
    assert settings.theme == "system"
    assert settings.language == "English"
    assert settings.ai_preferences.chat_response_style == "Detailed & Creative"

    # Update settings
    new_ai_pref = AiPreferences(chat_response_style="Concise", content_filter_level="Strict")
    new_game_pref = GamePreferencesSchema(chess_difficulty="Grandmaster Mind", no_spoiler_mode=True)
    upd_req = UserSettingsUpdate(
        theme="dark",
        language="Spanish",
        ai_preferences=new_ai_pref,
        game_preferences=new_game_pref
    )

    updated_settings = await svc.fun_update_settings(user_id, upd_req)
    assert updated_settings.theme == "dark"
    assert updated_settings.language == "Spanish"
    assert updated_settings.ai_preferences.chat_response_style == "Concise"
    assert updated_settings.game_preferences.no_spoiler_mode is True

@pytest.mark.asyncio
async def test_user_password_change_and_account_deletion(async_db):
    svc = UserService(async_db)
    user_id = "user_test_sec"

    # Seed user
    user = User(
        id=user_id,
        email="sec@test.com",
        username="SecUser",
        hashed_password=get_password_hash("OldPass123"),
        full_name="Sec User"
    )
    async_db.add(user)
    await async_db.commit()

    # Change password
    pass_res = await svc.fun_change_password(user_id, ChangePasswordRequest(current_password="OldPass123", new_password="NewPass123!"))
    assert pass_res is True

    # Delete account
    del_res = await svc.fun_delete_account(user_id, DeleteAccountRequest(password_confirmation="NewPass123!"))
    assert del_res is True
