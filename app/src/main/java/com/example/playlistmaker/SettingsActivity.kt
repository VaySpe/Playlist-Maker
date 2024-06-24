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
            finish()
        }


        val shareBtn = findViewById<com.google.android.material.textview.MaterialTextView>(R.id.share)

        shareBtn.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND)
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_TEXT, R.string.share_url)
            startActivity(shareIntent)
        }


        val supportBtn = findViewById<com.google.android.material.textview.MaterialTextView>(R.id.support)

        supportBtn.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SENDTO)
            shareIntent.data = Uri.parse("mailto:")
            shareIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf("vik.antufev@yandex.ru"))
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, R.string.mail_theme)
            shareIntent.putExtra(Intent.EXTRA_TEXT, R.string.mail_message)
            startActivity(shareIntent)
        }


        val docBtn = findViewById<com.google.android.material.textview.MaterialTextView>(R.id.doc)

        docBtn.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_VIEW)
            val docUrl = getString(R.string.doc_url)
            shareIntent.data = Uri.parse(docUrl)
            startActivity(shareIntent)
        }
    }
}