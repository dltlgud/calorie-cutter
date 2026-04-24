package com.example.main.information

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.main.R
import com.example.main.repository.ReviewRepository

class ReviewFragment : Fragment(R.layout.fragment_review) {

    private val args: ReviewFragmentArgs by navArgs()
    private val exerciseName: String get() = args.exerciseName

    private val repository = ReviewRepository()
    private val reviewList = mutableListOf<Review>()
    private lateinit var adapter: ReviewAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val editText  = view.findViewById<EditText>(R.id.editTextComment)
        val btnSubmit = view.findViewById<Button>(R.id.btnSubmitComment)
        val recycler  = view.findViewById<RecyclerView>(R.id.reviewRecyclerView)

        adapter = ReviewAdapter(reviewList)
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        btnSubmit.setOnClickListener {
            val comment = editText.text.toString().trim()
            if (comment.isNotEmpty()) {
                val review = Review(
                    userName     = "익명",
                    exerciseName = exerciseName,
                    comment      = comment,
                    timestamp    = System.currentTimeMillis()
                )
                repository.addReview(review,
                    onSuccess = {
                        reviewList.add(0, review)
                        adapter.notifyItemInserted(0)
                        recycler.scrollToPosition(0)
                        editText.text.clear()
                    },
                    onFailure = { e ->
                        Toast.makeText(requireContext(), "댓글 작성 실패: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }

        fetchReviews()
    }

    private fun fetchReviews() {
        repository.getReviews(exerciseName,
            onSuccess = { reviews ->
                reviewList.clear()
                reviewList.addAll(reviews)
                adapter.notifyDataSetChanged()
            },
            onFailure = { e ->
                Toast.makeText(requireContext(), "리뷰 불러오기 실패: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }
}
