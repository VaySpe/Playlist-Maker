package com.example.playlistmaker.player.ui

import android.os.Bundle
import android.util.Log
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
import com.example.playlistmaker.player.domain.PlayerState
import com.google.gson.Gson
import kotlinx.coroutines.launch

class AudioplayerActivity : AppCompatActivity() {

    private val viewModel: PlayerViewModel by viewModels {
        Creator.providePlayerViewModelFactory(this)
    }

    private lateinit var playBtn: ImageButton
    private lateinit var timerTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)

        // Поиск View
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
        val backBtn = findViewById<androidx.appcompat.widget.Toolbar>(R.id.audioplayer_back)

        // Извлечение данных трека из интента
        val trackJson = intent.getStringExtra("track_json") ?: ""
        val track = Gson().fromJson(trackJson, Track::class.java)

        // Заполнение UI данными трека
        trackNameTextView.text = track.trackName
        artistNameTextView.text = track.artistName
        albumTextView.text = track.collectionName
        durationTextView.text = track.trackTime
        yearTextView.text = track.releaseDate.substring(0, 4)
        genreTextView.text = track.primaryGenreName
        countryTextView.text = track.country
        timerTextView.text = getString(R.string.full_track_time)

        // Загрузка изображения через Glide с применением округленных углов
        val dpToPx = Creator.provideDpToPxUseCase().execute(8f)
        val artworkUrl512 = track.artworkUrl100.replaceAfterLast('/', "512x512bb.jpg")
        Glide.with(this)
            .load(artworkUrl512)
            .placeholder(R.drawable.placeholder)
            .centerCrop()
            .transform(RoundedCorners(dpToPx))
            .into(imageView)

        // Обработчик кнопки "Назад"
        backBtn.setNavigationOnClickListener {
            finish()
        }

        // Подписка на LiveData из ViewModel
        viewModel.playerState.observe(this) { state ->
            val drawable = if (state == PlayerState.PLAYING) {
                ContextCompat.getDrawable(this, R.drawable.button_pause)
            } else {
                ContextCompat.getDrawable(this, R.drawable.button_play)
            }
            playBtn.setImageDrawable(drawable)
        }

        viewModel.timerText.observe(this) { time ->
            timerTextView.text = time
        }

        // Обработчик кнопки play/pause
        playBtn.setOnClickListener {
            Log.d("AudioplayerActivity", "Play button pressed")
            viewModel.togglePlayPause()
        }


        // Подготовка плеера в корутине
        lifecycleScope.launch {
            viewModel.prepare(track)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pause()
    }
}
