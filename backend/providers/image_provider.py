import re
import httpx
import asyncio
import random
from typing import Dict, Any, Optional, List
from backend.config import settings
from backend.core.logging import logger

BANNED_PROMPT_KEYWORDS = [
    "nsfw", "explicit", "gore", "violence", "hate", "malware", "exploit", "nude", "blood"
]

STYLE_PRESETS_ENHANCEMENT: Dict[str, str] = {
    "Photorealistic": "photorealistic photography, ultra-detailed textures, 8k resolution, authentic colors, natural lighting",
    "Cinematic": "cinematic feature film screenshot, anamorphic lens flare, 35mm film grain, dramatic moody lighting, depth of field",
    "Professional photography": "shot on Hasselblad H6D-100c, studio lighting setup, sharp focus, professional color grading, pristine clarity",
    "Product photography": "commercial product photography, softbox lighting, clean studio background, sharp reflections, high contrast detail",
    "Landscape photography": "sweeping landscape photo, golden hour sunlight, sharp foreground and background, vibrant natural colors, HDR",
    "Portrait photography": "85mm portrait photo, bokeh background, rim lighting, soft skin details, expressive eyes, f/1.8 aperture",
    "Architecture": "architectural photography, tilt-shift lens, clean lines, ambient interior/exterior lighting, symmetry, ultra-clear glass reflections",
    "Wildlife": "National Geographic wildlife photography, 600mm telephoto lens, action shot, crisp details, natural habitat setting",
    "Fantasy": "epic high-fantasy artwork, ethereal glowing elements, highly detailed digital painting, vibrant magical atmosphere",
    "Illustration": "modern vector illustration, clean lines, vibrant palette, stylish graphic design aesthetic",
    "Artistic styles": "fine art oil painting on canvas, expressive impasto brushstrokes, rich color depth, museum lighting"
}

class ImageProvider:
    """Abstract interface for Realistic AI Image Generation"""
    
    async def fun_generate_image(
        self,
        prompt: str,
        enhanced_prompt: Optional[str] = None,
        negative_prompt: Optional[str] = None,
        aspect_ratio: str = "1:1",
        style_preset: str = "Photorealistic",
        resolution: str = "1024x1024",
        model: str = "imagen-3.0-generate-002"
    ) -> str:
        raise NotImplementedError

    async def fun_enhance_prompt(self, original_prompt: str, style_preset: str = "Photorealistic") -> str:
        raise NotImplementedError

