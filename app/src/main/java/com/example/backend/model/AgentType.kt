package com.example.backend.model

enum class AgentType(
    val title: String,
    val description: String,
    val icon: String,
    val capability: String
) {
    PLANNER("Planner Agent", "Breaks down goals into structured steps", "🧠", "Task Decomposition"),
    REASONING("Reasoning Agent", "Deep chain-of-thought analysis", "⚡", "Logic & Analysis"),
    RESEARCH("Research Agent", "Web searches & document extraction", "🔎", "Live Retrieval"),
    CODING("Coding Agent", "Syntax formatting & code sandbox", "💻", "Code Generation"),
    IMAGE("Image Studio", "Generative art & prompt assistant", "🎨", "Visual Synthesis"),
    MUSIC("Music Studio", "BPM, stems & genre synthesis", "🎵", "Audio Synthesis"),
    VIDEO("Video Studio", "Avatar & lip-sync pipeline", "🎬", "Motion Generation"),
    MEMORY("Memory Agent", "Short/Long term RAG recall", "💾", "Semantic Persistence"),
    AUTOMATION("MCP Tools", "System APIs, GitHub & Filesystem", "🛠️", "Tool Execution");

    companion object {
        fun defaultAgents() = values().toList()
    }
}
