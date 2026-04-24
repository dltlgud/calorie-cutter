package com.example.main.noticeboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.main.R
import com.example.main.repository.FollowRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SocialFragment : Fragment() {

    private val followRepository = FollowRepository()
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private lateinit var tvFollowers: TextView
    private lateinit var tvFollowing: TextView
    private lateinit var etSearch: EditText
    private lateinit var btnSearch: ImageButton
    private lateinit var rvUsers: RecyclerView
    private lateinit var tvEmpty: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_social, container, false)

        tvFollowers = view.findViewById(R.id.tv_followers_count)
        tvFollowing = view.findViewById(R.id.tv_following_count)
        etSearch = view.findViewById(R.id.et_user_search)
        btnSearch = view.findViewById(R.id.btn_user_search)
        rvUsers = view.findViewById(R.id.rv_users)
        tvEmpty = view.findViewById(R.id.tv_empty_users)

        rvUsers.layoutManager = LinearLayoutManager(requireContext())

        loadMyStats()
        loadUsers("")

        btnSearch.setOnClickListener {
            loadUsers(etSearch.text.toString().trim())
        }

        etSearch.setOnEditorActionListener { _, _, _ ->
            loadUsers(etSearch.text.toString().trim())
            true
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        loadMyStats()
    }

    private fun loadMyStats() {
        val uid = auth.currentUser?.uid ?: return
        followRepository.getFollowerCount(uid) { count ->
            activity?.runOnUiThread { tvFollowers.text = count.toString() }
        }
        followRepository.getFollowingCount(uid) { count ->
            activity?.runOnUiThread { tvFollowing.text = count.toString() }
        }
    }

    private fun loadUsers(query: String) {
        val myUid = auth.currentUser?.uid ?: return
        db.collection("users").get().addOnSuccessListener { result ->
            val filtered = result.documents.filter { doc ->
                val uid = doc.id
                val name = doc.getString("username") ?: ""
                uid != myUid && (query.isBlank() || name.contains(query, ignoreCase = true))
            }
            tvEmpty.visibility = if (filtered.isEmpty()) View.VISIBLE else View.GONE
            rvUsers.visibility = if (filtered.isEmpty()) View.GONE else View.VISIBLE

            rvUsers.adapter = SocialUserAdapter(filtered, myUid, followRepository) {
                loadMyStats()
            }
        }
    }
}
