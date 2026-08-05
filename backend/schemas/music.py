from typing import List, Optional
from pydantic import BaseModel

class MusicGenRequest(BaseModel):
    prompt: str
    enhancedPrompt: Optional[str] = None
    genre: str = "Lo-Fi Beats"
    mood: str = "Relaxing"
    tempoBpm: int = 90
    durationSeconds: int = 30
    keySignature: str = "C Major"
    instruments: str = "Piano, Strings"
    energyLevel: str = "Medium"
    isInstrumental: bool = True
    lyrics: Optional[str] = None
    model: str = "musicgen-stereo-large"

class EnhanceMusicPromptRequest(BaseModel):
    prompt: str
    genre: str = "Lo-Fi Beats"
    mood: str = "Relaxing"

class EnhanceMusicPromptResponse(BaseModel):
    originalPrompt: str
    enhancedPrompt: str

class MusicTrackResponse(BaseModel):
    id: Optional[str] = None
    prompt: str
    enhancedPrompt: Optional[str] = None
    genre: str = "Lo-Fi Beats"
    mood: str = "Relaxing"
    tempoBpm: int = 90
    durationSeconds: int = 30
    keySignature: str = "C Major"
    instruments: str = "Piano, Strings"
    energyLevel: str = "Medium"
    isInstrumental: bool = True
    lyrics: Optional[str] = None
    model: str = "musicgen-stereo-large"
    audioUrl: str
    audioStorageReference: Optional[str] = None
    syntheticNotes: Optional[str] = None
    createdAt: Optional[str] = None
    isSaved: bool = False

class MusicJobResponse(BaseModel):
    jobId: str
    status: str  # PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
    progress: int = 0
    errorMessage: Optional[str] = None
    results: List[MusicTrackResponse] = []
