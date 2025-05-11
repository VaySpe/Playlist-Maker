package com.example.playlistmaker.search.ui

import android.content.Context
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.example.playlistmaker.R
import com.example.playlistmaker.search.domain.Track

class TrackAdapter(
    private var tracks: List<Track>,
    private val onClick: (Track) -> Unit
) : RecyclerView.Adapter<TrackAdapter.TrackViewHolder>() {

    interface OnTrackClickListener {
        fun onTrackClick(track: Track)
    }

    fun setData(newTracks: List<Track>) {
        tracks = newTracks
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TrackViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.track_view, parent, false)
        return TrackViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: TrackViewHolder, position: Int) {
        holder.bind(tracks[position])
    }

    override fun getItemCount(): Int = tracks.size

    class TrackViewHolder(
        itemView: View,
        private val onClick: (Track) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        // Находим нужные View по их ID (меняйте на свои названия)
        private val trackNameView = itemView.findViewById<TextView>(R.id.trackName)
        private val artistNameView = itemView.findViewById<TextView>(R.id.artistName)
        private val trackTimeView = itemView.findViewById<TextView>(R.id.trackTime)
        private val trackImageView = itemView.findViewById<ImageView>(R.id.trackImage)

        fun bind(track: Track) {
            // Заполняем тексты
            trackNameView.text = track.trackName
            artistNameView.text = track.artistName
            trackTimeView.text = track.trackTime

            // Загружаем картинку (при необходимости)
            Glide.with(itemView.context)
                .load(track.artworkUrl100)
                .placeholder(R.drawable.placeholder)
                .centerCrop()
                .transform(RoundedCorners(dpToPx(2f, itemView.context)))
                .into(trackImageView)

            // Обработка клика
            itemView.setOnClickListener {
                onClick(track)
            }
        }

        // Пример функции для конвертации dp -> px, если нужен RoundedCorners
        private fun dpToPx(dp: Float, context: Context): Int {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp,
                context.resources.displayMetrics
            ).toInt()
        }
    }
}
