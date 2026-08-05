import pytest
import pytest_asyncio
from sqlalchemy.ext.asyncio import create_async_engine, AsyncSession, async_sessionmaker
from backend.database import Base
from backend.services.history_service import HistoryService
from backend.schemas.history import HistoryItemCreate

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
async def test_unified_history_creation_and_filtering(async_db):
    svc = HistoryService(async_db)
    user_id = "user_test_99"

    # Create history entries across modules
    chat_item = HistoryItemCreate(
        module_type="CHAT",
        title="AI Chat Session",
        summary="Discussed creative concepts."
    )
    image_item = HistoryItemCreate(
        module_type="IMAGE",
        title="Cyberpunk Landscape",
        summary="4K artwork generated."
    )
    chess_item = HistoryItemCreate(
        module_type="GAME_CHESS",
        title="Chess Match",
        summary="Victory against Grandmaster Mind."
    )

    await svc.fun_create_history_entry(user_id, chat_item)
    await svc.fun_create_history_entry(user_id, image_item)
    await svc.fun_create_history_entry(user_id, chess_item)

    # Query all history
    res_all = await svc.fun_get_user_history(user_id, category="ALL")
    assert res_all.total == 3

    # Query CHAT history filter
    res_chat = await svc.fun_get_user_history(user_id, category="CHAT")
    assert res_chat.total == 1
    assert res_chat.items[0].title == "AI Chat Session"

    # Search query
    res_search = await svc.fun_get_user_history(user_id, query="Cyberpunk")
    assert res_search.total == 1
    assert res_search.items[0].title == "Cyberpunk Landscape"

@pytest.mark.asyncio
async def test_unified_history_ownership_and_deletion(async_db):
    svc = HistoryService(async_db)
    user_a = "user_alpha"
    user_b = "user_beta"

    item_a = await svc.fun_create_history_entry(user_a, HistoryItemCreate(module_type="MUSIC", title="Song A"))
    item_b = await svc.fun_create_history_entry(user_b, HistoryItemCreate(module_type="MUSIC", title="Song B"))

    # User B attempting to delete User A's item should fail
    deleted_fail = await svc.fun_delete_history_item(user_b, item_a.id)
    assert deleted_fail is False

    # User A deleting User A's item should succeed
    deleted_ok = await svc.fun_delete_history_item(user_a, item_a.id)
    assert deleted_ok is True

    # User A clearing all history
    clear_count = await svc.fun_clear_user_history(user_a)
    assert clear_count == 0  # item_a was already deleted

    # User B history remains intact
    res_b = await svc.fun_get_user_history(user_b)
    assert res_b.total >= 1
