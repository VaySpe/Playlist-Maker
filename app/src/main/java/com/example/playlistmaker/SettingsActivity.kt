package com.example.playlistmaker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backSettingsBtn = findViewById<androidx.appcompat.widget.Toolbar>(R.id.settings_back)

        backSettingsBtn.setOnClickListener {
            val displayIntent = Intent(this, MainActivity::class.java)
            startActivity(displayIntent)
        }


        val shareBtn = findViewById<com.google.android.material.textview.MaterialTextView>(R.id.share)

        shareBtn.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            val message = "https://practicum.yandex.ru/profile/android-developer-plus/"
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, message)
            startActivity(shareIntent)
        }


        val supportBtn = findViewById<com.google.android.material.textview.MaterialTextView>(R.id.support)

        supportBtn.setOnClickListener {
            val messageTheme = "Сообщение разработчикам и разработчицам приложения Playlist Maker"
            val message = "Спасибо разработчикам и разработчицам за крутое приложение!"
            val shareIntent = Intent(Intent.ACTION_SENDTO)
            shareIntent.data = Uri.parse("mailto:")
            shareIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("vik.antufev@yandex.ru"))
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, messageTheme)
            shareIntent.putExtra(Intent.EXTRA_TEXT, message)
            startActivity(shareIntent)
        }


        val docBtn = findViewById<com.google.android.material.textview.MaterialTextView>(R.id.doc)

        docBtn.setOnClickListener {
            val url = "https://yandex.ru/legal/practicum_offer/"
            val shareIntent = Intent(Intent.ACTION_VIEW)
            shareIntent.data = Uri.parse(url)
            startActivity(shareIntent)
        }

    }
}