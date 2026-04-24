package com.example.main.repository

import com.google.firebase.firestore.FirebaseFirestore

class FollowRepository {

    private val db = FirebaseFirestore.getInstance()

    fun follow(myUid: String, targetUid: String, targetName: String, onDone: () -> Unit) {
        val batch = db.batch()
        val followingRef = db.collection("users").document(myUid)
            .collection("following").document(targetUid)
        val followerRef = db.collection("users").document(targetUid)
            .collection("followers").document(myUid)
        batch.set(followingRef, mapOf("uid" to targetUid, "username" to targetName))
        batch.set(followerRef, mapOf("uid" to myUid))
        batch.commit().addOnCompleteListener { onDone() }
    }

    fun unfollow(myUid: String, targetUid: String, onDone: () -> Unit) {
        val batch = db.batch()
        val followingRef = db.collection("users").document(myUid)
            .collection("following").document(targetUid)
        val followerRef = db.collection("users").document(targetUid)
            .collection("followers").document(myUid)
        batch.delete(followingRef)
        batch.delete(followerRef)
        batch.commit().addOnCompleteListener { onDone() }
    }

    fun isFollowing(myUid: String, targetUid: String, onResult: (Boolean) -> Unit) {
        db.collection("users").document(myUid)
            .collection("following").document(targetUid)
            .get()
            .addOnSuccessListener { onResult(it.exists()) }
            .addOnFailureListener { onResult(false) }
    }

    fun getFollowerCount(uid: String, onResult: (Int) -> Unit) {
        db.collection("users").document(uid)
            .collection("followers").get()
            .addOnSuccessListener { onResult(it.size()) }
            .addOnFailureListener { onResult(0) }
    }

    fun getFollowingCount(uid: String, onResult: (Int) -> Unit) {
        db.collection("users").document(uid)
            .collection("following").get()
            .addOnSuccessListener { onResult(it.size()) }
            .addOnFailureListener { onResult(0) }
    }

    fun getFollowingList(myUid: String, onResult: (List<String>) -> Unit) {
        db.collection("users").document(myUid)
            .collection("following").get()
            .addOnSuccessListener { snapshot ->
                val uids = snapshot.documents.map { it.id }
                onResult(uids)
            }
            .addOnFailureListener { onResult(emptyList()) }
    }
}
