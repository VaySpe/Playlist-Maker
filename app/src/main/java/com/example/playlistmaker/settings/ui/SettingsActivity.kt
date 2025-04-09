package com.example.playlistmaker.settings.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.playlistmaker.creator.App
import com.example.playlistmaker.creator.Creator
import com.example.playlistmaker.R
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView

class SettingsActivity : AppCompatActivity() {

    private val viewModel by lazy {
        Creator.provideSettingsViewModel()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        // UI ссылки
        val backSettingsBtn = findViewById<androidx.appcompat.widget.Toolbar>(R.id.settings_back)
        val shareBtn = findViewById<MaterialTextView>(R.id.share)
        val supportBtn = findViewById<MaterialTextView>(R.id.support)
        val docBtn = findViewById<MaterialTextView>(R.id.doc)
        val themeSwitcher = findViewById<SwitchMaterial>(R.id.themeSwitcher)

        // Подписка на LiveData
        viewModel.darkThemeEnabled.observe(this) { isDark ->
            themeSwitcher.isChecked = isDark
            (applicationContext as App).switchTheme(isDark)
        }

        // Загрузка текущей темы
        viewModel.loadTheme()

        // Переключатель
        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            viewModel.switchTheme(isChecked)
        }

        // Назад
        backSettingsBtn.setOnClickListener { finish() }

        // Share
        shareBtn.setOnClickListener {
            viewModel.shareApp()
        }

        // Support
        supportBtn.setOnClickListener {
            viewModel.openSupport()
        }

        // Docs
        docBtn.setOnClickListener {
            viewModel.openTerms()
        }

        // Цвета свитчера (если используешь)
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
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
    }
}
