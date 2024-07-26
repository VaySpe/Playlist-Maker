package com.example.playlistmaker

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.material.switchmaterial.SwitchMaterial

const val PLAYLIST_MAKER_PREFERENCES = "playlist_maker_preferences`"
const val DARK_MODE_KEY = "key_for_dark_mode"

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backSettingsBtn = findViewById<androidx.appcompat.widget.Toolbar>(R.id.settings_back)

        backSettingsBtn.setOnClickListener {
            finish()
        }


        val shareBtn =
            findViewById<com.google.android.material.textview.MaterialTextView>(R.id.share)

        shareBtn.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_url))
            startActivity(shareIntent)
        }


        val supportBtn =
            findViewById<com.google.android.material.textview.MaterialTextView>(R.id.support)

        supportBtn.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SENDTO)
            shareIntent.data = Uri.parse("mailto:")
            shareIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.mail)))
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_theme))
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.mail_message))
            startActivity(shareIntent)
        }


        val docBtn = findViewById<com.google.android.material.textview.MaterialTextView>(R.id.doc)

        docBtn.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_VIEW)
            shareIntent.data = Uri.parse(getString(R.string.doc_url))
            startActivity(shareIntent)
        }

        val sharedPrefs = getSharedPreferences(PLAYLIST_MAKER_PREFERENCES, MODE_PRIVATE)
        val themeSwitcher = findViewById<SwitchMaterial>(R.id.themeSwitcher)
        val isDarkMode = sharedPrefs.getBoolean(DARK_MODE_KEY, false)
        themeSwitcher.isChecked = isDarkMode

        // Установка цветов программно
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked), // checked
            intArrayOf(-android.R.attr.state_checked) // unchecked
        )

        val trackColors = intArrayOf(
            ContextCompat.getColor(this, R.color.track_color_checked),
            ContextCompat.getColor(this, R.color.track_color_unchecked)
        )
        val thumbColors = intArrayOf(
            ContextCompat.getColor(this, R.color.thumb_color_checked),
            ContextCompat.getColor(this, R.color.thumb_color_unchecked)
        )

        themeSwitcher.trackTintList = ColorStateList(states, trackColors)
        themeSwitcher.thumbTintList = ColorStateList(states, thumbColors)

        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            sharedPrefs.edit().putBoolean(DARK_MODE_KEY, isChecked).apply()
            (applicationContext as App).switchTheme(isChecked)
        }
    }
}