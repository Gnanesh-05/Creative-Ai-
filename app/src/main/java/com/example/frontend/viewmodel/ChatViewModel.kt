package com.example.frontend.viewmodel

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.backend.repository.CreativeAiRepository
import com.example.backend.model.ChatConversationDomain
import com.example.backend.model.ChatMessageItemDomain
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ChatUiState(
    val conversations: List<ChatConversationDomain> = emptyList(),
    val activeConversation: ChatConversationDomain? = null,
    val messages: List<ChatMessageItemDomain> = listOf(
        ChatMessageItemDomain(
            id = "default_welcome",
            sender = "AI",
            content = "Hello! I am Creative AI. Ask me anything, request code examples, or brainstorm creative ideas."
        )
    ),
    val inputText: String = "",
    val searchQuery: String = "",
    val isLoading: Boolean = false,
    val isStreaming: Boolean = false,
    val streamingText: String = "",
    val errorMessage: String? = null,
    val showConversationsDrawer: Boolean = false
)

class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = CreativeAiRepository(application)
    private var streamJob: Job? = null

    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadConversations()
    }

    fun loadConversations(query: String? = null) {
        viewModelScope.launch {
            val result = repository.getConversations(query)
            result.onSuccess { list ->
                _uiState.value = _uiState.value.copy(
                    conversations = list,
                    searchQuery = query ?: ""
                )
                if (_uiState.value.activeConversation == null && list.isNotEmpty()) {
                    selectConversation(list.first().id)
                }
            }
        }
    }

    fun toggleConversationsDrawer() {
        _uiState.value = _uiState.value.copy(showConversationsDrawer = !_uiState.value.showConversationsDrawer)
    }

    fun selectConversation(conversationId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, errorMessage = null)
            val result = repository.getConversationDetail(conversationId)
            result.onSuccess { conv ->
                val displayMsgs = if (conv.messages.isEmpty()) {
                    listOf(
                        ChatMessageItemDomain(
                            id = "welcome_${conv.id}",
                            conversationId = conv.id,
                            sender = "AI",
                            content = "Continued conversation '${conv.title}'. How can I assist you further?"
                        )
                    )
                } else conv.messages

                _uiState.value = _uiState.value.copy(
                    activeConversation = conv,
                    messages = displayMsgs,
                    isLoading = false,
                    showConversationsDrawer = false
                )
            }.onFailure { err ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = err.message ?: "Failed to load conversation details"
                )
            }
        }
    }

    fun createNewConversation(title: String = "New Conversation") {
        viewModelScope.launch {
            val result = repository.createConversation(title)
            result.onSuccess { conv ->
                val initialMsg = ChatMessageItemDomain(
                    id = "welcome_${conv.id}",
                    conversationId = conv.id,
                    sender = "AI",
                    content = "Started new conversation. Ask any question or request code!"
                )
                val newConv = conv.copy(messages = listOf(initialMsg))
                _uiState.value = _uiState.value.copy(
                    conversations = listOf(newConv) + _uiState.value.conversations,
                    activeConversation = newConv,
                    messages = listOf(initialMsg),
                    showConversationsDrawer = false
                )
            }
        }
    }

    fun renameConversation(conversationId: String, newTitle: String) {
        if (newTitle.isBlank()) return
        viewModelScope.launch {
            val result = repository.updateConversation(conversationId, newTitle)
            result.onSuccess { updated ->
                val updatedList = _uiState.value.conversations.map {
                    if (it.id == conversationId) it.copy(title = newTitle) else it
                }
                val currentActive = _uiState.value.activeConversation
                val newActive = if (currentActive?.id == conversationId) currentActive.copy(title = newTitle) else currentActive
                _uiState.value = _uiState.value.copy(
                    conversations = updatedList,
                    activeConversation = newActive
                )
            }
        }
    }

    fun deleteConversation(conversationId: String) {
        viewModelScope.launch {
            repository.deleteConversation(conversationId)
            val updatedList = _uiState.value.conversations.filter { it.id != conversationId }
            val nextActive = updatedList.firstOrNull()
            _uiState.value = _uiState.value.copy(conversations = updatedList)

            if (_uiState.value.activeConversation?.id == conversationId) {
                if (nextActive != null) {
                    selectConversation(nextActive.id)
                } else {
                    createNewConversation("New Conversation")
                }
            }
        }
    }

    fun clearCurrentConversation() {
        val currentConv = _uiState.value.activeConversation ?: return
        viewModelScope.launch {
            repository.clearConversationMessages(currentConv.id)
            val clearedMsg = ChatMessageItemDomain(
                id = "cleared_${System.currentTimeMillis()}",
                conversationId = currentConv.id,
                sender = "AI",
                content = "Conversation history cleared. How can I help you next?"
            )
            _uiState.value = _uiState.value.copy(
                messages = listOf(clearedMsg),
                streamingText = "",
                isStreaming = false
            )
        }
    }

    fun onInputChanged(text: String) {
        _uiState.value = _uiState.value.copy(inputText = text)
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
        loadConversations(query)
    }

    fun sendMessage() {
        val text = _uiState.value.inputText.trim()
        if (text.isEmpty() || _uiState.value.isStreaming) return

        val isImageRequest = text.startsWith("/image", ignoreCase = true) || 
                             text.startsWith("/draw", ignoreCase = true) ||
                             text.startsWith("/generate", ignoreCase = true)

        if (isImageRequest) {
            val prefix = when {
                text.startsWith("/image", ignoreCase = true) -> "/image"
                text.startsWith("/draw", ignoreCase = true) -> "/draw"
                else -> "/generate"
            }
            val imagePrompt = text.substring(prefix.length).trim()
            if (imagePrompt.isEmpty()) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Please specify an image prompt (e.g. /image a red sports car)"
                )
                return
            }

            val userMsg = ChatMessageItemDomain(
                id = System.currentTimeMillis().toString(),
                conversationId = _uiState.value.activeConversation?.id ?: "",
                sender = "USER",
                content = text
            )

            val aiGeneratingMsgId = (System.currentTimeMillis() + 1).toString()
            val aiGeneratingMsg = ChatMessageItemDomain(
                id = aiGeneratingMsgId,
                conversationId = _uiState.value.activeConversation?.id ?: "",
                sender = "AI",
                content = "Synthesizing AI image for prompt: '$imagePrompt'..."
            )

            val updatedMsgs = _uiState.value.messages + userMsg + aiGeneratingMsg
            _uiState.value = _uiState.value.copy(
                messages = updatedMsgs,
                inputText = "",
                isStreaming = true,
                streamingText = "",
                errorMessage = null
            )

            viewModelScope.launch {
                val result = repository.generateImage(
                    prompt = imagePrompt,
                    stylePreset = "Photorealistic",
                    aspectRatio = "1:1",
                    resolution = "1024x1024",
                    model = "imagen-3.0-generate-002",
                    numImages = 1
                )
                result.onSuccess { imgResult ->
                    val finalContent = "![Generated Image](${imgResult.imageUrl})\n\nHere is your generated image for prompt: \"$imagePrompt\""
                    val finalMsgs = _uiState.value.messages.map {
                        if (it.id == aiGeneratingMsgId) it.copy(content = finalContent) else it
                    }
                    _uiState.value = _uiState.value.copy(
                        messages = finalMsgs,
                        isStreaming = false
                    )

                    // Update conversation snippet in conversation list
                    val currentActive = _uiState.value.activeConversation
                    if (currentActive != null) {
                        val updatedConv = currentActive.copy(
                            lastMessageSnippet = "Generated image: $imagePrompt",
                            messageCount = finalMsgs.size
                        )
                        val list = _uiState.value.conversations.map {
                            if (it.id == updatedConv.id) updatedConv else it
                        }
                        _uiState.value = _uiState.value.copy(
                            activeConversation = updatedConv,
                            conversations = list
                        )
                    }
                }.onFailure { err ->
                    val errorContent = "Failed to generate image: ${err.message ?: "Unknown error"}"
                    val finalMsgs = _uiState.value.messages.map {
                        if (it.id == aiGeneratingMsgId) it.copy(content = errorContent) else it
                    }
                    _uiState.value = _uiState.value.copy(
                        messages = finalMsgs,
                        isStreaming = false
                    )
                }
            }
            return
        }

        val userMsg = ChatMessageItemDomain(
            id = System.currentTimeMillis().toString(),
            conversationId = _uiState.value.activeConversation?.id ?: "",
            sender = "USER",
            content = text
        )

        val updatedMsgs = _uiState.value.messages + userMsg
        _uiState.value = _uiState.value.copy(
            messages = updatedMsgs,
            inputText = "",
            isStreaming = true,
            streamingText = "",
            errorMessage = null
        )

        executeStreamingResponse(text, updatedMsgs)
    }

    private fun executeStreamingResponse(userText: String, historyMsgs: List<ChatMessageItemDomain>) {
        streamJob?.cancel()
        streamJob = viewModelScope.launch {
            var accumulatedText = ""
            val previousContext = historyMsgs.dropLast(1) // history excluding current prompt

            repository.sendChatMessageStream(userText, previousContext).collect { chunk ->
                accumulatedText += chunk
                _uiState.value = _uiState.value.copy(streamingText = accumulatedText)
            }

            val aiMsg = ChatMessageItemDomain(
                id = System.currentTimeMillis().toString(),
                conversationId = _uiState.value.activeConversation?.id ?: "",
                sender = "AI",
                content = accumulatedText
            )

            val finalMsgs = _uiState.value.messages + aiMsg
            _uiState.value = _uiState.value.copy(
                messages = finalMsgs,
                isStreaming = false,
                streamingText = ""
            )

            // Update conversation snippet in conversation list
            val currentActive = _uiState.value.activeConversation
            if (currentActive != null) {
                val updatedConv = currentActive.copy(
                    lastMessageSnippet = accumulatedText.take(50),
                    messageCount = finalMsgs.size
                )
                val list = _uiState.value.conversations.map {
                    if (it.id == updatedConv.id) updatedConv else it
                }
                _uiState.value = _uiState.value.copy(
                    activeConversation = updatedConv,
                    conversations = list
                )
            }
        }
    }

    fun stopGeneration() {
        streamJob?.cancel()
        if (_uiState.value.isStreaming) {
            val partial = _uiState.value.streamingText.ifBlank { "Generation stopped by user." }
            val stoppedMsg = ChatMessageItemDomain(
                id = System.currentTimeMillis().toString(),
                conversationId = _uiState.value.activeConversation?.id ?: "",
                sender = "AI",
                content = "$partial (Stopped)"
            )
            _uiState.value = _uiState.value.copy(
                messages = _uiState.value.messages + stoppedMsg,
                isStreaming = false,
                streamingText = ""
            )
        }
    }

    fun regenerateLastResponse() {
        val msgs = _uiState.value.messages
        if (msgs.isEmpty() || _uiState.value.isStreaming) return

        val lastUserMsgIndex = msgs.indexOfLast { it.sender == "USER" }
        if (lastUserMsgIndex == -1) return

        val userMsg = msgs[lastUserMsgIndex]
        val trimmedMsgs = msgs.take(lastUserMsgIndex + 1)

        _uiState.value = _uiState.value.copy(
            messages = trimmedMsgs,
            isStreaming = true,
            streamingText = "",
            errorMessage = null
        )

        executeStreamingResponse(userMsg.content, trimmedMsgs)
    }

    fun retryFailedResponse() {
        regenerateLastResponse()
    }

    fun copyResponseToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("AI Response", text)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Copied response to clipboard", Toast.LENGTH_SHORT).show()
    }
}
