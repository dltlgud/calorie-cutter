package com.example.main.auth

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.main.R
import com.example.main.repository.UserRepository
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class EditProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val userRepository = UserRepository()
    private var selectedImageUri: Uri? = null
    private var existingProfileUrl: String = ""

    private lateinit var ivProfile: ShapeableImageView
    private lateinit var tvProfileHint: TextView
    private lateinit var etNickname: EditText
    private lateinit var tvNicknameError: TextView
    private lateinit var etCurrentPwd: EditText
    private lateinit var etNewPwd: EditText
    private lateinit var etNewPwdConfirm: EditText
    private lateinit var tvPwdError: TextView
    private lateinit var btnSave: Button

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            tvProfileHint.visibility = View.GONE
            Glide.with(this).load(it).centerCrop().into(ivProfile)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)

        auth = FirebaseAuth.getInstance()

        ivProfile       = findViewById(R.id.iv_profile)
        tvProfileHint   = findViewById(R.id.tv_profile_hint)
        etNickname      = findViewById(R.id.et_nickname)
        tvNicknameError = findViewById(R.id.tv_nickname_error)
        etCurrentPwd    = findViewById(R.id.et_current_pwd)
        etNewPwd        = findViewById(R.id.et_new_pwd)
        etNewPwdConfirm = findViewById(R.id.et_new_pwd_confirm)
        tvPwdError      = findViewById(R.id.tv_pwd_error)
        btnSave         = findViewById(R.id.btn_save_profile)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.fl_profile).setOnClickListener { imagePicker.launch("image/*") }
        btnSave.setOnClickListener { saveProfile() }

        loadCurrentData()
    }

    private fun loadCurrentData() {
        val uid = auth.currentUser?.uid ?: return
        userRepository.getUser(
            uid = uid,
            onSuccess = { doc ->
                etNickname.setText(doc.getString("username") ?: "")
                val profileUrl = doc.getString("profileImageUrl") ?: ""
                existingProfileUrl = profileUrl
                if (profileUrl.isNotEmpty()) {
                    tvProfileHint.visibility = View.GONE
                    Glide.with(this).load(profileUrl).centerCrop().into(ivProfile)
                }
            },
            onFailure = {}
        )
    }

    private fun saveProfile() {
        val nickname = etNickname.text.toString().trim()
        val currentPwd = etCurrentPwd.text.toString()
        val newPwd = etNewPwd.text.toString()
        val newPwdConfirm = etNewPwdConfirm.text.toString()
        var isValid = true

        tvNicknameError.visibility = View.GONE
        tvPwdError.visibility = View.GONE

        if (nickname.isEmpty()) {
            tvNicknameError.text = "닉네임을 입력해주세요"
            tvNicknameError.visibility = View.VISIBLE
            isValid = false
        } else if (nickname.length < 2 || nickname.length > 20) {
            tvNicknameError.text = "닉네임은 2~20자로 입력해주세요"
            tvNicknameError.visibility = View.VISIBLE
            isValid = false
        }

        val changingPassword = newPwd.isNotEmpty() || currentPwd.isNotEmpty()
        if (changingPassword) {
            if (currentPwd.isEmpty()) {
                tvPwdError.text = "현재 비밀번호를 입력해주세요"
                tvPwdError.visibility = View.VISIBLE
                isValid = false
            } else if (newPwd.length < 8) {
                tvPwdError.text = "새 비밀번호는 8자 이상이어야 합니다"
                tvPwdError.visibility = View.VISIBLE
                isValid = false
            } else if (!newPwd.any { it.isLetter() } || !newPwd.any { it.isDigit() }) {
                tvPwdError.text = "영문과 숫자를 모두 포함해야 합니다"
                tvPwdError.visibility = View.VISIBLE
                isValid = false
            } else if (newPwd != newPwdConfirm) {
                tvPwdError.text = "비밀번호가 일치하지 않습니다"
                tvPwdError.visibility = View.VISIBLE
                isValid = false
            }
        }

        if (!isValid) return

        btnSave.isEnabled = false
        btnSave.text = "저장 중..."

        val uid = auth.currentUser?.uid ?: return
        val uri = selectedImageUri
        if (uri != null) {
            uploadProfileImage(uid, uri) { profileUrl ->
                updateData(uid, nickname, profileUrl, changingPassword, currentPwd, newPwd)
            }
        } else {
            updateData(uid, nickname, existingProfileUrl, changingPassword, currentPwd, newPwd)
        }
    }

    private fun uploadProfileImage(uid: String, uri: Uri, onComplete: (String) -> Unit) {
        val ref = FirebaseStorage.getInstance().reference.child("profiles/$uid.jpg")
        ref.putFile(uri)
            .continueWithTask { task ->
                if (!task.isSuccessful) throw task.exception!!
                ref.downloadUrl
            }
            .addOnSuccessListener { downloadUri -> onComplete(downloadUri.toString()) }
            .addOnFailureListener { onComplete(existingProfileUrl) }
    }

    private fun updateData(
        uid: String, nickname: String, profileUrl: String,
        changingPassword: Boolean, currentPwd: String, newPwd: String
    ) {
        userRepository.updateUser(
            uid = uid,
            updates = mapOf("username" to nickname, "profileImageUrl" to profileUrl),
            onSuccess = {
                if (changingPassword) {
                    reAuthAndChangePassword(currentPwd, newPwd)
                } else {
                    onSaveComplete()
                }
            },
            onFailure = {
                btnSave.isEnabled = true
                btnSave.text = "저장하기"
                Toast.makeText(this, "저장 실패: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun reAuthAndChangePassword(currentPwd: String, newPwd: String) {
        val user = auth.currentUser ?: return
        val email = user.email ?: return
        val credential = EmailAuthProvider.getCredential(email, currentPwd)
        user.reauthenticate(credential)
            .addOnSuccessListener {
                user.updatePassword(newPwd)
                    .addOnSuccessListener { onSaveComplete() }
                    .addOnFailureListener { e ->
                        btnSave.isEnabled = true
                        btnSave.text = "저장하기"
                        Toast.makeText(this, "비밀번호 변경 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                btnSave.isEnabled = true
                btnSave.text = "저장하기"
                tvPwdError.text = "현재 비밀번호가 올바르지 않습니다"
                tvPwdError.visibility = View.VISIBLE
            }
    }

    private fun onSaveComplete() {
        Toast.makeText(this, "프로필이 저장되었습니다", Toast.LENGTH_SHORT).show()
        finish()
    }
}
