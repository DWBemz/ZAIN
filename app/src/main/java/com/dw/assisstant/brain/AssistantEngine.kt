package com.dw.assisstant.brain

import android.content.Context
import com.dw.assisstant.data.ZainDatabase
import com.dw.assisstant.data.entities.Memory
import com.dw.assisstant.skills.SkillManager
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AssistantEngine(
    private val context: Context,
    private val database: ZainDatabase
) {

    private val skillManager =
        SkillManager(context)

    suspend fun process(
        input: String,
        conversationId: Long
    ): String {

        val text = input.trim()

        if (text.isEmpty()) {
            return "I'm listening."
        }

        val intent =
            skillManager.detectSkill(text)

        return when (intent) {

            AssistantIntent.Greeting ->
                greeting()

            AssistantIntent.Identity ->
                identity()

            AssistantIntent.Capabilities ->
                capabilities()

            AssistantIntent.Time ->
                currentTime()

            AssistantIntent.Date ->
                currentDate()

            AssistantIntent.OnlineStatus ->
                onlineStatus()

            AssistantIntent.Remember ->
                remember(text)

            AssistantIntent.RecallMemory ->
                recallMemory()

            AssistantIntent.SearchMemory ->
                searchMemory(text)

            AssistantIntent.Voice ->
                "Voice interaction is ready to be connected."

            AssistantIntent.Unknown ->
                unknown(text)
        }
    }

    private fun greeting(): String {

        val hour =
            java.util.Calendar
                .getInstance()
                .get(java.util.Calendar.HOUR_OF_DAY)

        return when {

            hour < 12 ->
                "Good morning 👋 I'm ZAIN. I'm ready."

            hour < 18 ->
                "Good afternoon 👋 I'm ZAIN. I'm ready."

            else ->
                "Good evening 👋 I'm ZAIN. I'm ready."
        }
    }

    private fun identity(): String {

        return """
            I'm ZAIN — your personal AI assistant.

            I'm being built to understand you, remember useful information, work locally, use online intelligence when available, and grow through an expandable skills system.
        """.trimIndent()
    }

    private fun capabilities(): String {

        return """
            My current foundation includes:

            • Local conversations
            • Local memory
            • Memory retrieval
            • Time and date
            • Online/offline awareness
            • Expandable skills
            • Voice architecture
            • Online AI architecture

            More capabilities are being connected.
        """.trimIndent()
    }

    private fun currentTime(): String {

        val formatter =
            SimpleDateFormat(
                "h:mm a",
                Locale.getDefault()
            )

        return "It's ${formatter.format(Date())}."
    }

    private fun currentDate(): String {

        val formatter =
            SimpleDateFormat(
                "EEEE, d MMMM yyyy",
                Locale.getDefault()
            )

        return "Today is ${formatter.format(Date())}."
    }

    private fun onlineStatus(): String {

        val connectivity =
            context
                .getSystemService(
                    Context.CONNECTIVITY_SERVICE
                ) as android.net.ConnectivityManager

        val network =
            connectivity.activeNetwork

        return if (network != null) {

            "I'm online. Internet connectivity is available."

        } else {

            "I'm offline. My local brain is still available."
        }
    }

    private suspend fun remember(
        text: String
    ): String {

        val memory =
            extractMemory(text)

        if (memory.isNullOrBlank()) {
            return "Tell me what you'd like me to remember."
        }

        database.memoryDao().insert(
            Memory(
                content = memory,
                category = "user_memory"
            )
        )

        return "Got it. I've saved that to my memory."
    }

    private suspend fun recallMemory(): String {

        val memories =
            database.memoryDao().getAll()

        if (memories.isEmpty()) {

            return "I don't have any saved memories about you yet."
        }

        return buildString {

            append("Here's what I remember:\n\n")

            memories
                .take(15)
                .forEachIndexed { index, memory ->

                    append("${index + 1}. ")
                    append(memory.content)
                    append("\n")
                }
        }
    }

    private suspend fun searchMemory(
        text: String
    ): String {

        val query =
            extractSearchWords(text)

        if (query.isBlank()) {

            return "Tell me what you want me to remember."
        }

        val individualWords =
            query.split(Regex("\\s+"))

        val results =
            individualWords
                .flatMap {
                    database.memoryDao().search(it)
                }
                .distinctBy {
                    it.id
                }
                .take(5)

        if (results.isEmpty()) {

            return "I couldn't find anything about that in my memory."
        }

        return buildString {

            append("I found this in my memory:\n\n")

            results.forEach {

                append("• ")
                append(it.content)
                append("\n")
            }
        }
    }

    private fun unknown(
        text: String
    ): String {

        return """
            I understand what you're saying.

            My local brain is active, but I don't have an online intelligence provider connected yet.

            I can still work with my local memories, conversations and built-in skills.
        """.trimIndent()
    }

    private fun extractMemory(
        text: String
    ): String? {

        val lower =
            text.lowercase(Locale.getDefault())

        val prefixes = listOf(

            "please remember that ",
            "please remember ",

            "remember that ",
            "remember ",

            "don't forget that ",
            "don't forget ",

            "dont forget that ",
            "dont forget "
        )

        for (prefix in prefixes) {

            if (lower.startsWith(prefix)) {

                return text
                    .drop(prefix.length)
                    .trim()
                    .takeIf {
                        it.isNotEmpty()
                    }
            }
        }

        /*
         * Natural personal statements.
         *
         * Example:
         * "My favorite color is red."
         */

        if (
            lower.startsWith("my ") &&
            (
                lower.contains(" is ") ||
                lower.contains(" are ")
            )
        ) {

            return text.trim()
        }

        return null
    }

    private fun extractSearchWords(
        text: String
    ): String {

        val cleaned =
            text
                .lowercase(Locale.getDefault())
                .replace(
                    "what do you remember about",
                    ""
                )
                .replace(
                    "what do you know about",
                    ""
                )
                .replace(
                    "tell me what you remember about",
                    ""
                )
                .replace(
                    "do you remember",
                    ""
                )
                .replace(
                    "remember about",
                    ""
                )
                .replace(
                    "know about",
                    ""
                )
                .replace(
                    "?",
                    ""
                )
                .trim()

        val ignored =
            setOf(
                "the",
                "about",
                "my",
                "you",
                "your",
                "that",
                "this",
                "know",
                "remember",
                "please",
                "tell",
                "what",
                "do"
            )

        return cleaned
            .split(Regex("\\s+"))
            .filter {
                it.length > 2 &&
                    it !in ignored
            }
            .joinToString(" ")
    }
}