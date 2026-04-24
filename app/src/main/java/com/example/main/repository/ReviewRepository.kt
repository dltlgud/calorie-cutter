package com.example.main.repository

import com.example.main.information.Review
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

class ReviewRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getReviews(
        exerciseName: String,
        onSuccess: (List<Review>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("reviews")
            .whereEqualTo("exerciseName", exerciseName)
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .get()
            .addOnSuccessListener { snaps ->
                val reviews = snaps.map { it.toObject(Review::class.java) }
                onSuccess(reviews)
            }
            .addOnFailureListener(onFailure)
    }

    fun addReview(
        review: Review,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("reviews")
            .add(review)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }
}
