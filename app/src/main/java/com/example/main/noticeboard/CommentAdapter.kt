package com.example.main.noticeboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*
import com.example.main.R
import com.example.main.model.Comment

class CommentAdapter(private val comments: List<Comment>) :
    RecyclerView.Adapter<CommentAdapter.CommentViewHolder>() {

    inner class CommentViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textContent: TextView = itemView.findViewById(R.id.textCommentContent)
        val textTimestamp: TextView = itemView.findViewById(R.id.textCommentTimestamp)
        val textAuthor: TextView = itemView.findViewById(R.id.textCommentAuthor)
        val textAvatar: TextView = itemView.findViewById(R.id.textCommentAvatar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommentViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_comment, parent, false)
        return CommentViewHolder(view)
    }

    override fun onBindViewHolder(holder: CommentViewHolder, position: Int) {
        val comment = comments[position]
        val displayName = comment.userId.ifBlank { "익명" }
        holder.textAuthor.text = displayName
        holder.textAvatar.text = displayName.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        holder.textContent.text = comment.content
        holder.textTimestamp.text = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
            .format(Date(comment.timestamp))
    }

    override fun getItemCount(): Int = comments.size
}