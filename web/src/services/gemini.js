// Gemini API integration for Nexus AI Web App

const BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models";

/**
 * Sends a query to the Gemini API using v1beta generateContent.
 * Supports system instructions and custom models.
 */
export const generateGeminiContent = async (userQuery, modelId, agent, memories = [], imageBase64 = null) => {
  const apiKey = import.meta.env.VITE_GEMINI_API_KEY || "";
  
  const memoryContext = memories.length > 0
    ? "User Saved Memories & Preferences:\n" + memories.slice(0, 5).map(m => `- [${m.category}] ${m.fact}`).join("\n")
    : "";

  const systemInstructionText = `
    You are Nexus AI OS — the world's most advanced AI Operating System.
    Active Agent Role: ${agent.title || "Planner"} (${agent.capability || "General cognitive reasoning"}).
    Current Model: ${modelId}.
    ${memoryContext}
    
    Provide ultra-helpful, precise, and human-like natural responses.
    If code is requested, present clean code in markdown code blocks.
    Include reasoning step summaries where appropriate.
  `.trim();

  // If API key is not available, return a beautiful simulated response from Nexus AI OS.
  if (!apiKey || apiKey === "MY_GEMINI_API_KEY") {
    console.warn("VITE_GEMINI_API_KEY is not set. Using local OS simulation.");
    return await simulateResponse(userQuery, agent, modelId);
  }

  const modelName = modelId === "gemini-3.1-pro-preview" 
    ? "gemini-1.5-pro"  // map preview to standard supported model
    : modelId === "gemini-2.5-flash-image" 
    ? "gemini-1.5-flash"
    : "gemini-1.5-flash"; // default fallback for web compatibility

  const url = `${BASE_URL}/${modelName}:generateContent?key=${apiKey}`;

  const parts = [{ text: userQuery }];
  if (imageBase64) {
    // imageBase64 is expected to be data URL or raw base64. If data URL, extract raw base64
    const base64Data = imageBase64.includes(",") ? imageBase64.split(",")[1] : imageBase64;
    const mimeType = imageBase64.includes(";") ? imageBase64.split(";")[0].split(":")[1] : "image/jpeg";
    parts.push({
      inlineData: {
        mimeType: mimeType,
        data: base64Data
      }
    });
  }

  const requestBody = {
    contents: [{ role: "user", parts }],
    systemInstruction: {
      parts: [{ text: systemInstructionText }]
    },
    generationConfig: {
      temperature: 0.7,
      maxOutputTokens: 2048
    }
  };

  try {
    const response = await fetch(url, {
      method: "POST",
      headers: {
        "Content-Type": "application/json"
      },
      body: JSON.stringify(requestBody)
    });

    if (!response.ok) {
      const errData = await response.json();
      throw new Error(errData?.error?.message || "HTTP error " + response.status);
    }

    const data = await response.json();
    const text = data.candidates?.[0]?.content?.parts?.[0]?.text;
    if (text) {
      return text;
    } else {
      throw new Error("Empty response from Gemini API");
    }
  } catch (error) {
    console.error("Gemini API Error:", error);
    return `[Local Fallback] Error connecting to Gemini API (${error.message}). Here is a simulated response:\n\n` + 
      await simulateResponse(userQuery, agent, modelId);
  }
};

/**
 * Simulates a response locally for offline testing or when VITE_GEMINI_API_KEY is not set.
 */
const simulateResponse = async (query, agent, modelId) => {
  await new Promise(resolve => setTimeout(resolve, 1500));
  const lower = query.toLowerCase();

  if (lower.includes("image") || lower.includes("draw") || lower.includes("photo") || lower.includes("paint")) {
    return "I have routed your prompt to the **Image Studio Agent**. Select the **Studio** tab and choose **Image Studio** to pick styles, aspect ratios, and generate high-fidelity visual assets!";
  }
  
  if (lower.includes("music") || lower.includes("song") || lower.includes("lyrics") || lower.includes("beat")) {
    return "I have routed your prompt to the **Music & Lyrics Studio**. Select the **Studio** tab and choose **Music & Lyrics** to pick genres, moods, tempo, instruments, and generate original 15-second audio stems alongside lyrics!";
  }

  if (lower.includes("game") || lower.includes("play") || lower.includes("chess") || lower.includes("tic")) {
    return "I have routed your request to the **AI Game Center**. Open the **Games** tab to challenge the AI in Tic-Tac-Toe, Chess AI, or navigate the Mind Maze pathfinder!";
  }

  return `[Simulated by Nexus AI Agent: ${agent.title || "Planner"}]
Using model: ${modelId}

I analyzed your query: "${query}"

Here is my recommendation:
- **Core Intent**: General query analysis
- **Context**: Local simulation active
- **Action**: You can save memory facts in the **Profile** tab to customize my responses. For instance, tell me "Remember that my name is Karan" and I will remember it in future chats!

Please let me know if you would like me to delegate this to specialized sub-agents (e.g. Coding Agent, Research Agent, or Memory Manager).`;
};
