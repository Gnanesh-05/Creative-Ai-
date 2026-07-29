from typing import List
from fastapi import APIRouter, Depends, HTTPException, status
from sqlalchemy.ext.asyncio import AsyncSession
from backend.database import get_db
from backend.core.dependencies import fun_get_current_user_token
from backend.schemas.auth import TokenData
from backend.schemas.image import (
    ImageGenRequest, ImageGenResponse, EnhancePromptRequest, EnhancePromptResponse, ImageJobResponse
)
from backend.schemas.common import StandardResponse
from backend.services.image_service import ImageService

router = APIRouter()

@router.post("/generate", response_model=StandardResponse[ImageGenResponse])
async def fun_generate_image(
    req: ImageGenRequest,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    image_svc = ImageService(db)
    user_id = user_token.user_id or "user_101"
    res = await image_svc.fun_generate_image(req, user_id=user_id)
    return StandardResponse(success=True, data=res)

@router.post("/enhance-prompt", response_model=StandardResponse[EnhancePromptResponse])
async def fun_enhance_prompt(
    req: EnhancePromptRequest,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    image_svc = ImageService(db)
    res = await image_svc.fun_enhance_prompt(req)
    return StandardResponse(success=True, data=res)

@router.post("/jobs", response_model=StandardResponse[ImageJobResponse])
async def fun_create_image_job(
    req: ImageGenRequest,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    image_svc = ImageService(db)
    user_id = user_token.user_id or "user_101"
    job = await image_svc.fun_create_generation_job(req, user_id=user_id)
    return StandardResponse(success=True, data=job, message="Image generation job started")

@router.get("/jobs/{job_id}", response_model=StandardResponse[ImageJobResponse])
async def fun_get_image_job_status(
    job_id: str,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    image_svc = ImageService(db)
    user_id = user_token.user_id or "user_101"
    job = await image_svc.fun_get_job_status(job_id, user_id=user_id)
    return StandardResponse(success=True, data=job)

@router.delete("/jobs/{job_id}", response_model=StandardResponse[bool])
async def fun_cancel_image_job(
    job_id: str,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    image_svc = ImageService(db)
    user_id = user_token.user_id or "user_101"
    success = await image_svc.fun_cancel_job(job_id, user_id=user_id)
    return StandardResponse(success=True, data=success, message="Job cancellation requested")

@router.get("/history", response_model=StandardResponse[List[ImageGenResponse]])
async def fun_get_image_history(
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    image_svc = ImageService(db)
    user_id = user_token.user_id or "user_101"
    history = await image_svc.fun_get_history(user_id=user_id)
    return StandardResponse(success=True, data=history)

@router.delete("/{image_id}", response_model=StandardResponse[bool])
async def fun_delete_image(
    image_id: str,
    user_token: TokenData = Depends(fun_get_current_user_token),
    db: AsyncSession = Depends(get_db)
):
    image_svc = ImageService(db)
    user_id = user_token.user_id or "user_101"
    success = await image_svc.fun_delete_image(image_id, user_id=user_id)
    if not success:
        raise HTTPException(status_code=404, detail="Image not found or unauthorized")
    return StandardResponse(success=True, data=True, message="Image deleted")
