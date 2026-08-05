import uuid
from typing import List, Optional
from sqlalchemy.ext.asyncio import AsyncSession
from backend.models.domain import UnifiedHistoryItem
from backend.repositories.history_repository import HistoryRepository
from backend.schemas.history import HistoryItemCreate, HistoryItemRead, HistoryListResponse

class HistoryService:
    def __init__(self, db: AsyncSession):
        self.repo = HistoryRepository(db)

    async def fun_get_user_history(
        self,
        user_id: str,
        category: Optional[str] = None,
        query: Optional[str] = None,
        sort: str = "newest",
        page: int = 1,
        page_size: int = 20
    ) -> HistoryListResponse:
        items, total = await self.repo.get_user_history(
            user_id=user_id,
            category=category,
            query=query,
            sort=sort,
            page=page,
            page_size=page_size
        )

        # Seed sample items if empty to provide rich default data for all 7 modules
        if total == 0 and not query and (not category or category == "ALL"):
            default_samples = [
                HistoryItemCreate(
                    module_type="CHAT",
                    title="Creative Writing Session: Quantum AI",
                    summary="Explored quantum computing paradigms and futuristic creative concepts.",
                    payload={"conversationId": "conv_quantum_01", "messageCount": 14}
                ),
                HistoryItemCreate(
                    module_type="IMAGE",
                    title="Cyberpunk Neon City Oasis",
                    summary="Generated high-definition 4K digital artwork of a futuristic skyline.",
                    payload={"imageUrl": "https://picsum.photos/seed/cyberpunk/600/400", "prompt": "Cyberpunk city with neon lights"}
                ),
                HistoryItemCreate(
                    module_type="MUSIC",
                    title="Ambient Synthwave Sunset",
                    summary="Synthesized a relaxing synthwave track at 120 BPM.",
                    payload={"audioUrl": "https://actions.google.com/sounds/v1/ambiences/outdoor_synth.ogg", "genre": "Synthwave"}
                ),
                HistoryItemCreate(
                    module_type="GAME_MIND",
                    title="AI Game Strategy Briefing",
                    summary="Evaluated chess opening theory and minimax search tree optimization.",
                    payload={"topic": "Chess Openings & Game AI Logic"}
                ),
                HistoryItemCreate(
                    module_type="GAME_CHESS",
                    title="Chess Match vs Grandmaster Mind",
                    summary="Won by checkmate in 24 moves using Sicilian Defense.",
                    payload={"gameType": "Chess", "result": "Won", "moves": 24, "score": 1250}
                ),
                HistoryItemCreate(
                    module_type="GAME_TICTACTOE",
                    title="Tic-Tac-Toe vs Minimax Engine",
                    summary="Draw game against Unbeatable Minimax AI.",
                    payload={"gameType": "Tic-Tac-Toe", "result": "Draw", "moves": 9, "score": 500}
                ),
                HistoryItemCreate(
                    module_type="GAME_MAZE",
                    title="AI Maze Pathfinder Challenge",
                    summary="Solved 15x15 maze in 32 steps using A* Heuristic Algorithm.",
                    payload={"gameType": "Maze", "result": "Won", "moves": 32, "score": 980}
                )
            ]
            for sample in default_samples:
                await self.fun_create_history_entry(user_id, sample)

            items, total = await self.repo.get_user_history(
                user_id=user_id,
                category=category,
                query=query,
                sort=sort,
                page=page,
                page_size=page_size
            )

        read_items = [HistoryItemRead.model_validate(item) for item in items]
        has_more = (page * page_size) < total

        return HistoryListResponse(
            items=read_items,
            total=total,
            page=page,
            page_size=page_size,
            has_more=has_more
        )

    async def fun_create_history_entry(self, user_id: str, item: HistoryItemCreate) -> HistoryItemRead:
        new_item = UnifiedHistoryItem(
            id=str(uuid.uuid4()),
            user_id=user_id,
            module_type=item.module_type,
            title=item.title,
            summary=item.summary,
            payload=item.payload
        )
        saved = await self.repo.create(new_item)
        return HistoryItemRead.model_validate(saved)

    async def fun_delete_history_item(self, user_id: str, item_id: str) -> bool:
        return await self.repo.delete_item_by_user(user_id, item_id)

    async def fun_clear_user_history(self, user_id: str) -> int:
        return await self.repo.clear_all_by_user(user_id)
