package com.example.playlistmaker

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

class TrackAdapter(private val tracks: ArrayList<Track>,
                   private val clickListener: OnTrackClickListener
) : RecyclerView.Adapter<TrackViewHolder> () {

    var selectedTrack: Track? = null

    interface OnTrackClickListener {
        fun onTrackClick(track: Track)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.track_view, parent, false)
        return TrackViewHolder(view)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position], clickListener)
    }

    override fun getItemCount() = tracks.size
}