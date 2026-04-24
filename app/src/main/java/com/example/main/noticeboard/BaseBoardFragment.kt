package com.example.main.noticeboard

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.main.R
import com.example.main.model.Post
import com.example.main.repository.PostRepository

abstract class BaseBoardFragment : Fragment() {

    abstract val category: String
    abstract val layoutResId: Int

    private lateinit var recyclerView: RecyclerView
    private lateinit var postAdapter: PostAdapter
    private lateinit var editSearch: EditText
    private lateinit var btnToggleSearch: ImageButton
    private lateinit var btnBackSearch: ImageButton
    private lateinit var searchContainer: LinearLayout

    private val postList = mutableListOf<Post>()
    private val repository = PostRepository()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(layoutResId, container, false)

        recyclerView = view.findViewById(R.id.recyclerView)
        editSearch = view.findViewById(R.id.editSearch)
        btnToggleSearch = view.findViewById(R.id.btnToggleSearch)
        btnBackSearch = view.findViewById(R.id.btnBackSearch)
        searchContainer = view.findViewById(R.id.searchContainer)

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        postAdapter = PostAdapter(postList.toMutableList(), this)
        recyclerView.adapter = postAdapter

        loadPosts()

        btnToggleSearch.setOnClickListener { showSearchInput() }
        btnBackSearch.setOnClickListener { hideSearchInput() }
        searchContainer.setOnClickListener {
            if (editSearch.visibility == View.GONE) showSearchInput()
        }

        editSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { filterPosts(s.toString()) }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        return view
    }

    override fun onResume() {
        super.onResume()
        loadPosts()
    }

    fun loadPosts() {
        repository.getPosts(
            category = category,
            onSuccess = { posts ->
                postList.clear()
                postList.addAll(posts)
                postAdapter.updateList(postList)
            },
            onFailure = { e ->
                Log.e("BoardFragment", "게시글 불러오기 실패", e)
            }
        )
    }

    private fun filterPosts(keyword: String) {
        val filtered = if (keyword.isBlank()) postList
        else postList.filter {
            it.title.contains(keyword, ignoreCase = true) ||
                    it.content.contains(keyword, ignoreCase = true)
        }
        postAdapter.updateList(filtered)
    }

    private fun showSearchInput() {
        btnToggleSearch.visibility = View.GONE
        btnBackSearch.visibility = View.VISIBLE
        editSearch.visibility = View.VISIBLE
        editSearch.requestFocus()
    }

    private fun hideSearchInput() {
        editSearch.setText("")
        editSearch.visibility = View.GONE
        btnBackSearch.visibility = View.GONE
        btnToggleSearch.visibility = View.VISIBLE
        postAdapter.updateList(postList)
    }
}
