package com.example.playlistmaker

import android.content.Context
import android.os.Bundle
import android.util.TypedValue
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.gson.Gson

class AudioplayerActivity : AppCompatActivity() {
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
        val timerTextView = findViewById<TextView>(R.id.audioplayer_timer)

        trackNameTextView.text = track.trackName
        artistNameTextView.text = track.artistName
        albumTextView.text = track.collectionName
        durationTextView.text = track.trackTime
        yearTextView.text = track.releaseDate.substring(0, 4)
        genreTextView.text = track.primaryGenreName
        countryTextView.text = track.country
        timerTextView.text = "0:30"

        Glide.with(this)
            .load(artworkUrl512)
            .placeholder(R.drawable.placeholder)
            .centerCrop()
            .transform(RoundedCorners(dpToPx(8.0f, this)))
            .into(imageView)


        val backSearchBtn = findViewById<androidx.appcompat.widget.Toolbar>(R.id.audioplayer_back)

        backSearchBtn.setOnClickListener {
            finish()
        }
    }
    private fun dpToPx(dp: Float, context: Context): Int {
        return TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            dp,
            context.resources.displayMetrics
        ).toInt()
    }
}