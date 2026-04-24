package com.example.main.noticeboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.main.R
import com.example.main.repository.FollowRepository
import com.google.firebase.firestore.DocumentSnapshot

class SocialUserAdapter(
    private val users: List<DocumentSnapshot>,
    private val myUid: String,
    private val followRepository: FollowRepository,
    private val onFollowChanged: () -> Unit
) : RecyclerView.Adapter<SocialUserAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvInitial: TextView = view.findViewById(R.id.tv_user_initial)
        val tvName: TextView = view.findViewById(R.id.tv_user_name)
        val btnFollow: Button = view.findViewById(R.id.btn_follow)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_user, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val doc = users[position]
        val name = doc.getString("username") ?: "알 수 없음"
        val targetUid = doc.id

        holder.tvName.text = name
        holder.tvInitial.text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"

        holder.btnFollow.isEnabled = false
        followRepository.isFollowing(myUid, targetUid) { isFollowing ->
            holder.itemView.post {
                holder.btnFollow.isEnabled = true
                if (isFollowing) {
                    holder.btnFollow.text = "팔로잉"
                    holder.btnFollow.alpha = 0.6f
                } else {
                    holder.btnFollow.text = "팔로우"
                    holder.btnFollow.alpha = 1f
                }
            }
        }

        holder.btnFollow.setOnClickListener {
            holder.btnFollow.isEnabled = false
            val isCurrentlyFollowing = holder.btnFollow.text == "팔로잉"
            if (isCurrentlyFollowing) {
                followRepository.unfollow(myUid, targetUid) {
                    holder.itemView.post {
                        holder.btnFollow.text = "팔로우"
                        holder.btnFollow.alpha = 1f
                        holder.btnFollow.isEnabled = true
                        onFollowChanged()
                    }
                }
            } else {
                followRepository.follow(myUid, targetUid, name) {
                    holder.itemView.post {
                        holder.btnFollow.text = "팔로잉"
                        holder.btnFollow.alpha = 0.6f
                        holder.btnFollow.isEnabled = true
                        onFollowChanged()
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = users.size
}
