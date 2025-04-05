package com.example.playlistmaker.presentation.settings

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.playlistmaker.App
import com.example.playlistmaker.Creator
import com.example.playlistmaker.R
import com.example.playlistmaker.domain.usecase.ThemeImpl
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsActivity : AppCompatActivity() {

    private lateinit var themeUseCase: ThemeImpl

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Обычное подключение макета
        setContentView(R.layout.activity_settings)

        // Получаем UseCase
        themeUseCase = Creator.provideThemeUseCase()

        // Кнопка Back
        val backSettingsBtn = findViewById<androidx.appcompat.widget.Toolbar>(R.id.settings_back)
        backSettingsBtn.setOnClickListener {
            finish()
        }

        // Share
        val shareBtn = findViewById<com.google.android.material.textview.MaterialTextView>(R.id.share)
        shareBtn.setOnClickListener {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, getString(R.string.share_url))
            }
            startActivity(shareIntent)
        }

        // Support
        val supportBtn = findViewById<com.google.android.material.textview.MaterialTextView>(R.id.support)
        supportBtn.setOnClickListener {
            val supportIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = Uri.parse("mailto:")
                putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.mail)))
                putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_theme))
                putExtra(Intent.EXTRA_TEXT, getString(R.string.mail_message))
            }
            startActivity(supportIntent)
        }

        // Doc
        val docBtn = findViewById<com.google.android.material.textview.MaterialTextView>(R.id.doc)
        docBtn.setOnClickListener {
            val docIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse(getString(R.string.doc_url))
            }
            startActivity(docIntent)
        }

        // Theme switcher
        val themeSwitcher = findViewById<SwitchMaterial>(R.id.themeSwitcher)
        val isDarkMode = themeUseCase.isDarkModeEnabled()
        themeSwitcher.isChecked = isDarkMode

        // Установка цветов (track/thumb)
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),  // checked
            intArrayOf(-android.R.attr.state_checked)  // unchecked
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
            // Сохраняем настройку через UseCase
            themeUseCase.setDarkModeEnabled(isChecked)
            // Вызываем метод приложения, который реально меняет тему
            (applicationContext as App).switchTheme(isChecked)
        }
    }
}
