package com.example.main.noticeboard

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.widget.Toolbar
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.main.R
import com.example.main.model.Comment
import com.example.main.repository.PostRepository
import com.example.main.repository.UserRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import java.util.concurrent.TimeUnit

class PostDetailActivity : AppCompatActivity() {

    private var postId: String? = null
    private var title: String? = null
    private var content: String? = null
    private var imageUrl: String? = null
    private var category: String? = null
    private var timestampMillis: Long = 0L
    private var authorId: String? = null
    private var authorName: String = "익명"
    private var currentUserName: String = "사용자"

    private val repository = PostRepository()
    private val userRepository = UserRepository()
    private lateinit var commentAdapter: CommentAdapter
    private val comments = mutableListOf<Comment>()
    private var commentListener: ListenerRegistration? = null
    private var likeListener: ListenerRegistration? = null

    private lateinit var iconLike: ImageView
    private lateinit var textLike: TextView

    private val editPostLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) finish()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_post_detail)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        category = intent.getStringExtra("category")
        title = intent.getStringExtra("title")
        content = intent.getStringExtra("content")
        imageUrl = intent.getStringExtra("imageUrl")
        postId = intent.getStringExtra("postId")
        timestampMillis = intent.getLongExtra("timestamp", System.currentTimeMillis())
        authorId = intent.getStringExtra("authorId")
        authorName = intent.getStringExtra("authorName") ?: "익명"

        supportActionBar?.title = when (category) {
            "schedule" -> "일정 공유"
            "free" -> "자유 게시판"
            else -> "community2"
        }

        val textTitle = findViewById<TextView>(R.id.textTitle)
        val textContent = findViewById<TextView>(R.id.textContent)
        val imageView = findViewById<ImageView>(R.id.imageView)
        val recyclerViewComments = findViewById<RecyclerView>(R.id.recyclerViewComments)
        val editComment = findViewById<EditText>(R.id.editComment)
        val btnSendComment = findViewById<ImageButton>(R.id.btnSendComment)
        val textWriter = findViewById<TextView>(R.id.textWriter)
        val layoutLike = findViewById<LinearLayout>(R.id.layoutLike)
        textLike = findViewById(R.id.textLike)
        iconLike = findViewById(R.id.iconLike)

        textTitle.text = title
        textContent.text = content
        textWriter.text = "$authorName · ${getTimeAgo(System.currentTimeMillis() - timestampMillis)}"

        if (!imageUrl.isNullOrEmpty()) {
            imageView.visibility = View.VISIBLE
            Glide.with(this).load(imageUrl).into(imageView)
        } else {
            imageView.visibility = View.GONE
        }

        commentAdapter = CommentAdapter(comments)
        recyclerViewComments.adapter = commentAdapter
        recyclerViewComments.layoutManager = LinearLayoutManager(this)

        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "anonymous"
        val id = postId ?: return

        // 현재 유저 닉네임 로드
        userRepository.getUser(userId,
            onSuccess = { doc -> currentUserName = doc.getString("username") ?: "사용자" },
            onFailure = {}
        )

        listenToComments(id)
        listenToLikeCount(id)

        repository.getUserLikeStatus(id, userId) { isLiked ->
            setLikeIcon(isLiked)
        }

        layoutLike.setOnClickListener {
            repository.toggleLike(id, userId) { isLiked -> setLikeIcon(isLiked) }
        }

        btnSendComment.setOnClickListener {
            val text = editComment.text.toString().trim()
            if (text.isNotEmpty()) {
                repository.addComment(id, Comment(userId = currentUserName, content = text),
                    onSuccess = { editComment.setText("") },
                    onFailure = { Toast.makeText(this, "댓글 업로드 실패", Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        commentListener?.remove()
        likeListener?.remove()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_post_detail, menu)
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
        val isAuthor = !authorId.isNullOrEmpty() && currentUserId == authorId
        menu?.findItem(R.id.action_edit)?.isVisible = isAuthor
        menu?.findItem(R.id.action_delete)?.isVisible = isAuthor
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_edit -> {
                editPostLauncher.launch(Intent(this, WritePostActivity::class.java).apply {
                    putExtra("mode", "edit")
                    putExtra("title", title)
                    putExtra("content", content)
                    putExtra("imageUrl", imageUrl)
                    putExtra("postId", postId)
                    putExtra("category", category)
                })
                true
            }
            R.id.action_delete -> {
                postId?.let { id ->
                    repository.deletePost(id,
                        onSuccess = {
                            Toast.makeText(this, "삭제 완료", Toast.LENGTH_SHORT).show()
                            finish()
                        },
                        onFailure = { Toast.makeText(this, "삭제 실패", Toast.LENGTH_SHORT).show() }
                    )
                }
                true
            }
            android.R.id.home -> {
                onBackPressedDispatcher.onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun listenToComments(postId: String) {
        val layoutNoComments = findViewById<LinearLayout>(R.id.layoutNoComments)
        val recyclerViewComments = findViewById<RecyclerView>(R.id.recyclerViewComments)
        val textCommentCount = findViewById<TextView>(R.id.textCommentCount)

        commentListener = repository.listenToComments(postId) { updatedComments ->
            comments.clear()
            comments.addAll(updatedComments)
            commentAdapter.notifyDataSetChanged()
            textCommentCount.text = "댓글 ${updatedComments.size}"
            layoutNoComments.visibility = if (updatedComments.isEmpty()) View.VISIBLE else View.GONE
            recyclerViewComments.visibility = if (updatedComments.isEmpty()) View.GONE else View.VISIBLE
        }
    }

    private fun listenToLikeCount(postId: String) {
        likeListener = repository.listenToLikeCount(postId) { count ->
            textLike.text = "공감 $count"
        }
    }

    private fun setLikeIcon(isLiked: Boolean) {
        iconLike.setColorFilter(
            ContextCompat.getColor(this, if (isLiked) R.color.primary else R.color.on_surface_secondary)
        )
    }

    private fun getTimeAgo(diffMillis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
        val days = TimeUnit.MILLISECONDS.toDays(diffMillis)
        return when {
            minutes < 1 -> "방금 전"
            minutes < 60 -> "${minutes}분 전"
            hours < 24 -> "${hours}시간 전"
            else -> "${days}일 전"
        }
    }
}
