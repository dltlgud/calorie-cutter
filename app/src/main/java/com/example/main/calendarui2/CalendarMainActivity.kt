package com.example.main.calendarui2

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.main.R
import java.text.SimpleDateFormat
import java.util.*

class CalendarMainActivity : AppCompatActivity() {

    private lateinit var tvWorkoutSelected: TextView
    private lateinit var tvTotalReps: TextView
    private lateinit var tvTotalVolume: TextView
    private lateinit var tvWorkoutTime: TextView
    private lateinit var tvTimerStatus: TextView
    private lateinit var tvSummaryTime: TextView
    private lateinit var tvSummaryCount: TextView
    private lateinit var tvSummaryVolume: TextView
    private lateinit var setContainer: LinearLayout
    private lateinit var summaryLayout: View
    private lateinit var scrollView: ScrollView
    private lateinit var btnStartWorkout: Button
    private lateinit var btnFinishWorkout: Button
    private lateinit var inflater: LayoutInflater

    private val timerHandler = Handler(Looper.getMainLooper())
    private var startTime = 0L
    private var setCount = 0
    private var selectedDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_workout_record)

        scrollView          = findViewById(R.id.scrollView)
        tvWorkoutSelected   = findViewById(R.id.tvWorkoutSelected)
        tvTotalReps         = findViewById(R.id.tvTotalReps)
        tvTotalVolume       = findViewById(R.id.tvTotalVolume)
        tvWorkoutTime       = findViewById(R.id.tvWorkoutTime)
        tvTimerStatus       = findViewById(R.id.tvTimerStatus)
        tvSummaryTime       = findViewById(R.id.tvSummaryTime)
        tvSummaryCount      = findViewById(R.id.tvSummaryCount)
        tvSummaryVolume     = findViewById(R.id.tvSummaryVolume)
        setContainer        = findViewById(R.id.setContainer)
        summaryLayout       = findViewById(R.id.summaryLayout)
        btnStartWorkout     = findViewById(R.id.btnStartWorkout)
        btnFinishWorkout    = findViewById(R.id.btnFinishWorkout)
        inflater            = LayoutInflater.from(this)

        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        intent?.getStringExtra("selectedWorkout")?.let { tvWorkoutSelected.text = it }

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
            if (selectedDate.isEmpty()) selectedDate = sdf.format(Date())
            startActivity(Intent(this, WorkoutSelectActivity::class.java).apply {
                putExtra("selectedDate", selectedDate)
            })
        }

        btnStartWorkout.setOnClickListener {
            startTime = System.currentTimeMillis()
            btnStartWorkout.visibility = View.GONE
            btnFinishWorkout.visibility = View.VISIBLE
            tvTimerStatus.text = "● 진행 중"
            startWorkoutTimer()
        }

        btnFinishWorkout.setOnClickListener {
            if (startTime == 0L) {
                Toast.makeText(this, "먼저 '운동 시작'을 눌러주세요!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            timerHandler.removeCallbacksAndMessages(null)
            tvTimerStatus.text = "완료"

            val minutes = ((System.currentTimeMillis() - startTime) / 1000 / 60).toInt()
            val totalSets = setContainer.childCount
            var totalVolume = 0
            for (i in 0 until totalSets) {
                val row = setContainer.getChildAt(i)
                val kg = row.findViewById<EditText>(R.id.etWeight).text.toString().toIntOrNull() ?: 0
                val reps = row.findViewById<EditText>(R.id.etReps).text.toString().toIntOrNull() ?: 0
                totalVolume += kg * reps
            }

            summaryLayout.visibility = View.VISIBLE
            tvSummaryTime.text = "운동 시간: ${minutes}분"
            tvSummaryCount.text = "운동 개수: ${totalSets}세트"
            tvSummaryVolume.text = "총 볼륨: ${totalVolume}kg"
            scrollView.post { scrollView.fullScroll(View.FOCUS_DOWN) }
            Toast.makeText(this, "운동이 완료되었습니다!", Toast.LENGTH_SHORT).show()
            btnFinishWorkout.visibility = View.GONE
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        timerHandler.removeCallbacksAndMessages(null)
    }

    private fun addSetRow() {
        val row = inflater.inflate(R.layout.item_set_row, setContainer, false)
        val tvSetLabel = row.findViewById<TextView>(R.id.tvSetLabel)
        val etWeight = row.findViewById<EditText>(R.id.etWeight)
        val etReps = row.findViewById<EditText>(R.id.etReps)
        val btnWeightMinus = row.findViewById<TextView>(R.id.btnWeightMinus)
        val btnWeightPlus = row.findViewById<TextView>(R.id.btnWeightPlus)
        val btnRepsMinus = row.findViewById<TextView>(R.id.btnRepsMinus)
        val btnRepsPlus = row.findViewById<TextView>(R.id.btnRepsPlus)

        tvSetLabel.text = "${setCount + 1}세트"

        btnWeightMinus.setOnClickListener {
            val cur = etWeight.text.toString().toFloatOrNull() ?: 0f
            if (cur >= 2.5f) etWeight.setText(if ((cur - 2.5f) == (cur - 2.5f).toInt().toFloat()) (cur - 2.5f).toInt().toString() else (cur - 2.5f).toString())
        }
        btnWeightPlus.setOnClickListener {
            val cur = etWeight.text.toString().toFloatOrNull() ?: 0f
            val next = cur + 2.5f
            etWeight.setText(if (next == next.toInt().toFloat()) next.toInt().toString() else next.toString())
        }
        btnRepsMinus.setOnClickListener {
            val cur = etReps.text.toString().toIntOrNull() ?: 0
            if (cur > 0) etReps.setText((cur - 1).toString())
        }
        btnRepsPlus.setOnClickListener {
            val cur = etReps.text.toString().toIntOrNull() ?: 0
            etReps.setText((cur + 1).toString())
        }

        setContainer.addView(row)
        setCount++
        updateStats()
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
    }

    private fun startWorkoutTimer() {
        timerHandler.postDelayed(object : Runnable {
            override fun run() {
                val elapsed = System.currentTimeMillis() - startTime
                val sec = (elapsed / 1000).toInt()
                tvWorkoutTime.text = String.format("%02d:%02d", sec / 60, sec % 60)
                timerHandler.postDelayed(this, 1000)
            }
        }, 1000)
    }
}
