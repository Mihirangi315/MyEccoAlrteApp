package com.example.myeccoalrteapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetectionAdapter(private val events: List<DetectionEvent>) :
    RecyclerView.Adapter<DetectionAdapter.DetectionViewHolder>() {

    private val dateFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())

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
        holder.tvSoundName.text = event.soundLabel
        
        // Format the Long timestamp into a readable string
        holder.tvTimestamp.text = dateFormat.format(Date(event.timestamp))

        // Map the label back to the correct icon for the UI
        val iconRes = when (event.soundLabel) {
            "Doorbell" -> R.drawable.ic_doorbell
            "Alarm" -> R.drawable.ic_alarm
            "Knock" -> R.drawable.ic_knock
            "Baby Crying" -> R.drawable.ic_baby
            "Phone Ringing" -> R.drawable.ic_phone_ring
            else -> R.drawable.ic_listening_active
        }
        holder.ivSoundIcon.setImageResource(iconRes)
    }

    override fun getItemCount(): Int = events.size
}
