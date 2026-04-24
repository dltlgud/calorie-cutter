package com.example.main.information

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.main.R

class ReviewAdapter(private val items: List<Review>) :
    RecyclerView.Adapter<ReviewAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userText: TextView = view.findViewById(R.id.textUser)
        val commentText: TextView = view.findViewById(R.id.textComment)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_review, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val r = items[position]
        holder.userText.text = r.userName
        holder.commentText.text = r.comment
    }

    override fun getItemCount() = items.size
}
