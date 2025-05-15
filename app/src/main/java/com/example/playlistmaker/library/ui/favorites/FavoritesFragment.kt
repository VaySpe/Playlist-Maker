package com.example.playlistmaker.library.ui.favorites


import androidx.fragment.app.Fragment
import com.example.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class FavoritesFragment : Fragment(R.layout.fragment_favorites) {
    private val viewModel: FavoritesViewModel by viewModel()

    companion object {
        fun newInstance() = FavoritesFragment()
    }
}