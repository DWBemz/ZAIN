package com.dw.assisstant.brain

import com.dw.assisstant.data.ZainDatabase
import com.dw.assisstant.data.entities.Memory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AssistantEngine(
    private val database: ZainDatabase
) {

    suspend fun process(
        input: String,
        conversationId: Long
    ): String {

        val text = input.trim()
        val lower = text.lowercase(Locale.getDefault())

        if (text.isEmpty()) {
            return "I'm listening."
        }

        // ---------------------------------------------------------
        // MEMORY
        // ---------------------------------------------------------

        val memoryToSave = extractMemory(text)

        if (memoryToSave != null) {

            database.memoryDao().insert(
                Memory(
                    content = memoryToSave,
                    category = "user_memory"
                )
            )

            return "Got it. I've saved that to my memory."
        }

        if (
            lower.contains("what do you remember") ||
            lower.contains("what do you know about me") ||
            lower.contains("tell me what you remember")
        ) {

            val memories = database.memoryDao().getAll()

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

        if (
            lower.contains("do you remember") ||
            lower.contains("remember about") ||
            lower.contains("know about")
        ) {

            val words = extractSearchWords(text)

            if (words.isNotEmpty()) {

                val results = database.memoryDao().search(words)

                if (results.isNotEmpty()) {

                    return buildString {

                        append("I found this in my memory:\n\n")

                        results
                            .take(5)
                            .forEach {

                                append("• ")
                                append(it.content)
                                append("\n")
                            }
                    }
                }
            }

            return "I couldn't find anything about that in my memory."
        }

        // ---------------------------------------------------------
        // TIME / DATE
        // ---------------------------------------------------------

        if (
            lower.contains("what time") ||
            lower == "time" ||
            lower.contains("current time")
        ) {

            val formatter =
                SimpleDateFormat("h:mm a", Locale.getDefault())

            return "It's ${formatter.format(Date())}."
        }

        if (
            lower.contains("what date") ||
            lower.contains("today's date") ||
            lower == "date"
        ) {

            val formatter =
                SimpleDateFormat(
                    "EEEE, d MMMM yyyy",
                    Locale.getDefault()
                )

            return "Today is ${formatter.format(Date())}."
        }

        // ---------------------------------------------------------
        // IDENTITY
        // ---------------------------------------------------------

        if (
            lower == "hello" ||
            lower == "hi" ||
            lower == "hey" ||
            lower.startsWith("hello ")
        ) {

            return "Hello 👋 I'm ZAIN. I'm ready."
        }

        if (lower.contains("who are you")) {

            return """
                I'm ZAIN — your personal AI assistant.

                I'm being built to understand you, remember useful information, work locally, and grow into a much more capable assistant.
            """.trimIndent()
        }

        if (
            lower.contains("what can you do") ||
            lower.contains("your capabilities")
        ) {

            return """
                Right now I can:

                • Chat with you
                • Remember information
                • Retrieve saved memories
                • Store conversations locally
                • Understand basic commands
                • Work with time and date

                More capabilities are being connected.
            """.trimIndent()
        }

        // ---------------------------------------------------------
        // ONLINE STATE
        // ---------------------------------------------------------

        if (
            lower.contains("are you online") ||
            lower.contains("are you connected")
        ) {

            return "My local brain is online. Internet intelligence and live services are being connected."
        }

        // ---------------------------------------------------------
        // THANKS
        // ---------------------------------------------------------

        if (lower.contains("thank")) {
            return "You're welcome."
        }

        // ---------------------------------------------------------
        // DEFAULT
        // ---------------------------------------------------------

        return """
            I understand what you're saying.

            My deeper intelligence layer is still being connected, but my local brain is active and your conversation is being stored locally.
        """.trimIndent()
    }

    private suspend fun extractMemory(
        text: String
    ): String? {

        val lower = text.lowercase(Locale.getDefault())

        val prefixes = listOf(
            "remember that ",
            "remember ",
            "please remember that ",
            "please remember ",
            "don't forget that ",
            "dont forget that ",
            "don't forget ",
            "dont forget "
        )

        for (prefix in prefixes) {

            if (lower.startsWith(prefix)) {

                return text
                    .drop(prefix.length)
                    .trim()
                    .takeIf { it.isNotEmpty() }
            }
        }

        // Natural statements such as:
        //
        // "My favorite color is red"
        // "My favorite food is rice"
        //
        // are also useful memories.

        if (
            lower.startsWith("my ") &&
            (
                lower.contains(" is ") ||
                lower.contains(" are ") ||
                lower.contains("i am") ||
                lower.contains("i'm")
            )
        ) {

            return text.trim()
        }

        return null
    }

    private fun extractSearchWords(
        text: String
    ): String {

        val cleaned = text
            .lowercase(Locale.getDefault())
            .replace("what do you remember about", "")
            .replace("what do you know about", "")
            .replace("tell me what you remember about", "")
            .replace("do you remember", "")
            .replace("remember about", "")
            .replace("know about", "")
            .replace("?", "")
            .trim()

        val ignored = setOf(
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
                it.length > 2 && it !in ignored
            }
            .joinToString(" ")
    }
}
