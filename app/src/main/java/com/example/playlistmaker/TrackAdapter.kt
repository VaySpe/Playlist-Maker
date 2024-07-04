package com.example.playlistmaker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TrackAdapter(private val tracks: ArrayList<Track>) : RecyclerView.Adapter<TrackViewHodler> () {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHodler {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_view, parent, false)
        return TrackViewHodler(view)
    }

    override fun onBindViewHolder(holder: TrackViewHodler, position: Int) {
        holder.bind(tracks[position])
    }

    override fun getItemCount() = tracks.size
}