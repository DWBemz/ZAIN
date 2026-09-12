package com.dw.assisstant

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
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

    private var conversationId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        database = ZainDatabase.getInstance(this)

        messageContainer = findViewById(R.id.messageContainer)
        messageInput = findViewById(R.id.messageInput)
        scrollView = findViewById(R.id.messageScroll)

        val sendButton: Button = findViewById(R.id.sendButton)
        val clearButton: Button = findViewById(R.id.clearButton)

        sendButton.setOnClickListener {
            sendMessage()
        }

        messageInput.setOnEditorActionListener { _, _, _ ->
            sendMessage()
            true
        }

        clearButton.setOnClickListener {
            messageContainer.removeAllViews()
            addZainMessage("Chat cleared. I'm ready for our next conversation.")
        }

        createConversation()
    }

    private fun createConversation() {
        lifecycleScope.launch(Dispatchers.IO) {
            conversationId = database.conversationDao().insert(
                Conversation(title = "ZAIN Conversation")
            )

            withContext(Dispatchers.Main) {
                addZainMessage(
                    "Hey, I'm ZAIN 👋\n\n" +
                            "I'm your personal AI assistant. " +
                            "My local brain is online and ready.\n\n" +
                            "What are we working on?"
                )
            }
        }
    }

    private fun sendMessage() {
        val text = messageInput.text.toString().trim()

        if (text.isEmpty()) return

        messageInput.text.clear()

        addUserMessage(text)

        lifecycleScope.launch(Dispatchers.IO) {

            if (conversationId != 0L) {
                database.messageDao().insert(
                    Message(
                        conversationId = conversationId,
                        role = "user",
                        content = text
                    )
                )
            }

            val response = processMessage(text)

            if (conversationId != 0L) {
                database.messageDao().insert(
                    Message(
                        conversationId = conversationId,
                        role = "assistant",
                        content = response
                    )
                )
            }

            withContext(Dispatchers.Main) {
                addZainMessage(response)
            }
        }
    }

    private suspend fun processMessage(text: String): String {

        val lower = text.lowercase().trim()

        if (
            lower.startsWith("remember that ") ||
            lower.startsWith("remember ")
        ) {
            val memoryText = text
                .removePrefix("Remember that ")
                .removePrefix("remember that ")
                .removePrefix("Remember ")
                .removePrefix("remember ")
                .trim()

            if (memoryText.isNotEmpty()) {
                database.memoryDao().insert(
                    Memory(
                        content = memoryText,
                        category = "user_memory"
                    )
                )

                return "Got it. I've saved that to my local memory."
            }
        }

        if (
            lower.contains("what do you remember") ||
            lower.contains("what do you know about me")
        ) {
            val memories = database.memoryDao().getAll()

            if (memories.isEmpty()) {
                return "My local memory is empty right now. You can tell me something by saying, \"Remember that...\""
            }

            return buildString {
                append("Here's what I currently remember:\n\n")

                memories.take(10).forEachIndexed { index, memory ->
                    append("${index + 1}. ${memory.content}\n")
                }
            }
        }

        return when {
            lower == "hello" ||
                    lower == "hi" ||
                    lower == "hey" -> {
                "Hello 👋 I'm ZAIN. What can I help you with?"
            }

            lower.contains("who are you") -> {
                "I'm ZAIN — your personal AI assistant. I'm being built to work locally, remember useful information, understand commands, and eventually become much more capable."
            }

            lower.contains("are you online") -> {
                "My local brain is online. Internet-based intelligence isn't connected yet, but that's coming."
            }

            lower.contains("what can you do") -> {
                "Right now I can chat with you, save conversations locally, and store simple memories. We're going to keep expanding my capabilities."
            }

            lower.contains("time") -> {
                "Time awareness is one of the next capabilities we'll connect to my core."
            }

            lower.contains("weather") -> {
                "Weather access isn't connected yet. We'll add it when we build my online and location capabilities."
            }

            lower.contains("thank") -> {
                "You're welcome. I'm here."
            }

            else -> {
                "I understand the message, but my full intelligence engine isn't connected yet. This is the foundation we're building on."
            }
        }
    }

    private fun addUserMessage(text: String) {
        addMessageBubble(
            text = text,
            isUser = true
        )
    }

    private fun addZainMessage(text: String) {
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
                if (isUser) R.color.zain_white else R.color.zain_white
            )
        )

        bubble.setPadding(
            22,
            16,
            22,
            16
        )

        val background = if (isUser) {
            R.drawable.bg_user_message
        } else {
            R.drawable.bg_zain_message
        }

        bubble.setBackgroundResource(background)

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
            scrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }
    }
}