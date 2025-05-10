package com.example.playlistmaker.settings.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.playlistmaker.R
import com.example.playlistmaker.app.App
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {

    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        val backSettingsBtn = findViewById<androidx.appcompat.widget.Toolbar>(R.id.settings_back)
        val shareBtn = findViewById<MaterialTextView>(R.id.share)
        val supportBtn = findViewById<MaterialTextView>(R.id.support)
        val docBtn = findViewById<MaterialTextView>(R.id.doc)
        val themeSwitcher = findViewById<SwitchMaterial>(R.id.themeSwitcher)

        viewModel.screenState.observe(this) { state ->
            themeSwitcher.isChecked = state.isDarkTheme
            (applicationContext as App).switchTheme(state.isDarkTheme)
        }

        viewModel.events.observe(this) { event ->
            when (event) {
                SettingsEvent.ShareApp -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, getString(R.string.share_url))
                    }
                    startActivity(intent)
                }
                SettingsEvent.OpenSupport -> {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:")
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(getString(R.string.mail)))
                        putExtra(Intent.EXTRA_SUBJECT, getString(R.string.mail_theme))
                        putExtra(Intent.EXTRA_TEXT, getString(R.string.mail_message))
                    }
                    startActivity(intent)
                }
                SettingsEvent.OpenTerms -> {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(getString(R.string.doc_url)))
                    startActivity(intent)
                }
                null -> { }
            }
            viewModel.onEventHandled()
        }

        viewModel.loadTheme()

        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            viewModel.switchTheme(isChecked)
        }

        backSettingsBtn.setNavigationOnClickListener { finish() }

        shareBtn.setOnClickListener { viewModel.shareApp() }
        supportBtn.setOnClickListener { viewModel.openSupport() }
        docBtn.setOnClickListener { viewModel.openTerms() }

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
