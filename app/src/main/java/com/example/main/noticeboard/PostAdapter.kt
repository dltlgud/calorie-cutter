package com.example.main.noticeboard

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.main.R
import com.example.main.model.Post
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class PostAdapter(
    private val postList: MutableList<Post>,
    private val fragment: Fragment
) : RecyclerView.Adapter<PostAdapter.PostViewHolder>() {

    private val db = FirebaseFirestore.getInstance()

    class PostViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textTitle: TextView = view.findViewById(R.id.textTitle)
        val textContent: TextView = view.findViewById(R.id.textContent)
        val textCommentCount: TextView = view.findViewById(R.id.textCommentCount)
        val textTimeAgo: TextView = view.findViewById(R.id.textTimeAgo)
        val textWriter: TextView = view.findViewById(R.id.textWriter)
        val textCategory: TextView = view.findViewById(R.id.textCategory)
        var commentListener: ListenerRegistration? = null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_post, parent, false)
        return PostViewHolder(view)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = postList[position]
        val ctx = holder.itemView.context

        holder.textTitle.text = post.title
        holder.textContent.text = post.content
        holder.textWriter.text = post.authorName.ifBlank { "익명" }

        // 카테고리 뱃지
        when (post.category) {
            "schedule" -> {
                holder.textCategory.text = "일정 공유"
                holder.textCategory.setTextColor(ContextCompat.getColor(ctx, R.color.primary))
                holder.textCategory.setBackgroundResource(R.drawable.bg_tag_primary)
            }
            "free" -> {
                holder.textCategory.text = "자유"
                holder.textCategory.setTextColor(ContextCompat.getColor(ctx, R.color.success))
                holder.textCategory.setBackgroundResource(R.drawable.bg_tag_success)
            }
            else -> {
                holder.textCategory.text = post.category
                holder.textCategory.setBackgroundResource(R.drawable.bg_tag_primary)
            }
        }

        // 시간 표시
        val now = System.currentTimeMillis()
        val timeInMillis = try { post.timestamp2?.toDate()?.time } catch (e: Exception) { null }
        val finalTime = timeInMillis ?: post.timestamp
        holder.textTimeAgo.text = getTimeAgo(now - finalTime, finalTime)

        // 댓글 수 실시간 반영
        holder.commentListener?.remove()
        holder.commentListener = post.id.takeIf { it.isNotEmpty() }?.let { postId ->
            db.collection("posts").document(postId).collection("comments")
                .addSnapshotListener { snapshot, _ ->
                    holder.textCommentCount.text = "💬 ${snapshot?.size() ?: 0}"
                }
        }

        // 게시글 클릭
        holder.itemView.setOnClickListener {
            ctx.startActivity(Intent(ctx, PostDetailActivity::class.java).apply {
                putExtra("title", post.title)
                putExtra("content", post.content)
                putExtra("imageUrl", post.imageUrl)
                putExtra("postId", post.id)
                putExtra("category", post.category)
                putExtra("timestamp", finalTime)
                putExtra("authorId", post.authorId)
                putExtra("authorName", post.authorName.ifBlank { "익명" })
            })
        }
    }

    override fun getItemCount(): Int = postList.size

    override fun onViewRecycled(holder: PostViewHolder) {
        super.onViewRecycled(holder)
        holder.commentListener?.remove()
        holder.commentListener = null
    }

    private fun getTimeAgo(diffMillis: Long, timestampMillis: Long): String {
        val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMillis)
        val hours = TimeUnit.MILLISECONDS.toHours(diffMillis)
        return when {
            minutes < 1 -> "방금 전"
            minutes < 60 -> "${minutes}분 전"
            hours < 3 -> "${hours}시간 전"
            else -> SimpleDateFormat("MM.dd HH:mm", Locale.getDefault()).format(Date(timestampMillis))
        }
    }

    fun updateList(newList: List<Post>) {
        postList.clear()
        postList.addAll(newList)
        notifyDataSetChanged()
    }
}
