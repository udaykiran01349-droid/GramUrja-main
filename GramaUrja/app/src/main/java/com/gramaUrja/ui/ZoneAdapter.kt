package com.gramaUrja.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gramaUrja.R
import com.gramaUrja.databinding.ItemZoneBinding
import com.gramaUrja.model.Zone
import com.gramaUrja.viewmodel.ZoneWithStatus

class ZoneAdapter(
    private val onZoneClick: (Zone) -> Unit
) : ListAdapter<ZoneWithStatus, ZoneAdapter.ZoneViewHolder>(ZoneDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ZoneViewHolder {
        val binding = ItemZoneBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ZoneViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ZoneViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ZoneViewHolder(private val binding: ItemZoneBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ZoneWithStatus) {
            val zone = item.zone
            val status = item.status

            binding.tvZoneName.text = zone.name
            binding.tvDistrict.text = zone.district
            
            // Bind Status UI
            if (status != null) {
                if (status.isOn) {
                    binding.ivStatusIcon.setImageResource(R.drawable.ic_bolt)
                    binding.ivStatusIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.power_on_green))
                    binding.tvStatusText.text = "ON"
                    binding.tvStatusText.setTextColor(ContextCompat.getColor(itemView.context, R.color.power_on_green))
                } else {
                    binding.ivStatusIcon.setImageResource(R.drawable.ic_bolt_off)
                    binding.ivStatusIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.power_off_red))
                    binding.tvStatusText.text = "OFF"
                    binding.tvStatusText.setTextColor(ContextCompat.getColor(itemView.context, R.color.power_off_red))
                }
            } else {
                // Default to OFF or Unknown if no status exists yet
                binding.ivStatusIcon.setImageResource(R.drawable.ic_bolt_off)
                binding.ivStatusIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.text_secondary))
                binding.tvStatusText.text = "--"
                binding.tvStatusText.setTextColor(ContextCompat.getColor(itemView.context, R.color.text_secondary))
            }

            binding.root.setOnClickListener { onZoneClick(zone) }
        }
    }

    class ZoneDiffCallback : DiffUtil.ItemCallback<ZoneWithStatus>() {
        override fun areItemsTheSame(oldItem: ZoneWithStatus, newItem: ZoneWithStatus) = 
            oldItem.zone.id == newItem.zone.id
            
        override fun areContentsTheSame(oldItem: ZoneWithStatus, newItem: ZoneWithStatus) = 
            oldItem == newItem
    }
}
