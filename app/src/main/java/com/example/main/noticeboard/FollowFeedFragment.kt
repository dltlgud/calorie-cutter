package com.example.main.noticeboard

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.main.R
import com.example.main.repository.FollowRepository
import com.example.main.repository.PostRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FollowFeedFragment : Fragment() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private val followRepository = FollowRepository()
    private val postRepository = PostRepository()

    private lateinit var etSearch: EditText
    private lateinit var tvSearchClear: TextView
    private lateinit var rvContent: RecyclerView
    private lateinit var llEmpty: LinearLayout
    private lateinit var tvEmpty: TextView
    private lateinit var tvEmptySub: TextView
    private lateinit var tvSectionLabel: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_follow_feed, container, false)

        etSearch = view.findViewById(R.id.et_follow_search)
        tvSearchClear = view.findViewById(R.id.tv_search_clear)
        rvContent = view.findViewById(R.id.rv_follow_content)
        llEmpty = view.findViewById(R.id.ll_follow_empty)
        tvEmpty = view.findViewById(R.id.tv_follow_empty)
        tvEmptySub = view.findViewById(R.id.tv_follow_empty_sub)
        tvSectionLabel = view.findViewById(R.id.tv_section_label)

        rvContent.layoutManager = LinearLayoutManager(requireContext())

        showFeed()

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val q = s.toString().trim()
                tvSearchClear.visibility = if (q.isNotEmpty()) View.VISIBLE else View.GONE
                if (q.isEmpty()) showFeed() else searchUsers(q)
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        tvSearchClear.setOnClickListener {
            etSearch.setText("")
            showFeed()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        if (etSearch.text.isNullOrEmpty()) showFeed()
    }

    private fun showFeed() {
        val myUid = auth.currentUser?.uid ?: return
        tvSectionLabel.text = "팔로우 피드"
        followRepository.getFollowingList(myUid) { followingUids ->
            if (followingUids.isEmpty()) {
                activity?.runOnUiThread { showEmpty("팔로우한 사용자가 없습니다", "위 검색창에서 사람을 찾아보세요") }
                return@getFollowingList
            }
            postRepository.getPostsByAuthors(followingUids) { posts ->
                activity?.runOnUiThread {
                    if (posts.isEmpty()) {
                        showEmpty("팔로우한 사용자의 게시글이 없습니다", "팔로우한 사용자가 글을 작성하면 여기에 표시됩니다")
                    } else {
                        showContent(posts.toMutableList())
                    }
                }
            }
        }
    }

    private fun searchUsers(query: String) {
        val myUid = auth.currentUser?.uid ?: return
        tvSectionLabel.text = "사용자 검색 결과"
        db.collection("users").get().addOnSuccessListener { result ->
            val filtered = result.documents.filter { doc ->
                val uid = doc.id
                val name = doc.getString("username") ?: ""
                uid != myUid && name.contains(query, ignoreCase = true)
            }
            activity?.runOnUiThread {
                if (filtered.isEmpty()) {
                    showEmpty("'$query'에 해당하는 사용자가 없습니다", "다른 이름으로 검색해보세요")
                } else {
                    rvContent.visibility = View.VISIBLE
                    llEmpty.visibility = View.GONE
                    rvContent.adapter = SocialUserAdapter(filtered, myUid, followRepository) {
                        etSearch.setText("")
                        showFeed()
                    }
                }
            }
        }
    }

    private fun showContent(posts: MutableList<com.example.main.model.Post>) {
        rvContent.visibility = View.VISIBLE
        llEmpty.visibility = View.GONE
        rvContent.adapter = PostAdapter(posts, this)
    }

    private fun showEmpty(main: String, sub: String) {
        rvContent.visibility = View.GONE
        llEmpty.visibility = View.VISIBLE
        tvEmpty.text = main
        tvEmptySub.text = sub
    }
}
