package com.example.playlistmaker.player.ui

import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.search.domain.Track
import com.example.playlistmaker.search.ui.DpToPxUseCase
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import com.google.gson.Gson

class AudioplayerActivity : AppCompatActivity() {

    private val viewModel: PlayerViewModel by viewModel()
    private val dpToPxUseCase: DpToPxUseCase by inject()
    private val gson: Gson by inject()

    private lateinit var playBtn: ImageButton
    private lateinit var timerTextView: TextView

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

        findViewById<androidx.appcompat.widget.Toolbar>(R.id.audioplayer_back)
            .setNavigationOnClickListener { finish() }

        viewModel.screenState.observe(this) { state ->
            trackNameTextView.text = state.trackName
            artistNameTextView.text = state.artistName
            albumTextView.text = state.album
            durationTextView.text = state.duration
            yearTextView.text = state.year
            genreTextView.text = state.genre
            countryTextView.text = state.country

            timerTextView.text = state.timerText

            val drawableRes = if (state.playerState ==
                com.example.playlistmaker.player.domain.PlayerState.PLAYING
            ) R.drawable.button_pause else R.drawable.button_play

            playBtn.setImageDrawable(ContextCompat.getDrawable(this, drawableRes))

            Glide.with(this)
                .load(state.artworkUrl)
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .transform(RoundedCorners(dpToPxUseCase.execute(8f)))
                .into(imageView)
        }

        playBtn.setOnClickListener {
            viewModel.togglePlayPause()
        }

        val trackJson = intent.getStringExtra("track_json") ?: return
        val track = gson.fromJson(trackJson, Track::class.java)

        lifecycleScope.launch {
            viewModel.prepare(track)
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.pause()
    }
}
