package com.niljak.firebasechat.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.niljak.firebasechat.repositories.ChatRepository
import com.niljak.firebasechat.repositories.UserRepository
import com.niljak.firebasechat.models.Message
import com.niljak.firebasechat.repositories.CancelFunc
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository
) : ViewModel() {
    private var cancelFunc: CancelFunc?
    private val _messages = MutableLiveData<ChatState>(ChatState.loading())
    val messages: LiveData<ChatState> = _messages

    init {
        cancelFunc = chatRepository
            .listenForChat { messages ->
                _messages.value = ChatState.success(decorateMessages(messages))
            }
    }

    override fun onCleared() {
        cancelFunc?.invoke()
        cancelFunc = null
        super.onCleared()
    }

    fun addMessage(message: String) {
        val user = userRepository.currentUser ?: return

        chatRepository.addMessage(user, message)
    }

    private fun decorateMessages(messages: List<Message>): List<DecoratedMessage> {
        val currentUser = userRepository.currentUser

        return messages.map { DecoratedMessage(it, it.username == currentUser?.name) }
    }
}

data class DecoratedMessage(val message: Message, val isFromMe: Boolean)

sealed class ChatState {
    object Loading : ChatState()
    data class Success(val messages: List<DecoratedMessage>) : ChatState()

    companion object {
        fun loading() = Loading
        fun success(messages: List<DecoratedMessage>) = Success(messages)
    }
}