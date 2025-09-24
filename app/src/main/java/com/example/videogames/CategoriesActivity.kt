package com.example.videogames


import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class CategoriesActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var btnLogout: MaterialButton
    private lateinit var sharedPreferences: SharedPreferences
    private var username: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        initViews()
        initSharedPreferences()
        setupUser()
        setupGameCards()
        setupLogout()
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun initSharedPreferences() {
        sharedPreferences = getSharedPreferences("GameUserPrefs", Context.MODE_PRIVATE)
    }

    private fun setupUser() {
        // Obtener username del Intent o SharedPreferences
        username = intent.getStringExtra("username")
            ?: sharedPreferences.getString("current_user", "Usuario")
                    ?: "Usuario"

        tvWelcome.text = "¡Hola, $username!"
    }

    private fun setupGameCards() {
        // Memorama
        findViewById<MaterialCardView>(R.id.cardMemory).setOnClickListener {
            openGame("Memorama", MemoramaActivity::class.java)
        }

        // Tennis
        findViewById<MaterialCardView>(R.id.cardTennis).setOnClickListener {
            openGame("Tennis", TennisActivity::class.java)
        }

        // Snake
        findViewById<MaterialCardView>(R.id.cardSnake).setOnClickListener {
            openGame("Snake", SnakeActivity::class.java)
        }

        // Buscaminas
        findViewById<MaterialCardView>(R.id.cardMinesweeper).setOnClickListener {
            openGame("Buscaminas", MinesweeperActivity::class.java)
        }
    }

    private fun openGame(gameName: String, gameActivity: Class<*>) {
        try {
            val intent = Intent(this, gameActivity)
            intent.putExtra("username", username)
            startActivity(intent)
        } catch (e: Exception) {
            // Si la Activity no existe todavía, mostrar mensaje
            Toast.makeText(this, "¡$gameName próximamente disponible!", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupLogout() {
        btnLogout.setOnClickListener {
            showLogoutDialog()
        }
    }

    private fun showLogoutDialog() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar Sesión")
            .setMessage("¿Estás seguro de que quieres cerrar sesión?")
            .setPositiveButton("Sí, salir") { _, _ ->
                logout()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun logout() {
        // Limpiar datos de sesión
        val editor = sharedPreferences.edit()
        editor.putBoolean("is_logged_in", false)
        editor.remove("current_user")
        editor.remove("login_time")
        editor.apply()

        // Volver a la pantalla de login/registro
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()

        Toast.makeText(this, "Sesión cerrada", Toast.LENGTH_SHORT).show()
    }

    // Prevenir que el usuario regrese con el botón atrás
    override fun onBackPressed() {
        showLogoutDialog()
    }
}