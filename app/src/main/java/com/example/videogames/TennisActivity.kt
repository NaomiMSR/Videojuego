package com.example.videogames

import android.annotation.SuppressLint
import android.graphics.Rect
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import kotlin.random.Random
import android.content.Intent


class TennisActivity : AppCompatActivity() {

    private lateinit var playerPaddle: View
    private lateinit var aiPaddle: View
    private lateinit var ball: View
    private lateinit var scoreText: TextView
    private lateinit var livesText: TextView
    private lateinit var menuButton: Button

    private var ballSpeedX = 9f
    private var ballSpeedY = 9f
    private var playerScore = 0  // Puntos del jugador
    private var aiScore = 0      // Puntos de la IA
    private var lives = 4
    private val maxLives = 4

    private var aiLives = 4
    private val maxAiLives = 4

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    private var aiTargetX = 0f
    private val aiSpeed = 10f

    private var isGameRunning = false
    private var isBallActive = true
    private var isGameOver = false

    // MediaPlayers
    private var startPlayer: MediaPlayer? = null
    private var losePlayer: MediaPlayer? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tennis)

        initializeViews()
        initializeSound()
        setupTouchListener()
        setupButtons()
        setupGameOverButton()
        startGame()
    }

    private fun initializeViews() {
        playerPaddle = findViewById(R.id.player_paddle)
        aiPaddle = findViewById(R.id.ai_paddle)
        ball = findViewById(R.id.ball)
        scoreText = findViewById(R.id.scoreText)
        livesText = findViewById(R.id.livesText)
        menuButton = findViewById(R.id.menu_button)
    }

    private fun initializeSound() {
        try {
            val introResourceId = resources.getIdentifier("intro", "raw", packageName)
            val perderResourceId = resources.getIdentifier("perder", "raw", packageName)

            if (introResourceId != 0) {
                startPlayer = MediaPlayer.create(this, introResourceId)
            }
            if (perderResourceId != 0) {
                losePlayer = MediaPlayer.create(this, perderResourceId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupTouchListener() {
        val layout = findViewById<View>(R.id.main_layout)
        layout.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_MOVE && isGameRunning && !isGameOver) {
                val x = event.x - playerPaddle.width / 2
                playerPaddle.x = x.coerceIn(0f, layout.width - playerPaddle.width.toFloat())
            }
            true
        }
    }

    private fun setupButtons() {
        menuButton.setOnClickListener {
            returnToMenu()
        }
    }

    private fun setupGameOverButton() {
        val restartButton = findViewById<Button>(R.id.game_over_button)
        restartButton.setOnClickListener {
            val panel = findViewById<View>(R.id.game_over_panel)
            panel.visibility = View.GONE
            startGame()
        }
    }

    private fun startGame() {
        playerScore = 0
        aiScore = 0
        lives = maxLives
        aiLives = maxAiLives
        isGameOver = false
        updateUI()
        resetBall()
        showGameElements()

        playSound(startPlayer)

        isGameRunning = true
        isBallActive = true

        runnable = object : Runnable {
            override fun run() {
                if (isGameRunning && !isGameOver) {
                    if (isBallActive) updateGame()
                    handler.postDelayed(this, 16)
                }
            }
        }
        handler.post(runnable)
    }

    // 🏓 Lógica principal del juego corregida
    private fun updateGame() {
        if (!isBallActive || isGameOver) return

        val layout = ball.parent as View

        ball.x += ballSpeedX
        ball.y += ballSpeedY

        // Rebote en paredes laterales
        if (ball.x <= 0) {
            ball.x = 0f
            ballSpeedX = abs(ballSpeedX)
        } else if (ball.x + ball.width >= layout.width) {
            ball.x = (layout.width - ball.width).toFloat()
            ballSpeedX = -abs(ballSpeedX)
        }

        val playerRect = Rect(
            playerPaddle.x.toInt(),
            playerPaddle.y.toInt(),
            (playerPaddle.x + playerPaddle.width).toInt(),
            (playerPaddle.y + playerPaddle.height).toInt()
        )
        val aiRect = Rect(
            aiPaddle.x.toInt(),
            aiPaddle.y.toInt(),
            (aiPaddle.x + aiPaddle.width).toInt(),
            (aiPaddle.y + aiPaddle.height).toInt()
        )
        val ballRect = Rect(
            ball.x.toInt(),
            ball.y.toInt(),
            (ball.x + ball.width).toInt(),
            (ball.y + ball.height).toInt()
        )

        // Colisión con paleta del jugador
        if (Rect.intersects(ballRect, playerRect) && ballSpeedY > 0) {
            ballSpeedY = -abs(ballSpeedY)
            ball.y = playerPaddle.y - ball.height - 2
            // Solo aumenta la velocidad ligeramente para mantener el juego dinámico
            ballSpeedX *= 1.02f
            ballSpeedY *= 1.02f
        }

        // Colisión con paleta de la IA
        if (Rect.intersects(ballRect, aiRect) && ballSpeedY < 0) {
            ballSpeedY = abs(ballSpeedY)
            ball.y = aiPaddle.y + aiPaddle.height + 2
            ballSpeedX *= 1.02f
            ballSpeedY *= 1.02f
        }

        // 🔥 JUGADOR PIERDE: Pelota sale por abajo
        if (ball.y > layout.height) {
            playerLosePoint()
        }

        // 🔥 IA PIERDE: Pelota sale por arriba
        if (ball.y + ball.height < 0) {
            aiLosePoint()
        }

        moveAIPaddle()
    }

    private fun moveAIPaddle() {
        val layout = aiPaddle.parent as View
        val ballCenterX = ball.x + ball.width / 2

        // IA sigue la bola con cierta imperfección
        if (Random.nextFloat() < 0.85f) {
            aiTargetX = ballCenterX - aiPaddle.width / 2
        } else {
            val error = Random.nextInt(-200, 200)
            aiTargetX = ballCenterX - aiPaddle.width / 2 + error
        }

        aiTargetX = aiTargetX.coerceIn(0f, (layout.width - aiPaddle.width).toFloat())

        // Movimiento fluido
        if (aiPaddle.x < aiTargetX) {
            aiPaddle.x = (aiPaddle.x + aiSpeed).coerceAtMost(aiTargetX)
        } else if (aiPaddle.x > aiTargetX) {
            aiPaddle.x = (aiPaddle.x - aiSpeed).coerceAtLeast(aiTargetX)
        }
    }

    // 💀 Cuando el jugador pierde un punto
    private fun playerLosePoint() {
        lives--
        aiScore++  // La IA gana un punto
        updateUI()

        playSound(losePlayer)

        Toast.makeText(this, "¡Perdiste una vida!", Toast.LENGTH_SHORT).show()
        isBallActive = false

        if (lives <= 0) {
            gameOver(playerWon = false)
        } else {
            handler.postDelayed({
                if (!isGameOver) {
                    resetBall()
                    isBallActive = true
                }
            }, 1200)
        }
    }

    // 🎯 Cuando la IA pierde un punto
    private fun aiLosePoint() {
        aiLives--
        playerScore++  // El jugador gana un punto
        updateUI()

        Toast.makeText(this, "¡El enemigo perdió una vida!", Toast.LENGTH_SHORT).show()
        isBallActive = false

        if (aiLives <= 0) {
            gameOver(playerWon = true)
        } else {
            handler.postDelayed({
                if (!isGameOver) {
                    resetBall()
                    isBallActive = true
                }
            }, 1000)
        }
    }

    private fun resetBall() {
        val layout = ball.parent as View
        ball.x = (layout.width / 2 - ball.width / 2).toFloat()
        ball.y = (layout.height / 2 - ball.height / 2).toFloat()

        // Resetear velocidad base para evitar que se acelere demasiado
        val baseSpeed = 9f
        ballSpeedX = if (Random.nextBoolean()) baseSpeed else -baseSpeed
        ballSpeedY = if (Random.nextBoolean()) baseSpeed else -baseSpeed

        ballSpeedX += Random.nextFloat() * 2f - 1f
    }

    private fun gameOver(playerWon: Boolean) {
        isGameRunning = false
        isGameOver = true
        isBallActive = false

        val panel = findViewById<View>(R.id.game_over_panel)
        val text = findViewById<TextView>(R.id.game_over_text)
        panel.visibility = View.VISIBLE

        text.text = if (playerWon) "¡GANASTE!\nPuntos: $playerScore - $aiScore" else "¡JUEGO TERMINADO!\nPuntos: $playerScore - $aiScore"

        playerPaddle.visibility = View.GONE
        aiPaddle.visibility = View.GONE
        ball.visibility = View.GONE
    }

    private fun returnToMenu() {
        isGameRunning = false
        isGameOver = false
        handler.removeCallbacks(runnable)

        val intent = Intent(this, CategoriesActivity::class.java)
        startActivity(intent)
        finish() // opcional, cierra esta activity para que no quede en segundo plano
    }


    private fun updateUI() {
        scoreText.text = "Jugador: $playerScore | Enemigo: $aiScore"
        livesText.text = "Vidas: $lives | Enemigo: $aiLives"
    }

    private fun showGameElements() {
        playerPaddle.visibility = View.VISIBLE
        aiPaddle.visibility = View.VISIBLE
        ball.visibility = View.VISIBLE
        scoreText.visibility = View.VISIBLE
        livesText.visibility = View.VISIBLE
        menuButton.visibility = View.VISIBLE
    }

    private fun playSound(mediaPlayer: MediaPlayer?) {
        try {
            mediaPlayer?.let {
                if (it.isPlaying) {
                    it.stop()
                    it.prepare()
                }
                it.seekTo(0)
                it.start()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        isGameRunning = false
        handler.removeCallbacks(runnable)

        startPlayer?.release()
        startPlayer = null
        losePlayer?.release()
        losePlayer = null
    }

    override fun onPause() {
        super.onPause()
        isGameRunning = false
    }

    override fun onResume() {
        super.onResume()
        if (!isGameOver && lives > 0 && aiLives > 0) {
            isGameRunning = true
            handler.post(runnable)
        }
    }
}