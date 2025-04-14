package com.example.playlistmaker.player.ui

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.search.domain.Track
import com.google.gson.Gson
import kotlinx.coroutines.launch

class AudioplayerActivity : AppCompatActivity() {

    private val viewModel: PlayerViewModel by viewModels {
        Creator.providePlayerViewModelFactory(this)
    }

    private lateinit var playBtn: ImageButton
    private lateinit var timerTextView: TextView

    // UI элементы для показа информации о треке
    private lateinit var trackNameTextView: TextView
    private lateinit var artistNameTextView: TextView
    private lateinit var albumTextView: TextView
    private lateinit var durationTextView: TextView
    private lateinit var yearTextView: TextView
    private lateinit var genreTextView: TextView
    private lateinit var countryTextView: TextView
    private lateinit var imageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)

        // Инициализация View
        playBtn = findViewById(R.id.play_btn)
        timerTextView = findViewById(R.id.audioplayer_timer)
        trackNameTextView = findViewById(R.id.audioplayer_trackName)
        artistNameTextView = findViewById(R.id.audioplayer_artistName)
        albumTextView = findViewById(R.id.value_album)
        durationTextView = findViewById(R.id.value_duration)
        yearTextView = findViewById(R.id.value_year)
        genreTextView = findViewById(R.id.value_genre)
        countryTextView = findViewById(R.id.value_country)
        imageView = findViewById(R.id.audioplayer_image)

        // Обработчик кнопки "Назад"
        findViewById<androidx.appcompat.widget.Toolbar>(R.id.audioplayer_back).setNavigationOnClickListener { finish() }

        // Подписка на единое состояние экрана
        viewModel.screenState.observe(this) { state ->
            // Обновляем информацию о треке
            trackNameTextView.text = state.trackName
            artistNameTextView.text = state.artistName
            albumTextView.text = state.album
            durationTextView.text = state.duration
            yearTextView.text = state.year
            genreTextView.text = state.genre
            countryTextView.text = state.country

            // Обновляем таймер
            timerTextView.text = state.timerText

            // Обновляем состояние кнопки
            val drawableRes = if (state.playerState == com.example.playlistmaker.player.domain.PlayerState.PLAYING)
                R.drawable.button_pause else R.drawable.button_play
            playBtn.setImageDrawable(ContextCompat.getDrawable(this, drawableRes))

            // Загружаем изображение трека через Glide
            Glide.with(this)
                .load(state.artworkUrl)
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .transform(RoundedCorners(Creator.provideDpToPxUseCase().execute(8f)))
                .into(imageView)
        }

        // Обработчик кнопки play/pause
        playBtn.setOnClickListener {
            viewModel.togglePlayPause()
        }

        // Извлечение трека из Intent и подготовка плеера
        val trackJson = intent.getStringExtra("track_json") ?: return
        val track = Gson().fromJson(trackJson, Track::class.java)
        lifecycleScope.launch {
            viewModel.prepare(track)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pause()
    }
}
