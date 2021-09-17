package com.niljak.firebasechat.repositories

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.niljak.firebasechat.models.Message
import com.niljak.firebasechat.models.User
import java.util.*
import javax.inject.Inject

class ChatRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    companion object {
        private const val ChatCollection = "chat"
    }

    fun listenForChat(listener: (List<Message>) -> Unit): CancelFunc  {
        val query = firestore
            .collection(ChatCollection)
            .orderBy("timestamp")

        val registration = query
            .addSnapshotListener { querySnapshot, _ ->
                querySnapshot?.let {
                    listener(it.documents.mapNotNull(DocumentSnapshot::asMessage))
                }
            }

        return { registration.remove() }
    }

    fun addMessage(user: User, message: String) {
        firestore.collection(ChatCollection).add(
            hashMapOf(
                "username" to user.name,
                "timestamp" to Date(),
                "message" to message
            )
        )
    }
}

private fun DocumentSnapshot.asMessage(): Message? {
    val username = getString("username") ?: return null
    val timestamp = getDate("timestamp") ?: return null
    val message = getString("message") ?: return null

    return Message(id, username, timestamp, message)
}