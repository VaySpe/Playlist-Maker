package com.example.playlistmaker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class HistoryTrackAdapter(private val tracks: ArrayList<Track>) : RecyclerView.Adapter<HistoryTrackViewHolder> () {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryTrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_view, parent, false)
        return HistoryTrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryTrackViewHolder, position: Int) {
        holder.bind(tracks[position])
    }

    override fun getItemCount() = tracks.size
}