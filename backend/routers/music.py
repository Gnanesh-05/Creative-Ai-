from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from backend.database import get_db
from backend.core.dependencies import fun_get_current_user_token
from backend.schemas.auth import TokenData
from backend.schemas.music import (
    MusicGenRequest, EnhanceMusicPromptRequest, EnhanceMusicPromptResponse,
    MusicTrackResponse, MusicJobResponse
)
from backend.schemas.common import StandardResponse
from backend.services.music_service import MusicService

router = APIRouter()

@router.post("/generate", response_model=StandardResponse[MusicTrackResponse])
async def fun_generate_music(
    req: MusicGenRequest,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    svc = MusicService(db)
    user_id = user_token.user_id or "user_101"
    res = await svc.fun_generate_music(req, user_id=user_id)
    return StandardResponse(success=True, data=res)

@router.post("/enhance-prompt", response_model=StandardResponse[EnhanceMusicPromptResponse])
async def fun_enhance_prompt(
    req: EnhanceMusicPromptRequest,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    svc = MusicService(db)
    res = await svc.fun_enhance_prompt(req)
    return StandardResponse(success=True, data=res)

@router.post("/jobs", response_model=StandardResponse[MusicJobResponse])
async def fun_create_music_job(
    req: MusicGenRequest,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    svc = MusicService(db)
    user_id = user_token.user_id or "user_101"
    job = await svc.fun_create_generation_job(req, user_id=user_id)
    return StandardResponse(success=True, data=job, message="Music generation job started")

@router.get("/jobs/{job_id}", response_model=StandardResponse[MusicJobResponse])
async def fun_get_music_job_status(
    job_id: str,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    svc = MusicService(db)
    user_id = user_token.user_id or "user_101"
    job = await svc.fun_get_job_status(job_id, user_id=user_id)
    return StandardResponse(success=True, data=job)

@router.delete("/jobs/{job_id}", response_model=StandardResponse[bool])
async def fun_cancel_music_job(
    job_id: str,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    svc = MusicService(db)
    user_id = user_token.user_id or "user_101"
    success = await svc.fun_cancel_job(job_id, user_id=user_id)
    return StandardResponse(success=True, data=success, message="Job cancellation requested")

@router.get("/history", response_model=StandardResponse[List[MusicTrackResponse]])
async def fun_get_music_history(
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    svc = MusicService(db)
    user_id = user_token.user_id or "user_101"
    history = await svc.fun_get_history(user_id=user_id)
    return StandardResponse(success=True, data=history)

@router.delete("/{track_id}", response_model=StandardResponse[bool])
async def fun_delete_track(
    track_id: str,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    svc = MusicService(db)
    user_id = user_token.user_id or "user_101"
    success = await svc.fun_delete_track(track_id, user_id=user_id)
    if not success:
        raise HTTPException(status_code=404, detail="Track not found or unauthorized")
    return StandardResponse(success=True, data=True, message="Track deleted")

@router.post("/{track_id}/save", response_model=StandardResponse[bool])
async def fun_toggle_save_track(
    track_id: str,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    svc = MusicService(db)
    user_id = user_token.user_id or "user_101"
    saved = await svc.fun_toggle_save_track(track_id, user_id=user_id)
    return StandardResponse(success=True, data=saved, message="Bookmark updated")
