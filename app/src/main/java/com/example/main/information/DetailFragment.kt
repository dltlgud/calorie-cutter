package com.example.main.information

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.main.R
import com.example.main.repository.BookmarkRepository

class DetailFragment : Fragment(R.layout.fragment_explanation) {

    private lateinit var bookmarkRepository: BookmarkRepository
    private lateinit var ivBookmark: ImageView

    private val exercise: Exercise?
        get() = arguments?.getParcelable(ARG_EXERCISE)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookmarkRepository = BookmarkRepository(requireContext())

        view.findViewById<ImageButton>(R.id.btn_back)?.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        ivBookmark      = view.findViewById(R.id.ivBookmark)
        val ivImage     = view.findViewById<ImageView>(R.id.iv_ex_image)
        val tvName      = view.findViewById<TextView>(R.id.tv_ex_name)
        val tvCategory  = view.findViewById<TextView>(R.id.tv_ex_category)
        val tvEquipment = view.findViewById<TextView>(R.id.tv_equipment)
        val tvTarget    = view.findViewById<TextView>(R.id.tv_ex_target)
        val tvDesc      = view.findViewById<TextView>(R.id.tv_ex_desc)
        val tvMethod    = view.findViewById<TextView>(R.id.tv_ex_method)
        val tvCaution   = view.findViewById<TextView>(R.id.tv_ex_caution)
        val tvTip       = view.findViewById<TextView>(R.id.tv_ex_tip)
        val btnReview   = view.findViewById<Button>(R.id.btn_review)
        val btnYoutube  = view.findViewById<Button>(R.id.btn_youtube)

        exercise?.let { ex ->
            tvName.text      = ex.name
            tvCategory.text  = ex.category
            tvEquipment.text = ex.equipment
            tvTarget.text    = ex.target
            tvDesc.text      = ex.description
            tvMethod.text    = ex.method
            tvCaution.text   = ex.caution
            tvTip.text       = ex.tip

            val resId = resources.getIdentifier(ex.imageName, "drawable", requireContext().packageName)
            if (resId != 0) Glide.with(this).load(resId).into(ivImage)

            updateBookmarkIcon(ex.name)

            ivBookmark.setOnClickListener {
                bookmarkRepository.toggleBookmark(ex.name)
                updateBookmarkIcon(ex.name)
            }

            childFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, ExplanationFragment.newInstance(ex))
                .commit()

            btnReview.setOnClickListener {
                view.findViewById<FrameLayout>(R.id.fragment_container).visibility = View.VISIBLE
                childFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, ReviewFragment().apply {
                        arguments = Bundle().apply { putString("exerciseName", ex.name) }
                    })
                    .addToBackStack(null)
                    .commit()
            }

            btnYoutube.setOnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW,
                    Uri.parse("https://www.youtube.com/results?search_query=${ex.name}")))
            }
        }
    }

    private fun updateBookmarkIcon(name: String) {
        val isBookmarked = bookmarkRepository.isBookmarked(name)
        ivBookmark.setColorFilter(
            resources.getColor(if (isBookmarked) R.color.teal_700 else R.color.gray, null)
        )
        ivBookmark.setImageResource(
            if (isBookmarked) R.drawable.ic_bookmark_filled else R.drawable.ic_bookmark_outline
        )
    }

    companion object {
        private const val ARG_EXERCISE = "exercise"

        fun newInstance(exercise: Exercise): DetailFragment =
            DetailFragment().apply {
                arguments = Bundle().apply { putParcelable(ARG_EXERCISE, exercise) }
            }
    }
}
