package com.example.main.routine

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.main.R

class RecommendationAdapter(
    private val exercises: List<ExerciseData>,
    private val onRecordClick: (ExerciseData) -> Unit
) : RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder>() {

    class RecommendationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvBadge: TextView = itemView.findViewById(R.id.tv_body_part_badge)
        val tvExerciseName: TextView = itemView.findViewById(R.id.tv_exercise_name)
        val tvExerciseDetails: TextView = itemView.findViewById(R.id.tv_exercise_details)
        val btnRecord: Button = itemView.findViewById(R.id.btn_record_exercise)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommendation, parent, false)
        return RecommendationViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        val exercise = exercises[position]
        holder.tvBadge.text = exercise.bodyPart
        holder.tvExerciseName.text = exercise.name
        holder.tvExerciseDetails.text = "${exercise.bodyPart} · ${exercise.bodyPartType}"
        holder.btnRecord.setOnClickListener { onRecordClick(exercise) }
    }

    override fun getItemCount(): Int = exercises.size
}
