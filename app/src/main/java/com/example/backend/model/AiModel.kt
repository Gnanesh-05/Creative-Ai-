package com.example.backend.model

data class AiModelInfo(
    val id: String,
    val name: String,
    val provider: String,
    val description: String,
    val isLiveApiAvailable: Boolean = true,
    val tag: String = "FAST"
)

object AiModelRegistry {
    val GEMINI_3_5_FLASH = AiModelInfo(
        id = "gemini-3.5-flash",
        name = "Nexus Core 3.5",
        provider = "Nexus AI Engine",
        description = "Ultra-fast streaming and human-like natural conversations",
        isLiveApiAvailable = true,
        tag = "DEFAULT"
    )
    
    val GEMINI_3_1_PRO = AiModelInfo(
        id = "gemini-3.1-pro-preview",
        name = "Nexus Pro Neural",
        provider = "Nexus AI Engine",
        description = "Advanced reasoning, coding, science, and multi-turn logic",
        isLiveApiAvailable = true,
        tag = "REASONING"
    )

    val GEMINI_2_5_FLASH_IMAGE = AiModelInfo(
        id = "gemini-2.5-flash-image",
        name = "Nexus Image Studio",
        provider = "Nexus AI Engine",
        description = "Native image understanding and generative art synthesis",
        isLiveApiAvailable = true,
        tag = "VISION"
    )

    val CLAUDE_3_5_SONNET = AiModelInfo(
        id = "claude-3-5-sonnet",
        name = "Nexus Code Engine",
        provider = "Nexus AI Engine",
        description = "Pristine code generation, system design, and prose",
        isLiveApiAvailable = false,
        tag = "ROUTER"
    )

    val GPT_4O = AiModelInfo(
        id = "gpt-4o",
        name = "Nexus Ultra Multimodal",
        provider = "Nexus AI Engine",
        description = "Versatile multimodal model with broad knowledge",
        isLiveApiAvailable = false,
        tag = "ROUTER"
    )

    val DEEPSEEK_R1 = AiModelInfo(
        id = "deepseek-r1",
        name = "Nexus Math & Logic",
        provider = "Nexus AI Engine",
        description = "Open reasoning model for math, algorithms, and logic",
        isLiveApiAvailable = false,
        tag = "MATH"
    )

    val LLAMA_3_3 = AiModelInfo(
        id = "llama-3.3-70b",
        name = "Nexus Open Weights",
        provider = "Nexus AI Engine",
        description = "High-performance open weights model",
        isLiveApiAvailable = false,
        tag = "OPEN"
    )

    val OLLAMA_LOCAL = AiModelInfo(
        id = "ollama-local",
        name = "Nexus Offline NPU",
        provider = "Local Device",
        description = "100% Offline execution on local NPU/GPU hardware",
        isLiveApiAvailable = false,
        tag = "OFFLINE"
    )

    val allModels = listOf(
        GEMINI_3_5_FLASH,
        GEMINI_3_1_PRO,
        GEMINI_2_5_FLASH_IMAGE,
        CLAUDE_3_5_SONNET,
        GPT_4O,
        DEEPSEEK_R1,
        LLAMA_3_3,
        OLLAMA_LOCAL
    )
}
