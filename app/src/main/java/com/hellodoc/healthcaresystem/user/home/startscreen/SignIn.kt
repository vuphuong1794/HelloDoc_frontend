package com.hellodoc.healthcaresystem.user.home.startscreen

import android.content.Context
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Intent
import android.text.InputType
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.hellodoc.healthcaresystem.retrofit.RetrofitInstance
import com.hellodoc.healthcaresystem.requestmodel.LoginRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.auth0.android.jwt.JWT
import com.hellodoc.core.common.activity.BaseActivity
import com.hellodoc.healthcaresystem.admin.AdminRoot
import com.hellodoc.healthcaresystem.R
import com.hellodoc.healthcaresystem.user.home.HomeActivity
import androidx.core.content.edit

class SignIn : BaseActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.sign_in)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_sign_in)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val nutdangki = findViewById<TextView>(R.id.signuplink)
        nutdangki.setOnClickListener {
            val intent = Intent(this, SignUp::class.java)
            startActivity(intent) // Chuyển đến SecondActivity
        }

        val nutdangnhap = findViewById<Button>(R.id.button)
        nutdangnhap.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent) // Chuyển đến SecondActivity
        }
        val returnButton = findViewById<ImageButton>(R.id.returnButton)
        returnButton.setOnClickListener {
            val intent = Intent(this, StartScreen::class.java)
            startActivity(intent) // Chuyển đến SecondActivity
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right) // Slide left when going back
        }

        val emailInput = findViewById<EditText>(R.id.email)
        val passwordInput = findViewById<EditText>(R.id.pass)

        nutdangnhap.setOnClickListener{
            val email = emailInput.text.toString().trim()
            val password = passwordInput.text.toString().trim()

            if(email.isEmpty() || password.isEmpty()){
                Toast.makeText(this, "Vui lòng điền đầy đủ thông tin"+ password, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            userLogin(email, password)
        }

        var isPasswordVisible = false
        val passwordEditText = findViewById<EditText>(R.id.pass)
        val togglePasswordBtn = findViewById<ImageView>(R.id.togglePasswordVisibility)

        togglePasswordBtn.setOnClickListener {
            isPasswordVisible = !isPasswordVisible

            if (isPasswordVisible) {
                passwordEditText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePasswordBtn.setImageResource(R.drawable.baseline_disabled_visible_24) // icon mắt mở
            } else {
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePasswordBtn.setImageResource(R.drawable.baseline_remove_red_eye_24) // icon mắt đóng
            }

            passwordEditText.setSelection(passwordEditText.text.length) // giữ vị trí con trỏ
        }

        val forgotPasswordBtn = findViewById<TextView>(R.id.forgot_password)
        forgotPasswordBtn.setOnClickListener {
            val intent = Intent(this@SignIn, ForgotPasswordActivity::class.java)
            startActivity(intent)
        }

    }

    private fun userLogin(email: String, password: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitInstance.api.login(LoginRequest(email, password))

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val loginResponse = response.body()
                        val token = loginResponse?.accessToken

                        if (!token.isNullOrEmpty()) {
                            saveToken(token)

                            try {
                                val jwt = JWT(token)
                                val role = jwt.getClaim("role").asString()


                                val intent = when (role) {
                                    "admin" -> Intent(this@SignIn, AdminRoot::class.java)
                                    "user" -> Intent(this@SignIn, HomeActivity::class.java)
                                    "doctor" -> Intent(this@SignIn, HomeActivity::class.java)
                                    else -> {
                                        Toast.makeText(this@SignIn, "Vai trò không hợp lệ!", Toast.LENGTH_SHORT).show()
                                        return@withContext
                                    }
                                }

                                Toast.makeText(this@SignIn, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show()
                                startActivity(intent)
                                finish()
                            } catch (e: Exception) {
                                Toast.makeText(this@SignIn, "Không thể đọc thông tin người dùng từ token", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(this@SignIn, "Token không hợp lệ!", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        val errorBody = response.errorBody()?.string()
                        Toast.makeText(this@SignIn, "Đăng nhập thất bại: $errorBody", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@SignIn, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    private fun saveToken(token: String) {
        val sharedPreferences = getSharedPreferences("user_prefs", MODE_PRIVATE)
        sharedPreferences.edit()
            .putString("access_token", token)
            .apply()
    }
}



