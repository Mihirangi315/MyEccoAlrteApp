package com.example.myeccoalrteapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class DetectionAdapter(private val events: List<DetectionEvent>) :
    RecyclerView.Adapter<DetectionAdapter.DetectionViewHolder>() {

    class DetectionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivSoundIcon: ImageView = view.findViewById(R.id.ivSoundIcon)
        val tvSoundName: TextView = view.findViewById(R.id.tvSoundName)
        val tvTimestamp: TextView = view.findViewById(R.id.tvTimestamp)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DetectionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_detection, parent, false)
        return DetectionViewHolder(view)
    }

    override fun onBindViewHolder(holder: DetectionViewHolder, position: Int) {
        val event = events[position]
        holder.tvSoundName.text = event.soundName
        holder.tvTimestamp.text = event.timestamp
        holder.ivSoundIcon.setImageResource(event.iconResId)
    }

    override fun getItemCount(): Int = events.size
}
