import uuid
from typing import List, Dict, Optional, AsyncGenerator
from sqlalchemy.ext.asyncio import AsyncSession
from backend.providers.llm_provider import llm_provider
from backend.repositories.chat_repository import ChatRepository
from backend.repositories.history_repository import HistoryRepository
from backend.models.domain import UnifiedHistoryItem
from backend.schemas.chat import (
    ChatRequest, ChatResponse, ConversationDto, ConversationDetailDto, ChatMessageDto
)

class ChatService:
    def __init__(self, db: Optional[AsyncSession] = None):
        self.db = db

    def _format_conversation_dto(self, conv) -> ConversationDto:
        snippet = ""
        msgs = getattr(conv, "messages", [])
        if msgs:
            sorted_msgs = sorted(msgs, key=lambda m: m.created_at or 0)
            snippet = sorted_msgs[-1].content[:60] if sorted_msgs else ""
        
        return ConversationDto(
            id=conv.id,
            title=conv.title,
            system_instruction=conv.system_instruction,
            model_name=conv.model_name or "gemini-2.0-flash",
            created_at=str(conv.created_at or ""),
            updated_at=str(conv.updated_at or ""),
            last_message_snippet=snippet,
            message_count=len(msgs)
        )

    def _format_message_dto(self, msg) -> ChatMessageDto:
        return ChatMessageDto(
            id=msg.id,
            conversation_id=msg.conversation_id,
            sender=msg.sender,
            content=msg.content,
            tokens_used=msg.tokens_used or 0,
            created_at=str(msg.created_at or "")
        )

    async def fun_process_chat(self, req: ChatRequest, user_id: str = "user_101") -> ChatResponse:
        history_dicts = []
        conversation_id = req.conversation_id

        if self.db and conversation_id:
            repo = ChatRepository(self.db)
            conv = await repo.get_conversation_by_id(conversation_id, user_id)
            if conv:
                history_dicts = [
                    {"role": m.sender, "content": m.content}
                    for m in sorted(conv.messages, key=lambda x: x.created_at or 0)
                ]

        if not history_dicts and req.history:
            history_dicts = [{"role": msg.role, "content": msg.content} for msg in req.history]

        # Generate response from LLM Provider
        reply_text = await llm_provider.fun_generate_reply(
            message=req.message,
            history=history_dicts,
            system_instruction=req.system_instruction,
            temperature=req.temperature or 0.7
        )

        tokens_used = len(req.message) + len(reply_text)
        res_message_id = str(uuid.uuid4())

        # Persist conversation & messages if DB session is available
        if self.db:
            repo = ChatRepository(self.db)
            if not conversation_id:
                # Auto-create conversation title based on user message snippet
                title_text = req.message[:30] + ("..." if len(req.message) > 30 else "")
                conv = await repo.create_conversation(user_id=user_id, title=title_text, system_instruction=req.system_instruction)
                conversation_id = conv.id

            # Save user message & model message
            await repo.add_message(conversation_id=conversation_id, sender="user", content=req.message)
            ai_msg = await repo.add_message(conversation_id=conversation_id, sender="model", content=reply_text, tokens_used=tokens_used)
            res_message_id = ai_msg.id

            # Save unified history item
            hist_repo = HistoryRepository(self.db)
            hist_item = UnifiedHistoryItem(
                user_id=user_id,
                module_type="CHAT",
                title=req.message[:40],
                summary=reply_text[:80],
                payload={"conversation_id": conversation_id, "reply": reply_text}
            )
            hist_repo.db.add(hist_item)
            await hist_repo.db.commit()

        if not conversation_id:
            conversation_id = str(uuid.uuid4())

        return ChatResponse(
            conversation_id=conversation_id,
            message_id=res_message_id,
            reply=reply_text,
            model="gemini-2.0-flash",
            tokens_used=tokens_used
        )

    async def fun_stream_chat(self, req: ChatRequest, user_id: str = "user_101") -> AsyncGenerator[str, None]:
        history_dicts = []
        if self.db and req.conversation_id:
            repo = ChatRepository(self.db)
            conv = await repo.get_conversation_by_id(req.conversation_id, user_id)
            if conv:
                history_dicts = [
                    {"role": m.sender, "content": m.content}
                    for m in sorted(conv.messages, key=lambda x: x.created_at or 0)
                ]

        if not history_dicts and req.history:
            history_dicts = [{"role": msg.role, "content": msg.content} for msg in req.history]

        full_reply_chunks = []
        async for chunk in llm_provider.fun_stream_generate_reply(
            message=req.message,
            history=history_dicts,
            system_instruction=req.system_instruction,
            temperature=req.temperature or 0.7
        ):
            full_reply_chunks.append(chunk)
            yield chunk

        # Save to DB after stream completes
        if self.db:
            full_text = "".join(full_reply_chunks)
            repo = ChatRepository(self.db)
            conv_id = req.conversation_id
            if not conv_id:
                title_text = req.message[:30] + ("..." if len(req.message) > 30 else "")
                conv = await repo.create_conversation(user_id=user_id, title=title_text)
                conv_id = conv.id

            await repo.add_message(conversation_id=conv_id, sender="user", content=req.message)
            await repo.add_message(conversation_id=conv_id, sender="model", content=full_text, tokens_used=len(full_text))

    async def get_user_conversations(self, user_id: str, search: Optional[str] = None) -> List[ConversationDto]:
        if not self.db:
            return []
        repo = ChatRepository(self.db)
        convs = await repo.get_user_conversations(user_id, search)
        return [self._format_conversation_dto(c) for c in convs]

    async def get_conversation_detail(self, conversation_id: str, user_id: str) -> Optional[ConversationDetailDto]:
        if not self.db:
            return None
        repo = ChatRepository(self.db)
        conv = await repo.get_conversation_by_id(conversation_id, user_id)
        if not conv:
            return None
        
        dto = self._format_conversation_dto(conv)
        messages_dto = [self._format_message_dto(m) for m in sorted(conv.messages, key=lambda x: x.created_at or 0)]
        return ConversationDetailDto(
            **dto.model_dump(),
            messages=messages_dto
        )

    async def create_conversation(self, user_id: str, title: str, system_instruction: Optional[str] = None) -> ConversationDto:
        if not self.db:
            conv_id = str(uuid.uuid4())
            return ConversationDto(id=conv_id, title=title, system_instruction=system_instruction, created_at="", updated_at="")
        repo = ChatRepository(self.db)
        conv = await repo.create_conversation(user_id, title, system_instruction)
        return self._format_conversation_dto(conv)

    async def update_conversation(self, conversation_id: str, user_id: str, title: Optional[str], system_instruction: Optional[str]) -> Optional[ConversationDto]:
        if not self.db:
            return None
        repo = ChatRepository(self.db)
        conv = await repo.update_conversation(conversation_id, user_id, title, system_instruction)
        if not conv:
            return None
        return self._format_conversation_dto(conv)

    async def delete_conversation(self, conversation_id: str, user_id: str) -> bool:
        if not self.db:
            return True
        repo = ChatRepository(self.db)
        return await repo.delete_conversation(conversation_id, user_id)

    async def clear_messages(self, conversation_id: str, user_id: str) -> bool:
        if not self.db:
            return True
        repo = ChatRepository(self.db)
        return await repo.clear_messages(conversation_id, user_id)
