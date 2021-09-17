package com.niljak.firebasechat.models

import java.util.Date

data class Message(
    val id: String,
    val username: String,
    val timestamp: Date,
    val message: String
)