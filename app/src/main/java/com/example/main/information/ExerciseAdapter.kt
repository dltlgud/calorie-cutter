// ExerciseAdapter.kt
package com.example.main.information

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.main.databinding.ItemExerciseBinding

class ExerciseAdapter(
    private val onClick: (Exercise) -> Unit,
    private val onBookmarkClick: (Exercise) -> Unit,
    private val isBookmarked: (Exercise) -> Boolean
) : ListAdapter<Exercise, ExerciseAdapter.ViewHolder>(ExerciseDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemExerciseBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ViewHolder(
        private val binding: ItemExerciseBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onClick(getItem(pos))
                }
            }
            binding.ivBookmark.setOnClickListener {
                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    onBookmarkClick(getItem(pos))
                }
            }
        }

        fun bind(item: Exercise) {
            binding.tvName.text = item.name
            binding.tvCategory.text = item.category
            binding.ivIcon.setImageResource(item.imageRes)
            binding.ivBookmark.isSelected = isBookmarked(item)
        }
    }

    class ExerciseDiffCallback : DiffUtil.ItemCallback<Exercise>() {
        override fun areItemsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem.name == newItem.name
        }
        override fun areContentsTheSame(oldItem: Exercise, newItem: Exercise): Boolean {
            return oldItem == newItem
        }
    }
}
