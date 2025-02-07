package com.example.playlistmaker.presentation.player

import android.icu.text.SimpleDateFormat
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.App
import com.example.playlistmaker.Creator
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.models.PlayerState
import com.example.playlistmaker.domain.models.Track
import com.example.playlistmaker.domain.usecase.AudioPlayerImpl
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.Locale

class AudioplayerActivity : AppCompatActivity() {

    private lateinit var audioPlayerUseCase: AudioPlayerImpl

    private lateinit var playBtn: ImageButton
    private lateinit var timerTextView: TextView
    private val handler = Handler(Looper.getMainLooper())
    private val updateTimerRunnable = object : Runnable {
        override fun run() {
            if (audioPlayerUseCase.getState() == PlayerState.PLAYING) {
                val currentMs = audioPlayerUseCase.getCurrentPositionMs()
                val formattedTime = SimpleDateFormat("mm:ss", Locale.getDefault()).format(currentMs)
                timerTextView.text = formattedTime
                handler.postDelayed(this, TRACK_DEBOUNCE_DELAY)
            } else if (audioPlayerUseCase.getState() == PlayerState.PREPARED) {
                // Принудительно обновляем таймер до конца трека
                pausePlayer()
                timerTextView.text = "00:30"
            }
        }
    }

    companion object {
        private const val TRACK_DEBOUNCE_DELAY = 200L
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)

        // 1) Инициализируем UseCase
        audioPlayerUseCase = Creator.provideAudioPlayerUseCase()

        // 2) Получаем Track из интента
        val trackJson = intent.getStringExtra("track_json") ?: ""
        val track = Gson().fromJson(trackJson, Track::class.java)

        Log.d("AudioplayerActivity", "Received track JSON: $trackJson")
        Log.d("AudioplayerActivity", "Parsed track object: $track")
        Log.d("AudioplayerActivity", "Final trackTime: ${track.trackTime}")

        // 3) Находим View
        timerTextView = findViewById(R.id.audioplayer_timer)
        playBtn = findViewById(R.id.play_btn)
        val trackNameTextView = findViewById<TextView>(R.id.audioplayer_trackName)
        val artistNameTextView = findViewById<TextView>(R.id.audioplayer_artistName)
        val albumTextView = findViewById<TextView>(R.id.value_album)
        val durationTextView = findViewById<TextView>(R.id.value_duration)
        val yearTextView = findViewById<TextView>(R.id.value_year)
        val genreTextView = findViewById<TextView>(R.id.value_genre)
        val countryTextView = findViewById<TextView>(R.id.value_country)
        val imageView = findViewById<ImageView>(R.id.audioplayer_image)
        val backSearchBtn = findViewById<androidx.appcompat.widget.Toolbar>(R.id.audioplayer_back)

        // 4) Заполняем данные
        trackNameTextView.text = track.trackName
        artistNameTextView.text = track.artistName
        albumTextView.text = track.collectionName
        durationTextView.text = track.trackTime
        yearTextView.text = track.releaseDate.substring(0, 4)
        genreTextView.text = track.primaryGenreName
        countryTextView.text = track.country
        timerTextView.text = "00:30"

        // Загружаем картинку
        val artworkUrl512 = track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")
        Glide.with(this)
            .load(artworkUrl512)
            .placeholder(R.drawable.placeholder)
            .centerCrop()
            .transform(RoundedCorners(8))
            .into(imageView)

        // 5) Подготовка плеера
        playBtn.isEnabled = false // пока не будет onPrepared
        preparePlayer(track.previewUrl)

        // 6) Обработчики
        backSearchBtn.setOnClickListener {
            finish()
        }
        playBtn.setOnClickListener {
            playbackControl()
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        audioPlayerUseCase.release()
        handler.removeCallbacks(updateTimerRunnable)
    }

    private fun preparePlayer(previewUrl: String) {
        // запрашиваем подготовку плеера в корутине
        // либо, если у вас это не suspend, можно без корутины
        // пример через корутину:
        val scope = (application as App).appScope
        scope.launch {
            try {
                audioPlayerUseCase.prepare(previewUrl)
                // плеер готов -> в главном потоке разрешаем кнопку play
                runOnUiThread {
                    playBtn.isEnabled = true
                }
            } catch (e: Exception) {
                // Обработать ошибку
            }
        }
    }

    private fun playbackControl() {
        when (audioPlayerUseCase.getState()) {
            PlayerState.PLAYING -> {
                pausePlayer()
            }
            PlayerState.PREPARED,
            PlayerState.PAUSED -> {
                startPlayer()
            }
            else -> {}
        }
    }

    private fun startPlayer() {
        audioPlayerUseCase.play()
        playBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.button_pause))
        handler.post(updateTimerRunnable)
    }

    private fun pausePlayer() {
        audioPlayerUseCase.pause()
        playBtn.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.button_play))
        handler.removeCallbacks(updateTimerRunnable)
    }
}
