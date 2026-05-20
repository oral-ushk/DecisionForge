package com.example.myapplication

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentInsightsBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

data class ChatMessage(val text: String, val isFromUser: Boolean)

private data class ApiMessage(val role: String, val content: String)

class InsightsViewModel : ViewModel() {
    val messageList = mutableListOf<ChatMessage>()
    private val conversationHistory = mutableListOf<ApiMessage>()

    var currentDataContext: String = "Менеджер еще не загрузил данные."

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    init {
        messageList.add(
            ChatMessage(
                "Привет! Я ваш AI-ассистент Decision Forge на базе Claude. " +
                "Я могу помочь вам проанализировать решения, дать рекомендации и объяснить данные. " +
                "Чем могу помочь?",
                isFromUser = false
            )
        )
    }

    suspend fun sendMessage(userText: String): String = withContext(Dispatchers.IO) {
        val hiddenPrompt = buildString {
            append(currentDataContext)
            append("\n\nВопрос пользователя: ")
            append(userText)
        }

        conversationHistory.add(ApiMessage("user", hiddenPrompt))

        val messagesArray = JSONArray().apply {
            conversationHistory.forEach { msg ->
                put(JSONObject().apply {
                    put("role", msg.role)
                    put("content", msg.content)
                })
            }
        }

        val body = JSONObject().apply {
            put("model", "claude-haiku-4-5-20251001")
            put("max_tokens", 1024)
            put("system", "Ты — бизнес-ассистент в приложении Decision Forge. Отвечай кратко, профессионально и по делу. Помогай анализировать графики и риски.")
            put("messages", messagesArray)
        }.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url("https://api.anthropic.com/v1/messages")
            .addHeader("x-api-key", BuildConfig.CLAUDE_API_KEY)
            .addHeader("anthropic-version", "2023-06-01")
            .post(body)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() ?: throw Exception("Пустой ответ от сервера")

        if (!response.isSuccessful) {
            throw Exception("Ошибка API ${response.code}")
        }

        val assistantText = JSONObject(responseBody)
            .getJSONArray("content")
            .getJSONObject(0)
            .getString("text")

        conversationHistory.add(ApiMessage("assistant", assistantText))
        assistantText
    }
}

class ChatAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val VIEW_TYPE_USER = 1
    private val VIEW_TYPE_AI = 2

    override fun getItemViewType(position: Int): Int =
        if (messages[position].isFromUser) VIEW_TYPE_USER else VIEW_TYPE_AI

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_USER) {
            UserViewHolder(inflater.inflate(R.layout.item_chat_user, parent, false))
        } else {
            AiViewHolder(inflater.inflate(R.layout.item_chat_ai, parent, false))
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        when (holder) {
            is UserViewHolder -> holder.tvMessage.text = message.text
            is AiViewHolder -> holder.tvMessage.text = message.text
        }
    }

    override fun getItemCount(): Int = messages.size

    class UserViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tvUserMessage)
    }

    class AiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvMessage: TextView = itemView.findViewById(R.id.tvAiMessage)
    }
}

class InsightsFragment : Fragment() {

    private var _binding: FragmentInsightsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: InsightsViewModel
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInsightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(requireActivity())[InsightsViewModel::class.java]
        chatAdapter = ChatAdapter(viewModel.messageList)
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChat.adapter = chatAdapter

        if (viewModel.messageList.isNotEmpty()) {
            binding.rvChat.scrollToPosition(viewModel.messageList.size - 1)
        }

        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) sendMessageToBot(text)
        }

        binding.chipSummarize.setOnClickListener { sendMessageToBot("Сделай краткую сводку по проекту") }
        binding.chipAnalyze.setOnClickListener { sendMessageToBot("Проанализируй текущие риски") }
        binding.chipForecast.setOnClickListener { sendMessageToBot("Какой прогноз на следующий месяц?") }
    }

    private fun sendMessageToBot(text: String) {
        viewModel.messageList.add(ChatMessage(text, isFromUser = true))
        chatAdapter.notifyItemInserted(viewModel.messageList.size - 1)
        binding.rvChat.scrollToPosition(viewModel.messageList.size - 1)
        binding.etMessage.text.clear()

        val typingIndex = viewModel.messageList.size
        viewModel.messageList.add(ChatMessage("Анализирую данные...", isFromUser = false))
        chatAdapter.notifyItemInserted(typingIndex)
        binding.rvChat.scrollToPosition(typingIndex)

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = viewModel.sendMessage(text)
                viewModel.messageList[typingIndex] = ChatMessage(response, isFromUser = false)
            } catch (e: Exception) {
                viewModel.messageList[typingIndex] = ChatMessage(
                    "Ошибка: ${e.localizedMessage}",
                    isFromUser = false
                )
            }
            chatAdapter.notifyItemChanged(typingIndex)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
