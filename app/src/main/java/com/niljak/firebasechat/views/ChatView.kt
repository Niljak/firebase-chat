package com.niljak.firebasechat.views

import android.app.Activity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.niljak.firebasechat.R
import com.niljak.firebasechat.databinding.FragmentChatBinding
import com.niljak.firebasechat.databinding.ViewMessageBinding
import com.niljak.firebasechat.databinding.ViewMessageSelfBinding
import com.niljak.firebasechat.viewmodels.ChatState
import com.niljak.firebasechat.viewmodels.ChatViewModel
import com.niljak.firebasechat.viewmodels.DecoratedMessage

class ChatView : BoundFragment<FragmentChatBinding>(
    R.layout.fragment_chat,
    FragmentChatBinding::inflate
) {
    private val chatViewModel: ChatViewModel by activityViewModels()
    private val adapter by lazy { ChatAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        showLoading()
        binding.messageInput.setOnEditorActionListener { textView, id, _ ->
            if (id == EditorInfo.IME_ACTION_DONE) {
                chatViewModel.addMessage(textView.text.toString())
                textView.text = ""
                closeKeyboard()
                true
            } else {
                false
            }
        }
        binding.chatList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, true)
        binding.chatList.adapter = adapter
        chatViewModel.messages.observe(viewLifecycleOwner, { state ->
            when (state) {
                is ChatState.Loading -> showLoading()
                is ChatState.Success -> showChat(state.messages)
            }
        })
    }

    private fun showChat(messages: List<DecoratedMessage>) {
        adapter.updateMessages(messages)
        binding.chatList.visibility = View.VISIBLE
        binding.progressIndicator.visibility = View.INVISIBLE
    }

    private fun showLoading() {
        binding.chatList.visibility = View.INVISIBLE
        binding.progressIndicator.visibility = View.VISIBLE
    }

    private fun closeKeyboard() {
        activity?.let {
            val windowToken = it.currentFocus?.windowToken ?: return@let
            val inputMethodManger = it.getSystemService(Activity.INPUT_METHOD_SERVICE)

            if (inputMethodManger is InputMethodManager) {
                inputMethodManger.hideSoftInputFromWindow(windowToken, 0)
            }
        }
    }
}

private class ChatAdapter : ListAdapter<DecoratedMessage, ChatAdapter.ViewHolder>(DiffCallback)  {
    companion object {
        private const val MessageFromOthers = 0
        private const val MessageFromSelf = 1
    }

    private object DiffCallback : DiffUtil.ItemCallback<DecoratedMessage>() {
        override fun areItemsTheSame(
            oldItem: DecoratedMessage,
            newItem: DecoratedMessage
        ): Boolean = oldItem.message.id == newItem.message.id

        override fun areContentsTheSame(
            oldItem: DecoratedMessage,
            newItem: DecoratedMessage
        ): Boolean = oldItem == newItem
    }

    sealed class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
    class ViewHolderOther(val binding: ViewMessageBinding) : ViewHolder(binding.root)
    class ViewHolderSelf(val binding: ViewMessageSelfBinding) : ViewHolder(binding.root)

    fun updateMessages(messages: List<DecoratedMessage>) {
        submitList(messages)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        when (viewType) {
            MessageFromOthers -> ViewHolderOther(
                ViewMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            MessageFromSelf -> ViewHolderSelf(
                ViewMessageSelfBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
            else -> throw RuntimeException("Invalid viewtype")
        }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val message = currentList[position].message
        when (holder) {
            is ViewHolderOther -> {
                holder.binding.label.text = holder.binding.root.context.getString(
                    R.string.user_and_date,
                    message.username,
                    message.timestamp
                )
                holder.binding.message.text = message.message
            }
            is ViewHolderSelf -> {
                holder.binding.label.text = holder.binding.root.context.getString(
                    R.string.user_and_date,
                    message.username,
                    message.timestamp
                )
                holder.binding.message.text = message.message
            }
        }

    }

    override fun getItemViewType(position: Int): Int =
        if (currentList[position].isFromMe) {
            MessageFromSelf
        } else {
            MessageFromOthers
        }


    override fun getItemCount(): Int = currentList.size
}