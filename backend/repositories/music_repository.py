from typing import List, Optional
from sqlalchemy.future import select
from sqlalchemy.ext.asyncio import AsyncSession
from backend.models.domain import MusicGeneration, MusicGenerationJob
from backend.repositories.base import BaseRepository

class MusicRepository(BaseRepository[MusicGeneration]):
    def __init__(self, db: AsyncSession):
        super().__init__(MusicGeneration, db)

    async def create_job(
        self,
        user_id: str,
        prompt: str,
        enhanced_prompt: Optional[str] = None,
        mood: str = "Relaxing",
        genre: str = "Lo-Fi Beats",
        tempo_bpm: int = 90,
        duration_seconds: int = 30,
        key_signature: str = "C Major",
        instruments: str = "Piano, Strings",
        energy_level: str = "Medium",
        is_instrumental: bool = True,
        lyrics: Optional[str] = None,
        model: str = "musicgen-stereo-large"
    ) -> MusicGenerationJob:
        job = MusicGenerationJob(
            user_id=user_id,
            prompt=prompt,
            enhanced_prompt=enhanced_prompt,
            mood=mood,
            genre=genre,
            tempo_bpm=tempo_bpm,
            duration_seconds=duration_seconds,
            key_signature=key_signature,
            instruments=instruments,
            energy_level=energy_level,
            is_instrumental=is_instrumental,
            lyrics=lyrics,
            model=model,
            status="PENDING",
            progress=0
        )
        self.db.add(job)
        await self.db.commit()
        await self.db.refresh(job)
        return job

    async def get_job_by_id(self, job_id: str, user_id: str) -> Optional[MusicGenerationJob]:
        stmt = select(MusicGenerationJob).where(
            MusicGenerationJob.id == job_id,
            MusicGenerationJob.user_id == user_id
        )
        result = await self.db.execute(stmt)
        return result.scalars().first()

    async def update_job_status(
        self,
        job_id: str,
        status: str,
        progress: int = 0,
        error_message: Optional[str] = None
    ) -> Optional[MusicGenerationJob]:
        stmt = select(MusicGenerationJob).where(MusicGenerationJob.id == job_id)
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

    async def save_generated_track(
        self,
        user_id: str,
        prompt: str,
        audio_url: str,
        enhanced_prompt: Optional[str] = None,
        mood: str = "Relaxing",
        genre: str = "Lo-Fi Beats",
        tempo_bpm: int = 90,
        duration_seconds: int = 30,
        key_signature: str = "C Major",
        instruments: str = "Piano, Strings",
        energy_level: str = "Medium",
        is_instrumental: bool = True,
        lyrics: Optional[str] = None,
        model: str = "musicgen-stereo-large",
        audio_storage_reference: Optional[str] = None,
        synthetic_notes: Optional[str] = None,
        metadata_json: Optional[dict] = None
    ) -> MusicGeneration:
        track = MusicGeneration(
            user_id=user_id,
            prompt=prompt,
            enhanced_prompt=enhanced_prompt,
            mood=mood,
            genre=genre,
            tempo_bpm=tempo_bpm,
            duration_seconds=duration_seconds,
            key_signature=key_signature,
            instruments=instruments,
            energy_level=energy_level,
            is_instrumental=is_instrumental,
            lyrics=lyrics,
            model=model,
            generation_status="COMPLETED",
            audio_url=audio_url,
            audio_storage_reference=audio_storage_reference,
            synthetic_notes=synthetic_notes,
            metadata_json=metadata_json or {}
        )
        self.db.add(track)
        await self.db.commit()
        await self.db.refresh(track)
        return track

    async def get_user_tracks(self, user_id: str, limit: int = 50) -> List[MusicGeneration]:
        stmt = (
            select(MusicGeneration)
            .where(MusicGeneration.user_id == user_id, MusicGeneration.is_deleted == False)
            .order_by(MusicGeneration.created_at.desc())
            .limit(limit)
        )
        result = await self.db.execute(stmt)
        return result.scalars().all()

    async def get_track_by_id(self, track_id: str, user_id: str) -> Optional[MusicGeneration]:
        stmt = select(MusicGeneration).where(
            MusicGeneration.id == track_id,
            MusicGeneration.user_id == user_id,
            MusicGeneration.is_deleted == False
        )
        result = await self.db.execute(stmt)
        return result.scalars().first()

    async def delete_track(self, track_id: str, user_id: str) -> bool:
        track = await self.get_track_by_id(track_id, user_id)
        if not track:
            return False
        track.is_deleted = True
        await self.db.commit()
        return True

    async def toggle_save_track(self, track_id: str, user_id: str) -> Optional[bool]:
        track = await self.get_track_by_id(track_id, user_id)
        if not track:
            return None
        track.is_saved = not track.is_saved
        await self.db.commit()
        return track.is_saved
