from typing import List, Optional
from pydantic import BaseModel

class ImageGenRequest(BaseModel):
    prompt: str
    enhancedPrompt: Optional[str] = None
    negativePrompt: Optional[str] = None
    aspectRatio: str = "1:1"
    stylePreset: str = "Photorealistic"
    resolution: str = "1024x1024"
    model: str = "imagen-3.0-generate-002"
    numImages: int = 1

class EnhancePromptRequest(BaseModel):
    prompt: str
    stylePreset: str = "Photorealistic"

class EnhancePromptResponse(BaseModel):
    originalPrompt: str
    enhancedPrompt: str

class ImageGenResponse(BaseModel):
    id: Optional[str] = None
    imageUrl: str
    prompt: str
    enhancedPrompt: Optional[str] = None
    negativePrompt: Optional[str] = None
    aspectRatio: str = "1:1"
    stylePreset: str = "Photorealistic"
    resolution: str = "1024x1024"
    model: str = "imagen-3.0-generate-002"
    storageReference: Optional[str] = None
    createdAt: Optional[str] = None

class ImageJobResponse(BaseModel):
    jobId: str
    status: str  # PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
    progress: int = 0
    errorMessage: Optional[str] = None
    results: List[ImageGenResponse] = []
