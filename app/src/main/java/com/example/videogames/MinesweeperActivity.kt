package com.example.videogames

import android.content.Context
import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import kotlin.random.Random
import android.content.Intent

class MinesweeperActivity : AppCompatActivity() {

    // UI Components
    private lateinit var gridLayout: GridLayout
    private lateinit var tvMinesCount: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvGameStatus: TextView
    private lateinit var btnNewGame: Button
    private lateinit var btnEasy: Button
    private lateinit var btnMedium: Button
    private lateinit var btnHard: Button
    private lateinit var btnBackToMenu: Button

    // Sound Components
    private var mediaPlayer: MediaPlayer? = null
    private var perderSound: MediaPlayer? = null

    // Game Variables
    private var rows = 9
    private var cols = 9
    private var totalMines = 10
    private var gameBoard: Array<Array<Cell>> = arrayOf()
    private var buttons: Array<Array<Button>> = arrayOf()
    private var gameStarted = false
    private var gameEnded = false
    private var timer = 0
    private var timerHandler = Handler(Looper.getMainLooper())
    private var timerRunnable: Runnable? = null
    private var flaggedCells = 0

    // Cell class para representar cada celda
    data class Cell(
        var isMine: Boolean = false,
        var isRevealed: Boolean = false,
        var isFlagged: Boolean = false,
        var neighborMines: Int = 0
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_minesweeper)

