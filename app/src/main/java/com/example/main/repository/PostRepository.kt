package com.example.main.repository

import android.net.Uri
import com.example.main.model.Comment
import com.example.main.model.Post
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage

class PostRepository {

    private val db = FirebaseFirestore.getInstance()
    private val storage = FirebaseStorage.getInstance()

    fun getPosts(
        category: String,
        onSuccess: (List<Post>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("posts")
            .whereEqualTo("category", category)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                val posts = documents.map { doc ->
                    doc.toObject(Post::class.java).also { it.id = doc.id }
                }
                onSuccess(posts)
            }
            .addOnFailureListener(onFailure)
    }

    fun createPost(
        title: String,
        content: String,
        category: String,
        imageUri: Uri?,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val uid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val authorName = doc.getString("username") ?: "사용자"
                if (imageUri != null) {
                    val fileName = "IMG_${System.currentTimeMillis()}.jpg"
                    val ref = storage.reference.child("postImages/$fileName")
                    ref.putFile(imageUri)
                        .continueWithTask { task ->
                            if (!task.isSuccessful) throw task.exception ?: Exception("업로드 실패")
                            ref.downloadUrl
                        }
                        .addOnSuccessListener { uri ->
                            savePostToFirestore(title, content, category, uri.toString(), uid, authorName, onSuccess, onFailure)
                        }
                        .addOnFailureListener(onFailure)
                } else {
                    savePostToFirestore(title, content, category, "", uid, authorName, onSuccess, onFailure)
                }
            }
            .addOnFailureListener { onFailure(it) }
    }

    private fun savePostToFirestore(
        title: String,
        content: String,
        category: String,
        imageUrl: String,
        authorId: String,
        authorName: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val post = hashMapOf(
            "title" to title,
            "content" to content,
            "category" to category,
            "timestamp" to System.currentTimeMillis(),
            "timestamp2" to Timestamp.now(),
            "imageUrl" to imageUrl,
            "authorId" to authorId,
            "authorName" to authorName
        )
        db.collection("posts").add(post)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    fun updatePost(
        postId: String,
        title: String,
        content: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        val updates = mapOf(
            "title" to title,
            "content" to content,
            "timestamp" to System.currentTimeMillis()
        )
        db.collection("posts").document(postId)
            .update(updates)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    fun deletePost(
        postId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("posts").document(postId)
            .delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    fun addComment(
        postId: String,
        comment: Comment,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        db.collection("posts").document(postId)
            .collection("comments")
            .add(comment)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener(onFailure)
    }

    fun listenToComments(
        postId: String,
        onUpdate: (List<Comment>) -> Unit
    ): ListenerRegistration {
        return db.collection("posts").document(postId)
            .collection("comments")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, _ ->
                val comments = snapshot?.map { it.toObject(Comment::class.java) } ?: emptyList()
                onUpdate(comments)
            }
    }

    fun listenToLikeCount(
        postId: String,
        onUpdate: (Int) -> Unit
    ): ListenerRegistration {
        return db.collection("posts").document(postId)
            .collection("likes")
            .addSnapshotListener { snapshot, _ ->
                onUpdate(snapshot?.size() ?: 0)
            }
    }

    fun getUserLikeStatus(
        postId: String,
        userId: String,
        onResult: (Boolean) -> Unit
    ) {
        db.collection("posts").document(postId)
            .collection("likes").document(userId)
            .get()
            .addOnSuccessListener { onResult(it.exists()) }
    }

    fun toggleLike(
        postId: String,
        userId: String,
        onToggled: (isLiked: Boolean) -> Unit
    ) {
        val likeRef = db.collection("posts").document(postId)
            .collection("likes").document(userId)
        likeRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                likeRef.delete()
                onToggled(false)
            } else {
                likeRef.set(mapOf("liked" to true))
                onToggled(true)
            }
        }
    }

    fun getPostsByAuthors(
        authorIds: List<String>,
        onSuccess: (List<Post>) -> Unit
    ) {
        if (authorIds.isEmpty()) { onSuccess(emptyList()); return }
        val chunks = authorIds.chunked(30)
        val result = mutableListOf<Post>()
        var remaining = chunks.size
        chunks.forEach { chunk ->
            db.collection("posts")
                .whereIn("authorId", chunk)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { docs ->
                    result.addAll(docs.map { doc -> doc.toObject(Post::class.java).also { it.id = doc.id } })
                    if (--remaining == 0) { result.sortByDescending { it.timestamp }; onSuccess(result) }
                }
                .addOnFailureListener {
                    if (--remaining == 0) { result.sortByDescending { it.timestamp }; onSuccess(result) }
                }
        }
    }
}
