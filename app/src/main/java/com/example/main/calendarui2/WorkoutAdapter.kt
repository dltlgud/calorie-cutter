package com.example.main.calendarui2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.main.R

class WorkoutAdapter(private var items: List<WorkoutRecord>) :
    RecyclerView.Adapter<WorkoutAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.tvWorkoutName)
        val info: TextView = itemView.findViewById(R.id.tvSetInfo)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.info.text = "${item.sets}세트 ${item.kg}kg x ${item.reps}회"
    }

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<WorkoutRecord>) {
        items = newItems
        notifyDataSetChanged()
    }
}
