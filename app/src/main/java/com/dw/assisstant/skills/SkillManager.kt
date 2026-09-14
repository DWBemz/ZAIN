package com.dw.assisstant.skills

import android.content.Context
import com.dw.assisstant.brain.AssistantIntent

class SkillManager(
    private val context: Context
) {

    fun detectSkill(message: String): AssistantIntent {

        val text = message
            .trim()
            .lowercase()

        return when {

            text == "hello" ||
            text == "hi" ||
            text == "hey" ||
            text.startsWith("hello ") ||
            text.startsWith("hi ") ->
                AssistantIntent.Greeting

            text.contains("who are you") ||
            text.contains("what are you") ->
                AssistantIntent.Identity

            text.contains("what can you do") ||
            text.contains("your capabilities") ||
            text.contains("your skills") ->
                AssistantIntent.Capabilities

            text == "time" ||
            text.contains("what time") ||
            text.contains("current time") ->
                AssistantIntent.Time

            text == "date" ||
            text.contains("what date") ||
            text.contains("today's date") ||
            text.contains("today date") ->
                AssistantIntent.Date

            text.contains("are you online") ||
            text.contains("are you connected") ||
            text.contains("do you have internet") ->
                AssistantIntent.OnlineStatus

            text.startsWith("remember ") ||
            text.startsWith("remember that ") ||
            text.startsWith("please remember ") ||
            text.startsWith("please remember that ") ||
            text.startsWith("don't forget ") ||
            text.startsWith("dont forget ") ->
                AssistantIntent.Remember

            text.contains("what do you remember") ||
            text.contains("what do you know about me") ||
            text.contains("tell me what you remember") ->
                AssistantIntent.RecallMemory

            text.contains("do you remember") ||
            text.contains("remember about") ||
            text.contains("know about") ->
                AssistantIntent.SearchMemory

            text.contains("voice") ||
            text.contains("speak") ||
            text.contains("listen") ->
                AssistantIntent.Voice

            else ->
                AssistantIntent.Unknown
        }
    }
}
