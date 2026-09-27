package com.nova.healthconnect.data.models

import com.google.gson.annotations.SerializedName

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val sender: String, // "user" or "nova"
    val text: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class AiChatRequest(
    @SerializedName("message") val message: String,
    @SerializedName("context") val context: Map<String, Any>? = null
)

data class AiChatResponse(
    val status: String = "success",
    val reply: String = "",
    val error: String? = null
)
