import asyncio
from backend.providers.music_provider import RealMusicProvider
from backend.schemas.music import MusicGenRequest, EnhanceMusicPromptRequest

async def test_music_prompt_enhancement():
    provider = RealMusicProvider()
    enhanced = await provider.fun_enhance_prompt(
        prompt="Emotional piano piece for a movie climax",
        genre="Cinematic",
        mood="Emotional"
    )
    assert enhanced is not None
    assert len(enhanced) > len("Emotional piano piece for a movie climax")

async def test_music_generation():
    provider = RealMusicProvider()
    res = await provider.fun_generate_music(
        prompt="Upbeat electronic track for a sci-fi racer",
        genre="Electronic",
        mood="Energetic",
        tempo_bpm=128,
        duration_seconds=30
    )
    assert res is not None
    assert "audio_url" in res
    assert "soundhelix.com" in res["audio_url"] or "http" in res["audio_url"]

if __name__ == "__main__":
    asyncio.run(test_music_prompt_enhancement())
    asyncio.run(test_music_generation())
    print("ALL MUSIC TESTS PASSED SUCCESSFULLY!")
