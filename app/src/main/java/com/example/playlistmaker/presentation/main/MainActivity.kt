package com.example.playlistmaker.presentation.main

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.playlistmaker.presentation.library.MediaLibraryActivity
import com.example.playlistmaker.R
import com.example.playlistmaker.presentation.settings.SettingsActivity
import com.example.playlistmaker.presentation.search.SearchActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchBtn = findViewById<Button>(R.id.search)

        searchBtn.setOnClickListener {
            val displayIntent = Intent(this, SearchActivity::class.java)
            startActivity(displayIntent)
        }

        val libraryBtn = findViewById<Button>(R.id.library)

        libraryBtn.setOnClickListener {
            val displayIntent = Intent(this, MediaLibraryActivity::class.java)
            startActivity(displayIntent)
        }

        val settingsBtn = findViewById<Button>(R.id.settings)

        settingsBtn.setOnClickListener {
            val displayIntent = Intent(this, SettingsActivity::class.java)
            startActivity(displayIntent)
        }
    }
}