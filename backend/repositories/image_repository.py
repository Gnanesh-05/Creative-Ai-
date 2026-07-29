from typing import List, Optional
from sqlalchemy.future import select
from sqlalchemy.ext.asyncio import AsyncSession
from backend.models.domain import ImageGeneration, ImageGenerationJob
from backend.repositories.base import BaseRepository

class ImageRepository(BaseRepository[ImageGeneration]):
    def __init__(self, db: AsyncSession):
        super().__init__(ImageGeneration, db)

    async def create_job(
        self,
        user_id: str,
        prompt: str,
        enhanced_prompt: Optional[str] = None,
        negative_prompt: Optional[str] = None,
        model: str = "imagen-3.0-generate-002",
        style_preset: str = "Photorealistic",
        aspect_ratio: str = "1:1",
        resolution: str = "1024x1024",
        num_images: int = 1
    ) -> ImageGenerationJob:
        job = ImageGenerationJob(
            user_id=user_id,
            prompt=prompt,
            enhanced_prompt=enhanced_prompt,
            negative_prompt=negative_prompt,
            model=model,
            style_preset=style_preset,
            aspect_ratio=aspect_ratio,
            resolution=resolution,
            num_images=num_images,
            status="PENDING",
            progress=0
        )
        self.db.add(job)
        await self.db.commit()
        await self.db.refresh(job)
        return job

    async def get_job_by_id(self, job_id: str, user_id: str) -> Optional[ImageGenerationJob]:
        stmt = select(ImageGenerationJob).where(
            ImageGenerationJob.id == job_id,
            ImageGenerationJob.user_id == user_id
        )
        result = await self.db.execute(stmt)
        return result.scalars().first()

    async def update_job_status(
        self,
        job_id: str,
        status: str,
        progress: int = 0,
        error_message: Optional[str] = None
    ) -> Optional[ImageGenerationJob]:
        stmt = select(ImageGenerationJob).where(ImageGenerationJob.id == job_id)
        result = await self.db.execute(stmt)
        job = result.scalars().first()
        if job:
            job.status = status
            job.progress = progress
            if error_message:
                job.error_message = error_message
            await self.db.commit()
            await self.db.refresh(job)
        return job

    async def save_generated_image(
        self,
        user_id: str,
        prompt: str,
        image_url: str,
        enhanced_prompt: Optional[str] = None,
        negative_prompt: Optional[str] = None,
        model: str = "imagen-3.0-generate-002",
        style_preset: str = "Photorealistic",
        aspect_ratio: str = "1:1",
        resolution: str = "1024x1024",
        storage_reference: Optional[str] = None
    ) -> ImageGeneration:
        gen = ImageGeneration(
            user_id=user_id,
            prompt=prompt,
            enhanced_prompt=enhanced_prompt,
            negative_prompt=negative_prompt,
            model=model,
            style_preset=style_preset,
            aspect_ratio=aspect_ratio,
            resolution=resolution,
            generation_status="COMPLETED",
            image_url=image_url,
            storage_reference=storage_reference
        )
        self.db.add(gen)
        await self.db.commit()
        await self.db.refresh(gen)
        return gen

    async def get_user_images(self, user_id: str, limit: int = 50) -> List[ImageGeneration]:
        stmt = (
            select(ImageGeneration)
            .where(ImageGeneration.user_id == user_id, ImageGeneration.is_deleted == False)
            .order_by(ImageGeneration.created_at.desc())
            .limit(limit)
        )
        result = await self.db.execute(stmt)
        return result.scalars().all()

    async def get_image_by_id(self, image_id: str, user_id: str) -> Optional[ImageGeneration]:
        stmt = select(ImageGeneration).where(
            ImageGeneration.id == image_id,
            ImageGeneration.user_id == user_id,
            ImageGeneration.is_deleted == False
        )
        result = await self.db.execute(stmt)
        return result.scalars().first()

    async def delete_image(self, image_id: str, user_id: str) -> bool:
        img = await self.get_image_by_id(image_id, user_id)
        if not img:
            return False
        img.is_deleted = True
        await self.db.commit()
        return True
