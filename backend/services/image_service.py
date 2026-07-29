import uuid
import asyncio
from typing import List, Optional
from sqlalchemy.ext.asyncio import AsyncSession
from backend.providers.image_provider import image_provider
from backend.providers.storage_provider import storage_provider
from backend.repositories.image_repository import ImageRepository
from backend.repositories.history_repository import HistoryRepository
from backend.models.domain import UnifiedHistoryItem
from backend.schemas.image import (
    ImageGenRequest, ImageGenResponse, EnhancePromptRequest, EnhancePromptResponse, ImageJobResponse
)

class ImageService:
    def __init__(self, db: Optional[AsyncSession] = None):
        self.db = db

    async def fun_enhance_prompt(self, req: EnhancePromptRequest) -> EnhancePromptResponse:
        enhanced = await image_provider.fun_enhance_prompt(req.prompt, style_preset=req.stylePreset)
        return EnhancePromptResponse(
            originalPrompt=req.prompt,
            enhancedPrompt=enhanced
        )

    async def fun_generate_image(self, req: ImageGenRequest, user_id: str = "user_101") -> ImageGenResponse:
        # Generate image using provider
        raw_image_url = await image_provider.fun_generate_image(
            prompt=req.prompt,
            enhanced_prompt=req.enhancedPrompt,
            negative_prompt=req.negativePrompt,
            aspect_ratio=req.aspectRatio,
            style_preset=req.stylePreset,
            resolution=req.resolution,
            model=req.model
        )

        # Store image via storage provider abstraction
        public_url, storage_ref = await storage_provider.fun_store_image_reference(raw_image_url)

        res_id = str(uuid.uuid4())

        # Persist image metadata & unified history if DB session is available
        if self.db:
            repo = ImageRepository(self.db)
            db_img = await repo.save_generated_image(
                user_id=user_id,
                prompt=req.prompt,
                image_url=public_url,
                enhanced_prompt=req.enhancedPrompt,
                negative_prompt=req.negativePrompt,
                model=req.model,
                style_preset=req.stylePreset,
                aspect_ratio=req.aspectRatio,
                resolution=req.resolution,
                storage_reference=storage_ref
            )
            res_id = db_img.id

            hist_repo = HistoryRepository(self.db)
            hist_item = UnifiedHistoryItem(
                user_id=user_id,
                module_type="IMAGE",
                title=f"Image ({req.stylePreset}): {req.prompt[:30]}",
                summary=f"Style: {req.stylePreset}, Aspect: {req.aspectRatio}",
                payload={"image_id": res_id, "image_url": public_url, "prompt": req.prompt}
            )
            hist_repo.db.add(hist_item)
            await hist_repo.db.commit()

        return ImageGenResponse(
            id=res_id,
            imageUrl=public_url,
            prompt=req.prompt,
            enhancedPrompt=req.enhancedPrompt,
            negativePrompt=req.negativePrompt,
            aspectRatio=req.aspectRatio,
            stylePreset=req.stylePreset,
            resolution=req.resolution,
            model=req.model,
            storageReference=storage_ref
        )

    async def fun_create_generation_job(self, req: ImageGenRequest, user_id: str = "user_101") -> ImageJobResponse:
        job_id = str(uuid.uuid4())
        
        if self.db:
            repo = ImageRepository(self.db)
            job = await repo.create_job(
                user_id=user_id,
                prompt=req.prompt,
                enhanced_prompt=req.enhancedPrompt,
                negative_prompt=req.negativePrompt,
                model=req.model,
                style_preset=req.stylePreset,
                aspect_ratio=req.aspectRatio,
                resolution=req.resolution,
                num_images=req.numImages
            )
            job_id = job.id

        # Launch async generation in background task simulation
        asyncio.create_task(self._execute_async_job(job_id, req, user_id))

        return ImageJobResponse(
            jobId=job_id,
            status="PROCESSING",
            progress=25
        )

    async def _execute_async_job(self, job_id: str, req: ImageGenRequest, user_id: str):
        try:
            if self.db:
                repo = ImageRepository(self.db)
                await repo.update_job_status(job_id, "PROCESSING", progress=50)

            # Generate result image
            res = await self.fun_generate_image(req, user_id=user_id)

            if self.db:
                repo = ImageRepository(self.db)
                await repo.update_job_status(job_id, "COMPLETED", progress=100)
        except Exception as e:
            if self.db:
                repo = ImageRepository(self.db)
                await repo.update_job_status(job_id, "FAILED", progress=0, error_message=str(e))

    async def fun_get_job_status(self, job_id: str, user_id: str = "user_101") -> ImageJobResponse:
        if not self.db:
            return ImageJobResponse(jobId=job_id, status="COMPLETED", progress=100)

        repo = ImageRepository(self.db)
        job = await repo.get_job_by_id(job_id, user_id)
        if not job:
            return ImageJobResponse(jobId=job_id, status="FAILED", errorMessage="Job not found")

        results = []
        if job.status == "COMPLETED":
            # Fetch latest image for user
            imgs = await repo.get_user_images(user_id, limit=1)
            if imgs:
                img = imgs[0]
                results.append(
                    ImageGenResponse(
                        id=img.id,
                        imageUrl=img.image_url,
                        prompt=img.prompt,
                        enhancedPrompt=img.enhanced_prompt,
                        negativePrompt=img.negative_prompt,
                        aspectRatio=img.aspect_ratio,
                        stylePreset=img.style_preset,
                        resolution=img.resolution,
                        model=img.model
                    )
                )

        return ImageJobResponse(
            jobId=job.id,
            status=job.status,
            progress=job.progress,
            errorMessage=job.error_message,
            results=results
        )

    async def fun_cancel_job(self, job_id: str, user_id: str = "user_101") -> bool:
        if self.db:
            repo = ImageRepository(self.db)
            job = await repo.get_job_by_id(job_id, user_id)
            if job and job.status in ["PENDING", "PROCESSING"]:
                await repo.update_job_status(job_id, "CANCELLED", progress=0)
                return True
        return True

    async def fun_get_history(self, user_id: str = "user_101") -> List[ImageGenResponse]:
        if not self.db:
            return []
        repo = ImageRepository(self.db)
        imgs = await repo.get_user_images(user_id)
        return [
            ImageGenResponse(
                id=i.id,
                imageUrl=i.image_url,
                prompt=i.prompt,
                enhancedPrompt=i.enhanced_prompt,
                negativePrompt=i.negative_prompt,
                aspectRatio=i.aspect_ratio,
                stylePreset=i.style_preset,
                resolution=i.resolution,
                model=i.model,
                createdAt=str(i.created_at)
            ) for i in imgs
        ]

    async def fun_delete_image(self, image_id: str, user_id: str = "user_101") -> bool:
        if not self.db:
            return True
        repo = ImageRepository(self.db)
        return await repo.delete_image(image_id, user_id)
