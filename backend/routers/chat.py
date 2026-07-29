from typing import List, Optional
from fastapi import APIRouter, Depends, HTTPException, Query, status
from fastapi.responses import StreamingResponse
from sqlalchemy.ext.asyncio import AsyncSession
from backend.database import get_db
from backend.core.dependencies import fun_get_current_user_token
from backend.schemas.auth import TokenData
from backend.schemas.chat import (
    ChatRequest, ChatResponse, ConversationDto, ConversationDetailDto,
    CreateConversationRequest, UpdateConversationRequest
)
from backend.schemas.common import StandardResponse
from backend.services.chat_service import ChatService

router = APIRouter()

@router.post("", response_model=StandardResponse[ChatResponse])
async def fun_chat(
    req: ChatRequest,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    chat_svc = ChatService(db)
    user_id = user_token.user_id or "user_101"
    res = await chat_svc.fun_process_chat(req, user_id=user_id)
    return StandardResponse(success=True, data=res)

@router.post("/stream")
async def fun_chat_stream(
    req: ChatRequest,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    chat_svc = ChatService(db)
    user_id = user_token.user_id or "user_101"
    
    async def event_generator():
        async for chunk in chat_svc.fun_stream_chat(req, user_id=user_id):
            yield f"{chunk}"

    return StreamingResponse(event_generator(), media_type="text/plain")

@router.get("/conversations", response_model=StandardResponse[List[ConversationDto]])
async def fun_get_conversations(
    q: Optional[str] = Query(None, description="Search query for filtering conversations"),
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    chat_svc = ChatService(db)
    user_id = user_token.user_id or "user_101"
    convs = await chat_svc.get_user_conversations(user_id=user_id, search=q)
    return StandardResponse(success=True, data=convs)

@router.post("/conversations", response_model=StandardResponse[ConversationDto])
async def fun_create_conversation(
    req: CreateConversationRequest,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    chat_svc = ChatService(db)
    user_id = user_token.user_id or "user_101"
    conv = await chat_svc.create_conversation(
        user_id=user_id,
        title=req.title or "New Conversation",
        system_instruction=req.system_instruction
    )
    return StandardResponse(success=True, data=conv, message="Conversation created successfully")

@router.get("/conversations/{conversation_id}", response_model=StandardResponse[ConversationDetailDto])
async def fun_get_conversation_detail(
    conversation_id: str,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    chat_svc = ChatService(db)
    user_id = user_token.user_id or "user_101"
    detail = await chat_svc.get_conversation_detail(conversation_id, user_id)
    if not detail:
        raise HTTPException(status_code=404, detail="Conversation not found")
    return StandardResponse(success=True, data=detail)

@router.put("/conversations/{conversation_id}", response_model=StandardResponse[ConversationDto])
async def fun_update_conversation(
    conversation_id: str,
    req: UpdateConversationRequest,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    chat_svc = ChatService(db)
    user_id = user_token.user_id or "user_101"
    conv = await chat_svc.update_conversation(conversation_id, user_id, req.title, req.system_instruction)
    if not conv:
        raise HTTPException(status_code=404, detail="Conversation not found or unauthorized")
    return StandardResponse(success=True, data=conv, message="Conversation updated")

@router.delete("/conversations/{conversation_id}", response_model=StandardResponse[bool])
async def fun_delete_conversation(
    conversation_id: str,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    chat_svc = ChatService(db)
    user_id = user_token.user_id or "user_101"
    success = await chat_svc.delete_conversation(conversation_id, user_id)
    if not success:
        raise HTTPException(status_code=404, detail="Conversation not found or unauthorized")
    return StandardResponse(success=True, data=True, message="Conversation deleted")

@router.delete("/conversations/{conversation_id}/messages", response_model=StandardResponse[bool])
async def fun_clear_conversation_messages(
    conversation_id: str,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    chat_svc = ChatService(db)
    user_id = user_token.user_id or "user_101"
    success = await chat_svc.clear_messages(conversation_id, user_id)
    if not success:
        raise HTTPException(status_code=404, detail="Conversation not found or unauthorized")
    return StandardResponse(success=True, data=True, message="Messages cleared")
