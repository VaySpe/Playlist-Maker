package com.example.playlistmaker

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
                Toast.makeText(this@MainActivity, "Нажали на кнопку1!", Toast.LENGTH_SHORT).show()
            }
        }
        searchBtn.setOnClickListener(btnClickListener)

        val libraryBtn = findViewById<Button>(R.id.library)

        libraryBtn.setOnClickListener {
            Toast.makeText(this@MainActivity, "Нажали на кнопку2!", Toast.LENGTH_SHORT).show()
        }

        val settingsBtn = findViewById<Button>(R.id.settings)

        settingsBtn.setOnClickListener {
            Toast.makeText(this@MainActivity, "Нажали на кнопку3!", Toast.LENGTH_SHORT).show()
        }
    }
}