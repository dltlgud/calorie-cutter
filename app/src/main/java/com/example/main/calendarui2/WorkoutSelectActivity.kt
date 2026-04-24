package com.example.main.calendarui2

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.main.R
import com.example.main.repository.RoutineRepository
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup

class WorkoutSelectActivity : AppCompatActivity() {

    private lateinit var adapter: WorkoutSelectAdapter
    private lateinit var routineRepo: RoutineRepository
    private var selectedCategory = "전체"
    private var searchQuery = ""
    private var myRoutineFilterNames: List<String>? = null

    private val masterList = listOf(
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

    private val categoryMap = mapOf(
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_select)

        routineRepo = RoutineRepository(this)

        val recyclerView = findViewById<RecyclerView>(R.id.recyclerViewSelect)
        val chipGroup = findViewById<ChipGroup>(R.id.chipGroup)
        val etSearch = findViewById<EditText>(R.id.etWorkoutSearch)
        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val btnSelectConfirm = findViewById<Button>(R.id.btnSelectConfirm)

        btnBack.setOnClickListener { finish() }

        adapter = WorkoutSelectAdapter(masterList)
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter

        val routines = routineRepo.getAll()
        val hasRoutines = routines.isNotEmpty()
        val categories = buildList {
            add("전체")
            if (hasRoutines) add("⭐ 내 루틴")
            addAll(listOf("하체", "가슴", "등", "어깨", "팔"))
        }
        categories.forEach { cat ->
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
            val checkedId = checkedIds.firstOrNull() ?: return@setOnCheckedStateChangeListener
            val category = group.findViewById<Chip>(checkedId)?.text?.toString() ?: "전체"
            if (category == "⭐ 내 루틴") {
                showRoutineGroupPicker(group, checkedId)
            } else {
                myRoutineFilterNames = null
                selectedCategory = category
                applyFilter()
            }
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { searchQuery = s.toString().trim(); applyFilter() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnSelectConfirm.setOnClickListener {
            val selected = adapter.getSelectedWorkout() ?: run {
                Toast.makeText(this, "운동을 선택해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val selectedDate = intent.getStringExtra("selectedDate")
            startActivity(Intent(this, WorkoutRecordActivity::class.java).apply {
                putExtra("selectedWorkout", selected)
                if (!selectedDate.isNullOrEmpty()) putExtra("selectedDate", selectedDate)
            })
            finish()
        }
    }

    private fun showRoutineGroupPicker(chipGroup: ChipGroup, myRoutineChipId: Int) {
        val routines = routineRepo.getAll()
        if (routines.isEmpty()) {
            Toast.makeText(this, "저장된 루틴 그룹이 없어요. 홈에서 먼저 만들어보세요!", Toast.LENGTH_SHORT).show()
            (chipGroup.getChildAt(0) as? Chip)?.isChecked = true
            return
        }

        val dialog = BottomSheetDialog(this)
        val sheetView = LayoutInflater.from(this).inflate(R.layout.bottom_sheet_routine, null)
        sheetView.findViewById<TextView>(R.id.tv_bs_title).text = "루틴 그룹 선택"
        dialog.setContentView(sheetView)

        val resetChip = { (chipGroup.getChildAt(0) as? Chip)?.isChecked = true }
        sheetView.findViewById<ImageButton>(R.id.btn_bs_close).setOnClickListener {
            resetChip()
            dialog.dismiss()
        }
        dialog.setOnCancelListener { resetChip() }

        val container = sheetView.findViewById<LinearLayout>(R.id.ll_bs_content)
        routines.forEach { routine ->
            val card = LayoutInflater.from(this).inflate(R.layout.item_bs_group, container, false)
            card.findViewById<TextView>(R.id.tv_bs_group_name).text = routine.name
            card.findViewById<TextView>(R.id.tv_bs_group_exercises).text =
                if (routine.exercises.isEmpty()) "운동 없음" else routine.exercises.joinToString(", ")
            card.setOnClickListener {
                myRoutineFilterNames = routine.exercises
                selectedCategory = "⭐ 내 루틴"
                applyFilter()
                dialog.dismiss()
            }
            container.addView(card)
        }

        dialog.show()
    }

    private fun applyFilter() {
        var filtered = masterList
        when {
            selectedCategory == "⭐ 내 루틴" -> {
                val names = myRoutineFilterNames ?: emptyList()
                filtered = masterList.filter { it in names }
            }
            selectedCategory != "전체" -> {
                val catSet = categoryMap[selectedCategory] ?: emptySet()
                filtered = filtered.filter { it in catSet }
            }
        }
        if (searchQuery.isNotBlank()) {
            filtered = filtered.filter { it.contains(searchQuery, ignoreCase = true) }
        }
        adapter.updateList(filtered)
    }
}
