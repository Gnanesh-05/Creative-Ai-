import uuid
import asyncio
from typing import List, Optional
from sqlalchemy.ext.asyncio import AsyncSession
from backend.providers.music_provider import music_provider
from backend.providers.storage_provider import storage_provider
from backend.repositories.music_repository import MusicRepository
from backend.repositories.history_repository import HistoryRepository
from backend.models.domain import UnifiedHistoryItem
from backend.schemas.music import (
    MusicGenRequest, EnhanceMusicPromptRequest, EnhanceMusicPromptResponse,
    MusicTrackResponse, MusicJobResponse
)

class MusicService:
    def __init__(self, db: Optional[AsyncSession] = None):
        self.db = db

    async def fun_enhance_prompt(self, req: EnhanceMusicPromptRequest) -> EnhanceMusicPromptResponse:
        enhanced = await music_provider.fun_enhance_prompt(req.prompt, genre=req.genre, mood=req.mood)
        return EnhanceMusicPromptResponse(
            originalPrompt=req.prompt,
            enhancedPrompt=enhanced
        )

    async def fun_generate_music(self, req: MusicGenRequest, user_id: str = "user_101") -> MusicTrackResponse:
        raw_res = await music_provider.fun_generate_music(
            prompt=req.prompt,
            enhanced_prompt=req.enhancedPrompt,
            mood=req.mood,
            genre=req.genre,
            tempo_bpm=req.tempoBpm,
            duration_seconds=req.durationSeconds,
            key_signature=req.keySignature,
            instruments=req.instruments,
            energy_level=req.energyLevel,
            is_instrumental=req.isInstrumental,
            lyrics=req.lyrics,
            model=req.model
        )

        public_audio_url, storage_ref = await storage_provider.fun_store_audio_reference(raw_res["audio_url"])
        res_id = str(uuid.uuid4())

        if self.db:
            repo = MusicRepository(self.db)
            db_track = await repo.save_generated_track(
                user_id=user_id,
                prompt=req.prompt,
                audio_url=public_audio_url,
                enhanced_prompt=req.enhancedPrompt,
                mood=req.mood,
                genre=req.genre,
                tempo_bpm=req.tempoBpm,
                duration_seconds=req.durationSeconds,
                key_signature=req.keySignature,
                instruments=req.instruments,
                energy_level=req.energyLevel,
                is_instrumental=req.isInstrumental,
                lyrics=req.lyrics,
                model=req.model,
                audio_storage_reference=storage_ref,
                synthetic_notes=raw_res.get("synthetic_notes"),
                metadata_json={"waveform_peaks": raw_res.get("waveform_peaks", [])}
            )
            res_id = db_track.id

            hist_repo = HistoryRepository(self.db)
            hist_item = UnifiedHistoryItem(
                user_id=user_id,
                module_type="MUSIC",
                title=f"Music ({req.genre}): {req.prompt[:30]}",
                summary=f"Mood: {req.mood}, BPM: {req.tempoBpm}, Key: {req.keySignature}",
                payload={"track_id": res_id, "audio_url": public_audio_url, "prompt": req.prompt}
            )
            hist_repo.db.add(hist_item)
            await hist_repo.db.commit()

        return MusicTrackResponse(
            id=res_id,
            prompt=req.prompt,
            enhancedPrompt=req.enhancedPrompt,
            genre=req.genre,
            mood=req.mood,
            tempoBpm=req.tempoBpm,
            durationSeconds=req.durationSeconds,
            keySignature=req.keySignature,
            instruments=req.instruments,
            energyLevel=req.energyLevel,
            isInstrumental=req.isInstrumental,
            lyrics=req.lyrics,
            model=req.model,
            audioUrl=public_audio_url,
            audioStorageReference=storage_ref,
            syntheticNotes=raw_res.get("synthetic_notes")
        )

    async def fun_create_generation_job(self, req: MusicGenRequest, user_id: str = "user_101") -> MusicJobResponse:
        job_id = str(uuid.uuid4())

        if self.db:
            repo = MusicRepository(self.db)
            job = await repo.create_job(
                user_id=user_id,
                prompt=req.prompt,
                enhanced_prompt=req.enhancedPrompt,
                mood=req.mood,
                genre=req.genre,
                tempo_bpm=req.tempoBpm,
                duration_seconds=req.durationSeconds,
                key_signature=req.keySignature,
                instruments=req.instruments,
                energy_level=req.energyLevel,
                is_instrumental=req.isInstrumental,
                lyrics=req.lyrics,
                model=req.model
            )
            job_id = job.id

        asyncio.create_task(self._execute_async_job(job_id, req, user_id))

        return MusicJobResponse(
            jobId=job_id,
            status="PROCESSING",
            progress=20
        )

    async def _execute_async_job(self, job_id: str, req: MusicGenRequest, user_id: str):
        try:
            if self.db:
                repo = MusicRepository(self.db)
                await repo.update_job_status(job_id, "PROCESSING", progress=50)

            res = await self.fun_generate_music(req, user_id=user_id)

            if self.db:
                repo = MusicRepository(self.db)
                await repo.update_job_status(job_id, "COMPLETED", progress=100)
        except Exception as e:
            if self.db:
                repo = MusicRepository(self.db)
                await repo.update_job_status(job_id, "FAILED", progress=0, error_message=str(e))

    async def fun_get_job_status(self, job_id: str, user_id: str = "user_101") -> MusicJobResponse:
        if not self.db:
            return MusicJobResponse(jobId=job_id, status="COMPLETED", progress=100)

        repo = MusicRepository(self.db)
        job = await repo.get_job_by_id(job_id, user_id)
        if not job:
            return MusicJobResponse(jobId=job_id, status="FAILED", errorMessage="Job not found")

        results = []
        if job.status == "COMPLETED":
            tracks = await repo.get_user_tracks(user_id, limit=1)
            if tracks:
                t = tracks[0]
                results.append(
                    MusicTrackResponse(
                        id=t.id,
                        prompt=t.prompt,
                        enhancedPrompt=t.enhanced_prompt,
                        genre=t.genre,
                        mood=t.mood,
                        tempoBpm=t.tempo_bpm,
                        durationSeconds=t.duration_seconds,
                        keySignature=t.key_signature,
                        instruments=t.instruments,
                        energyLevel=t.energy_level,
                        isInstrumental=t.is_instrumental,
                        lyrics=t.lyrics,
                        model=t.model,
                        audioUrl=t.audio_url,
                        audioStorageReference=t.audio_storage_reference,
                        syntheticNotes=t.synthetic_notes,
                        isSaved=t.is_saved
                    )
                )

        return MusicJobResponse(
            jobId=job.id,
            status=job.status,
            progress=job.progress,
            errorMessage=job.error_message,
            results=results
        )

    async def fun_cancel_job(self, job_id: str, user_id: str = "user_101") -> bool:
        if self.db:
            repo = MusicRepository(self.db)
            job = await repo.get_job_by_id(job_id, user_id)
            if job and job.status in ["PENDING", "PROCESSING"]:
                await repo.update_job_status(job_id, "CANCELLED", progress=0)
                return True
        return True

    async def fun_get_history(self, user_id: str = "user_101") -> List[MusicTrackResponse]:
        if not self.db:
            return []
        repo = MusicRepository(self.db)
        tracks = await repo.get_user_tracks(user_id)
        return [
            MusicTrackResponse(
                id=t.id,
                prompt=t.prompt,
                enhancedPrompt=t.enhanced_prompt,
                genre=t.genre,
                mood=t.mood,
                tempoBpm=t.tempo_bpm,
                durationSeconds=t.duration_seconds,
                keySignature=t.key_signature,
                instruments=t.instruments,
                energyLevel=t.energy_level,
                isInstrumental=t.is_instrumental,
                lyrics=t.lyrics,
                model=t.model,
                audioUrl=t.audio_url,
                audioStorageReference=t.audio_storage_reference,
                syntheticNotes=t.synthetic_notes,
                createdAt=str(t.created_at),
                isSaved=t.is_saved
            ) for t in tracks
        ]

    async def fun_delete_track(self, track_id: str, user_id: str = "user_101") -> bool:
        if not self.db:
            return True
        repo = MusicRepository(self.db)
        return await repo.delete_track(track_id, user_id)

    async def fun_toggle_save_track(self, track_id: str, user_id: str = "user_101") -> bool:
        if not self.db:
            return True
        repo = MusicRepository(self.db)
        res = await repo.toggle_save_track(track_id, user_id)
        return res if res is not None else True
