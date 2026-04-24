package com.example.main.routine

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.main.R
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class ExercisePickerActivity : AppCompatActivity() {

    companion object {
        const val EXTRA_SELECTED = "selected_exercises"
        const val EXTRA_EXISTING = "existing_exercises"

        val MASTER_LIST = listOf(
            "레그 익스텐션", "런지", "레그 프레스", "스쿼트", "레그 컬",
            "힙 쓰러스트", "스티프 레그 데드리프트", "스플릿 스쿼트", "케이블 킥백", "카프 레이즈",
            "벤치프레스", "바벨 벤치프레스", "인클라인 벤치프레스", "덤벨 벤치프레스",
            "체스트 프레스 머신", "펙덱 플라이", "케이블 크로스오버", "딥스", "푸쉬업",
            "인클라인 덤벨 플라이", "디클라인 벤치프레스",
            "랫풀다운", "바벨로우", "데드리프트", "시티드 케이블 로우", "풀업",
            "덤벨 로우", "T바 로우", "슈러그", "백 익스텐션", "체스트 서포티드 로우",
            "오버헤드 프레스", "덤벨 숄더 프레스", "레터럴 레이즈", "프론트 레이즈",
            "리어 델트 레이즈", "업라이트 로우", "아놀드 프레스", "케이블 레터럴 레이즈",
            "머신 숄더 프레스", "페이스풀",
            "바벨 컬", "덤벨 컬", "해머 컬", "컨센트레이션 컬", "프리처 컬",
            "트라이셉스 푸시다운", "스컬크러셔", "덤벨 킥백",
            "오버헤드 트라이셉스 익스텐션", "케이블 로프 푸시다운"
        )

        val CATEGORY_MAP = mapOf(
            "하체" to setOf("레그 익스텐션", "런지", "레그 프레스", "스쿼트", "레그 컬",
                "힙 쓰러스트", "스티프 레그 데드리프트", "스플릿 스쿼트", "케이블 킥백", "카프 레이즈"),
            "가슴" to setOf("벤치프레스", "바벨 벤치프레스", "인클라인 벤치프레스", "덤벨 벤치프레스",
                "체스트 프레스 머신", "펙덱 플라이", "케이블 크로스오버", "딥스", "푸쉬업",
                "인클라인 덤벨 플라이", "디클라인 벤치프레스"),
            "등" to setOf("랫풀다운", "바벨로우", "데드리프트", "시티드 케이블 로우", "풀업",
                "덤벨 로우", "T바 로우", "슈러그", "백 익스텐션", "체스트 서포티드 로우"),
            "어깨" to setOf("오버헤드 프레스", "덤벨 숄더 프레스", "레터럴 레이즈", "프론트 레이즈",
                "리어 델트 레이즈", "업라이트 로우", "아놀드 프레스", "케이블 레터럴 레이즈",
                "머신 숄더 프레스", "페이스풀"),
            "팔" to setOf("바벨 컬", "덤벨 컬", "해머 컬", "컨센트레이션 컬", "프리처 컬",
                "트라이셉스 푸시다운", "스컬크러셔", "덤벨 킥백",
                "오버헤드 트라이셉스 익스텐션", "케이블 로프 푸시다운")
        )

        fun getCategoryOf(name: String): String =
            CATEGORY_MAP.entries.firstOrNull { name in it.value }?.key ?: ""
    }

    private lateinit var adapter: PickerAdapter
    private var selectedCategory = "전체"
    private var searchQuery = ""
    private val selected = mutableSetOf<String>()
    private lateinit var tvCount: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_exercise_picker)

        val existing = intent.getStringArrayListExtra(EXTRA_EXISTING) ?: arrayListOf()
        selected.addAll(existing)

        tvCount = findViewById(R.id.tv_selected_count)
        val recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroup)
        val etSearch = findViewById<EditText>(R.id.etSearch)

        adapter = PickerAdapter(MASTER_LIST, selected) { updateCount() }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        listOf("전체", "하체", "가슴", "등", "어깨", "팔").forEach { cat ->
            val chip = Chip(this).apply {
                text = cat
                isCheckable = true
                isChecked = cat == "전체"
                setChipBackgroundColorResource(R.color.surface)
                setCheckedIconVisible(false)
                chipStrokeWidth = 1.5f
                setChipStrokeColorResource(R.color.border)
            }
            chipGroup.addView(chip)
        }

        chipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            val id = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            selectedCategory = group.findViewById<Chip>(id)?.text?.toString() ?: "전체"
            applyFilter()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { searchQuery = s.toString().trim(); applyFilter() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btnConfirm).setOnClickListener { returnResult() }

        updateCount()
    }

    private fun applyFilter() {
        var filtered = MASTER_LIST
        if (selectedCategory != "전체") {
            val catSet = CATEGORY_MAP[selectedCategory] ?: emptySet()
            filtered = filtered.filter { it in catSet }
        }
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { it.contains(searchQuery, ignoreCase = true) }
        }
        adapter.updateList(filtered)
    }

    private fun updateCount() {
        tvCount.text = "${selected.size}개 선택"
    }

    private fun returnResult() {
        val result = Intent().apply {
            putStringArrayListExtra(EXTRA_SELECTED, ArrayList(selected))
        }
        setResult(Activity.RESULT_OK, result)
        finish()
    }

    inner class PickerAdapter(
        private var list: List<String>,
        private val selected: MutableSet<String>,
        private val onChanged: () -> Unit
    ) : RecyclerView.Adapter<PickerAdapter.VH>() {

        inner class VH(val view: View) : RecyclerView.ViewHolder(view) {
            val iv: android.widget.ImageView = view.findViewById(R.id.ivExerciseImage)
            val cb: CheckBox = view.findViewById(R.id.cbExercise)
            val tvName: TextView = view.findViewById(R.id.tvExerciseName)
            val tvCat: TextView = view.findViewById(R.id.tvCategory)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            VH(LayoutInflater.from(parent.context).inflate(R.layout.item_exercise_pick, parent, false))

        override fun getItemCount() = list.size

        override fun onBindViewHolder(holder: VH, position: Int) {
            val name = list[position]
            holder.tvName.text = name
            holder.tvCat.text = getCategoryOf(name)
            holder.cb.isChecked = name in selected
            holder.iv.setImageResource(getImageRes(name))
            holder.view.setOnClickListener {
                if (name in selected) selected.remove(name) else selected.add(name)
                holder.cb.isChecked = name in selected
                onChanged()
            }
        }

        fun updateList(newList: List<String>) {
            list = newList
            notifyDataSetChanged()
        }
    }

    private fun getImageRes(name: String): Int = when (name) {
        "레그 익스텐션"              -> R.drawable.ic_leg_extension
        "런지"                      -> R.drawable.ic_lunge
        "레그 프레스"                -> R.drawable.ic_leg_press
        "스쿼트"                    -> R.drawable.ic_squat
        "레그 컬"                   -> R.drawable.ic_leg_curl
        "힙 쓰러스트"               -> R.drawable.ic_hip_thrust
        "스티프 레그 데드리프트"     -> R.drawable.ic_stiff_leg_deadlift
        "스플릿 스쿼트"             -> R.drawable.ic_split_squat
        "케이블 킥백"               -> R.drawable.ic_cable_kickback
        "카프 레이즈"               -> R.drawable.ic_calf_raise
        "벤치프레스", "바벨 벤치프레스" -> R.drawable.ic_barbell_bench_press
        "인클라인 벤치프레스"        -> R.drawable.ic_incline_bench_press
        "덤벨 벤치프레스"           -> R.drawable.ic_dumbbell_bench_press
        "체스트 프레스 머신"         -> R.drawable.ic_chest_press_machine
        "펙덱 플라이"               -> R.drawable.ic_pec_deck_fly
        "케이블 크로스오버"          -> R.drawable.ic_cable_crossover
        "딥스"                      -> R.drawable.ic_dips
        "푸쉬업"                    -> R.drawable.ic_push_up
        "인클라인 덤벨 플라이"       -> R.drawable.ic_incline_dumbbell_fly
        "디클라인 벤치프레스"        -> R.drawable.ic_decline_bench_press
        "랫풀다운"                  -> R.drawable.ic_lat_pulldown
        "바벨로우"                  -> R.drawable.ic_barbell_row
        "데드리프트"                -> R.drawable.ic_deadlift
        "시티드 케이블 로우"         -> R.drawable.ic_seated_cable_row
        "풀업"                      -> R.drawable.ic_pull_up
        "덤벨 로우"                 -> R.drawable.ic_dumbbell_row
        "T바 로우"                  -> R.drawable.ic_t_bar_row
        "슈러그"                    -> R.drawable.ic_shrug
        "백 익스텐션"               -> R.drawable.ic_back_extension
        "체스트 서포티드 로우"       -> R.drawable.ic_chest_supported_row
        "오버헤드 프레스"            -> R.drawable.ic_overhead_press
        "덤벨 숄더 프레스"          -> R.drawable.ic_dumbbell_shoulder_press
        "레터럴 레이즈"             -> R.drawable.ic_lateral_raise
        "프론트 레이즈"             -> R.drawable.ic_front_raise
        "리어 델트 레이즈"          -> R.drawable.ic_rear_delt_raise
        "업라이트 로우"             -> R.drawable.ic_upright_row
        "아놀드 프레스"             -> R.drawable.ic_arnold_press
        "케이블 레터럴 레이즈"      -> R.drawable.ic_cable_lateral_raise
        "머신 숄더 프레스"          -> R.drawable.ic_machine_shoulder_press
        "페이스풀"                  -> R.drawable.ic_face_pull
        "바벨 컬"                   -> R.drawable.ic_barbell_curl
        "덤벨 컬"                   -> R.drawable.ic_dumbbell_curl
        "해머 컬"                   -> R.drawable.ic_hammer_curl
        "컨센트레이션 컬"           -> R.drawable.ic_concentration_curl
        "프리처 컬"                 -> R.drawable.ic_preacher_curl
        "트라이셉스 푸시다운"       -> R.drawable.ic_triceps_pushdown
        "스컬크러셔"                -> R.drawable.ic_skull_crusher
        "덤벨 킥백"                 -> R.drawable.ic_dumbbell_kickback
        "오버헤드 트라이셉스 익스텐션" -> R.drawable.ic_overhead_triceps_extension
        "케이블 로프 푸시다운"      -> R.drawable.ic_cable_rope_pushdown
        else                        -> R.drawable.ic_exercise_sample
    }
}
