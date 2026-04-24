package com.example.main.repository

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Date

class UserRepository {

    private val db = FirebaseFirestore.getInstance()

    fun getUser(
        uid: String,
        onSuccess: (DocumentSnapshot) -> Unit,
        onFailure: () -> Unit
    ) {
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { onSuccess(it) }
            .addOnFailureListener { onFailure() }
    }

    fun saveUser(
        uid: String,
        data: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users").document(uid)
            .set(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    fun updateUser(
        uid: String,
        updates: Map<String, Any>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("users").document(uid)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    fun addWeightRecord(uid: String, weight: Double) {
        db.collection("users").document(uid)
            .collection("weight_records")
            .add(mapOf("weight" to weight, "timestamp" to Date()))
    }

    fun getWeightRecords(
        uid: String,
        onSuccess: (List<DocumentSnapshot>) -> Unit
    ) {
        db.collection("users").document(uid)
            .collection("weight_records")
            .orderBy("timestamp")
            .get()
            .addOnSuccessListener { result -> onSuccess(result.documents) }
    }
}
