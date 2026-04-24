package com.example.main.noticeboard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import com.example.main.R
import com.example.main.auth.LoginActivity
import com.example.main.repository.PostRepository
import com.google.firebase.auth.FirebaseAuth

class WritePostActivity : AppCompatActivity() {

    private lateinit var editTitle: EditText
    private lateinit var editContent: EditText
    private lateinit var btnUpload: Button
    private lateinit var imagePreview: ImageView
    private var selectedImageUri: Uri? = null

    private val repository = PostRepository()
    private var mode = "create"
    private var postId: String? = null
    private var category = "schedule"

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            imagePreview.setImageURI(uri)
            imagePreview.visibility = View.VISIBLE
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (FirebaseAuth.getInstance().currentUser == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        setContentView(R.layout.activity_write_post)

        val toolbar = findViewById<Toolbar>(R.id.toolbar_write)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { showCancelDialog() }

        editTitle = findViewById(R.id.editTitle)
        editContent = findViewById(R.id.editContent)
        btnUpload = findViewById(R.id.btnUpload)
        imagePreview = findViewById(R.id.imagePreview)
        imagePreview.visibility = View.GONE

        mode = intent.getStringExtra("mode") ?: "create"
        category = intent.getStringExtra("category") ?: "schedule"

        if (mode == "create") {
            intent.getStringExtra("title")?.let { editTitle.setText(it) }
            intent.getStringExtra("content")?.let { editContent.setText(it) }
        } else if (mode == "edit") {
            postId = intent.getStringExtra("postId")
            editTitle.setText(intent.getStringExtra("title"))
            editContent.setText(intent.getStringExtra("content"))
            btnUpload.text = "수정 완료"
        }

        findViewById<ImageButton>(R.id.btnSelectImage).setOnClickListener {
            imagePicker.launch("image/*")
        }

        btnUpload.setOnClickListener {
            val title = editTitle.text.toString()
            val content = editContent.text.toString()
            if (title.isBlank() || content.isBlank()) {
                Toast.makeText(this, "제목과 내용을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val currentPostId = postId
            if (mode == "edit" && currentPostId != null) {
                repository.updatePost(currentPostId, title, content,
                    onSuccess = {
                        Toast.makeText(this, "수정 완료!", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    },
                    onFailure = { Toast.makeText(this, "수정 실패", Toast.LENGTH_SHORT).show() }
                )
            } else {
                repository.createPost(title, content, category, selectedImageUri,
                    onSuccess = {
                        Toast.makeText(this, "게시글 등록 완료", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    },
                    onFailure = { Toast.makeText(this, "등록 실패", Toast.LENGTH_SHORT).show() }
                )
            }
        }
    }

    private fun showCancelDialog() {
        AlertDialog.Builder(this)
            .setTitle("작성 취소")
            .setMessage("작성 중인 내용이 사라집니다. 정말 나가시겠습니까?")
            .setPositiveButton("나가기") { _, _ -> finish() }
            .setNegativeButton("계속 작성", null)
            .show()
    }
}
