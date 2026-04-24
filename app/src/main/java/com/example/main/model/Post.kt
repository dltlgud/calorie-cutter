package com.example.main.model

import com.google.firebase.Timestamp

data class Post(
    var timestamp2: Timestamp? = null,
    val imageUrl: String = "",
    var id: String = "",
    val title: String = "",
    val content: String = "",
    val category: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val authorId: String = "",
    val authorName: String = ""
)
