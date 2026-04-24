package com.example.main.noticeboard

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.main.R
import com.example.main.calendarui2.WorkoutRecordActivity
import com.example.main.calendarui2.WorkoutSelectActivity
import com.example.main.diet.DietAddActivity
import com.example.main.diet.DietRepository
import com.example.main.repository.WorkoutRepository
import com.example.main.routine.RuMainActivity
import com.example.main.routine.RoutineManageActivity
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.view.CalendarView
import com.kizitonwose.calendar.view.MonthDayBinder
import com.kizitonwose.calendar.view.ViewContainer
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.util.*

class HomeFragment : Fragment() {

    companion object {
        var currentSelectedDate: LocalDate = LocalDate.now()
    }

    private lateinit var workoutRepository: WorkoutRepository
    private lateinit var dietRepository: DietRepository
    private lateinit var calendarView: CalendarView
    private lateinit var tvMonthYear: TextView
    private lateinit var tvStatDays: TextView
    private lateinit var tvStatMinutes: TextView
    private lateinit var tvStatBurned: TextView
    private lateinit var tvStatIntake: TextView

    // 인라인 날짜 상세 뷰
    private lateinit var tvSelectedDate: TextView
    private lateinit var llDayWorkouts: LinearLayout
    private lateinit var tvDayWorkoutEmpty: TextView
    private lateinit var llDayDiet: LinearLayout
    private lateinit var tvDayDietEmpty: TextView
    private lateinit var tvDayCalorieTotal: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.activity_cal_main, container, false)

        workoutRepository = WorkoutRepository(requireContext())
        dietRepository    = DietRepository(requireContext())
        calendarView      = view.findViewById(R.id.calendarView)
        tvMonthYear       = view.findViewById(R.id.tvMonthYear)
        tvStatDays        = view.findViewById(R.id.tv_home_stat_days)
        tvStatMinutes     = view.findViewById(R.id.tv_home_stat_minutes)
        tvStatBurned      = view.findViewById(R.id.tv_home_stat_burned)
        tvStatIntake      = view.findViewById(R.id.tv_home_stat_intake)

        tvSelectedDate      = view.findViewById(R.id.tv_selected_date)
        llDayWorkouts       = view.findViewById(R.id.ll_day_workouts)
        tvDayWorkoutEmpty   = view.findViewById(R.id.tv_day_workout_empty)
        llDayDiet           = view.findViewById(R.id.ll_day_diet)
        tvDayDietEmpty      = view.findViewById(R.id.tv_day_diet_empty)
        tvDayCalorieTotal   = view.findViewById(R.id.tv_day_calorie_total)

        setupCalendar()
        setupNavigation(view)
        setupDayActionButtons(view)
        loadWeeklyStats()
        loadDayDetail(currentSelectedDate.toString())

        return view
    }

    override fun onResume() {
        super.onResume()
        loadWeeklyStats()
        loadDayDetail(currentSelectedDate.toString())
        calendarView.notifyDateChanged(currentSelectedDate)
    }

    private fun setupCalendar() {
        val currentMonth = YearMonth.now()
        calendarView.setup(
            currentMonth.minusMonths(24),
            currentMonth.plusMonths(24),
            DayOfWeek.SUNDAY
        )
        calendarView.scrollToMonth(currentMonth)

        calendarView.dayBinder = object : MonthDayBinder<DayViewContainer> {
            override fun create(view: View) = DayViewContainer(view)
            override fun bind(container: DayViewContainer, data: CalendarDay) = container.bind(data)
        }

        calendarView.monthScrollListener = { month ->
            tvMonthYear.text = "${month.yearMonth.year}년 ${month.yearMonth.monthValue}월"
        }
        tvMonthYear.text = "${currentMonth.year}년 ${currentMonth.monthValue}월"
    }

    private fun setupNavigation(view: View) {
        view.findViewById<ImageButton>(R.id.btnPrevMonth).setOnClickListener {
            val current = calendarView.findFirstVisibleMonth()?.yearMonth ?: return@setOnClickListener
            calendarView.smoothScrollToMonth(current.minusMonths(1))
        }
        view.findViewById<ImageButton>(R.id.btnNextMonth).setOnClickListener {
            val current = calendarView.findFirstVisibleMonth()?.yearMonth ?: return@setOnClickListener
            calendarView.smoothScrollToMonth(current.plusMonths(1))
        }
    }

    private fun setupDayActionButtons(view: View) {
        view.findViewById<View>(R.id.btn_day_workout).setOnClickListener {
            val dateStr = currentSelectedDate.toString()
            startActivity(Intent(requireContext(), WorkoutSelectActivity::class.java).apply {
                putExtra("selectedDate", dateStr)
            })
        }
        view.findViewById<View>(R.id.btn_day_diet).setOnClickListener {
            val dateStr = currentSelectedDate.toString()
            startActivity(Intent(requireContext(), DietAddActivity::class.java).apply {
                putExtra("selectedDate", dateStr)
            })
        }
        view.findViewById<View>(R.id.btn_day_routine).setOnClickListener {
            startActivity(Intent(requireContext(), RuMainActivity::class.java))
        }
        view.findViewById<View>(R.id.btn_routine_manage).setOnClickListener {
            startActivity(Intent(requireContext(), RoutineManageActivity::class.java))
        }
    }

    inner class DayViewContainer(view: View) : ViewContainer(view) {
        val tvDay: TextView = view.findViewById(R.id.tvDay)
        val workoutBar: View = view.findViewById(R.id.workoutBar)
        lateinit var day: CalendarDay

        init {
            view.setOnClickListener {
                if (day.position == DayPosition.MonthDate) {
                    val old = currentSelectedDate
                    currentSelectedDate = day.date
                    calendarView.notifyDateChanged(old)
                    calendarView.notifyDateChanged(currentSelectedDate)
                    loadDayDetail(currentSelectedDate.toString())
                }
            }
        }

        fun bind(data: CalendarDay) {
            day = data
            tvDay.text = data.date.dayOfMonth.toString()

            if (data.position == DayPosition.MonthDate) {
                tvDay.alpha = 1f
                tvDay.setTextColor(requireContext().getColor(
                    when (data.date.dayOfWeek) {
                        DayOfWeek.SUNDAY -> R.color.cal_sunday
                        DayOfWeek.SATURDAY -> R.color.cal_saturday
                        else -> R.color.back
                    }
                ))
                when (data.date) {
                    LocalDate.now() -> {
                        tvDay.setBackgroundResource(R.drawable.bg_today_circle)
                        tvDay.setTextColor(requireContext().getColor(R.color.white))
                    }
                    currentSelectedDate -> {
                        tvDay.setBackgroundResource(R.drawable.bg_selected_circle)
                    }
                    else -> tvDay.background = null
                }
                val hasSummary = workoutRepository.getSummary(data.date.toString()).isNotBlank()
                workoutBar.visibility = if (hasSummary) View.VISIBLE else View.INVISIBLE
            } else {
                tvDay.alpha = 0.25f
                tvDay.background = null
                workoutBar.visibility = View.INVISIBLE
            }
        }
    }

    fun loadDayDetail(dateStr: String) {
        val date = LocalDate.parse(dateStr)
        val dayLabel = when (date.dayOfWeek) {
            DayOfWeek.MONDAY    -> "월요일"
            DayOfWeek.TUESDAY   -> "화요일"
            DayOfWeek.WEDNESDAY -> "수요일"
            DayOfWeek.THURSDAY  -> "목요일"
            DayOfWeek.FRIDAY    -> "금요일"
            DayOfWeek.SATURDAY  -> "토요일"
            else                -> "일요일"
        }
        val isToday = date == LocalDate.now()
        tvSelectedDate.text = "${date.monthValue}월 ${date.dayOfMonth}일 $dayLabel" +
                if (isToday) "  ·  오늘" else ""

        // 운동 기록
        llDayWorkouts.removeAllViews()
        val workoutList = workoutRepository.getSummaryList(dateStr)
        if (workoutList.isEmpty()) {
            tvDayWorkoutEmpty.visibility = View.VISIBLE
        } else {
            tvDayWorkoutEmpty.visibility = View.GONE
            workoutList.forEach { entry ->
                val tv = TextView(requireContext()).apply {
                    text = "• $entry"
                    textSize = 13f
                    setTextColor(Color.parseColor("#555555"))
                    setPadding(0, 4, 0, 4)
                }
                llDayWorkouts.addView(tv)
            }
        }

        // 식단 기록
        llDayDiet.removeAllViews()
        val dietEntries = dietRepository.getEntries(dateStr)
        val totalCal = dietEntries.sumOf { it.calories }

        if (dietEntries.isEmpty()) {
            tvDayDietEmpty.visibility = View.VISIBLE
            tvDayCalorieTotal.text = ""
        } else {
            tvDayDietEmpty.visibility = View.GONE
            tvDayCalorieTotal.text = "${String.format("%,d", totalCal)} kcal"

            // 식사 유형별로 묶어서 표시
            val grouped = dietEntries.groupBy { it.mealType }
            val mealOrder = listOf("아침", "점심", "저녁", "간식")
            mealOrder.filter { grouped.containsKey(it) }.forEach { mealType ->
                val meals = grouped[mealType] ?: return@forEach
                val mealCal = meals.sumOf { it.calories }

                val row = LayoutInflater.from(requireContext())
                    .inflate(android.R.layout.simple_list_item_2, llDayDiet, false)
                row.setPadding(0, 4, 0, 4)

                val header = TextView(requireContext()).apply {
                    text = "$mealType  ${String.format("%,d", mealCal)}kcal"
                    textSize = 12f
                    setTextColor(Color.parseColor("#5B67CA"))
                    setTypeface(null, android.graphics.Typeface.BOLD)
                }
                llDayDiet.addView(header)

                meals.forEach { record ->
                    val tv = TextView(requireContext()).apply {
                        text = "   · ${record.foodName}  ${record.calories}kcal"
                        textSize = 12f
                        setTextColor(Color.parseColor("#555555"))
                        setPadding(0, 2, 0, 2)
                    }
                    llDayDiet.addView(tv)
                }
            }
        }
    }

    fun loadWeeklyStats() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val prefs = requireContext().getSharedPreferences("WorkoutPrefs", Context.MODE_PRIVATE)
        var activeDays = 0
        var totalMinutes = 0f
        var totalIntake = 0

        for (i in 6 downTo 0) {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -i) }
            val dateKey = sdf.format(cal.time)
            val summary = prefs.getString("summary_$dateKey", "") ?: ""
            val lines = summary.split("\n").filter { it.isNotBlank() }
            var dayMinutes = 0f
            for (line in lines) {
                val display = if (line.contains("::")) line.substringAfter("::") else line
                dayMinutes += Regex("(\\d+)분").find(display)?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
            }
            if (dayMinutes > 0f) activeDays++
            totalMinutes += dayMinutes
            totalIntake += dietRepository.getTotalCalories(dateKey)
        }

        val burned = (totalMinutes * 6.8f).toInt()
        tvStatDays.text = "${activeDays}일"
        tvStatMinutes.text = "${totalMinutes.toInt()}분"
        tvStatBurned.text = "${burned}kcal"
        tvStatIntake.text = "${totalIntake}kcal"
    }
}
