import pytest
import pytest_asyncio
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker
from backend.database import Base
from backend.services.auth_service import AuthService
from backend.schemas.auth import RegisterRequest, LoginRequest, PasswordResetConfirmRequest

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
async def test_register_and_login_flow(async_db):
    auth_service = AuthService(async_db)
    
    # Register
    reg_req = RegisterRequest(username="testuser", email="test@example.com", password="SecurePassword123!")
    auth_res = await auth_service.fun_register(reg_req)
    assert auth_res.email == "test@example.com"
    assert auth_res.token is not None

    # Login
    login_req = LoginRequest(email="test@example.com", password="SecurePassword123!")
    login_res = await auth_service.fun_login(login_req)
    assert login_res.username == "testuser"
    assert login_res.token is not None

@pytest.mark.asyncio
async def test_forgot_password_user_enumeration_protection(async_db):
    auth_service = AuthService(async_db)
    
    # Existing email
    reg_req = RegisterRequest(username="knownuser", email="known@example.com", password="Password123!")
    await auth_service.fun_register(reg_req)
    
    # Request reset for known user
    res_known = await auth_service.fun_send_password_reset("known@example.com")
    assert res_known is True
    
    # Request reset for non-existent user
    res_unknown = await auth_service.fun_send_password_reset("unknown@example.com")
    assert res_unknown is True

@pytest.mark.asyncio
async def test_password_reset_confirmation(async_db):
    auth_service = AuthService(async_db)
    
    # Register
    reg_req = RegisterRequest(username="resetuser", email="reset@example.com", password="OldPassword123!")
    await auth_service.fun_register(reg_req)
    
    # Send reset
    await auth_service.fun_send_password_reset("reset@example.com")
    
    # Confirm password reset with dev/token code
    confirm_req = PasswordResetConfirmRequest(token="RES-TEST1234", newPassword="BrandNewPassword123!")
    reset_ok = await auth_service.fun_confirm_password_reset(confirm_req)
    assert reset_ok is True
