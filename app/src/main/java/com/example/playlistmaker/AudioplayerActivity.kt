package com.example.playlistmaker

import android.content.Context
import android.icu.text.SimpleDateFormat
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson
import java.util.Locale

class AudioplayerActivity : AppCompatActivity() {

    private lateinit var play: ImageButton
    private lateinit var previewUrl: String
    private var mediaPlayer = MediaPlayer()
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var timerTextView: TextView
    private lateinit var updateTimerRunnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_audioplayer)

        val trackJson = intent.getStringExtra("track_json")
        val gson = Gson()
        val track = gson.fromJson(trackJson, Track::class.java)
        val artworkUrl512 = track.artworkUrl100.replaceAfterLast('/',"512x512bb.jpg")


        val trackNameTextView = findViewById<TextView>(R.id.audioplayer_trackName)
        val artistNameTextView = findViewById<TextView>(R.id.audioplayer_artistName)
        val albumTextView = findViewById<TextView>(R.id.value_album)
        val durationTextView = findViewById<TextView>(R.id.value_duration)
        val yearTextView = findViewById<TextView>(R.id.value_year)
        val genreTextView = findViewById<TextView>(R.id.value_genre)
        val countryTextView = findViewById<TextView>(R.id.value_country)
        val imageView = findViewById<ImageView>(R.id.audioplayer_image)
        timerTextView = findViewById<TextView>(R.id.audioplayer_timer)

        trackNameTextView.text = track.trackName
        artistNameTextView.text = track.artistName
        albumTextView.text = track.collectionName
        durationTextView.text = track.trackTime
        yearTextView.text = track.releaseDate.substring(0, 4)
        genreTextView.text = track.primaryGenreName
        countryTextView.text = track.country
        timerTextView.text = "00:00"
        play = findViewById(R.id.play_btn)

        Glide.with(this)
            .load(artworkUrl512)
            .placeholder(R.drawable.placeholder)
            .centerCrop()
            .transform(RoundedCorners(dpToPx(8.0f, this)))
            .into(imageView)

        updateTimerRunnable = Runnable {
            if(playerState == STATE_PLAYING){
                val formattedTime = SimpleDateFormat("mm:ss", Locale.getDefault()).format(mediaPlayer.currentPosition)
                timerTextView.text = formattedTime
                handler.postDelayed(updateTimerRunnable, TRACK_DEBOUNCE_DELAY)
            }
        }

        previewUrl = track.previewUrl
        preparePlayer()

        play.setOnClickListener {
            playbackControl()
        }

        val backSearchBtn = findViewById<androidx.appcompat.widget.Toolbar>(R.id.audioplayer_back)

        backSearchBtn.setOnClickListener {
            finish()
        }
    }

    override fun onPause() {
        super.onPause()
        pausePlayer()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaPlayer.release()
        handler.removeCallbacks(updateTimerRunnable)
    }

    private fun dpToPx(dp: Float, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }
        companion object {
        private const val STATE_DEFAULT = 0
        private const val STATE_PREPARED = 1
        private const val STATE_PLAYING = 2
        private const val STATE_PAUSED = 3
        private const val TRACK_DEBOUNCE_DELAY = 200L
    }

    private var playerState = STATE_DEFAULT

    private fun preparePlayer() {
        mediaPlayer.setDataSource(previewUrl)
        mediaPlayer.prepareAsync()
        mediaPlayer.setOnPreparedListener {
            play.isEnabled = true
            playerState = STATE_PREPARED
        }
        mediaPlayer.setOnCompletionListener {
            play.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.button_play))
            playerState = STATE_PREPARED
            handler.removeCallbacks(updateTimerRunnable)
            findViewById<TextView>(R.id.audioplayer_timer).text = "00:00"
        }
    }

    private fun startPlayer() {
        mediaPlayer.start()
        play.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.button_pause))
        playerState = STATE_PLAYING
        handler.post(updateTimerRunnable)

    }

    private fun pausePlayer() {
        mediaPlayer.pause()
        play.setImageDrawable(ContextCompat.getDrawable(this, R.drawable.button_play))
        playerState = STATE_PAUSED
        handler.removeCallbacks(updateTimerRunnable)
    }

    private fun playbackControl() {
        when(playerState) {
            STATE_PLAYING -> {
                pausePlayer()
            }
            STATE_PREPARED, STATE_PAUSED -> {
                startPlayer()
            }
        }
    }
}