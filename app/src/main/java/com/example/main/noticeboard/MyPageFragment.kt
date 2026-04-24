package com.example.main.noticeboard

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.main.R
import com.example.main.auth.EditProfileActivity
import com.example.main.auth.LoginActivity
import com.example.main.diet.DietRepository
import com.example.main.notification.ReminderWorker
import com.example.main.repository.UserRepository
import com.github.mikephil.charting.charts.*
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class MyPageFragment : Fragment() {

    private lateinit var tvUsername: TextView
    private lateinit var tvAge: TextView
    private lateinit var tvHeight: TextView
    private lateinit var tvWeight: TextView
    private lateinit var etAge: EditText
    private lateinit var etHeight: EditText
    private lateinit var etWeight: EditText
    private lateinit var btnEdit: Button
    private lateinit var btnEditProfile: Button
    private lateinit var btnSave: Button
    private lateinit var btnLogout: Button
    private lateinit var btnStatWeekly: Button
    private lateinit var btnStatMonthly: Button

    private lateinit var tvProfileInitial: TextView
    private lateinit var tvStatDays: TextView
    private lateinit var tvStatMinutes: TextView
    private lateinit var tvStatCalories: TextView
    private lateinit var tvStatIntake: TextView

    private lateinit var barChart: BarChart
    private lateinit var barChartCalorie: BarChart
    private lateinit var chartWeightTrend: LineChart
    private lateinit var pieChartExerciseRatio: PieChart

    private lateinit var auth: FirebaseAuth
    private lateinit var dietRepository: DietRepository
    private val userRepository = UserRepository()
    private var userWeight = 0.0

    private lateinit var rgNotification: RadioGroup

    companion object {
        private const val PREFS_NOTIF = "NotifPrefs"
        private const val KEY_INTERVAL = "reminder_interval_hours"
    }

    private var weeklyExerciseMinutes = MutableList(7) { 0f }
    private var currentDays = 7

    // App primary color
    private val colorPrimary = Color.parseColor("#5B67CA")
    private val colorPrimaryDark = Color.parseColor("#3D4AB0")
    private val colorAccent1 = Color.parseColor("#7C83D4")
    private val chartColors = listOf(
        Color.parseColor("#5B67CA"),
        Color.parseColor("#7C83D4"),
        Color.parseColor("#22C55E"),
        Color.parseColor("#F59E0B"),
        Color.parseColor("#EF4444"),
        Color.parseColor("#06B6D4"),
        Color.parseColor("#EC4899")
    )

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_mypage, container, false)

        auth = FirebaseAuth.getInstance()
        dietRepository = DietRepository(requireContext())

        barChart = view.findViewById(R.id.chart_exercise_trend)
        barChartCalorie = view.findViewById(R.id.chart_calorie_trend)
        chartWeightTrend = view.findViewById(R.id.chart_weight_trend)
        pieChartExerciseRatio = view.findViewById(R.id.chart_exercise_ratio)

        tvUsername = view.findViewById(R.id.tv_username)
        tvAge = view.findViewById(R.id.tv_age)
        tvHeight = view.findViewById(R.id.tv_height)
        tvWeight = view.findViewById(R.id.tv_weight)
        tvProfileInitial = view.findViewById(R.id.tv_profile_initial)
        tvStatDays = view.findViewById(R.id.tv_stat_days)
        tvStatMinutes = view.findViewById(R.id.tv_stat_minutes)
        tvStatCalories = view.findViewById(R.id.tv_stat_calories)
        tvStatIntake = view.findViewById(R.id.tv_stat_intake)

        etAge = view.findViewById(R.id.et_age)
        etHeight = view.findViewById(R.id.et_height)
        etWeight = view.findViewById(R.id.et_weight)

        btnEdit = view.findViewById(R.id.btn_edit_userinfo)
        btnEditProfile = view.findViewById(R.id.btn_edit_profile)
        btnSave = view.findViewById(R.id.btn_save_userinfo)
        btnLogout = view.findViewById(R.id.btn_logout)
        btnStatWeekly = view.findViewById(R.id.btn_stat_weekly)
        btnStatMonthly = view.findViewById(R.id.btn_stat_monthly)

        btnStatWeekly.setOnClickListener {
            btnStatWeekly.setBackgroundResource(R.drawable.bg_chip_selected)
            btnStatWeekly.setTextColor(Color.WHITE)
            btnStatMonthly.setBackgroundColor(Color.TRANSPARENT)
            btnStatMonthly.setTextColor(colorPrimary)
            weeklyExerciseMinutes = MutableList(7) { 0f }
            loadExerciseSummaryFromPrefsAndDrawChart(days = 7)
        }

        btnStatMonthly.setOnClickListener {
            btnStatMonthly.setBackgroundResource(R.drawable.bg_chip_selected)
            btnStatMonthly.setTextColor(Color.WHITE)
            btnStatWeekly.setBackgroundColor(Color.TRANSPARENT)
            btnStatWeekly.setTextColor(colorPrimary)
            weeklyExerciseMinutes = MutableList(30) { 0f }
            loadExerciseSummaryFromPrefsAndDrawChart(days = 30)
        }

        rgNotification = view.findViewById(R.id.rg_notification_interval)
        setupNotificationSettings()

        btnEdit.setOnClickListener { toggleEditMode(true) }
        btnEditProfile.setOnClickListener {
            startActivity(Intent(requireContext(), EditProfileActivity::class.java))
        }
        btnSave.setOnClickListener { saveUserInfo() }
        btnLogout.setOnClickListener {
            auth.signOut()
            Toast.makeText(requireContext(), "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
            startActivity(
                Intent(requireContext(), LoginActivity::class.java)
                    .apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
            )
        }

        loadUserInfo()
        return view
    }

    private fun setupNotificationSettings() {
        val prefs = requireContext().getSharedPreferences(PREFS_NOTIF, Context.MODE_PRIVATE)
        val savedHours = prefs.getLong(KEY_INTERVAL, 48L)

        val checkedId = when (savedHours) {
            0L -> R.id.rb_notif_off
            24L -> R.id.rb_notif_24h
            48L -> R.id.rb_notif_48h
            72L -> R.id.rb_notif_72h
            else -> R.id.rb_notif_1week
        }
        rgNotification.check(checkedId)

        rgNotification.setOnCheckedChangeListener { _, id ->
            val hours = when (id) {
                R.id.rb_notif_off -> 0L
                R.id.rb_notif_24h -> 24L
                R.id.rb_notif_48h -> 48L
                R.id.rb_notif_72h -> 72L
                R.id.rb_notif_1week -> 168L
                else -> 48L
            }
            prefs.edit().putLong(KEY_INTERVAL, hours).apply()
            scheduleReminder(hours)
            val msg = if (hours == 0L) "알림이 꺼졌습니다" else "${if (hours >= 168) "1주일" else "${hours}시간"} 후 알림으로 설정했습니다"
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    private fun scheduleReminder(hours: Long) {
        val wm = WorkManager.getInstance(requireContext())
        if (hours == 0L) {
            wm.cancelUniqueWork("inactivity_reminder")
            return
        }
        val work = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(hours, TimeUnit.HOURS)
            .build()
        wm.enqueueUniqueWork("inactivity_reminder", ExistingWorkPolicy.REPLACE, work)
    }

    private fun toggleEditMode(editing: Boolean) {
        tvAge.visibility = if (editing) View.GONE else View.VISIBLE
        tvHeight.visibility = if (editing) View.GONE else View.VISIBLE
        tvWeight.visibility = if (editing) View.GONE else View.VISIBLE

        etAge.visibility = if (editing) View.VISIBLE else View.GONE
        etHeight.visibility = if (editing) View.VISIBLE else View.GONE
        etWeight.visibility = if (editing) View.VISIBLE else View.GONE

        btnEdit.visibility = if (editing) View.GONE else View.VISIBLE
        btnSave.visibility = if (editing) View.VISIBLE else View.GONE

        if (editing) {
            etAge.setText(tvAge.text.toString().replace("세", "").trim())
            etHeight.setText(tvHeight.text.toString().replace("cm", "").trim())
            etWeight.setText(tvWeight.text.toString().replace("kg", "").trim())
        }
    }

    private fun loadUserInfo() {
        val uid = auth.currentUser?.uid ?: return
        userRepository.getUser(uid,
            onSuccess = { doc ->
                if (doc.exists()) {
                    val name = doc.getString("username") ?: "알 수 없음"
                    tvUsername.text = name
                    tvProfileInitial.text = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
                    doc.getLong("age")?.let { tvAge.text = "${it}세" }
                    doc.getDouble("height")?.let { tvHeight.text = "${it}cm" }
                    doc.getDouble("weight")?.let {
                        tvWeight.text = "${it}kg"
                        userWeight = it
                    }
                    loadExerciseSummaryFromPrefsAndDrawChart()
                    drawWeightTrendChart()
                }
            },
            onFailure = {
                Toast.makeText(requireContext(), "유저 정보 불러오기 실패", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun saveUserInfo() {
        val uid = auth.currentUser?.uid ?: return
        val updates = hashMapOf<String, Any>(
            "age" to etAge.text.toString().toInt(),
            "height" to etHeight.text.toString().toDouble(),
            "weight" to etWeight.text.toString().toDouble()
        )
        userWeight = updates["weight"] as Double
        userRepository.updateUser(uid, updates,
            onSuccess = {
                tvAge.text = "${updates["age"]}세"
                tvHeight.text = "${updates["height"]}cm"
                tvWeight.text = "${updates["weight"]}kg"
                toggleEditMode(false)
                saveWeightRecord(userWeight)
                drawWeightTrendChart()
            },
            onFailure = {
                Toast.makeText(requireContext(), "저장 실패. 다시 시도해주세요.", Toast.LENGTH_SHORT).show()
            }
        )
    }

    private fun saveWeightRecord(weight: Double) {
        val uid = auth.currentUser?.uid ?: return
        userRepository.addWeightRecord(uid, weight)
    }

    private fun drawWeightTrendChart() {
        val uid = auth.currentUser?.uid ?: return
        userRepository.getWeightRecords(uid) { documents ->
            val entries = mutableListOf<Entry>()
            val labels = mutableListOf<String>()
            val sdf = SimpleDateFormat("MM/dd", Locale.getDefault())
            documents.forEachIndexed { index, doc ->
                doc.getDouble("weight")?.let { weight ->
                    val date = doc.getTimestamp("timestamp")?.toDate() ?: Date()
                    entries.add(Entry(index.toFloat(), weight.toFloat()))
                    labels.add(sdf.format(date))
                }
            }

            val dataSet = LineDataSet(entries, "몸무게(kg)").apply {
                color = colorPrimary
                lineWidth = 2.5f
                setDrawCircles(true)
                circleRadius = 4f
                circleHoleRadius = 2f
                setCircleColor(colorPrimary)
                setDrawValues(true)
                valueTextSize = 10f
                valueTextColor = colorPrimary
                mode = LineDataSet.Mode.CUBIC_BEZIER
            }

            chartWeightTrend.apply {
                data = LineData(dataSet)
                xAxis.apply {
                    valueFormatter = IndexAxisValueFormatter(labels)
                    position = XAxis.XAxisPosition.BOTTOM
                    granularity = 1f
                    setDrawGridLines(false)
                    textColor = Color.parseColor("#888888")
                    textSize = 10f
                }
                axisLeft.apply {
                    textColor = Color.parseColor("#888888")
                    textSize = 10f
                    setDrawGridLines(true)
                    gridColor = Color.parseColor("#F0F0F0")
                }
                axisRight.isEnabled = false
                description.isEnabled = false
                legend.isEnabled = false
                setExtraOffsets(8f, 8f, 8f, 8f)
                invalidate()
            }
        }
    }

    private fun loadExerciseSummaryFromPrefsAndDrawChart(days: Int = 7) {
        currentDays = days
        val sdfKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val prefs = requireContext().getSharedPreferences("WorkoutPrefs", Context.MODE_PRIVATE)
        val calendar = Calendar.getInstance()

        val exerciseMap = mutableMapOf<String, Float>()
        var activeDays = 0
        var totalMinutes = 0f

        for (i in (days - 1) downTo 0) {
            val date = Calendar.getInstance().apply {
                timeInMillis = calendar.timeInMillis
                add(Calendar.DAY_OF_YEAR, -i)
            }
            val summary = prefs.getString("summary_${sdfKey.format(date.time)}", "") ?: ""
            val lines = summary.split("\n").filter { it.isNotBlank() }
            var dayMinutes = 0f
            for (line in lines) {
                val displayLine = if (line.contains("::")) line.substringAfter("::") else line
                val name = displayLine.substringBefore(" /").trim()
                val minutes = Regex("(\\d+)분").find(displayLine)?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
                if (name.isNotBlank()) {
                    exerciseMap[name] = (exerciseMap[name] ?: 0f) + minutes
                }
                dayMinutes += minutes
                weeklyExerciseMinutes[(days - 1) - i] += minutes
            }
            if (dayMinutes > 0f) activeDays++
            totalMinutes += dayMinutes
        }

        val totalCalories = (totalMinutes * 6.8f).toInt()
        var totalIntake = 0
        for (i in (days - 1) downTo 0) {
            val date = Calendar.getInstance().apply {
                timeInMillis = calendar.timeInMillis
                add(Calendar.DAY_OF_YEAR, -i)
            }
            totalIntake += dietRepository.getTotalCalories(sdfKey.format(date.time))
        }

        tvStatDays.text = "${activeDays}일"
        tvStatMinutes.text = totalMinutes.toInt().toString()
        tvStatCalories.text = totalCalories.toString()
        tvStatIntake.text = totalIntake.toString()

        drawExerciseBarChart(days)
        drawCalorieBarChart(days)
        drawExerciseRatioChart(exerciseMap)
    }

    private fun drawExerciseBarChart(days: Int = currentDays) {
        val entries = weeklyExerciseMinutes.mapIndexed { i, v -> BarEntry(i.toFloat(), v) }
        val labels = (0 until days).map {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -(days - 1) + it) }
            SimpleDateFormat("MM/dd", Locale.getDefault()).format(cal.time)
        }

        val dataSet = BarDataSet(entries, "운동 시간(분)").apply {
            color = colorPrimary
            valueTextColor = colorPrimary
            valueTextSize = 9f
        }

        barChart.apply {
            data = BarData(dataSet).apply { barWidth = 0.7f }
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                textColor = Color.parseColor("#888888")
                textSize = 10f
            }
            axisLeft.apply {
                textColor = Color.parseColor("#888888")
                setDrawGridLines(true)
                gridColor = Color.parseColor("#F0F0F0")
            }
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
            setFitBars(true)
            setExtraOffsets(8f, 8f, 8f, 8f)
            invalidate()
        }
    }

    private fun drawCalorieBarChart(days: Int = currentDays) {
        val entries = weeklyExerciseMinutes.mapIndexed { i, v -> BarEntry(i.toFloat(), v * 6.8f) }
        val labels = (0 until days).map {
            val cal = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, -(days - 1) + it) }
            SimpleDateFormat("MM/dd", Locale.getDefault()).format(cal.time)
        }

        val dataSet = BarDataSet(entries, "칼로리(kcal)").apply {
            color = colorAccent1
            valueTextColor = colorPrimaryDark
            valueTextSize = 9f
        }

        barChartCalorie.apply {
            data = BarData(dataSet).apply { barWidth = 0.7f }
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(labels)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawGridLines(false)
                textColor = Color.parseColor("#888888")
                textSize = 10f
            }
            axisLeft.apply {
                textColor = Color.parseColor("#888888")
                setDrawGridLines(true)
                gridColor = Color.parseColor("#F0F0F0")
            }
            axisRight.isEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
            setFitBars(true)
            setExtraOffsets(8f, 8f, 8f, 8f)
            invalidate()
        }
    }

    private fun drawExerciseRatioChart(exerciseMap: Map<String, Float>) {
        if (exerciseMap.values.sum() <= 0f) {
            pieChartExerciseRatio.clear()
            pieChartExerciseRatio.setNoDataText("이번 주 운동 기록이 없습니다")
            pieChartExerciseRatio.setNoDataTextColor(Color.parseColor("#888888"))
            return
        }

        val total = exerciseMap.values.sum()
        val pieEntries = exerciseMap.map { PieEntry(it.value / total * 100f, it.key) }

        val dataSet = PieDataSet(pieEntries, "").apply {
            colors = chartColors
            valueTextSize = 12f
            valueTextColor = Color.WHITE
            sliceSpace = 3f
            selectionShift = 6f
        }

        pieChartExerciseRatio.apply {
            data = PieData(dataSet)
            description.isEnabled = false
            setUsePercentValues(true)
            setEntryLabelColor(Color.TRANSPARENT)
            setHoleColor(Color.WHITE)
            holeRadius = 44f
            transparentCircleRadius = 48f
            isDrawHoleEnabled = true
            legend.apply {
                isEnabled = true
                verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
                horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
                orientation = Legend.LegendOrientation.HORIZONTAL
                setDrawInside(false)
                textSize = 11f
                textColor = Color.parseColor("#444444")
                form = Legend.LegendForm.CIRCLE
                formSize = 9f
                xEntrySpace = 12f
            }
            invalidate()
        }
    }
}
