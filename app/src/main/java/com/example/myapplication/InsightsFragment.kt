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

// 3. Сам Фрагмент
class InsightsFragment : Fragment() {

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

        // Настраиваем RecyclerView
        chatAdapter = ChatAdapter(messageList)
        binding.rvChat.layoutManager = LinearLayoutManager(requireContext())
        binding.rvChat.adapter = chatAdapter

        // Добавляем стартовое приветствие от ИИ (как на макете)
        addAiMessage("Привет! Я ваш AI-ассистент Decision Forge. Я могу помочь вам проанализировать решения, дать рекомендации и объяснить данные. Чем могу помочь?")

        // Логика нажатия на кнопку "Отправить"
        binding.btnSend.setOnClickListener {
            val text = binding.etMessage.text.toString().trim()
            if (text.isNotEmpty()) {

                // 1. Добавляем сообщение пользователя
                messageList.add(ChatMessage(text, isFromUser = true))
                chatAdapter.notifyItemInserted(messageList.size - 1)

                // Прокручиваем список вниз
                binding.rvChat.scrollToPosition(messageList.size - 1)

                // Очищаем поле ввода
                binding.etMessage.text.clear()

                // 2. Имитируем задержку (как будто ИИ думает) и выдаем заглушку
                Handler(Looper.getMainLooper()).postDelayed({
                    addAiMessage("Временно не работает")
                }, 1000) // Задержка 1 секунда
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