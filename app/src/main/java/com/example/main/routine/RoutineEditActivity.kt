package com.example.main.routine

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.main.R
import com.example.main.repository.RoutineRepository

class RoutineEditActivity : AppCompatActivity() {

    private lateinit var repo: RoutineRepository
    private lateinit var llExercises: LinearLayout
    private lateinit var etRoutineName: EditText
    private lateinit var tvTitle: TextView
    private lateinit var tvExerciseCount: TextView
    private val exercises = mutableListOf<String>()
    private var editingRoutineId: String? = null

    private val pickerLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val picked = result.data?.getStringArrayListExtra(ExercisePickerActivity.EXTRA_SELECTED)
                ?: return@registerForActivityResult
            val pickedSet = picked.toSet()
            // 피커에서 해제된 운동 제거
            exercises.filter { it !in pickedSet }.toList().forEach { removeExercise(it) }
            // 새로 추가된 운동 삽입 (순서 유지)
            picked.forEach { name ->
                if (name !in exercises) {
                    exercises.add(name)
                    addExerciseRow(name)
                }
            }
            updateCount()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routine_edit)

        repo = RoutineRepository(this)
        llExercises = findViewById(R.id.ll_exercises)
        etRoutineName = findViewById(R.id.et_routine_name)
        tvTitle = findViewById(R.id.tv_title)
        tvExerciseCount = findViewById(R.id.tv_exercise_count)

        editingRoutineId = intent.getStringExtra("routine_id")

        if (editingRoutineId != null) {
            tvTitle.text = "그룹 수정"
            val routine = repo.getById(editingRoutineId!!)
            if (routine != null) {
                etRoutineName.setText(routine.name)
                routine.exercises.forEach { name ->
                    exercises.add(name)
                    addExerciseRow(name)
                }
            }
        }

        updateCount()

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_add_exercise).setOnClickListener { openPicker() }
        findViewById<View>(R.id.btn_save).setOnClickListener { saveRoutine() }
    }

    private fun openPicker() {
        val intent = Intent(this, ExercisePickerActivity::class.java).apply {
            putStringArrayListExtra(ExercisePickerActivity.EXTRA_EXISTING, ArrayList(exercises))
        }
        pickerLauncher.launch(intent)
    }

    private fun addExerciseRow(name: String) {
        val row = LayoutInflater.from(this).inflate(R.layout.item_exercise_row, llExercises, false)
        row.tag = name
        row.findViewById<TextView>(R.id.tv_exercise_name).text = name
        row.findViewById<TextView>(R.id.tv_exercise_category).text =
            ExercisePickerActivity.getCategoryOf(name)
        row.findViewById<View>(R.id.btn_remove).setOnClickListener {
            removeExercise(name)
        }
        llExercises.addView(row)
    }

    private fun removeExercise(name: String) {
        exercises.remove(name)
        val row = (0 until llExercises.childCount)
            .map { llExercises.getChildAt(it) }
            .firstOrNull { it.tag == name }
        row?.let { llExercises.removeView(it) }
        updateCount()
    }

    private fun updateCount() {
        tvExerciseCount.text = "${exercises.size}개"
    }

    private fun saveRoutine() {
        val name = etRoutineName.text.toString().trim()
        if (name.isEmpty()) {
            Toast.makeText(this, "그룹 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
            return
        }
        if (exercises.isEmpty()) {
            Toast.makeText(this, "운동을 하나 이상 추가해주세요", Toast.LENGTH_SHORT).show()
            return
        }
        val id = editingRoutineId
        if (id != null) {
            repo.update(id, name, exercises)
            Toast.makeText(this, "그룹이 수정되었습니다", Toast.LENGTH_SHORT).show()
        } else {
            repo.save(name, exercises)
            Toast.makeText(this, "그룹이 저장되었습니다", Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}
