package com.dw.assisstant.brain

interface AiProvider {

    suspend fun generateResponse(
        message: String,
        conversationContext: List<String>
    ): String?
}