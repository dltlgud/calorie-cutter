package com.example.main.information

import android.os.Parcelable
import androidx.annotation.DrawableRes
import kotlinx.parcelize.Parcelize

@Parcelize
data class Exercise(
    val name: String,
    val category: String,       // ex) "가슴", "하체", ...
    val target: String,
    val description: String,
    val method: String,
    val caution: String,
    val tip: String,
    val imageName: String,
    val equipment: String,
    var imageRes: Int = 0
) : Parcelable
