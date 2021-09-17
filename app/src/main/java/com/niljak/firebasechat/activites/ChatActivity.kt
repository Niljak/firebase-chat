package com.niljak.firebasechat.activites

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.add
import androidx.fragment.app.commit
import com.niljak.firebasechat.R
import com.niljak.firebasechat.viewmodels.AuthViewModel
import com.niljak.firebasechat.views.AuthView
import com.niljak.firebasechat.views.ChatView
import com.niljak.firebasechat.views.SplashView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ChatActivity : AppCompatActivity(R.layout.activity_chat) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        supportFragmentManager.commit {
            add<SplashView>(R.id.container)
        }

        val authViewModel by viewModels<AuthViewModel>()
        authViewModel.user.observe(this, { user ->
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                if (user == null) {
                    replace(R.id.container, AuthView())
                } else {
                    replace(R.id.container, ChatView())
                }
            }
        })
    }
}