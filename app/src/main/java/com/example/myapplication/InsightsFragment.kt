package com.example.myapplication

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.myapplication.databinding.FragmentInsightsBinding // Проверь правильность импорта
import androidx.lifecycle.lifecycleScope // Добавь в начало файла
import kotlinx.coroutines.launch // Добавь в начало файла
import com.google.ai.client.generativeai.GenerativeModel // Добавь в начало файла
// 1. Модель данных для сообщения
data class ChatMessage(val text: String, val isFromUser: Boolean)

// 2. Адаптер для списка
class ChatAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    // Типы сообщений
    private val VIEW_TYPE_USER = 1
    private val VIEW_TYPE_AI = 2

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isFromUser) VIEW_TYPE_USER else VIEW_TYPE_AI
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == VIEW_TYPE_USER) {
            val view = inflater.inflate(R.layout.item_chat_user, parent, false)
            UserViewHolder(view)
        } else {
            val view = inflater.inflate(R.layout.item_chat_ai, parent, false)
            AiViewHolder(view)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if (holder is UserViewHolder) {
            holder.tvMessage.text = message.text
        } else if (holder is AiViewHolder) {
            holder.tvMessage.text = message.text
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

class InsightsFragment  : Fragment() {

    private var _binding: FragmentInsightsBinding? = null
    private val binding get() = _binding!!

    // Список сообщений
    private val messageList = mutableListOf<ChatMessage>()
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInsightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chatAdapter = ChatAdapter(messageList)
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChat.adapter = chatAdapter

        // Стартовое приветствие
        if (messageList.isEmpty()) {
            addAiMessage("Привет! Я ваш AI-ассистент Decision Forge. Я могу помочь вам проанализировать решения, дать рекомендации и объяснить данные. Чем могу помочь?")
        }

        // Обновленная логика кнопки "Отправить"
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessageToBot(text)
            }
        }

        // Оживляем кнопки-подсказки (Chips)
        binding.chipSummarize.setOnClickListener { sendMessageToBot("Summarize") }
        binding.chipAnalyze.setOnClickListener { sendMessageToBot("Analyze") }
        binding.chipForecast.setOnClickListener { sendMessageToBot("Forecast") }
    }

    // Вспомогательная функция для отправки сообщения (чтобы не дублировать код)
    private fun sendMessageToBot(text: String) {
        // 1. Показываем сообщение пользователя в чате
        messageList.add(ChatMessage(text, isFromUser = true))
        chatAdapter.notifyItemInserted(messageList.size - 1)
        binding.rvChat.scrollToPosition(messageList.size - 1)

        // Очищаем поле ввода
        binding.etMessage.text.clear()

        // 2. Добавляем "ИИ печатает..." (временное сообщение)
        val typingIndex = messageList.size
        messageList.add(ChatMessage("Думаю...", isFromUser = false))
        chatAdapter.notifyItemInserted(typingIndex)
        binding.rvChat.scrollToPosition(typingIndex)

        // 3. Отправляем запрос к настоящему ИИ в фоновом потоке (Корутины)
        lifecycleScope.launch {
            try {
                // Инициализируем модель Gemini
                val generativeModel = GenerativeModel(
                    modelName = "gemini-2.5-flash",
                    apiKey = BuildConfig.GEMINI_API_KEY // <-- Теперь ключ берется из секретного места!
                )
                // Формируем промпт. Мы говорим ИИ, кто он такой, чтобы он отвечал в стиле приложения.
                val systemPrompt = "Ты ИИ-ассистент в бизнес-приложении Decision Forge. Отвечай кратко и по делу. Вопрос пользователя: $text"

                // Ждем ответ от сервера
                val response = generativeModel.generateContent(systemPrompt)

                // 4. Обновляем UI: убираем "Думаю..." и ставим реальный ответ
                messageList[typingIndex] = ChatMessage(response.text ?: "Не смог сгенерировать ответ", isFromUser = false)
                chatAdapter.notifyItemChanged(typingIndex)

            } catch (e: Exception) {
                // Если нет интернета или ошибка ключа
                messageList[typingIndex] = ChatMessage("Ошибка сети: ${e.message}", isFromUser = false)
                chatAdapter.notifyItemChanged(typingIndex)
            }
        }
    }

    private fun addAiMessage(text: String) {
        messageList.add(ChatMessage(text, isFromUser = false))
        chatAdapter.notifyItemInserted(messageList.size - 1)
        binding.rvChat.scrollToPosition(messageList.size - 1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}