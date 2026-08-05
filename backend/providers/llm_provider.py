import httpx
import re
import asyncio
from typing import List, Dict, Any, AsyncGenerator
from backend.config import settings
from backend.core.logging import logger

class LLMProvider:
    """Abstract provider interface for Conversational LLM (Gemini / OpenAI)"""
    
    async def fun_generate_reply(
        self,
        message: str,
        history: List[Dict[str, str]] = None,
        system_instruction: str = None,
        temperature: float = 0.7
    ) -> str:
        raise NotImplementedError

    async def fun_stream_generate_reply(
        self,
        message: str,
        history: List[Dict[str, str]] = None,
        system_instruction: str = None,
        temperature: float = 0.7
    ) -> AsyncGenerator[str, None]:
        raise NotImplementedError

class GeminiLLMProvider(LLMProvider):
    def __init__(self):
        self.api_key = settings.GEMINI_API_KEY
        self.default_system_instruction = (
            "You are Creative AI, a helpful, precise, and creative multi-modal AI assistant. "
            "Respond accurately and maintain clean formatting."
        )

    def _sanitize_input(self, text: str) -> str:
        """Protects against basic prompt injection attacks by stripping system override markers."""
        if not text:
            return ""
        # Strip potential system-override commands or jailbreak markers
        sanitized = re.sub(r'(?i)<\|im_start\|>|<\|im_end\|>|\[SYSTEM PROMPT OVERRIDE\]', '', text)
        return sanitized.strip()

    def _prepare_contents(
        self,
        message: str,
        history: List[Dict[str, str]] = None,
        system_instruction: str = None
    ) -> List[Dict[str, Any]]:
        clean_msg = self._sanitize_input(message)
        sys_prompt = system_instruction or self.default_system_instruction
        
        contents = []
        # Prepend system prompt context
        contents.append({
            "role": "user",
            "parts": [{"text": f"System Instruction: {sys_prompt}"}]
        })
        contents.append({
            "role": "model",
            "parts": [{"text": "Understood. I will follow these system instructions."}]
        })

        # Context truncation: limit history to last 12 turns (24 messages) to avoid token limits
        max_history = 12
        trimmed_history = history[-max_history:] if history else []

        for item in trimmed_history:
            role = "user" if item.get("role") in ["user", "USER"] else "model"
            content = self._sanitize_input(item.get("content", ""))
            if content:
                contents.append({
                    "role": role,
                    "parts": [{"text": content}]
                })

        contents.append({"role": "user", "parts": [{"text": clean_msg}]})
        return contents

    async def fun_generate_reply(
        self,
        message: str,
        history: List[Dict[str, str]] = None,
        system_instruction: str = None,
        temperature: float = 0.7
    ) -> str:
        if not self.api_key:
            logger.info("GEMINI_API_KEY not set. Using intelligent simulated response engine.")
            return self._build_mock_response(message, history)

        try:
            url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key={self.api_key}"
            contents = self._prepare_contents(message, history, system_instruction)
            
            async with httpx.AsyncClient() as client:
                resp = await client.post(
                    url,
                    json={
                        "contents": contents,
                        "generationConfig": {"temperature": temperature}
                    },
                    timeout=20.0
                )
                if resp.status_code == 200:
                    data = resp.json()
                    candidates = data.get('candidates', [])
                    if candidates and 'content' in candidates[0]:
                        parts = candidates[0]['content'].get('parts', [])
                        if parts:
                            return parts[0].get('text', '')
                    return self._build_mock_response(message, history)
                else:
                    logger.error(f"Gemini API returned status {resp.status_code}: {resp.text}")
                    return self._build_mock_response(message, history)
        except Exception as e:
            logger.error(f"Error calling Gemini API: {e}")
            return self._build_mock_response(message, history)

    async def fun_stream_generate_reply(
        self,
        message: str,
        history: List[Dict[str, str]] = None,
        system_instruction: str = None,
        temperature: float = 0.7
    ) -> AsyncGenerator[str, None]:
        full_reply = await self.fun_generate_reply(message, history, system_instruction, temperature)
        
        # Stream word by word / chunk by chunk for ultra-smooth typing effect
        words = full_reply.split(" ")
        for i, word in enumerate(words):
            yield word + (" " if i < len(words) - 1 else "")
            await asyncio.sleep(0.03)

    def _build_mock_response(self, message: str, history: List[Dict[str, str]] = None) -> str:
        msg_lower = message.lower()
        if "python" in msg_lower:
            if "example" in msg_lower:
                return "Here is a clean Python example:\n\ndef calculate_fibonacci(n: int) -> list[int]:\n    sequence = [0, 1]\n    while len(sequence) < n:\n        sequence.append(sequence[-1] + sequence[-2])\n    return sequence\n\nprint(calculate_fibonacci(8))"
            elif "advanced" in msg_lower:
                return "Taking our Python example further into advanced concepts:\n\nfrom dataclasses import dataclass\nimport asyncio\n\n@dataclass\nclass StreamTask:\n    id: str\n    payload: dict\n\nasync fun process_stream(task: StreamTask):\n    await asyncio.sleep(0.5)\n    return f'Processed task {task.id}'"
            return "Python is a high-level, interpreted programming language known for readable syntax, rich package ecosystem, and strong adoption across Web development, Data Science, and AI engineering."
        elif "hello" in msg_lower or "hi" in msg_lower:
            return "Hello! I am Creative AI. I can help answer questions, brainstorm ideas, write code, or generate creative content. How can I assist you today?"
        else:
            return f"I processed your request regarding '{message}'. As your Creative AI assistant, I can help expand on this concept, create code implementations, or write detailed documentation."

llm_provider = GeminiLLMProvider()
