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
import com.google.ai.client.generativeai.Chat
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.launch

// 1. Модель данных для сообщения
data class ChatMessage(val text: String, val isFromUser: Boolean)

// 2. ViewModel: "Мозг", который выживает при переключении вкладок
class InsightsViewModel : ViewModel() {
    val messageList = mutableListOf<ChatMessage>()
    lateinit var chatSession: Chat

    var currentDataContext: String = "Менеджер еще не загрузил данные."

    init {
        // Инициализируем ИИ один раз при запуске приложения
        val generativeModel = GenerativeModel(
            modelName = "gemini-2.5-flash",
            apiKey = BuildConfig.GEMINI_API_KEY,
            systemInstruction = content {
                text("Ты — бизнес-ассистент в приложении Decision Forge. Отвечай кратко, профессионально и по делу. Помогай анализировать графики и риски.")
            }
        )
        chatSession = generativeModel.startChat()

        // Стартовое приветствие добавляется только один раз
        messageList.add(ChatMessage("Привет! Я ваш AI-ассистент Decision Forge. Я могу помочь вам проанализировать решения, дать рекомендации и объяснить данные. Чем могу помочь?", isFromUser = false))
    }
}

// 3. Адаптер для списка
class ChatAdapter(private val messages: List<ChatMessage>) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

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

// 4. Сам фрагмент
class InsightsFragment : Fragment() {

    private var _binding: FragmentInsightsBinding? = null
    private val binding get() = _binding!!

    // Ссылка на нашу бессмертную ViewModel
    private lateinit var viewModel: InsightsViewModel
    private lateinit var chatAdapter: ChatAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentInsightsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Подключаемся к ViewModel, привязанной к Activity (чтобы она не умирала при смене вкладок)
        viewModel = ViewModelProvider(requireActivity())[InsightsViewModel::class.java]

        // Передаем адаптеру список сообщений из ViewModel
        chatAdapter = ChatAdapter(viewModel.messageList)
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChat.adapter = chatAdapter

        // Скроллим вниз, если там уже есть история сообщений
        if (viewModel.messageList.isNotEmpty()) {
            binding.rvChat.scrollToPosition(viewModel.messageList.size - 1)
        }

        // Логика кнопки "Отправить"
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {
                sendMessageToBot(text)
            }
        }

        // Кнопки-подсказки (Chips)
        binding.chipSummarize.setOnClickListener { sendMessageToBot("Сделай краткую сводку по проекту") }
        binding.chipAnalyze.setOnClickListener { sendMessageToBot("Проанализируй текущие риски") }
        binding.chipForecast.setOnClickListener { sendMessageToBot("Какой прогноз на следующий месяц?") }
    }

    private fun sendMessageToBot(text: String) {
        // 1. Показываем сообщение пользователя в чате
        viewModel.messageList.add(ChatMessage(text, isFromUser = true))
        chatAdapter.notifyItemInserted(viewModel.messageList.size - 1)
        binding.rvChat.scrollToPosition(viewModel.messageList.size - 1)

        // Очищаем поле ввода
        binding.etMessage.text.clear()

        // 2. Добавляем "Анализирую..." (временное сообщение)
        val typingIndex = viewModel.messageList.size
        viewModel.messageList.add(ChatMessage("Анализирую данные...", isFromUser = false))
        chatAdapter.notifyItemInserted(typingIndex)
        binding.rvChat.scrollToPosition(typingIndex)

        // 3. Отправляем запрос через ИИ-сессию из ViewModel
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val hiddenPrompt = """
                    ${viewModel.currentDataContext}
                    
                    Вопрос пользователя: $text
                """.trimIndent()

                // Отправляем скрытый промпт нейросети
                val response = viewModel.chatSession.sendMessage(hiddenPrompt)

                // 4. Обновляем UI
                viewModel.messageList[typingIndex] = ChatMessage(response.text ?: "Не смог сгенерировать ответ", isFromUser = false)
                chatAdapter.notifyItemChanged(typingIndex)

            } catch (e: Exception) {
                viewModel.messageList[typingIndex] = ChatMessage("Ошибка подключения: ${e.localizedMessage}", isFromUser = false)
                chatAdapter.notifyItemChanged(typingIndex)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}