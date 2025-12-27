package com.example.androidapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.androidapp.databinding.ItemVehicleBinding
import com.example.androidapp.model.Vehicle

class VehicleAdapter (
    private val vehicles: MutableList<Vehicle>,
    private val onClick: (Vehicle) -> Unit
) : RecyclerView.Adapter<VehicleAdapter.VehicleViewHolder>(){
    inner class VehicleViewHolder(val binding: ItemVehicleBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): VehicleViewHolder {
        val binding = ItemVehicleBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return VehicleViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: VehicleViewHolder,
        position: Int
    ) {
        val vehicle = vehicles[position]

        holder.binding.tvVehicleId.text = vehicle.id
        holder.binding.tvLocation.text = vehicle.location
        holder.binding.tvVehicleType.text = vehicle.type
        holder.binding.tvAcceleration.text = vehicle.acceleration.toString()

        holder.itemView.setOnClickListener {
            onClick(vehicle)
        }
    }

    override fun getItemCount() = vehicles.size
}