package com.example.videogames

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {

    private lateinit var tilUsername: TextInputLayout
    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etUsername: TextInputEditText
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnPrimary: MaterialButton
    private lateinit var tvSwitch: android.widget.TextView
    private lateinit var tvTitle: android.widget.TextView
    private lateinit var tvSubtitle: android.widget.TextView

    private lateinit var sharedPreferences: SharedPreferences
    private var isLoginMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        initSharedPreferences()
        checkIfAlreadyLoggedIn()
        setupListeners()
        updateUI()
    }

    private fun initViews() {
        tilUsername = findViewById(R.id.tilUsername)
        tilEmail = findViewById(R.id.tilEmail)
        tilPassword = findViewById(R.id.tilPassword)
        etUsername = findViewById(R.id.etUsername)
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnPrimary = findViewById(R.id.btnPrimary)
        tvSwitch = findViewById(R.id.tvSwitch)
        tvTitle = findViewById(R.id.tvTitle)
        tvSubtitle = findViewById(R.id.tvSubtitle)
    }

    private fun initSharedPreferences() {
        sharedPreferences = getSharedPreferences("GameUserPrefs", Context.MODE_PRIVATE)
    }

    private fun checkIfAlreadyLoggedIn() {
        if (sharedPreferences.getBoolean("is_logged_in", false)) {
            val currentUser = sharedPreferences.getString("current_user", "")
            if (!currentUser.isNullOrEmpty()) {
                goToMainActivity(currentUser)
            }
        }
    }

    private fun setupListeners() {
        btnPrimary.setOnClickListener {
            if (isLoginMode) {
                loginUser()
            } else {
                if (validateInputs()) {
                    registerUser()
                }
            }
        }

        tvSwitch.setOnClickListener {
            isLoginMode = !isLoginMode
            updateUI()
            clearErrors()
            clearFields()
        }
    }

    private fun updateUI() {
        if (isLoginMode) {
            // Modo Login
            tvTitle.text = "Iniciar Sesión"
            tvSubtitle.text = "Bienvenido de vuelta"
            tilEmail.visibility = View.GONE
            btnPrimary.text = "Iniciar Sesión"
            tvSwitch.text = "¿No tienes cuenta? Regístrate"
        } else {
            // Modo Registro
            tvTitle.text = "Crear Cuenta"
            tvSubtitle.text = "Únete y comienza a jugar"
            tilEmail.visibility = View.VISIBLE
            btnPrimary.text = "Registrarse"
            tvSwitch.text = "¿Ya tienes cuenta? Iniciar Sesión"
        }
    }

    private fun clearErrors() {
        tilUsername.error = null
        tilEmail.error = null
        tilPassword.error = null
    }

    private fun clearFields() {
        etUsername.setText("")
        etEmail.setText("")
        etPassword.setText("")
    }

    private fun validateInputs(): Boolean {
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        clearErrors()
        var isValid = true

        // Validar username
        if (username.isEmpty()) {
            tilUsername.error = "Ingresa un nombre de usuario"
            isValid = false
        } else if (!isLoginMode) {
            if (username.length < 3) {
                tilUsername.error = "Mínimo 3 caracteres"
                isValid = false
            } else if (isUsernameTaken(username)) {
                tilUsername.error = "Este nombre ya está en uso"
                isValid = false
            }
        }

        // Validar email (solo en registro)
        if (!isLoginMode) {
            if (email.isEmpty()) {
                tilEmail.error = "Ingresa tu correo"
                isValid = false
            } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                tilEmail.error = "Correo inválido"
                isValid = false
            } else if (isEmailTaken(email)) {
                tilEmail.error = "Este correo ya está registrado"
                isValid = false
            }
        }

        // Validar password
        if (password.isEmpty()) {
            tilPassword.error = "Ingresa una contraseña"
            isValid = false
        } else if (!isLoginMode && password.length < 6) {
            tilPassword.error = "Mínimo 6 caracteres"
            isValid = false
        }

        return isValid
    }

    private fun registerUser() {
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        saveUserData(username, email, password)
        markUserAsLoggedIn(username)

        Toast.makeText(this, "¡Registro exitoso! Bienvenido $username", Toast.LENGTH_LONG).show()
        goToMainActivity(username)
    }

    private fun loginUser() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString()

        val storedPassword = sharedPreferences.getString("user_${username.lowercase()}_password", null)

        if (storedPassword != null && storedPassword == password) {
            markUserAsLoggedIn(username)
            Toast.makeText(this, "¡Bienvenido de nuevo, $username!", Toast.LENGTH_LONG).show()
            goToMainActivity(username)
        } else {
            if (storedPassword == null) {
                tilUsername.error = "Usuario no encontrado"
            } else {
                tilPassword.error = "Contraseña incorrecta"
            }
        }
    }

    private fun isUsernameTaken(username: String): Boolean {
        val existingUsers = sharedPreferences.getStringSet("registered_users", setOf()) ?: setOf()
        return existingUsers.contains(username.lowercase())
    }

    private fun isEmailTaken(email: String): Boolean {
        return sharedPreferences.contains("user_email_${email.lowercase()}")
    }

    private fun saveUserData(username: String, email: String, password: String) {
        val editor = sharedPreferences.edit()

        editor.putString("user_${username.lowercase()}_email", email)
        editor.putString("user_${username.lowercase()}_password", password)
        editor.putString("user_email_${email.lowercase()}", username)

        val existingUsers = sharedPreferences.getStringSet("registered_users", mutableSetOf()) ?: mutableSetOf()
        existingUsers.add(username.lowercase())
        editor.putStringSet("registered_users", existingUsers)

        editor.putLong("user_${username.lowercase()}_register_date", System.currentTimeMillis())
        editor.apply()
    }

    private fun markUserAsLoggedIn(username: String) {
        val editor = sharedPreferences.edit()
        editor.putBoolean("is_logged_in", true)
        editor.putString("current_user", username)
        editor.putLong("login_time", System.currentTimeMillis())
        editor.apply()
    }

    private fun goToMainActivity(username: String) {
        val intent = Intent(this, CategoriesActivity::class.java)
        intent.putExtra("username", username)
        startActivity(intent)
        finish()
    }

    // Función para cerrar sesión (puedes llamarla desde MainActivity)
    fun logout() {
        val editor = sharedPreferences.edit()
        editor.putBoolean("is_logged_in", false)
        editor.remove("current_user")
        editor.remove("login_time")
        editor.apply()
    }
}