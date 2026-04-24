package com.example.main.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.main.R
import com.example.main.repository.UserRepository
import com.google.android.material.imageview.ShapeableImageView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val userRepository = UserRepository()
    private var selectedImageUri: Uri? = null

    private lateinit var ivProfile: ShapeableImageView
    private lateinit var tvProfileHint: TextView
    private lateinit var etName: EditText
    private lateinit var tvNameError: TextView
    private lateinit var etNickname: EditText
    private lateinit var tvNicknameError: TextView
    private lateinit var etEmail: EditText
    private lateinit var tvEmailError: TextView
    private lateinit var etPwd: EditText
    private lateinit var tvPwdError: TextView
    private lateinit var etPwdConfirm: EditText
    private lateinit var tvPwdConfirmError: TextView
    private lateinit var btnRegister: Button

    private val imagePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            selectedImageUri = it
            tvProfileHint.visibility = View.GONE
            Glide.with(this).load(it).centerCrop().into(ivProfile)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        ivProfile        = findViewById(R.id.iv_profile)
        tvProfileHint    = findViewById(R.id.tv_profile_hint)
        etName           = findViewById(R.id.et_name)
        tvNameError      = findViewById(R.id.tv_name_error)
        etNickname       = findViewById(R.id.et_nickname)
        tvNicknameError  = findViewById(R.id.tv_nickname_error)
        etEmail          = findViewById(R.id.et_email)
        tvEmailError     = findViewById(R.id.tv_email_error)
        etPwd            = findViewById(R.id.et_pwd)
        tvPwdError       = findViewById(R.id.tv_pwd_error)
        etPwdConfirm     = findViewById(R.id.et_pwd_confirm)
        tvPwdConfirmError = findViewById(R.id.tv_pwd_confirm_error)
        btnRegister      = findViewById(R.id.btn_register)

        findViewById<FrameLayout>(R.id.fl_profile).setOnClickListener {
            imagePicker.launch("image/*")
        }

        btnRegister.setOnClickListener { registerUser() }
    }

    private fun registerUser() {
        val name           = etName.text.toString().trim()
        val nickname       = etNickname.text.toString().trim()
        val email          = etEmail.text.toString().trim()
        val password       = etPwd.text.toString()
        val passwordConfirm = etPwdConfirm.text.toString()
        var isValid        = true

        if (name.isEmpty()) {
            tvNameError.text = "이름을 입력해주세요"
            tvNameError.visibility = View.VISIBLE
            isValid = false
        } else if (!Regex("^[가-힣\\s]+$").matches(name)) {
            tvNameError.text = "이름은 한글만 입력 가능합니다"
            tvNameError.visibility = View.VISIBLE
            isValid = false
        } else {
            tvNameError.visibility = View.GONE
        }

        if (nickname.isEmpty()) {
            tvNicknameError.text = "닉네임을 입력해주세요"
            tvNicknameError.visibility = View.VISIBLE
            isValid = false
        } else if (nickname.length < 2 || nickname.length > 20) {
            tvNicknameError.text = "닉네임은 2~20자로 입력해주세요"
            tvNicknameError.visibility = View.VISIBLE
            isValid = false
        } else {
            tvNicknameError.visibility = View.GONE
        }

        if (email.isEmpty()) {
            tvEmailError.text = "이메일을 입력해주세요"
            tvEmailError.visibility = View.VISIBLE
            isValid = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tvEmailError.text = "올바른 이메일 형식이 아닙니다"
            tvEmailError.visibility = View.VISIBLE
            isValid = false
        } else {
            tvEmailError.visibility = View.GONE
        }

        if (password.isEmpty()) {
            tvPwdError.text = "비밀번호를 입력해주세요"
            tvPwdError.visibility = View.VISIBLE
            isValid = false
        } else if (password.length < 8) {
            tvPwdError.text = "비밀번호는 8자 이상이어야 합니다"
            tvPwdError.visibility = View.VISIBLE
            isValid = false
        } else if (!password.any { it.isLetter() } || !password.any { it.isDigit() }) {
            tvPwdError.text = "영문과 숫자를 모두 포함해야 합니다"
            tvPwdError.visibility = View.VISIBLE
            isValid = false
        } else {
            tvPwdError.visibility = View.GONE
        }

        if (passwordConfirm.isEmpty()) {
            tvPwdConfirmError.text = "비밀번호를 다시 입력해주세요"
            tvPwdConfirmError.visibility = View.VISIBLE
            isValid = false
        } else if (password != passwordConfirm) {
            tvPwdConfirmError.text = "비밀번호가 일치하지 않습니다"
            tvPwdConfirmError.visibility = View.VISIBLE
            isValid = false
        } else {
            tvPwdConfirmError.visibility = View.GONE
        }

        if (!isValid) return

        btnRegister.isEnabled = false
        btnRegister.text = "가입 중..."

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                    val uri = selectedImageUri
                    if (uri != null) {
                        uploadProfileImage(uid, uri) { profileUrl ->
                            saveUserData(uid, name, nickname, email, profileUrl)
                        }
                    } else {
                        saveUserData(uid, name, nickname, email, "")
                    }
                } else {
                    btnRegister.isEnabled = true
                    btnRegister.text = "가입하기"
                    Toast.makeText(this, "회원가입 실패: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
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
            .addOnFailureListener { onComplete("") }
    }

    private fun saveUserData(uid: String, name: String, nickname: String, email: String, profileUrl: String) {
        userRepository.saveUser(
            uid = uid,
            data = mapOf(
                "name"            to name,
                "username"        to nickname,
                "email"           to email,
                "profileImageUrl" to profileUrl
            ),
            onSuccess = {
                Toast.makeText(this, "회원가입 성공! 환영합니다 🎉", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            },
            onFailure = {
                Toast.makeText(this, "프로필 저장 실패. 마이페이지에서 다시 입력해주세요.", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            }
        )
    }
}
