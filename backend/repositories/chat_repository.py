from typing import List, Optional
from sqlalchemy.future import select
from sqlalchemy.ext.asyncio import AsyncSession
from sqlalchemy.orm import selectinload
from backend.models.domain import ChatConversation, ChatMessage
from backend.repositories.base import BaseRepository

class ChatRepository(BaseRepository[ChatConversation]):
    def __init__(self, db: AsyncSession):
        super().__init__(ChatConversation, db)

    async def get_user_conversations(self, user_id: str, search_query: Optional[str] = None) -> List[ChatConversation]:
        stmt = (
            select(ChatConversation)
            .options(selectinload(ChatConversation.messages))
            .where(ChatConversation.user_id == user_id, ChatConversation.is_deleted == False)
            .order_by(ChatConversation.updated_at.desc())
        )
        result = await self.db.execute(stmt)
        conversations = result.scalars().all()

        if search_query and search_query.strip():
            query_lower = search_query.strip().lower()
            filtered = []
            for conv in conversations:
                title_match = query_lower in conv.title.lower()
                message_match = any(query_lower in msg.content.lower() for msg in conv.messages)
                if title_match or message_match:
                    filtered.append(conv)
            return filtered

        return conversations

    async def get_conversation_by_id(self, conversation_id: str, user_id: str) -> Optional[ChatConversation]:
        stmt = (
            select(ChatConversation)
            .options(selectinload(ChatConversation.messages))
            .where(
                ChatConversation.id == conversation_id,
                ChatConversation.user_id == user_id,
                ChatConversation.is_deleted == False
            )
        )
        result = await self.db.execute(stmt)
        return result.scalars().first()

    async def create_conversation(
        self,
        user_id: str,
        title: str = "New Conversation",
        system_instruction: Optional[str] = None
    ) -> ChatConversation:
        conv = ChatConversation(
            user_id=user_id,
            title=title,
            system_instruction=system_instruction,
            model_name="gemini-2.0-flash"
        )
        self.db.add(conv)
        await self.db.commit()
        await self.db.refresh(conv)
        return conv

    async def update_conversation(
        self,
        conversation_id: str,
        user_id: str,
        title: Optional[str] = None,
        system_instruction: Optional[str] = None
    ) -> Optional[ChatConversation]:
        conv = await self.get_conversation_by_id(conversation_id, user_id)
        if not conv:
            return None
        if title is not None:
            conv.title = title
        if system_instruction is not None:
            conv.system_instruction = system_instruction
        await self.db.commit()
        await self.db.refresh(conv)
        return conv

    async def delete_conversation(self, conversation_id: str, user_id: str) -> bool:
        conv = await self.get_conversation_by_id(conversation_id, user_id)
        if not conv:
            return False
        conv.is_deleted = True
        await self.db.commit()
        return True

    async def add_message(
        self,
        conversation_id: str,
        sender: str,
        content: str,
        tokens_used: int = 0
    ) -> ChatMessage:
        msg = ChatMessage(
            conversation_id=conversation_id,
            sender=sender,
            content=content,
            tokens_used=tokens_used
        )
        self.db.add(msg)
        await self.db.commit()
        await self.db.refresh(msg)
        return msg

    async def clear_messages(self, conversation_id: str, user_id: str) -> bool:
        conv = await self.get_conversation_by_id(conversation_id, user_id)
        if not conv:
            return False
        for msg in conv.messages:
            await self.db.delete(msg)
        await self.db.commit()
        return True
