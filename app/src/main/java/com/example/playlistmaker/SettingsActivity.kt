package com.example.playlistmaker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.switchmaterial.SwitchMaterial

const val PLAYLIST_MAKER_PREFERENCES  = "playlist_maker_preferences`"
const val DARK_MODE_KEY = "key_for_dark_mode"

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backSettingsBtn = findViewById<androidx.appcompat.widget.Toolbar>(R.id.settings_back)

        backSettingsBtn.setOnClickListener {
            finish()
        }


        val shareBtn = findViewById<com.google.android.material.textview.MaterialTextView>(R.id.share)

        shareBtn.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_url))
            startActivity(shareIntent)
        }


        val supportBtn = findViewById<com.google.android.material.textview.MaterialTextView>(R.id.support)

        supportBtn.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SENDTO)
            shareIntent.data = Uri.parse("mailto:")
            shareIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.mail)))
            shareIntent.putExtra(Intent.EXTRA_SUBJECT,  getString(R.string.mail_theme))
            shareIntent.putExtra(Intent.EXTRA_TEXT,  getString(R.string.mail_message))
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

        themeSwitcher.setOnCheckedChangeListener { switcher, checked ->
            sharedPrefs.edit().putBoolean(DARK_MODE_KEY, checked).apply()
            (applicationContext as App).switchTheme(checked)
        }
    }
}