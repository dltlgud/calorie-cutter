package com.example.main.diet

data class DietRecord(
    val mealType: String,   // "아침", "점심", "저녁", "간식"
    val foodName: String,
    val calories: Int
)
