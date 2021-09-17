package com.niljak.firebasechat.repositories

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.niljak.firebasechat.models.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class UserRepository @Inject constructor(
    private val auth: FirebaseAuth
) {
    val currentUser: User?
        get() = auth.currentUser?.let { mapFirebaseUserToUser(it) }

    fun listenToAuthChanges(listener: (User?) -> Unit) {
        auth.addAuthStateListener {
            listener(auth.currentUser?.let { mapFirebaseUserToUser(it) })
        }
    }

    fun signIn(email: String, password: String)
        = auth.signInWithEmailAndPassword(email, password)

    suspend fun signUp(name: String, email: String, password: String) {
        val firebaseUser = auth.createUserWithEmailAndPassword(email, password).await().user ?: return

        firebaseUser.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(name).build()).await()
    }

    fun signOut() = auth.signOut()

    private fun mapFirebaseUserToUser(firebaseUser: FirebaseUser): User? {
        val email = firebaseUser.email ?: return null
        val name = firebaseUser.displayName ?: return null
        return User(name, email)
    }
}