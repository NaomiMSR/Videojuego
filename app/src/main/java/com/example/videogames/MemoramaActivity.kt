package com.example.videogames

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MemoramaActivity : AppCompatActivity() {

    private lateinit var tvPares: TextView
    private lateinit var tvVidas: TextView
    private lateinit var cards: Array<ImageButton>
    private lateinit var btnReiniciar: Button
    private lateinit var btnMenu: Button

    // MediaPlayer para música de inicio
    private lateinit var mediaPlayer: MediaPlayer

    // Efecto de sonido al perder vida
    private lateinit var perderSound: MediaPlayer

    // Lista de imágenes (8 pares)
    private val cardImages = listOf(
        R.drawable.belly,
        R.drawable.conrad,
        R.drawable.jeremiah,
        R.drawable.taylor,
        R.drawable.steven,
        R.drawable.susa,
        R.drawable.mama,
        R.drawable.tres
    )

    private lateinit var cardValues: MutableList<Int>

    private var firstCardIndex: Int? = null
    private var secondCardIndex: Int? = null
    private var isBusy = false
    private var pairsFound = 0
    private var vidas = 10

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_memorama)

        // Reproducir intro al inicio (solo una vez)
        mediaPlayer = MediaPlayer.create(this, R.raw.intro)
        mediaPlayer.start()

        // Inicializar sonido de perder
        perderSound = MediaPlayer.create(this, R.raw.perder)

        // Botones
        btnReiniciar = findViewById(R.id.btnReiniciar)
        btnReiniciar.setOnClickListener { reiniciarJuego() }

        btnMenu = findViewById(R.id.btnMenu)
        btnMenu.setOnClickListener { regresarAlMenu() }

        // Referencias a TextViews
        tvPares = findViewById(R.id.tvPares)
        tvVidas = findViewById(R.id.tvVidas)

        Toast.makeText(this, "¡Bienvenido al Memorama! 🎮", Toast.LENGTH_SHORT).show()

        // Barajar cartas
        cardValues = (cardImages + cardImages).shuffled().toMutableList()

        // Referencias a las cartas
        cards = arrayOf(
            findViewById(R.id.carta1),
            findViewById(R.id.carta2),
            findViewById(R.id.carta3),
            findViewById(R.id.carta4),
            findViewById(R.id.carta5),
            findViewById(R.id.carta6),
            findViewById(R.id.carta7),
            findViewById(R.id.carta8),
            findViewById(R.id.carta9),
            findViewById(R.id.carta10),
            findViewById(R.id.carta11),
            findViewById(R.id.carta12),
            findViewById(R.id.carta13),
            findViewById(R.id.carta14),
            findViewById(R.id.carta15),
            findViewById(R.id.carta16)
        )

        // Inicializar cartas con reverso
        for (i in cards.indices) {
            cards[i].setImageResource(R.drawable.logo)
            cards[i].setOnClickListener { onCardClicked(i) }
        }

        actualizarMarcadores()
    }

    private fun onCardClicked(index: Int) {
        if (isBusy || index == firstCardIndex) return

        cards[index].setImageResource(cardValues[index])

        if (firstCardIndex == null) {
            firstCardIndex = index
        } else if (secondCardIndex == null) {
            secondCardIndex = index
            checkForMatch()
        }
    }

    private fun checkForMatch() {
        isBusy = true
        val first = firstCardIndex!!
        val second = secondCardIndex!!

        if (cardValues[first] == cardValues[second]) {
            // ✅ Son par
            pairsFound++
            resetSelection()
            isBusy = false
            actualizarMarcadores()

            if (pairsFound == cardImages.size) {
                Toast.makeText(this, "¡Ganaste! Encontraste todos los pares", Toast.LENGTH_LONG).show()
            }
        } else {
            // ❌ No son iguales → perder vida
            vidas--
            actualizarMarcadores()

            // 🔊 Sonido de perder vida
            perderSound.start()

            Handler(Looper.getMainLooper()).postDelayed({
                cards[first].setImageResource(R.drawable.logo)
                cards[second].setImageResource(R.drawable.logo)
                resetSelection()
                isBusy = false
            }, 1000)

            if (vidas == 0) {
                Toast.makeText(this, "¡JUEGO TERMINADO! Te quedaste sin vidas", Toast.LENGTH_LONG).show()
                bloquearJuego()
            }
        }
    }

    private fun actualizarMarcadores() {
        tvPares.text = "Pares: $pairsFound/${cardImages.size}"
        tvVidas.text = "Vidas: $vidas"
    }

    private fun resetSelection() {
        firstCardIndex = null
        secondCardIndex = null
    }

    private fun bloquearJuego() {
        for (card in cards) {
            card.isEnabled = false
        }
    }

    private fun reiniciarJuego() {
        pairsFound = 0
        vidas = 10
        firstCardIndex = null
        secondCardIndex = null
        isBusy = false

        cardValues = (cardImages + cardImages).shuffled().toMutableList()

        for (i in cards.indices) {
            cards[i].isEnabled = true
            cards[i].setImageResource(R.drawable.logo)
        }

        actualizarMarcadores()

        Toast.makeText(this, "Se reinició el juego", Toast.LENGTH_SHORT).show()

        // 🎵 Reproducir intro también cuando reinicie
        mediaPlayer.release()
        mediaPlayer = MediaPlayer.create(this, R.raw.intro)
        mediaPlayer.start()
    }

    private fun regresarAlMenu() {
        // Detener todos los sonidos antes de salir
        try {
            if (::mediaPlayer.isInitialized && mediaPlayer.isPlaying) {
                mediaPlayer.stop()
            }
            if (::perderSound.isInitialized && perderSound.isPlaying) {
                perderSound.stop()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        // Regresar al menú de categorías (reemplaza CategoriesActivity por el nombre correcto)
        val intent = Intent(this, CategoriesActivity::class.java)
        startActivity(intent)
        finish()

        // Si prefieres solo cerrar la actividad actual, usa solo esto:
        // finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        // 🎵 Liberar recursos
        if (::mediaPlayer.isInitialized) mediaPlayer.release()
        if (::perderSound.isInitialized) perderSound.release()
    }
}