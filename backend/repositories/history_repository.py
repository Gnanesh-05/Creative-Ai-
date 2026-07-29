from typing import List, Tuple, Optional
from sqlalchemy.future import select
from sqlalchemy import func, delete, or_
from sqlalchemy.ext.asyncio import AsyncSession
from backend.models.domain import UnifiedHistoryItem
from backend.repositories.base import BaseRepository

class HistoryRepository(BaseRepository[UnifiedHistoryItem]):
    def __init__(self, db: AsyncSession):
        super().__init__(UnifiedHistoryItem, db)

    async def get_user_history(
        self,
        user_id: str,
        category: Optional[str] = None,
        query: Optional[str] = None,
        sort: str = "newest",
        page: int = 1,
        page_size: int = 20
    ) -> Tuple[List[UnifiedHistoryItem], int]:
        stmt = select(UnifiedHistoryItem).where(
            UnifiedHistoryItem.user_id == user_id,
            UnifiedHistoryItem.is_deleted == False
        )

        if category and category.upper() != "ALL":
            cat_upper = category.upper()
            if cat_upper == "GAME":
                stmt = stmt.where(UnifiedHistoryItem.module_type.like("GAME_%"))
            else:
                stmt = stmt.where(UnifiedHistoryItem.module_type == cat_upper)

        if query and query.strip():
            search_pattern = f"%{query.strip()}%"
            stmt = stmt.where(
                or_(
                    UnifiedHistoryItem.title.ilike(search_pattern),
                    UnifiedHistoryItem.summary.ilike(search_pattern)
                )
            )

        # Count total
        count_stmt = select(func.count()).select_from(stmt.subquery())
        total_res = await self.db.execute(count_stmt)
        total = total_res.scalar_one_or_none() or 0

        # Sorting
        if sort == "oldest":
            stmt = stmt.order_by(UnifiedHistoryItem.created_at.asc())
        elif sort == "title":
            stmt = stmt.order_by(UnifiedHistoryItem.title.asc())
        else:
            stmt = stmt.order_by(UnifiedHistoryItem.created_at.desc())

        # Pagination
        offset = max(0, (page - 1) * page_size)
        stmt = stmt.offset(offset).limit(page_size)

        result = await self.db.execute(stmt)
        return result.scalars().all(), total

    async def delete_item_by_user(self, user_id: str, item_id: str) -> bool:
        stmt = select(UnifiedHistoryItem).where(
            UnifiedHistoryItem.id == item_id,
            UnifiedHistoryItem.user_id == user_id,
            UnifiedHistoryItem.is_deleted == False
        )
        res = await self.db.execute(stmt)
        item = res.scalars().first()
        if not item:
            return False
        item.is_deleted = True
        await self.db.commit()
        return True

    async def clear_all_by_user(self, user_id: str) -> int:
        stmt = select(UnifiedHistoryItem).where(
            UnifiedHistoryItem.user_id == user_id,
            UnifiedHistoryItem.is_deleted == False
        )
        res = await self.db.execute(stmt)
        items = res.scalars().all()
        count = len(items)
        for item in items:
            item.is_deleted = True
        await self.db.commit()
        return count
