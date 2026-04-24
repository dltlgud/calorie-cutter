package com.example.main.calendarui2

import android.content.Intent
import android.graphics.Paint
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomsheet.BottomSheetDialog
import androidx.core.content.ContextCompat
import com.example.main.R
import com.example.main.noticeboard.CommunityMainActivity
import com.example.main.noticeboard.WritePostActivity
import com.example.main.repository.RoutineRepository
import com.example.main.repository.WorkoutRepository
import java.text.SimpleDateFormat
import java.util.*

class WorkoutRecordActivity : AppCompatActivity() {

    private lateinit var tvWorkoutSelected: TextView
    private lateinit var tvTimerStatus: TextView
    private lateinit var tvWorkoutTime: TextView
    private lateinit var tvTotalReps: TextView
    private lateinit var tvTotalVolume: TextView
    private lateinit var tvSetCount: TextView
    private lateinit var tvSummaryTime: TextView
    private lateinit var tvSummaryCount: TextView
    private lateinit var tvSummaryVolume: TextView
    private lateinit var tvSummaryExerciseNames: TextView
    private lateinit var setContainer: LinearLayout
    private lateinit var summaryLayout: View
    private lateinit var scrollView: ScrollView
    private lateinit var btnStartWorkout: Button
    private lateinit var btnFinishWorkout: Button
    private lateinit var timerProgressBar: ProgressBar
    private lateinit var inflater: LayoutInflater
    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var routineRepository: RoutineRepository

