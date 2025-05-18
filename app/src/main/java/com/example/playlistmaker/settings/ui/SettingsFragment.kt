package com.example.playlistmaker.settings.ui

import android.content.Intent
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import com.example.playlistmaker.app.App
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.android.material.textview.MaterialTextView
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val viewModel: SettingsViewModel by viewModel()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val shareBtn = view.findViewById<MaterialTextView>(R.id.share)
        val supportBtn = view.findViewById<MaterialTextView>(R.id.support)
        val docBtn = view.findViewById<MaterialTextView>(R.id.doc)
        val themeSwitcher = view.findViewById<SwitchMaterial>(R.id.themeSwitcher)

        // Обновление состояния UI по состоянию экрана
        viewModel.screenState.observe(viewLifecycleOwner) { state ->
            themeSwitcher.isChecked = state.isDarkTheme
            (requireActivity().application as App).switchTheme(state.isDarkTheme)
        }

        // Обработка одноразовых событий
        viewModel.events.observe(viewLifecycleOwner) { event ->
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
                null -> {}
            }
            viewModel.onEventHandled()
        }

        viewModel.loadTheme()

        themeSwitcher.setOnCheckedChangeListener { _, isChecked ->
            viewModel.switchTheme(isChecked)
        }

        shareBtn.setOnClickListener { viewModel.shareApp() }
        supportBtn.setOnClickListener { viewModel.openSupport() }
        docBtn.setOnClickListener { viewModel.openTerms() }

        // Настройка цветов переключателя
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        val trackColors = intArrayOf(
            ContextCompat.getColor(requireContext(), R.color.track_color_checked),
            ContextCompat.getColor(requireContext(), R.color.track_color_unchecked)
        )
        val thumbColors = intArrayOf(
            ContextCompat.getColor(requireContext(), R.color.thumb_color_checked),
            ContextCompat.getColor(requireContext(), R.color.thumb_color_unchecked)
        )
        themeSwitcher.trackTintList = ColorStateList(states, trackColors)
        themeSwitcher.thumbTintList = ColorStateList(states, thumbColors)
    }

    companion object {
        fun newInstance() = SettingsFragment()
    }
}
