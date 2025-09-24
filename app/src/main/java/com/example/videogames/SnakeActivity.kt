package com.example.videogames

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class SnakeActivity : AppCompatActivity() {

    // UI Components
    private lateinit var gameGrid: GridLayout
    private lateinit var tvScore: TextView
    private lateinit var tvHighScore: TextView
    private lateinit var tvGameStatus: TextView
    private lateinit var btnNewGame: Button
    private lateinit var btnBackToMenu: Button
    private lateinit var btnUp: Button
    private lateinit var btnDown: Button
    private lateinit var btnLeft: Button
    private lateinit var btnRight: Button

    // Audio Components
    private var mediaPlayer: MediaPlayer? = null
    private var perderSound: MediaPlayer? = null

    // Game Variables
    private val boardWidth = 15
    private val boardHeight = 15
    private var gameBoard: Array<Array<String>> = Array(boardHeight) { Array(boardWidth) { "." } }
    private lateinit var gameCells: Array<Array<TextView>>
    private var snake = mutableListOf<Pair<Int, Int>>()
    private var food = Pair(0, 0)
    private var direction = Pair(0, 1) // Derecha por defecto
    private var nextDirection = Pair(0, 1)
    private var score = 0
    private var highScore = 0
    private var gameRunning = false
    private var gameSpeed = 300L // milisegundos
    private var gameHandler = Handler(Looper.getMainLooper())
    private var gameRunnable: Runnable? = null
    private lateinit var sharedPrefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_snake)

        initializeUI()
        initializeAudio()
        setupClickListeners()
        loadHighScore()
        initializeGame()
    }

    private fun initializeUI() {
        gameGrid = findViewById(R.id.game_grid)
        tvScore = findViewById(R.id.tv_score)
        tvHighScore = findViewById(R.id.tv_high_score)
        tvGameStatus = findViewById(R.id.tv_game_status)
        btnNewGame = findViewById(R.id.btn_new_game)
        btnBackToMenu = findViewById(R.id.btn_back_to_menu)
        btnUp = findViewById(R.id.btn_up)
        btnDown = findViewById(R.id.btn_down)
        btnLeft = findViewById(R.id.btn_left)
        btnRight = findViewById(R.id.btn_right)

        sharedPrefs = getSharedPreferences("SummerSnakeGame", Context.MODE_PRIVATE)

        createGameGrid()
    }

    private fun createGameGrid() {
        gameGrid.removeAllViews()
        val cellSize = dpToPx(20)

        // Inicializar el array después de que el contexto esté listo
        gameCells = Array(boardHeight) { Array(boardWidth) { TextView(this) } }

        for (i in 0 until boardHeight) {
            for (j in 0 until boardWidth) {
                val cell = TextView(this)
                cell.layoutParams = GridLayout.LayoutParams().apply {
                    width = cellSize
                    height = cellSize
                    setMargins(1, 1, 1, 1)
                }
                cell.textSize = 14f
                cell.gravity = android.view.Gravity.CENTER
                cell.setBackgroundColor(android.graphics.Color.parseColor("#FF003366"))
                cell.text = "."
                cell.textAlignment = TextView.TEXT_ALIGNMENT_CENTER

                gameCells[i][j] = cell
                gameGrid.addView(cell)
            }
        }
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    // Funciones de Audio
    private fun initializeAudio() {
        try {
            // Reproducir intro al inicio (solo una vez)
            mediaPlayer = MediaPlayer.create(this, R.raw.intro)
            mediaPlayer?.start()

            // Inicializar sonido de perder
            perderSound = MediaPlayer.create(this, R.raw.perder)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun setupClickListeners() {
        btnNewGame.setOnClickListener {
            initializeGame()
            startGame()
        }

        btnBackToMenu.setOnClickListener {
            backToMenu()
        }

        btnUp.setOnClickListener { changeDirection(-1, 0) }
        btnDown.setOnClickListener { changeDirection(1, 0) }
        btnLeft.setOnClickListener { changeDirection(0, -1) }
        btnRight.setOnClickListener { changeDirection(0, 1) }
    }

    private fun backToMenu() {
        // Detener el juego si está corriendo
        if (gameRunning) {
            gameRunning = false
            stopGameLoop()
        }

        // Guardar puntaje si es necesario
        saveHighScore()

        // Ir al menú principal (CategoriesActivity)
        val intent = Intent(this, CategoriesActivity::class.java)
        startActivity(intent)
        finish() // Opcional: cierra esta actividad para no poder volver con el botón atrás
    }

    private fun loadHighScore() {
        highScore = sharedPrefs.getInt("best_summer", 0)
        updateHighScore()
    }

    private fun saveHighScore() {
        if (score > highScore) {
            highScore = score
            sharedPrefs.edit().putInt("best_summer", highScore).apply()
            updateHighScore()
        }
    }

    private fun initializeGame() {
        gameRunning = false
        score = 0
        direction = Pair(0, 1)
        nextDirection = Pair(0, 1)

        // Reiniciar tablero
        gameBoard = Array(boardHeight) { Array(boardWidth) { "." } }

        // Inicializar serpiente en el centro
        snake.clear()
        val startRow = boardHeight / 2
        val startCol = boardWidth / 2
        snake.add(Pair(startRow, startCol))
        snake.add(Pair(startRow, startCol - 1))
        snake.add(Pair(startRow, startCol - 2))

        generateFood()
        updateScore()
        updateGameBoard() // Esto mostrará la serpiente inmediatamente

        tvGameStatus.text = "¡Presiona 🏖️ para empezar tu verano!"
        btnNewGame.text = "🏖️INICIAR"
    }

    private fun startGame() {
        gameRunning = true
        tvGameStatus.text = "¡Verano en Cousins Beach! 🌊"
        btnNewGame.text = "🏖️REINICIO"
        startGameLoop()
    }

    private fun startGameLoop() {
        gameRunnable = object : Runnable {
            override fun run() {
                if (gameRunning) {
                    gameUpdate()
                    gameHandler.postDelayed(this, gameSpeed)
                }
            }
        }
        gameHandler.post(gameRunnable!!)
    }

    private fun restartGameLoop() {
        stopGameLoop()
        if (gameRunning) {
            startGameLoop()
        }
    }

    private fun stopGameLoop() {
        gameRunnable?.let {
            gameHandler.removeCallbacks(it)
        }
    }

    private fun gameUpdate() {
        moveSnake()
        if (checkCollision()) {
            gameOver()
            return
        }
        if (checkFood()) {
            eatFood()
        }
        updateGameBoard()
    }

    private fun moveSnake() {
        direction = nextDirection
        val head = snake[0]
        val newHead = Pair(head.first + direction.first, head.second + direction.second)
        snake.add(0, newHead)

        // Si no comió comida, quitar la cola
        if (newHead != food) {
            snake.removeAt(snake.size - 1)
        }
    }

    private fun checkCollision(): Boolean {
        val head = snake[0]

        // Colisión con paredes (fin del océano)
        if (head.first < 0 || head.first >= boardHeight ||
            head.second < 0 || head.second >= boardWidth) {
            return true
        }

        // Colisión consigo misma (corazones enredados)
        for (i in 1 until snake.size) {
            if (head == snake[i]) {
                return true
            }
        }

        return false
    }

    private fun checkFood(): Boolean {
        return snake[0] == food
    }

    private fun eatFood() {
        score += 10
        updateScore()
        generateFood()
    }

    private fun generateFood() {
        do {
            food = Pair(Random.nextInt(boardHeight), Random.nextInt(boardWidth))
        } while (snake.contains(food))
    }

    private fun changeDirection(newRow: Int, newCol: Int) {
        // No permitir movimiento opuesto (como el corazón no puede ir hacia atrás)
        if (direction.first != -newRow || direction.second != -newCol) {
            nextDirection = Pair(newRow, newCol)
        }
    }

    private fun updateGameBoard() {
        // Verificar que gameCells esté inicializado
        if (!::gameCells.isInitialized) return

        // Limpiar todas las celdas (océano)
        for (i in 0 until boardHeight) {
            for (j in 0 until boardWidth) {
                gameCells[i][j].text = "."
                gameCells[i][j].setBackgroundColor(android.graphics.Color.parseColor("#FF003366"))
            }
        }

        // Colocar concha marina
        if (food.first >= 0 && food.first < boardHeight &&
            food.second >= 0 && food.second < boardWidth) {
            gameCells[food.first][food.second].text = "🐚"
            gameCells[food.first][food.second].setBackgroundColor(android.graphics.Color.parseColor("#FF006699"))
        }

        // Colocar serpiente de corazones
        for (i in snake.indices) {
            val segment = snake[i]
            if (segment.first >= 0 && segment.first < boardHeight &&
                segment.second >= 0 && segment.second < boardWidth) {
                if (i == 0) {
                    // Cabeza
                    gameCells[segment.first][segment.second].text = "💖"
                    gameCells[segment.first][segment.second].setBackgroundColor(android.graphics.Color.parseColor("#FFFF69B4"))
                } else {
                    // Cuerpo
                    gameCells[segment.first][segment.second].text = "🩷"
                    gameCells[segment.first][segment.second].setBackgroundColor(android.graphics.Color.parseColor("#FFFF1493"))
                }
            }
        }
    }

    private fun updateScore() {
        tvScore.text = score.toString()
    }

    private fun updateHighScore() {
        tvHighScore.text = highScore.toString()
    }

    private fun gameOver() {
        gameRunning = false
        stopGameLoop()
        saveHighScore()

        // Reproducir sonido de perder (el verano terminó)
        perderSound?.start()

        tvGameStatus.text = "¡El verano terminó! 💔 Conchas: $score"
        btnNewGame.text = "REINICIO"
    }

    override fun onDestroy() {
        super.onDestroy()
        stopGameLoop()
        // Liberar recursos de audio
        mediaPlayer?.release()
        perderSound?.release()
    }

    override fun onPause() {
        super.onPause()
        if (gameRunning) {
            gameRunning = false
            stopGameLoop()
        }
    }

}