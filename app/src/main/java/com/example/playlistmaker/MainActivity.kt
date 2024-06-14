package com.example.playlistmaker

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val searchBtn = findViewById<Button>(R.id.search)

        val btnClickListener: View.OnClickListener = object : View.OnClickListener {
            override fun onClick(v: View?) {
                val displayIntent = Intent(this@MainActivity, MainActivity2::class.java)
                startActivity(displayIntent)
            }
        }
        searchBtn.setOnClickListener(btnClickListener)

        val libraryBtn = findViewById<Button>(R.id.library)

        libraryBtn.setOnClickListener {
            val displayIntent = Intent(this, MainActivity2::class.java)
            startActivity(displayIntent)
        }

        val settingsBtn = findViewById<Button>(R.id.settings)

        settingsBtn.setOnClickListener {
            val displayIntent = Intent(this, SettingsActivity::class.java)
            startActivity(displayIntent)
        }
    }
}