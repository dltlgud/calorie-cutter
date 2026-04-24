package com.example.main.noticeboard

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.main.R
import com.example.main.information.DetailFragment
import com.example.main.information.Exercise
import com.example.main.information.ExerciseAdapter
import com.example.main.information.ExerciseRepository
import com.example.main.repository.BookmarkRepository
import com.google.android.material.chip.Chip

class LibraryFragment : Fragment(R.layout.fragment_library) {

    private lateinit var allExercises: List<Exercise>
    private lateinit var adapter: ExerciseAdapter
    private lateinit var bookmarkRepository: BookmarkRepository

    private var currentCategory = "전체"
    private var bookmarksOnly = false
    private var currentQuery = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bookmarkRepository = BookmarkRepository(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        allExercises = ExerciseRepository.loadAll(requireContext())

        adapter = ExerciseAdapter(
            onClick = { ex ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.main_frame, DetailFragment.newInstance(ex))
                    .addToBackStack(null)
                    .commit()
            },
            onBookmarkClick = { ex ->
                bookmarkRepository.toggleBookmark(ex.name)
                adapter.notifyDataSetChanged()
                filterAndSubmit()
            },
            isBookmarked = { ex -> bookmarkRepository.isBookmarked(ex.name) }
        )

        view.findViewById<RecyclerView>(R.id.rv_exercises).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@LibraryFragment.adapter
        }

        // 검색 (EditText + TextWatcher)
        view.findViewById<EditText>(R.id.et_search).addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                currentQuery = s.toString()
                filterAndSubmit()
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 카테고리 칩 설정
        val llCat = view.findViewById<LinearLayout>(R.id.ll_categories)
        val buttons = (0 until llCat.childCount).map { llCat.getChildAt(it) as Button }
        val bookmarkBtn = buttons.first { it.id == R.id.btn_bookmark }
        val categoryBtns = buttons.filter { it.id != R.id.btn_bookmark }

        // 초기 상태: "전체" 선택
        setChipSelected(buttons.first { it.id == R.id.btn_all }, true)
        setChipSelected(bookmarkBtn, false)

        buttons.forEach { btn ->
            btn.setOnClickListener {
                if (btn.id == R.id.btn_bookmark) {
                    bookmarksOnly = !bookmarksOnly
                    setChipSelected(bookmarkBtn, bookmarksOnly)
                    if (bookmarksOnly) {
                        currentCategory = "전체"
                        categoryBtns.forEach { setChipSelected(it, false) }
                        setChipSelected(categoryBtns.first { it.id == R.id.btn_all }, false)
                    }
                } else {
                    bookmarksOnly = false
                    setChipSelected(bookmarkBtn, false)
                    categoryBtns.forEach { setChipSelected(it, false) }
                    setChipSelected(btn, true)
                    currentCategory = btn.text.toString()
                }
                filterAndSubmit()
            }
        }

        filterAndSubmit()
    }

    private fun setChipSelected(btn: Button, selected: Boolean) {
        val chip = btn as? Chip
        if (chip != null) {
            val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
            val borderColor = ContextCompat.getColor(requireContext(), R.color.border)
            val surfaceColor = ContextCompat.getColor(requireContext(), R.color.surface)
            val density = resources.displayMetrics.density
            if (selected) {
                chip.chipBackgroundColor = ColorStateList.valueOf(primaryColor)
                chip.setTextColor(Color.WHITE)
                chip.chipStrokeWidth = 0f
            } else {
                chip.chipBackgroundColor = ColorStateList.valueOf(surfaceColor)
                chip.setTextColor(ContextCompat.getColor(requireContext(), R.color.on_surface_secondary))
                chip.chipStrokeColor = ColorStateList.valueOf(borderColor)
                chip.chipStrokeWidth = 1.5f * density
            }
        } else {
            if (selected) {
                btn.setBackgroundResource(R.drawable.bg_chip_selected)
                btn.setTextColor(Color.WHITE)
            } else {
                btn.setBackgroundResource(R.drawable.bg_chip_unselected)
                btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary))
            }
        }
    }

    private fun filterAndSubmit() {
        var list = allExercises
        if (currentCategory != "전체") list = list.filter { it.category == currentCategory }
        if (currentQuery.isNotBlank()) list = list.filter {
            it.name.lowercase().contains(currentQuery.trim().lowercase())
        }
        if (bookmarksOnly) list = list.filter { bookmarkRepository.isBookmarked(it.name) }
        adapter.submitList(list)
    }
}
