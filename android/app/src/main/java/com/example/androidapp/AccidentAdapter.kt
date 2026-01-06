package com.example.androidapp

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.androidapp.databinding.ItemAccidentBinding
import com.example.androidapp.model.Accident

class AccidentAdapter(
    private val accidents: MutableList<Accident>,
    private val onClick: (Accident) -> Unit
) : RecyclerView.Adapter<AccidentAdapter.AccidentViewHolder>() {

    inner class AccidentViewHolder(val binding: ItemAccidentBinding) :
        RecyclerView.ViewHolder(binding.root)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AccidentViewHolder {
        val binding = ItemAccidentBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return AccidentViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: AccidentViewHolder,
        position: Int
    ) {
        val accident = accidents[position]

        holder.binding.tvAccidentId.text = accident.id
        holder.binding.tvLocation.text = accident.location
        holder.binding.tvAccidentType.text = accident.type

        holder.itemView.setOnClickListener {
            onClick(accident)
        }
    }

    override fun getItemCount() = accidents.size

    fun addAccident(accident: Accident) {
        accidents.add(0, accident)
        notifyItemInserted(0)
    }

}