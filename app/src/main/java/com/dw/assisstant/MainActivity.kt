package com.dw.assisstant

import android.os.Bundle
import android.view.MenuItem
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.PopupMenu
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.dw.assisstant.data.ZainDatabase
import com.dw.assisstant.data.entities.Conversation
import com.dw.assisstant.data.entities.Memory
import com.dw.assisstant.data.entities.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var database: ZainDatabase
    private lateinit var messageContainer: LinearLayout
    private lateinit var messageInput: EditText
    private lateinit var scrollView: ScrollView
    private lateinit var sendButton: Button
    private lateinit var menuButton: Button

    private var conversationId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = ZainDatabase.getInstance(this)

        messageContainer = findViewById(R.id.messageContainer)
        messageInput = findViewById(R.id.messageInput)
        scrollView = findViewById(R.id.messageScroll)
        sendButton = findViewById(R.id.sendButton)
        menuButton = findViewById(R.id.menuButton)

        sendButton.setOnClickListener {
            sendMessage()
        }

        messageInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage()
                true
            } else {
                false
            }
        }

        menuButton.setOnClickListener {
            showMainMenu()
        }

        loadConversation()
    }

    private fun showMainMenu() {

        val popup = PopupMenu(this, menuButton)

        popup.menuInflater.inflate(
            R.menu.main_menu,
            popup.menu
        )

        popup.setOnMenuItemClickListener { item: MenuItem ->

            when (item.itemId) {

                R.id.menu_memory -> {
                    showMemory()
                    true
                }

                R.id.menu_conversations -> {
                    showConversations()
                    true
                }

                R.id.menu_voice -> {
                    showComingSoon("Voice")
                    true
                }

                R.id.menu_skills -> {
                    showComingSoon("Skills & Capabilities")
                    true
                }

                R.id.menu_settings -> {
                    showComingSoon("Settings")
                    true
                }

                R.id.menu_about -> {
                    showAbout()
                    true
                }

                R.id.menu_clear_chat -> {
                    confirmClearChat()
                    true
                }

                else -> false
            }
        }

        popup.show()
    }

    private fun showMemory() {

        lifecycleScope.launch(Dispatchers.IO) {

            val memories = database
                .memoryDao()
                .getAll()

            withContext(Dispatchers.Main) {

                if (memories.isEmpty()) {

                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("🧠 ZAIN Memory")
                        .setMessage(
                            "ZAIN doesn't have any saved memories about you yet."
                        )
                        .setPositiveButton("OK", null)
                        .show()

                    return@withContext
                }

                val memoryText = buildString {

                    memories
                        .take(20)
                        .forEachIndexed { index, memory ->

                            append("${index + 1}. ")
                            append(memory.content)
                            append("\n\n")
                        }
                }

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("🧠 ZAIN Memory")
                    .setMessage(memoryText)
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    private fun showConversations() {

        lifecycleScope.launch(Dispatchers.IO) {

            val conversations = database
                .conversationDao()
                .getAll()

            withContext(Dispatchers.Main) {

                if (conversations.isEmpty()) {

                    AlertDialog.Builder(this@MainActivity)
                        .setTitle("💬 Conversations")
                        .setMessage("No conversations yet.")
                        .setPositiveButton("OK", null)
                        .show()

                    return@withContext
                }

                val conversationText = buildString {

                    conversations
                        .take(20)
                        .forEachIndexed { index, conversation ->

                            append("${index + 1}. ")
                            append(conversation.title)
                            append("\n\n")
                        }
                }

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("💬 Conversations")
                    .setMessage(conversationText)
                    .setPositiveButton("OK", null)
                    .show()
            }
        }
    }

    private fun showComingSoon(feature: String) {

        Toast.makeText(
            this,
            "$feature is coming in the next ZAIN update.",
            Toast.LENGTH_SHORT
        ).show()
    }

    private fun showAbout() {

        AlertDialog.Builder(this)
            .setTitle("ℹ About ZAIN")
            .setMessage(
                "ZAIN\n\n" +
                        "Personal AI Assistant\n\n" +
                        "ZAIN is being built as a local-first personal assistant " +
                        "designed to understand you, remember useful information, " +
                        "work offline, and become more capable over time.\n\n" +
                        "Version 1.0"
            )
            .setPositiveButton("OK", null)
            .show()
    }

    private fun confirmClearChat() {

        AlertDialog.Builder(this)
            .setTitle("Clear Chat?")
            .setMessage(
                "This will clear the messages in the current conversation.\n\n" +
                        "Your saved memories will NOT be deleted."
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Clear") { _, _ ->
                clearChat()
            }
            .show()
    }

    private fun loadConversation() {

        lifecycleScope.launch(Dispatchers.IO) {

            val conversations = database
                .conversationDao()
                .getAll()

            if (conversations.isEmpty()) {

                conversationId = database
                    .conversationDao()
                    .insert(
                        Conversation(
                            title = "ZAIN Conversation"
                        )
                    )

                withContext(Dispatchers.Main) {

                    addZainMessage(
                        "Hey, I'm ZAIN 👋\n\n" +
                                "I'm your personal AI assistant.\n\n" +
                                "My local brain is ready."
                    )
                }

            } else {

                val latestConversation = conversations.first()

                conversationId = latestConversation.id

                val messages = database
                    .messageDao()
                    .getForConversation(conversationId)

                withContext(Dispatchers.Main) {

                    if (messages.isEmpty()) {

                        addZainMessage(
                            "Welcome back. I'm ready."
                        )

                    } else {

                        messages.forEach { message ->

                            if (message.role == "user") {
                                addUserMessage(message.content)
                            } else {
                                addZainMessage(message.content)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun sendMessage() {

        val text = messageInput.text
            .toString()
            .trim()

        if (text.isEmpty()) return

        messageInput.text.clear()

        addUserMessage(text)

        sendButton.isEnabled = false

        lifecycleScope.launch(Dispatchers.IO) {

            database.messageDao().insert(
                Message(
                    conversationId = conversationId,
                    role = "user",
                    content = text
                )
            )

            database.conversationDao().updateTimestamp(
                conversationId = conversationId,
                updatedAt = System.currentTimeMillis()
            )

            val response = processMessage(text)

            database.messageDao().insert(
                Message(
                    conversationId = conversationId,
                    role = "assistant",
                    content = response
                )
            )

            database.conversationDao().updateTimestamp(
                conversationId = conversationId,
                updatedAt = System.currentTimeMillis()
            )

            withContext(Dispatchers.Main) {

                addZainMessage(response)

                sendButton.isEnabled = true

                messageInput.requestFocus()
            }
        }
    }

    private suspend fun processMessage(
        text: String
    ): String {

        val lower = text
            .lowercase()
            .trim()

        if (
            lower.startsWith("remember that ") ||
            lower.startsWith("remember ") ||
            lower.startsWith("please remember that ") ||
            lower.startsWith("please remember ")
        ) {

            var memoryText = text

            memoryText = memoryText
                .removePrefix("Remember that ")
                .removePrefix("remember that ")
                .removePrefix("Remember ")
                .removePrefix("remember ")
                .removePrefix("Please remember that ")
                .removePrefix("please remember that ")
                .removePrefix("Please remember ")
                .removePrefix("please remember ")
                .trim()

            if (memoryText.isNotEmpty()) {

                database.memoryDao().insert(
                    Memory(
                        content = memoryText,
                        category = "user_memory"
                    )
                )

                return "Got it. I've saved that to my memory."
            }
        }

        if (
            lower.contains("what do you remember") ||
            lower.contains("what do you know about me") ||
            lower.contains("tell me what you remember")
        ) {

            val memories = database
                .memoryDao()
                .getAll()

            if (memories.isEmpty()) {

                return "I don't have any memories about you yet."
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
            lower.contains("remember about") ||
            lower.contains("know about")
        ) {

            val searchWords = extractSearchWords(text)

            if (searchWords.isNotEmpty()) {

                val results = database
                    .memoryDao()
                    .search(searchWords)

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

        return when {

            lower == "hello" ||
                    lower == "hi" ||
                    lower == "hey" -> {

                "Hello 👋 I'm ZAIN. What are we working on?"
            }

            lower.contains("who are you") -> {

                "I'm ZAIN — your personal AI assistant. I'm being built to understand you, remember useful information, work locally, and become much more capable over time."
            }

            lower.contains("what can you do") -> {

                "Right now I can chat with you, remember information you give me, retrieve saved memories, and store our conversations locally."
            }

            lower.contains("are you online") -> {

                "My local brain is online. Internet-based intelligence isn't connected yet."
            }

            lower.contains("time") -> {

                "Time awareness is one of the capabilities we'll connect to me next."
            }

            lower.contains("weather") -> {

                "Weather access isn't connected yet. We'll add it later."
            }

            lower.contains("thank") -> {

                "You're welcome. I'm here."
            }

            else -> {

                "I understand you. My full intelligence engine isn't connected yet, but this is the foundation we're building on."
            }
        }
    }

    private fun extractSearchWords(
        text: String
    ): String {

        val cleaned = text
            .lowercase()
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
            .replace("?", "")
            .trim()

        val words = cleaned
            .split(" ")
            .filter {

                it.length > 2 &&
                        it !in setOf(
                    "the",
                    "about",
                    "my",
                    "you",
                    "your",
                    "that",
                    "this",
                    "know",
                    "remember"
                )
            }

        return words.joinToString(" ")
    }

    private fun clearChat() {

        lifecycleScope.launch(Dispatchers.IO) {

            database.messageDao()
                .deleteForConversation(conversationId)

            withContext(Dispatchers.Main) {

                messageContainer.removeAllViews()

                addZainMessage(
                    "Chat cleared.\n\n" +
                            "My saved memories are still safe."
                )
            }
        }
    }

    private fun addUserMessage(
        text: String
    ) {

        addMessageBubble(
            text = text,
            isUser = true
        )
    }

    private fun addZainMessage(
        text: String
    ) {

        addMessageBubble(
            text = text,
            isUser = false
        )
    }

    private fun addMessageBubble(
        text: String,
        isUser: Boolean
    ) {

        val bubble = TextView(this)

        bubble.text = text
        bubble.textSize = 16f

        bubble.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.zain_white
            )
        )

        bubble.setPadding(
            22,
            16,
            22,
            16
        )

        bubble.setBackgroundResource(
            if (isUser) {
                R.drawable.bg_user_message
            } else {
                R.drawable.bg_zain_message
            }
        )

        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )

        params.setMargins(
            if (isUser) 70 else 0,
            8,
            if (isUser) 0 else 70,
            8
        )

        bubble.layoutParams = params

        messageContainer.addView(bubble)

        scrollView.post {
            scrollView.fullScroll(
                ScrollView.FOCUS_DOWN
            )
        }
    }
}