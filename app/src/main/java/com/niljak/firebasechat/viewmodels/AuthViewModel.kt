package com.niljak.firebasechat.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.niljak.firebasechat.models.User
import com.niljak.firebasechat.repositories.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val userRepository: UserRepository
): ViewModel() {
    private val _user = MutableLiveData<User?>(userRepository.currentUser)
    val user: LiveData<User?> = _user

    private val _errors = MutableLiveData<Set<ValidationError>>(setOf())
    val errors: LiveData<Set<ValidationError>> = _errors

    init {
        userRepository.listenToAuthChanges {
            _user.value = it
        }
    }

    fun signIn(email: String, password: String) {
        val errors = mutableSetOf<ValidationError>()
        if (email.isEmpty()) {
            errors.add(ValidationError.MissingEmail)
        }
        if (password.isEmpty()) {
            errors.add(ValidationError.MissingPassword)
        }

        if (errors.isNotEmpty()) {
            _errors.value = errors
            return
        }

        userRepository.signIn(email, password)
    }

    fun signUp(username: String, email: String, password: String, repeatPassword: String) {
        val errors = mutableSetOf<ValidationError>()
        if (username.isEmpty()) {
            errors.add(ValidationError.MissingUsername)
        }
        if (email.isEmpty()) {
            errors.add(ValidationError.MissingEmail)
        }
        when {
            password.isEmpty() -> {
                errors.add(ValidationError.MissingPassword)
            }
            password.length < 6 -> {
                errors.add(ValidationError.PasswordTooShort)
            }
            password != repeatPassword -> {
                errors.add(ValidationError.MismatchedPasswords)
            }
        }

        if (errors.isNotEmpty()) {
            _errors.value = errors
            return
        }

        viewModelScope.launch {
            userRepository.signUp(username, email, password)
        }
    }
}

enum class ValidationError {
    MissingUsername, MissingEmail, MissingPassword, PasswordTooShort, MismatchedPasswords
}