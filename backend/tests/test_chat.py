import pytest
import asyncio
from backend.providers.llm_provider import GeminiLLMProvider
from backend.schemas.chat import ChatRequest

@pytest.mark.asyncio
async def test_llm_provider_generation():
    provider = GeminiLLMProvider()
    response = await provider.fun_generate_reply("Tell me about Python.")
    assert response is not None
    assert len(response) > 0

@pytest.mark.asyncio
async def test_llm_provider_follow_up():
    provider = GeminiLLMProvider()
    history = [{"role": "user", "content": "Tell me about Python."}, {"role": "model", "content": "Python is a programming language."}]
    response = await provider.fun_generate_reply("Give me a simple example.", history=history)
    assert response is not None
    assert len(response) > 0

@pytest.mark.asyncio
async def test_llm_provider_streaming():
    provider = GeminiLLMProvider()
    chunks = []
    async for chunk in provider.fun_stream_generate_reply("Hello"):
        chunks.append(chunk)
    assert len(chunks) > 0
    full_text = "".join(chunks)
    assert len(full_text) > 0
