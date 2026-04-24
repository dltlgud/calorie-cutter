package com.example.main.model

data class Comment(
    val userId: String = "",
    val content: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