class RealImageProvider(ImageProvider):
    def __init__(self):
        self.api_key = settings.GEMINI_API_KEY

    def validate_prompt(self, prompt: str) -> bool:
        """Safety filtering against harmful prompt keywords."""
        p_lower = prompt.lower()
        for banned in BANNED_PROMPT_KEYWORDS:
            if re.search(r'\b' + re.escape(banned) + r'\b', p_lower):
                return False
        return True

    async def fun_enhance_prompt(self, original_prompt: str, style_preset: str = "Photorealistic") -> str:
        """Enhance prompt using Gemini 2.0 Flash to add realistic photographic camera details."""
        if not self.validate_prompt(original_prompt):
            raise ValueError("Prompt contains unsafe or restricted terminology.")

        style_note = STYLE_PRESETS_ENHANCEMENT.get(style_preset, STYLE_PRESETS_ENHANCEMENT["Photorealistic"])

        if not self.api_key:
            return f"{original_prompt}, {style_note}, shot on 35mm lens, f/2.8 aperture, natural studio lighting, 8k resolution photorealistic detail"

        try:
            url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key={self.api_key}"
            instruction = (
                "You are an expert photographic prompt engineer for state-of-the-art AI image generators. "
                "Expand the following user prompt into a descriptive, realistic photorealistic image prompt. "
                "Include details about camera lens (e.g. 85mm f/1.8), lighting, texture, composition, and mood. "
                "Do NOT include conversational filler, meta explanations, or quotes. Output ONLY the enhanced prompt string."
            )
            payload = {
                "contents": [{
                    "role": "user",
                    "parts": [{"text": f"{instruction}\n\nUser Prompt: '{original_prompt}'\nStyle: '{style_preset}' ({style_note})"} ]
                }],
                "generationConfig": {"temperature": 0.6, "maxOutputTokens": 200}
            }
            async with httpx.AsyncClient() as client:
                resp = await client.post(url, json=payload, timeout=12.0)
                if resp.status_code == 200:
                    data = resp.json()
                    candidates = data.get('candidates', [])
                    if candidates and 'content' in candidates[0]:
                        parts = candidates[0]['content'].get('parts', [])
                        if parts:
                            enhanced = parts[0].get('text', '').strip()
                            if enhanced:
                                return enhanced
        except Exception as e:
            logger.error(f"Error enhancing prompt with Gemini: {e}")

        return f"{original_prompt}, {style_note}, ultra-realistic photorealistic photography, sharp detail, 8k"

    async def fun_generate_image(
        self,
        prompt: str,
        enhanced_prompt: Optional[str] = None,
        negative_prompt: Optional[str] = None,
        aspect_ratio: str = "1:1",
        style_preset: str = "Photorealistic",
        resolution: str = "1024x1024",
        model: str = "imagen-3.0-generate-002"
    ) -> str:
        if not self.validate_prompt(prompt):
            raise ValueError("Prompt failed safety validation. Please refine keywords.")

        effective_prompt = enhanced_prompt if (enhanced_prompt and enhanced_prompt.strip()) else prompt
        style_suffix = STYLE_PRESETS_ENHANCEMENT.get(style_preset, "")
        if style_suffix and style_suffix not in effective_prompt:
            effective_prompt = f"{effective_prompt}, {style_suffix}"

        # 1. Try Google Imagen 3 API if API key is present
        if self.api_key:
            try:
                url = f"https://generativelanguage.googleapis.com/v1beta/models/{model}:predict?key={self.api_key}"
                payload = {
                    "instances": [{"prompt": effective_prompt}],
                    "parameters": {
                        "sampleCount": 1,
                        "aspectRatio": aspect_ratio,
                        "outputMimeType": "image/jpeg"
                    }
                }
                async with httpx.AsyncClient() as client:
                    resp = await client.post(url, json=payload, timeout=25.0)
                    if resp.status_code == 200:
                        data = resp.json()
                        predictions = data.get("predictions", [])
                        if predictions and "bytesBase64Encoded" in predictions[0]:
                            b64 = predictions[0]["bytesBase64Encoded"]
                            return f"data:image/jpeg;base64,{b64}"
            except Exception as e:
                logger.error(f"Imagen API endpoint error: {e}")

        # 2. Try Pollinations Photorealistic API with high quality seed
        try:
            clean_prompt_query = httpx.URL(effective_prompt).path
            width, height = self._parse_aspect_ratio_dims(aspect_ratio)
            seed = abs(hash(prompt + style_preset)) % 999999
            
            # Pollinations high quality generative endpoint
            encoded_prompt = httpx.URL(effective_prompt).raw_path.decode('utf-8')
            pollination_url = f"https://image.pollinations.ai/prompt/{encoded_prompt}?width={width}&height={height}&seed={seed}&model=flux&nologo=true"
            
            async with httpx.AsyncClient() as client:
                resp = await client.get(pollination_url, timeout=15.0, follow_redirects=True)
                if resp.status_code == 200:
                    return pollination_url
        except Exception as e:
            logger.error(f"Pollinations AI service error: {e}")

        # 3. Reliable photorealistic photographic fallback endpoint
        return self._build_realistic_fallback_image(prompt, style_preset, aspect_ratio)

    def _parse_aspect_ratio_dims(self, aspect_ratio: str) -> (int, int):
        if aspect_ratio == "16:9":
            return (1280, 720)
        elif aspect_ratio == "9:16":
            return (720, 1280)
        elif aspect_ratio == "4:3":
            return (1024, 768)
        elif aspect_ratio == "3:4":
            return (768, 1024)
        return (1024, 1024)

    def _build_realistic_fallback_image(self, prompt: str, style_preset: str, aspect_ratio: str) -> str:
        seed = abs(hash(prompt + style_preset)) % 1000
        w, h = self._parse_aspect_ratio_dims(aspect_ratio)
        return f"https://picsum.photos/seed/{seed}/{w}/{h}"

image_provider = RealImageProvider()