    private val timerHandler = Handler(Looper.getMainLooper())
    private var startTime = 0L
    private var setCount = 0
    private var selectedWorkoutName = "운동 미선택"
    private var recordDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_record)

        workoutRepository = WorkoutRepository(this)
        routineRepository = RoutineRepository(this)

        selectedWorkoutName = intent?.getStringExtra("selectedWorkout") ?: "운동 미선택"
        recordDate = intent?.getStringExtra("selectedDate") ?: ""
        if (recordDate.isEmpty()) {
            recordDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        }

        Log.d("WorkoutRecordActivity", "날짜=$recordDate, 운동=$selectedWorkoutName")

        scrollView             = findViewById(R.id.scrollView)
        tvWorkoutSelected      = findViewById(R.id.tvWorkoutSelected)
        tvTimerStatus          = findViewById(R.id.tvTimerStatus)
        tvWorkoutTime          = findViewById(R.id.tvWorkoutTime)
        tvTotalReps            = findViewById(R.id.tvTotalReps)
        tvTotalVolume          = findViewById(R.id.tvTotalVolume)
        tvSetCount             = findViewById(R.id.tvSetCount)
        tvSummaryTime          = findViewById(R.id.tvSummaryTime)
        tvSummaryCount         = findViewById(R.id.tvSummaryCount)
        tvSummaryVolume        = findViewById(R.id.tvSummaryVolume)
        tvSummaryExerciseNames = findViewById(R.id.tvSummaryExerciseNames)
        setContainer           = findViewById(R.id.setContainer)
        summaryLayout          = findViewById(R.id.summaryLayout)
        btnStartWorkout        = findViewById(R.id.btnStartWorkout)
        btnFinishWorkout       = findViewById(R.id.btnFinishWorkout)
        timerProgressBar       = findViewById(R.id.timerProgressBar)
        inflater               = LayoutInflater.from(this)

        tvWorkoutSelected.text = selectedWorkoutName

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            startActivity(
                Intent(this, CommunityMainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
            finish()
        }

        addSetRow()

        findViewById<View>(R.id.btnAddSet).setOnClickListener { addSetRow() }
        findViewById<View>(R.id.btnRemoveSet).setOnClickListener {
            if (setCount > 1) {
                setContainer.removeViewAt(setCount - 1)
                setCount--
                updateStats()
            }
        }

        findViewById<View>(R.id.btnAddWorkout).setOnClickListener {
            startActivity(Intent(this, WorkoutSelectActivity::class.java).apply {
                putExtra("selectedDate", recordDate)
            })
        }

        findViewById<View>(R.id.btnLoadWorkout).setOnClickListener {
            showLoadRoutineDialog()
        }

        btnStartWorkout.setOnClickListener {
            startTime = System.currentTimeMillis()
            btnStartWorkout.visibility = View.GONE
            btnFinishWorkout.visibility = View.VISIBLE
            tvTimerStatus.text = "● 진행 중"
            tvTimerStatus.setTextColor(ContextCompat.getColor(this, R.color.primary))
            startWorkoutTimer()
        }

        btnFinishWorkout.setOnClickListener {
            if (startTime == 0L) {
                Toast.makeText(this, "먼저 '운동 시작'을 눌러주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            timerHandler.removeCallbacksAndMessages(null)
            timerProgressBar.progress = 0
            tvTimerStatus.text = "완료"
            tvTimerStatus.setTextColor(ContextCompat.getColor(this, R.color.on_surface_secondary))

            val minutes = ((System.currentTimeMillis() - startTime) / 1000 / 60).toInt()
            val totalSets = setContainer.childCount
            var totalVolume = 0
            val setsData = (0 until totalSets).map { i ->
                val row = setContainer.getChildAt(i)
                val kg = row.findViewById<EditText>(R.id.etWeight).text.toString().toIntOrNull() ?: 0
                val reps = row.findViewById<EditText>(R.id.etReps).text.toString().toIntOrNull() ?: 0
                totalVolume += kg * reps
                Pair(kg, reps)
            }

            val memo = findViewById<EditText>(R.id.etMemo).text.toString().trim()
            summaryLayout.visibility = View.VISIBLE
            tvSummaryTime.text = "운동 시간: ${minutes}분"
            tvSummaryCount.text = "운동 개수: ${totalSets}세트"
            tvSummaryVolume.text = "총 볼륨: ${totalVolume}kg"
            tvSummaryExerciseNames.text = "운동: $selectedWorkoutName"
            scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }

            val entry = buildString {
                append("$selectedWorkoutName / ${minutes}분 / ${totalSets}세트 / ${totalVolume}kg")
                if (memo.isNotEmpty()) append(" / 메모: $memo")
            }
            workoutRepository.addEntry(recordDate, entry, setsData)
            Toast.makeText(this, "운동이 완료되었습니다!", Toast.LENGTH_SHORT).show()
            btnFinishWorkout.visibility = View.GONE
        }

        findViewById<View>(R.id.btnShareSummary).setOnClickListener {
            val date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val summaryText = "${tvSummaryTime.text}\n${tvSummaryCount.text}\n" +
                    "${tvSummaryVolume.text}\n${tvSummaryExerciseNames.text}"
            startActivity(Intent(this, WritePostActivity::class.java).apply {
                putExtra("title", "$date 운동 요약")
                putExtra("content", summaryText)
                putExtra("category", "schedule")
            })
        }

        findViewById<View>(R.id.btnGoHome).setOnClickListener {
            startActivity(Intent(this, CommunityMainActivity::class.java))
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timerHandler.removeCallbacksAndMessages(null)
    }

    private fun addSetRow(kg: Int = 0, reps: Int = 0) {
        val row = inflater.inflate(R.layout.item_set_row, setContainer, false)
        val tvSetLabel = row.findViewById<TextView>(R.id.tvSetLabel)
        val etWeight = row.findViewById<EditText>(R.id.etWeight)
        val etReps = row.findViewById<EditText>(R.id.etReps)
        val chkDone = row.findViewById<CheckBox>(R.id.chkDone)
        val btnWeightMinus = row.findViewById<TextView>(R.id.btnWeightMinus)
        val btnWeightPlus = row.findViewById<TextView>(R.id.btnWeightPlus)
        val btnRepsMinus = row.findViewById<TextView>(R.id.btnRepsMinus)
        val btnRepsPlus = row.findViewById<TextView>(R.id.btnRepsPlus)

        tvSetLabel.text = "${setCount + 1}세트"
        if (kg > 0) etWeight.setText(kg.toString())
        if (reps > 0) etReps.setText(reps.toString())

        btnWeightMinus.setOnClickListener {
            val cur = etWeight.text.toString().toFloatOrNull() ?: 0f
            if (cur >= 2.5f) etWeight.setText(formatWeight(cur - 2.5f))
        }
        btnWeightPlus.setOnClickListener {
            val cur = etWeight.text.toString().toFloatOrNull() ?: 0f
            etWeight.setText(formatWeight(cur + 2.5f))
        }
        btnRepsMinus.setOnClickListener {
            val cur = etReps.text.toString().toIntOrNull() ?: 0
            if (cur > 0) etReps.setText((cur - 1).toString())
        }
        btnRepsPlus.setOnClickListener {
            val cur = etReps.text.toString().toIntOrNull() ?: 0
            etReps.setText((cur + 1).toString())
        }

        val watcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { updateStats() }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        }
        etWeight.addTextChangedListener(watcher)
        etReps.addTextChangedListener(watcher)

        chkDone.setOnCheckedChangeListener { _, isChecked ->
            val alpha = if (isChecked) 0.4f else 1f
            etWeight.alpha = alpha
            etReps.alpha = alpha
            tvSetLabel.alpha = alpha
            if (isChecked) {
                etWeight.paintFlags = etWeight.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                etReps.paintFlags = etReps.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                etWeight.paintFlags = etWeight.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                etReps.paintFlags = etReps.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }

        setContainer.addView(row)
        setCount++
        updateStats()
    }

    private fun formatWeight(value: Float): String {
        return if (value == value.toInt().toFloat()) value.toInt().toString() else value.toString()
    }

    private fun updateStats() {
        var totalReps = 0
        var totalVolume = 0
        for (i in 0 until setContainer.childCount) {
            val row = setContainer.getChildAt(i)
            val reps = row.findViewById<EditText>(R.id.etReps).text.toString().toIntOrNull() ?: 0
            val kg = row.findViewById<EditText>(R.id.etWeight).text.toString().toIntOrNull() ?: 0
            totalReps += reps
            totalVolume += kg * reps
        }
        tvTotalReps.text = "${totalReps}회"
        tvTotalVolume.text = "${totalVolume}kg"
        tvSetCount.text = setContainer.childCount.toString()
    }

    private fun showLoadRoutineDialog() {
        val routines = routineRepository.getAll()
        if (routines.isEmpty()) {
            Toast.makeText(this, "저장된 루틴 그룹이 없어요. 홈에서 먼저 만들어보세요!", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = BottomSheetDialog(this)
        val sheetView = inflater.inflate(R.layout.bottom_sheet_routine, null)
        dialog.setContentView(sheetView)

        sheetView.findViewById<ImageButton>(R.id.btn_bs_close).setOnClickListener { dialog.dismiss() }

        val container = sheetView.findViewById<LinearLayout>(R.id.ll_bs_content)
        routines.forEach { routine ->
            val card = inflater.inflate(R.layout.item_bs_group, container, false)
            card.findViewById<TextView>(R.id.tv_bs_group_name).text = routine.name
            card.findViewById<TextView>(R.id.tv_bs_group_exercises).text =
                if (routine.exercises.isEmpty()) "운동 없음" else routine.exercises.joinToString(", ")
            card.setOnClickListener {
                if (routine.exercises.isEmpty()) {
                    Toast.makeText(this, "이 그룹에 운동이 없습니다", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                AlertDialog.Builder(this)
                    .setTitle(routine.name)
                    .setItems(routine.exercises.toTypedArray()) { _, exIndex ->
                        loadExerciseFromRoutine(routine.exercises[exIndex])
                        dialog.dismiss()
                    }
                    .setNegativeButton("취소", null)
                    .show()
            }
            container.addView(card)
        }

        dialog.show()
    }

    private fun loadExerciseFromRoutine(exerciseName: String) {
        selectedWorkoutName = exerciseName
        tvWorkoutSelected.text = exerciseName
        setContainer.removeAllViews()
        setCount = 0
        addSetRow()
        Toast.makeText(this, "\"$exerciseName\" 운동을 불러왔습니다", Toast.LENGTH_SHORT).show()
    }

    private fun startWorkoutTimer() {
        timerHandler.postDelayed(object : Runnable {
            override fun run() {
                val elapsed = System.currentTimeMillis() - startTime
                val sec = (elapsed / 1000).toInt()
                val h = sec / 3600
                val m = (sec % 3600) / 60
                val s = sec % 60
                tvWorkoutTime.text = if (h > 0)
                    String.format("%d:%02d:%02d", h, m, s)
                else
                    String.format("%02d:%02d", m, s)
                timerProgressBar.setProgress(minOf(sec, 3600), true)
                timerHandler.postDelayed(this, 1000)
            }
        }, 1000)
    }
}
