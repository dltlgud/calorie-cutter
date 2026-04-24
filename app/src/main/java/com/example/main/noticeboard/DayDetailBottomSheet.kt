package com.example.main.noticeboard

import android.content.DialogInterface
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.example.main.R
import com.example.main.calendarui2.WorkoutRecordActivity
import com.example.main.diet.DietAddActivity
import com.example.main.diet.DietRepository
import com.example.main.repository.WorkoutRepository
import com.example.main.routine.RuMainActivity
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.time.DayOfWeek
import java.time.LocalDate

class DayDetailBottomSheet : BottomSheetDialogFragment() {

    companion object {
        private const val ARG_DATE = "date"

        fun newInstance(dateStr: String) = DayDetailBottomSheet().apply {
            arguments = Bundle().apply { putString(ARG_DATE, dateStr) }
        }
    }

    var onDismissed: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottom_sheet_day_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dateStr = arguments?.getString(ARG_DATE) ?: LocalDate.now().toString()
        val workoutRepo = WorkoutRepository(requireContext())
        val dietRepo = DietRepository(requireContext())

        // 날짜 헤더
        val date = LocalDate.parse(dateStr)
        val dayLabel = when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "월요일"
            DayOfWeek.TUESDAY -> "화요일"
            DayOfWeek.WEDNESDAY -> "수요일"
            DayOfWeek.THURSDAY -> "목요일"
            DayOfWeek.FRIDAY -> "금요일"
            DayOfWeek.SATURDAY -> "토요일"
            else -> "일요일"
        }
        val isToday = date == LocalDate.now()
        val dateText = "${date.monthValue}월 ${date.dayOfMonth}일 $dayLabel" + if (isToday) " · 오늘" else ""
        view.findViewById<TextView>(R.id.tv_bs_date).text = dateText

        // 운동 기록
        val llWorkouts = view.findViewById<LinearLayout>(R.id.ll_bs_workouts)
        val tvWorkoutEmpty = view.findViewById<TextView>(R.id.tv_bs_workout_empty)
        val summaryList = workoutRepo.getSummaryList(dateStr)

        if (summaryList.isEmpty()) {
            tvWorkoutEmpty.visibility = View.VISIBLE
        } else {
            tvWorkoutEmpty.visibility = View.GONE
            summaryList.forEach { entry ->
                val tv = TextView(requireContext()).apply {
                    text = "• $entry"
                    textSize = 13f
                    setTextColor(Color.parseColor("#555555"))
                    setPadding(0, 4, 0, 4)
                }
                llWorkouts.addView(tv)
            }
        }

        // 식단 기록
        val tvDiet = view.findViewById<TextView>(R.id.tv_bs_diet)
        val dietSummary = dietRepo.getSummary(dateStr)
        tvDiet.text = dietSummary.ifBlank { "식단 기록 없음" }

        // 버튼 클릭
        view.findViewById<View>(R.id.btn_bs_workout).setOnClickListener {
            startActivity(Intent(requireContext(), WorkoutRecordActivity::class.java).apply {
                putExtra("selectedDate", dateStr)
                putExtra("selectedWorkout", "사용자 선택 운동")
            })
            dismiss()
        }

        view.findViewById<View>(R.id.btn_bs_diet).setOnClickListener {
            startActivity(Intent(requireContext(), DietAddActivity::class.java).apply {
                putExtra("selectedDate", dateStr)
            })
            dismiss()
        }

        view.findViewById<View>(R.id.btn_bs_routine).setOnClickListener {
            startActivity(Intent(requireContext(), RuMainActivity::class.java))
            dismiss()
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        onDismissed?.invoke()
    }
}
