package com.dw.assisstant

import com.dw.assisstant.util.CrashReporter

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
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
    private lateinit var messageScroll: ScrollView

    private lateinit var sendButton: Button
    private lateinit var voiceButton: Button

    private lateinit var statusPill: TextView
    private lateinit var coreStatus: TextView
    private lateinit var zainCore: TextView
    private lateinit var welcomeText: TextView

    private var conversationId = 0L

    override fun onCreate(
        savedInstanceState: Bundle?
    ) {

        super.onCreate(savedInstanceState)
 
        CrashReporter.install(this)

        setContentView(
            R.layout.activity_main
        )

        database =
            ZainDatabase.getInstance(this)

        engine =
            AssistantEngine(
                this,
                database
            )

        messageContainer =
            findViewById(
                R.id.messageContainer
            )

        messageInput =
            findViewById(
                R.id.messageInput
            )

        messageScroll =
            findViewById(
                R.id.messageScroll
            )

        sendButton =
            findViewById(
                R.id.sendButton
            )

        voiceButton =
            findViewById(
                R.id.voiceButton
            )

        statusPill =
            findViewById(
                R.id.statusPill
            )

        coreStatus =
            findViewById(
                R.id.coreStatus
            )

        zainCore =
            findViewById(
                R.id.zainCore
            )

        welcomeText =
            findViewById(
                R.id.welcomeText
            )

        val menuButton =
            findViewById<Button>(
                R.id.menuButton
            )

        updateGreeting()

        updateConnectionState()

        sendButton.setOnClickListener {
            sendMessage()
        }

        messageInput.setOnEditorActionListener {
                _, actionId, _ ->

            if (
                actionId ==
                EditorInfo.IME_ACTION_SEND
            ) {

                sendMessage()

                true

            } else {

                false
            }
        }

        voiceButton.setOnClickListener {

            setAssistantState(
                "VOICE READY"
            )

            Toast.makeText(
                this,
                "Voice interface is being connected.",
                Toast.LENGTH_SHORT
            ).show()
        }

        menuButton.setOnClickListener {

            showMainMenu(
                menuButton
            )
        }

        startCoreAnimation()

        loadConversation()
    }

    // ---------------------------------------------------------
    // GREETING
    // ---------------------------------------------------------

    private fun updateGreeting() {

        val hour =
            Calendar.getInstance()
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

    // ---------------------------------------------------------
    // CONNECTION
    // ---------------------------------------------------------

    private fun isOnline(): Boolean {

        val manager =
            getSystemService(
                CONNECTIVITY_SERVICE
            ) as ConnectivityManager

        val network =
            manager.activeNetwork
                ?: return false

        val capabilities =
            manager.getNetworkCapabilities(
                network
            ) ?: return false

        return capabilities.hasCapability(
            NetworkCapabilities.NET_CAPABILITY_INTERNET
        )
    }

    private fun updateConnectionState() {

        if (isOnline()) {

            statusPill.text =
                "● ONLINE"

            coreStatus.text =
                "ONLINE • LOCAL CORE READY"

        } else {

            statusPill.text =
                "● OFFLINE"

            coreStatus.text =
                "OFFLINE • LOCAL CORE READY"
        }
    }

    // ---------------------------------------------------------
    // CORE
    // ---------------------------------------------------------

    private fun startCoreAnimation() {

        zainCore.animate()
            .scaleX(1.04f)
            .scaleY(1.04f)
            .setDuration(1200)
            .withEndAction {

                zainCore.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(1200)
                    .withEndAction {

                        startCoreAnimation()
                    }
                    .start()
            }
            .start()
    }

    private fun setAssistantState(
        state: String
    ) {

        coreStatus.text =
            state.uppercase()

        zainCore.animate()
            .scaleX(1.09f)
            .scaleY(1.09f)
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

    // ---------------------------------------------------------
    // CONVERSATION
    // ---------------------------------------------------------

    private fun loadConversation() {

        lifecycleScope.launch(
            Dispatchers.IO
        ) {

            val conversations =
                database
                    .conversationDao()
                    .getAll()

            if (
                conversations.isEmpty()
            ) {

                conversationId =
                    database
                        .conversationDao()
                        .insert(
                            Conversation(
                                title =
                                    "ZAIN Conversation"
                            )
                        )

                withContext(
                    Dispatchers.Main
                ) {

                    addZainMessage(
                        "Welcome back.\n\n" +
                            "What are we working on?"
                    )
                }

            } else {

                conversationId =
                    conversations
                        .first()
                        .id

                val messages =
                    database
                        .messageDao()
                        .getForConversation(
                            conversationId
                        )

                withContext(
                    Dispatchers.Main
                ) {

                    if (
                        messages.isEmpty()
                    ) {

                        addZainMessage(
                            "Welcome back.\n\n" +
                                "What are we working on?"
                        )

                    } else {

                        messages.forEach {

                            if (
                                it.role ==
                                "user"
                            ) {

                                addUserMessage(
                                    it.content
                                )

                            } else {

                                addZainMessage(
                                    it.content
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // ---------------------------------------------------------
    // MESSAGE
    // ---------------------------------------------------------

    private fun sendMessage() {

        val text =
            messageInput
                .text
                .toString()
                .trim()

        if (
            text.isEmpty()
        ) {
            return
        }

        messageInput.text.clear()

        addUserMessage(text)

        sendButton.isEnabled =
            false

        voiceButton.isEnabled =
            false

        setAssistantState(
            "THINKING"
        )

        lifecycleScope.launch(
            Dispatchers.IO
        ) {

            database
                .messageDao()
                .insert(
                    Message(
                        conversationId =
                            conversationId,
                        role = "user",
                        content = text
                    )
                )

            database
                .conversationDao()
                .updateTimestamp(
                    conversationId,
                    System.currentTimeMillis()
                )

            val response =
                engine.process(
                    text,
                    conversationId
                )

            database
                .messageDao()
                .insert(
                    Message(
                        conversationId =
                            conversationId,
                        role =
                            "assistant",
                        content =
                            response
                    )
                )

            database
                .conversationDao()
                .updateTimestamp(
                    conversationId,
                    System.currentTimeMillis()
                )

            withContext(
                Dispatchers.Main
            ) {

                addZainMessage(
                    response
                )

                sendButton.isEnabled =
                    true

                voiceButton.isEnabled =
                    true

                updateConnectionState()

                messageInput.requestFocus()
            }
        }
    }

    // ---------------------------------------------------------
    // MENU
    // ---------------------------------------------------------

    private fun showMainMenu(
        anchor: Button
    ) {

        val popup =
            PopupMenu(
                this,
                anchor
            )

        popup.menuInflater.inflate(
            R.menu.main_menu,
            popup.menu
        )

        popup.setOnMenuItemClickListener {

            when (it.itemId) {

                R.id.menu_memory -> {

                    showMemory()

                    true
                }

                R.id.menu_conversations -> {

                    showConversations()

                    true
                }

                R.id.menu_voice -> {

                    showInfo(
                        "Voice",
                        "Voice interaction is being connected."
                    )

                    true
                }

                R.id.menu_skills -> {

                    showInfo(
                        "Skills",
                        "ZAIN's expandable skill system is active and will continue growing."
                    )

                    true
                }

                R.id.menu_settings -> {

                    showInfo(
                        "Settings",
                        "The full ZAIN settings system is coming next."
                    )

                    true
                }

                R.id.menu_about -> {

                    showInfo(
                        "About ZAIN",
                        "ZAIN is a personal AI assistant designed around local memory, expandable skills, online intelligence and future multi-device synchronization."
                    )

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

    // ---------------------------------------------------------
    // MEMORY
    // ---------------------------------------------------------

    private fun showMemory() {

        lifecycleScope.launch(
            Dispatchers.IO
        ) {

            val memories =
                database
                    .memoryDao()
                    .getAll()

            withContext(
                Dispatchers.Main
            ) {

                if (
                    memories.isEmpty()
                ) {

                    showInfo(
                        "ZAIN Memory",
                        "I don't have any saved memories yet."
                    )

                    return@withContext
                }

                val text =
                    buildString {

                        memories
                            .take(20)
                            .forEachIndexed {
                                    index,
                                    memory ->

                                append(
                                    "${index + 1}. "
                                )

                                append(
                                    memory.content
                                )

                                append(
                                    "\n\n"
                                )
                            }
                    }

                showInfo(
                    "🧠 ZAIN Memory",
                    text
                )
            }
        }
    }

    // ---------------------------------------------------------
    // CONVERSATIONS
    // ---------------------------------------------------------

    private fun showConversations() {

        lifecycleScope.launch(
            Dispatchers.IO
        ) {

            val conversations =
                database
                    .conversationDao()
                    .getAll()

            withContext(
                Dispatchers.Main
            ) {

                if (
                    conversations.isEmpty()
                ) {

                    showInfo(
                        "Conversations",
                        "No conversations yet."
                    )

                    return@withContext
                }

                val text =
                    buildString {

                        conversations
                            .take(20)
                            .forEachIndexed {
                                    index,
                                    conversation ->

                                append(
                                    "${index + 1}. "
                                )

                                append(
                                    conversation.title
                                )

                                append(
                                    "\n\n"
                                )
                            }
                    }

                showInfo(
                    "💬 Conversations",
                    text
                )
            }
        }
    }

    // ---------------------------------------------------------
    // INFO
    // ---------------------------------------------------------

    private fun showInfo(
        title: String,
        message: String
    ) {

        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(
                "Close",
                null
            )
            .show()
    }

    // ---------------------------------------------------------
    // CLEAR CHAT
    // ---------------------------------------------------------

    private fun confirmClearChat() {

        AlertDialog.Builder(this)
            .setTitle(
                "Clear current chat?"
            )
            .setMessage(
                "Messages in this conversation will be deleted. Saved memories will remain."
            )
            .setNegativeButton(
                "Cancel",
                null
            )
            .setPositiveButton(
                "Clear"
            ) { _, _ ->

                lifecycleScope.launch(
                    Dispatchers.IO
                ) {

                    database
                        .messageDao()
                        .deleteForConversation(
                            conversationId
                        )

                    withContext(
                        Dispatchers.Main
                    ) {

                        messageContainer
                            .removeAllViews()

                        addZainMessage(
                            "Chat cleared.\n\n" +
                                "My saved memories are still safe."
                        )
                    }
                }
            }
            .show()
    }

    // ---------------------------------------------------------
    // MESSAGE BUBBLES
    // ---------------------------------------------------------

    private fun addUserMessage(
        text: String
    ) {

        addMessageBubble(
            text,
            true
        )
    }

    private fun addZainMessage(
        text: String
    ) {

        addMessageBubble(
            text,
            false
        )
    }

    private fun addMessageBubble(
        text: String,
        isUser: Boolean
    ) {

        val bubble =
            TextView(this)

        bubble.text =
            text

        bubble.textSize =
            15f

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

        bubble.layoutParams =
            params

        messageContainer.addView(
            bubble
        )

        messageScroll.post {

            messageScroll.fullScroll(
                ScrollView.FOCUS_DOWN
            )
        }
    }
}