from typing import List, Optional
from pydantic import BaseModel
from datetime import datetime

class ChatMessageDto(BaseModel):
    id: Optional[str] = None
    conversation_id: Optional[str] = None
    sender: str  # "user" or "model" / "assistant"
    content: str
    tokens_used: Optional[int] = 0
    created_at: Optional[str] = None

class ConversationDto(BaseModel):
    id: str
    title: str
    system_instruction: Optional[str] = None
    model_name: str = "gemini-2.0-flash"
    created_at: str
    updated_at: str
    last_message_snippet: Optional[str] = None
    message_count: int = 0

class ConversationDetailDto(ConversationDto):
    messages: List[ChatMessageDto] = []

class CreateConversationRequest(BaseModel):
    title: Optional[str] = "New Conversation"
    system_instruction: Optional[str] = "You are Creative AI, a helpful, intelligent assistant."

class UpdateConversationRequest(BaseModel):
    title: Optional[str] = None
    system_instruction: Optional[str] = None

class ChatRequest(BaseModel):
    conversation_id: Optional[str] = None
    message: str
    history: Optional[List[ChatMessageDto]] = []
    system_instruction: Optional[str] = None
    temperature: Optional[float] = 0.7

class ChatResponse(BaseModel):
    conversation_id: str
    message_id: str
    reply: str
    model: str = "gemini-2.0-flash"
    tokens_used: int = 0
