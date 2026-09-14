package com.dw.assisstant

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.PopupMenu
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.dw.assisstant.brain.AssistantEngine
import com.dw.assisstant.data.ZainDatabase
import com.dw.assisstant.data.entities.Conversation
import com.dw.assisstant.data.entities.Message
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class MainActivity : AppCompatActivity() {

    private lateinit var database: ZainDatabase
    private lateinit var engine: AssistantEngine

    private lateinit var messageContainer: LinearLayout
    private lateinit var messageInput: EditText
    private lateinit var scrollView: ScrollView
    private lateinit var sendButton: Button
    private lateinit var voiceButton: Button

    private lateinit var statusPill: TextView
    private lateinit var coreStatus: TextView
    private lateinit var zainCore: TextView
    private lateinit var welcomeText: TextView

    private var conversationId: Long = 0L

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        database = ZainDatabase.getInstance(this)
        engine = AssistantEngine(database)

        messageContainer = findViewById(R.id.messageContainer)
        messageInput = findViewById(R.id.messageInput)
        scrollView = findViewById(R.id.messageScroll)
        sendButton = findViewById(R.id.sendButton)
        voiceButton = findViewById(R.id.voiceButton)

        statusPill = findViewById(R.id.statusPill)
        coreStatus = findViewById(R.id.coreStatus)
        zainCore = findViewById(R.id.zainCore)
        welcomeText = findViewById(R.id.welcomeText)

        val menuButton: Button = findViewById(R.id.menuButton)

        setupTimeBasedGreeting()

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

        voiceButton.setOnClickListener {

            Toast.makeText(
                this,
                "Voice interface is being connected.",
                Toast.LENGTH_SHORT
            ).show()

            setAssistantState("VOICE READY")
        }

        menuButton.setOnClickListener {
            showMainMenu(menuButton)
        }

        updateOnlineAppearance()

        loadConversation()
    }

    // -------------------------------------------------------------
    // GREETING
    // -------------------------------------------------------------

    private fun setupTimeBasedGreeting() {

        val hour = Calendar
            .getInstance()
            .get(Calendar.HOUR_OF_DAY)

        welcomeText.text = when {

            hour < 12 ->
                "Good morning."

            hour < 18 ->
                "Good afternoon."

            else ->
                "Good evening."
        }
    }

    // -------------------------------------------------------------
    // ONLINE APPEARANCE
    // -------------------------------------------------------------

    private fun updateOnlineAppearance() {

        statusPill.text = "● ONLINE"
        coreStatus.text = "LOCAL BRAIN READY"

        zainCore.animate()
            .scaleX(1.04f)
            .scaleY(1.04f)
            .setDuration(1000)
            .withEndAction {

                zainCore.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(1000)
                    .start()
            }
            .start()
    }

    private fun setAssistantState(state: String) {

        coreStatus.text = state.uppercase()

        zainCore.animate()
            .scaleX(1.08f)
            .scaleY(1.08f)
            .setDuration(180)
            .withEndAction {

                zainCore.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(300)
                    .start()
            }
            .start()
    }

    // -------------------------------------------------------------
    // CONVERSATION
    // -------------------------------------------------------------

    private fun loadConversation() {

        lifecycleScope.launch(Dispatchers.IO) {

            val conversations =
                database.conversationDao().getAll()

            if (conversations.isEmpty()) {

                conversationId =
                    database.conversationDao().insert(
                        Conversation(
                            title = "ZAIN Conversation"
                        )
                    )

                withContext(Dispatchers.Main) {

                    addZainMessage(
                        "Hello. I'm ZAIN.\n\n" +
                            "Your local assistant brain is ready."
                    )
                }

            } else {

                val latest = conversations.first()

                conversationId = latest.id

                val messages =
                    database.messageDao()
                        .getForConversation(conversationId)

                withContext(Dispatchers.Main) {

                    if (messages.isEmpty()) {

                        addZainMessage(
                            "Welcome back.\n\n" +
                                "What are we working on?"
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

    // -------------------------------------------------------------
    // SEND
    // -------------------------------------------------------------

    private fun sendMessage() {

        val text =
            messageInput.text
                .toString()
                .trim()

        if (text.isEmpty()) return

        messageInput.text.clear()

        addUserMessage(text)

        sendButton.isEnabled = false
        voiceButton.isEnabled = false

        setAssistantState("THINKING")

        lifecycleScope.launch(Dispatchers.IO) {

            database.messageDao().insert(
                Message(
                    conversationId = conversationId,
                    role = "user",
                    content = text
                )
            )

            database.conversationDao()
                .updateTimestamp(
                    conversationId,
                    System.currentTimeMillis()
                )

            val response =
                engine.process(
                    text,
                    conversationId
                )

            database.messageDao().insert(
                Message(
                    conversationId = conversationId,
                    role = "assistant",
                    content = response
                )
            )

            database.conversationDao()
                .updateTimestamp(
                    conversationId,
                    System.currentTimeMillis()
                )

            withContext(Dispatchers.Main) {

                addZainMessage(response)

                sendButton.isEnabled = true
                voiceButton.isEnabled = true

                setAssistantState("SYSTEM READY")

                messageInput.requestFocus()
            }
        }
    }

    // -------------------------------------------------------------
    // MENU
    // -------------------------------------------------------------

    private fun showMainMenu(anchor: Button) {

        val popup = PopupMenu(this, anchor)

        popup.menuInflater.inflate(
            R.menu.main_menu,
            popup.menu
        )

        popup.setOnMenuItemClickListener { item ->

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
                    showComingSoon(
                        "Voice",
                        "Voice interaction is being connected to ZAIN."
                    )
                    true
                }

                R.id.menu_skills -> {
                    showComingSoon(
                        "Skills & Capabilities",
                        "ZAIN's expandable skills system is being built."
                    )
                    true
                }

                R.id.menu_settings -> {
                    showComingSoon(
                        "Settings",
                        "ZAIN settings will appear here."
                    )
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

    // -------------------------------------------------------------
    // MEMORY
    // -------------------------------------------------------------

    private fun showMemory() {

        lifecycleScope.launch(Dispatchers.IO) {

            val memories =
                database.memoryDao().getAll()

            withContext(Dispatchers.Main) {

                if (memories.isEmpty()) {

                    showComingSoon(
                        "Memory",
                        "ZAIN doesn't have any saved memories yet."
                    )

                    return@withContext
                }

                val text = buildString {

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
                    .setMessage(text)
                    .setPositiveButton("Close", null)
                    .show()
            }
        }
    }

    // -------------------------------------------------------------
    // CONVERSATIONS
    // -------------------------------------------------------------

    private fun showConversations() {

        lifecycleScope.launch(Dispatchers.IO) {

            val conversations =
                database.conversationDao().getAll()

            withContext(Dispatchers.Main) {

                if (conversations.isEmpty()) {

                    showComingSoon(
                        "Conversations",
                        "No conversations have been created yet."
                    )

                    return@withContext
                }

                val text = buildString {

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
                    .setMessage(text)
                    .setPositiveButton("Close", null)
                    .show()
            }
        }
    }

    // -------------------------------------------------------------
    // ABOUT
    // -------------------------------------------------------------

    private fun showAbout() {

        AlertDialog.Builder(this)
            .setTitle("ZAIN")
            .setMessage(
                "Personal AI Assistant\n\n" +
                    "ZAIN is being built as a local-first personal assistant " +
                    "with memory, conversations, voice, live information, " +
                    "skills and multi-device capabilities."
            )
            .setPositiveButton("Close", null)
            .show()
    }

    // -------------------------------------------------------------
    // COMING SOON
    // -------------------------------------------------------------

    private fun showComingSoon(
        title: String,
        message: String
    ) {

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }

    // -------------------------------------------------------------
    // CLEAR CHAT
    // -------------------------------------------------------------

    private fun confirmClearChat() {

        AlertDialog.Builder(this)
            .setTitle("Clear current chat?")
            .setMessage(
                "This will delete the messages in this conversation. " +
                    "Saved memories will remain safe."
            )
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Clear") { _, _ ->
                clearChat()
            }
            .show()
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

    // -------------------------------------------------------------
    // MESSAGE UI
    // -------------------------------------------------------------

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
        bubble.textSize = 15f
        bubble.setTextColor(
            ContextCompat.getColor(
                this,
                R.color.zain_white
            )
        )

        bubble.setPadding(
            20,
            15,
            20,
            15
        )

        bubble.setBackgroundResource(
            if (isUser)
                R.drawable.bg_user_message
            else
                R.drawable.bg_zain_message
        )

        val params =
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )

        params.setMargins(
            if (isUser) 60 else 0,
            7,
            if (isUser) 0 else 60,
            7
        )

        params.gravity =
            if (isUser)
                Gravity.END
            else
                Gravity.START

        bubble.layoutParams = params

        messageContainer.addView(bubble)

        scrollView.post {
            scrollView.fullScroll(
                ScrollView.FOCUS_DOWN
            )
        }
    }
}