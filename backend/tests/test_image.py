import pytest
import asyncio
from backend.providers.image_provider import RealImageProvider
from backend.schemas.image import EnhancePromptRequest, ImageGenRequest

@pytest.mark.asyncio
async def test_prompt_validation():
    provider = RealImageProvider()
    assert provider.validate_prompt("A beautiful sunset over lake") == True
    assert provider.validate_prompt("A photo with nsfw blood content") == False

@pytest.mark.asyncio
async def test_prompt_enhancement():
    provider = RealImageProvider()
    enhanced = await provider.fun_enhance_prompt("A modern house beside a lake", style_preset="Photorealistic")
    assert enhanced is not None
    assert len(enhanced) > len("A modern house beside a lake")

@pytest.mark.asyncio
async def test_image_generation_fallback():
    provider = RealImageProvider()
    image_url = await provider.fun_generate_image(
        prompt="A professional product photograph of a luxury watch",
        style_preset="Product photography",
        aspect_ratio="1:1"
    )
    assert image_url is not None
    assert "http" in image_url or "data:image/" in image_url
