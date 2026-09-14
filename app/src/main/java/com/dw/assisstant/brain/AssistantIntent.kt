package com.dw.assisstant.brain

sealed class AssistantIntent {

    data object Greeting : AssistantIntent()

    data object Identity : AssistantIntent()

    data object Capabilities : AssistantIntent()

    data object Time : AssistantIntent()

    data object Date : AssistantIntent()

    data object OnlineStatus : AssistantIntent()

    data object Remember : AssistantIntent()

    data object RecallMemory : AssistantIntent()

    data object SearchMemory : AssistantIntent()

    data object Voice : AssistantIntent()

    data object Unknown : AssistantIntent()
}