        initializeUI()
        initializeSounds()
        setupClickListeners()
        initializeGame()
    }

    private fun initializeSounds() {
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

    private fun initializeUI() {
        gridLayout = findViewById(R.id.grid_layout)
        tvMinesCount = findViewById(R.id.tv_mines_count)
        tvTimer = findViewById(R.id.tv_timer)
        tvGameStatus = findViewById(R.id.tv_game_status)
        btnNewGame = findViewById(R.id.btn_new_game)
        btnEasy = findViewById(R.id.btn_easy)
        btnMedium = findViewById(R.id.btn_medium)
        btnHard = findViewById(R.id.btn_hard)
        btnBackToMenu = findViewById(R.id.btn_back_to_menu)
    }

    private fun setupClickListeners() {
        btnNewGame.setOnClickListener { initializeGame() }

        btnEasy.setOnClickListener {
            rows = 9; cols = 9; totalMines = 10
            initializeGame()
        }

        btnMedium.setOnClickListener {
            rows = 16; cols = 16; totalMines = 40
            initializeGame()
        }

        btnHard.setOnClickListener {
            rows = 16; cols = 30; totalMines = 99
            initializeGame()
        }

        // Implementación del botón de regresar al menú
        btnBackToMenu.setOnClickListener {
            // Detener el timer si está corriendo
            stopTimer()

            // Regresar al menú de categorías específicamente
            val intent = Intent(this, CategoriesActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun initializeGame() {
        gameStarted = false
        gameEnded = false
        timer = 0
        flaggedCells = 0

        stopTimer()
        updateTimer()
        updateMinesCount()

        tvGameStatus.text = "¡Toca para jugar!"
        btnNewGame.text = "INICIAR"

        createGameBoard()
        createButtonGrid()
    }

    private fun createGameBoard() {
        gameBoard = Array(rows) { Array(cols) { Cell() } }
    }

    private fun createButtonGrid() {
        gridLayout.removeAllViews()
        gridLayout.rowCount = rows
        gridLayout.columnCount = cols

        buttons = Array(rows) { Array(cols) { Button(this) } }

        val buttonSize = dpToPx(30)

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                val button = Button(this)
                button.layoutParams = GridLayout.LayoutParams().apply {
                    width = buttonSize
                    height = buttonSize
                    setMargins(1, 1, 1, 1)
                }

                button.textSize = 12f
                button.setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))
                button.setTextColor(Color.BLACK)
                button.setPadding(0, 0, 0, 0)

                // Click listener para revelar celda
                button.setOnClickListener {
                    if (!gameEnded && !gameBoard[i][j].isFlagged) {
                        if (!gameStarted) {
                            startGame(i, j)
                        }
                        revealCell(i, j)
                    }
                }

                // Long click listener para marcar/desmarcar bandera
                button.setOnLongClickListener {
                    if (!gameEnded && !gameBoard[i][j].isRevealed) {
                        toggleFlag(i, j)
                    }
                    true
                }

                buttons[i][j] = button
                gridLayout.addView(button)
            }
        }
    }

    private fun startGame(firstRow: Int, firstCol: Int) {
        gameStarted = true
        placeMines(firstRow, firstCol)
        calculateNeighborMines()
        startTimer()
    }

    private fun placeMines(firstRow: Int, firstCol: Int) {
        var minesPlaced = 0

        while (minesPlaced < totalMines) {
            val row = Random.nextInt(rows)
            val col = Random.nextInt(cols)

            // No colocar mina en la primera celda clickeada ni en sus vecinos
            if (!gameBoard[row][col].isMine &&
                !isNeighborOf(row, col, firstRow, firstCol) &&
                !(row == firstRow && col == firstCol)) {
                gameBoard[row][col].isMine = true
                minesPlaced++
            }
        }
    }

    private fun isNeighborOf(row: Int, col: Int, targetRow: Int, targetCol: Int): Boolean {
        return Math.abs(row - targetRow) <= 1 && Math.abs(col - targetCol) <= 1
    }

    private fun calculateNeighborMines() {
        for (i in 0 until rows) {
            for (j in 0 until cols) {
                if (!gameBoard[i][j].isMine) {
                    var count = 0
                    for (di in -1..1) {
                        for (dj in -1..1) {
                            val ni = i + di
                            val nj = j + dj
                            if (ni in 0 until rows && nj in 0 until cols && gameBoard[ni][nj].isMine) {
                                count++
                            }
                        }
                    }
                    gameBoard[i][j].neighborMines = count
                }
            }
        }
    }

    private fun revealCell(row: Int, col: Int) {
        if (gameBoard[row][col].isRevealed || gameBoard[row][col].isFlagged) return

        gameBoard[row][col].isRevealed = true
        updateButtonAppearance(row, col)

        if (gameBoard[row][col].isMine) {
            gameOver(false)
        } else {
            if (gameBoard[row][col].neighborMines == 0) {
                // Revelar celdas vecinas automáticamente
                for (di in -1..1) {
                    for (dj in -1..1) {
                        val ni = row + di
                        val nj = col + dj
                        if (ni in 0 until rows && nj in 0 until cols) {
                            revealCell(ni, nj)
                        }
                    }
                }
            }
            checkWinCondition()
        }
    }

    private fun toggleFlag(row: Int, col: Int) {
        val cell = gameBoard[row][col]

        if (cell.isFlagged) {
            cell.isFlagged = false
            flaggedCells--
            buttons[row][col].text = ""
            buttons[row][col].setBackgroundColor(ContextCompat.getColor(this, android.R.color.darker_gray))
        } else {
            cell.isFlagged = true
            flaggedCells++
            buttons[row][col].text = "🚩"
            buttons[row][col].setBackgroundColor(Color.YELLOW)
        }

        updateMinesCount()
    }

    private fun updateButtonAppearance(row: Int, col: Int) {
        val button = buttons[row][col]
        val cell = gameBoard[row][col]

        if (cell.isRevealed) {
            if (cell.isMine) {
                button.text = "💣"
                button.setBackgroundColor(Color.RED)
            } else {
                button.setBackgroundColor(Color.LTGRAY)
                if (cell.neighborMines > 0) {
                    button.text = cell.neighborMines.toString()
                    button.setTextColor(getNumberColor(cell.neighborMines))
                } else {
                    button.text = ""
                }
            }
        }
    }

    private fun getNumberColor(number: Int): Int {
        return when (number) {
            1 -> Color.BLUE
            2 -> Color.GREEN
            3 -> Color.RED
            4 -> Color.parseColor("#000080") // Navy
            5 -> Color.parseColor("#800000") // Maroon
            6 -> Color.parseColor("#008080") // Teal
            7 -> Color.BLACK
            8 -> Color.parseColor("#808080") // Gray
            else -> Color.BLACK
        }
    }

    private fun checkWinCondition() {
        var revealedSafeCells = 0

        for (i in 0 until rows) {
            for (j in 0 until cols) {
                if (!gameBoard[i][j].isMine && gameBoard[i][j].isRevealed) {
                    revealedSafeCells++
                }
            }
        }

        if (revealedSafeCells == (rows * cols) - totalMines) {
            gameOver(true)
        }
    }

    private fun gameOver(won: Boolean) {
        gameEnded = true
        stopTimer()

        if (won) {
            tvGameStatus.text = "Felicidades, ¡Ganaste!"
            btnNewGame.text = "Reiniciar"
            // Marcar todas las minas con banderas
            for (i in 0 until rows) {
                for (j in 0 until cols) {
                    if (gameBoard[i][j].isMine && !gameBoard[i][j].isFlagged) {
                        buttons[i][j].text = "🚩"
                        buttons[i][j].setBackgroundColor(Color.GREEN)
                    }
                }
            }
        } else {
            tvGameStatus.text = "¡Juego terminado! Inténtalo de nuevo"
            btnNewGame.text = "Reiniciar"

            // Reproducir sonido de perder
            try {
                perderSound?.start()
            } catch (e: Exception) {
                e.printStackTrace()
            }

            // Revelar todas las minas
            for (i in 0 until rows) {
                for (j in 0 until cols) {
                    if (gameBoard[i][j].isMine) {
                        updateButtonAppearance(i, j)
                    }
                }
            }
        }
    }

    private fun startTimer() {
        timerRunnable = object : Runnable {
            override fun run() {
                timer++
                updateTimer()
                timerHandler.postDelayed(this, 1000)
            }
        }
        timerHandler.post(timerRunnable!!)
    }

    private fun stopTimer() {
        timerRunnable?.let {
            timerHandler.removeCallbacks(it)
        }
    }

    private fun updateTimer() {
        tvTimer.text = String.format("%03d", timer)
    }

    private fun updateMinesCount() {
        val remainingMines = totalMines - flaggedCells
        tvMinesCount.text = remainingMines.toString()
    }

    private fun dpToPx(dp: Int): Int {
        return (dp * resources.displayMetrics.density).toInt()
    }

    override fun onDestroy() {
        super.onDestroy()
        stopTimer()

        // Liberar recursos de audio
        try {
            mediaPlayer?.release()
            perderSound?.release()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}