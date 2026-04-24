package com.example.main.diet

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.main.R
import com.google.android.material.card.MaterialCardView
import com.google.android.material.progressindicator.CircularProgressIndicator

class DietAddActivity : AppCompatActivity() {

    private lateinit var dietRepository: DietRepository
    private lateinit var mealCards: Map<String, MaterialCardView>
    private lateinit var mealLabels: Map<String, TextView>
    private lateinit var calorieRing: CircularProgressIndicator
    private lateinit var tvCalorieIntake: TextView
    private lateinit var tvCalorieGoal: TextView
    private lateinit var tvCalorieRemaining: TextView
    private var selectedMealType = "아침"
    private var selectedDate = ""

    private val goalPrefs by lazy { getSharedPreferences("DietGoalPrefs", Context.MODE_PRIVATE) }
    private var goalCalories: Int
        get() = goalPrefs.getInt("daily_calorie_goal", 2000)
        set(value) { goalPrefs.edit().putInt("daily_calorie_goal", value).apply() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_diet_add)

        selectedDate = intent.getStringExtra("selectedDate") ?: ""
        dietRepository = DietRepository(this)

        val tvDateHeader = findViewById<TextView>(R.id.tvDateHeader)
        tvDateHeader.text = if (selectedDate.isNotEmpty()) "${selectedDate} 식단 기록" else "식단 기록"

        calorieRing = findViewById(R.id.calorieRing)
        tvCalorieIntake = findViewById(R.id.tvCalorieIntake)
        tvCalorieGoal = findViewById(R.id.tvCalorieGoal)
        tvCalorieRemaining = findViewById(R.id.tvCalorieRemaining)

        updateGoalDisplay()

        val etFoodName = findViewById<EditText>(R.id.etFoodName)
        val etCalories = findViewById<EditText>(R.id.etCalories)

        mealCards = mapOf(
            "아침" to findViewById(R.id.cardMealBreakfast),
            "점심" to findViewById(R.id.cardMealLunch),
            "저녁" to findViewById(R.id.cardMealDinner),
            "간식" to findViewById(R.id.cardMealSnack)
        )
        mealLabels = mapOf(
            "아침" to findViewById(R.id.tvBreakfastLabel),
            "점심" to findViewById(R.id.tvLunchLabel),
            "저녁" to findViewById(R.id.tvDinnerLabel),
            "간식" to findViewById(R.id.tvSnackLabel)
        )

        selectMealCard("아침")

        mealCards.forEach { (type, card) ->
            card.setOnClickListener {
                selectedMealType = type
                selectMealCard(type)
            }
        }

        // 목표 칼로리 편집
        findViewById<View>(R.id.layoutGoalEdit).setOnClickListener {
            showGoalEditDialog()
        }

        findViewById<ImageButton>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<Button>(R.id.btnSaveDiet).setOnClickListener {
            val name = etFoodName.text.toString().trim()
            val cal = etCalories.text.toString().toIntOrNull()

            if (name.isEmpty()) {
                Toast.makeText(this, "음식 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (cal == null || cal <= 0) {
                Toast.makeText(this, "칼로리를 올바르게 입력해주세요", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val date = currentDate()
            dietRepository.addEntry(date, DietRecord(selectedMealType, name, cal))
            etFoodName.setText("")
            etCalories.setText("")
            Toast.makeText(this, "$selectedMealType · $name (${cal}kcal) 추가됨", Toast.LENGTH_SHORT).show()
            refreshDietList(date)
        }

        refreshDietList(currentDate())
    }

    private fun currentDate() = selectedDate.ifEmpty {
        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date())
    }

    private fun showGoalEditDialog() {
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(64, 32, 64, 16)
        }

        val etGoal = EditText(this).apply {
            setText(goalCalories.toString())
            inputType = InputType.TYPE_CLASS_NUMBER
            textSize = 20f
            gravity = android.view.Gravity.CENTER
            selectAll()
            hint = "예: 2000"
        }

        val tvUnit = TextView(this).apply {
            text = "kcal"
            textSize = 13f
            gravity = android.view.Gravity.CENTER
            setTextColor(ContextCompat.getColor(this@DietAddActivity, R.color.on_surface_secondary))
            setPadding(0, 4, 0, 0)
        }

        container.addView(etGoal)
        container.addView(tvUnit)

        AlertDialog.Builder(this)
            .setTitle("하루 목표 칼로리 설정")
            .setMessage("섭취 목표 칼로리를 입력하세요")
            .setView(container)
            .setPositiveButton("저장") { _, _ ->
                val newGoal = etGoal.text.toString().toIntOrNull()
                when {
                    newGoal == null || newGoal <= 0 ->
                        Toast.makeText(this, "올바른 숫자를 입력해주세요", Toast.LENGTH_SHORT).show()
                    newGoal > 10000 ->
                        Toast.makeText(this, "목표 칼로리가 너무 높습니다 (최대 10,000 kcal)", Toast.LENGTH_SHORT).show()
                    else -> {
                        goalCalories = newGoal
                        updateGoalDisplay()
                        refreshDietList(currentDate())
                        Toast.makeText(this, "목표 칼로리가 ${String.format("%,d", newGoal)} kcal로 설정됐습니다", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun updateGoalDisplay() {
        tvCalorieGoal.text = "${String.format("%,d", goalCalories)} kcal"
    }

    private fun selectMealCard(selected: String) {
        val primaryColor = ContextCompat.getColor(this, R.color.primary)
        val primaryLightColor = ContextCompat.getColor(this, R.color.primary_light)
        val borderColor = ContextCompat.getColor(this, R.color.border)
        val surfaceColor = ContextCompat.getColor(this, R.color.surface)
        val secondaryTextColor = ContextCompat.getColor(this, R.color.on_surface_secondary)
        val density = resources.displayMetrics.density

        mealCards.forEach { (type, card) ->
            val isSelected = type == selected
            card.setCardBackgroundColor(if (isSelected) primaryLightColor else surfaceColor)
            card.strokeColor = if (isSelected) primaryColor else borderColor
            card.strokeWidth = if (isSelected) (2f * density).toInt() else (1.5f * density).toInt()
            mealLabels[type]?.setTextColor(if (isSelected) primaryColor else secondaryTextColor)
        }
    }

    private fun refreshDietList(date: String) {
        val entries = dietRepository.getEntries(date)
        val cardDietList = findViewById<View>(R.id.cardDietList)
        val llEntries = findViewById<LinearLayout>(R.id.llDietEntries)
        val tvTotal = findViewById<TextView>(R.id.tvTotalCalories)

        val totalCal = entries.sumOf { it.calories }
        updateCalorieRing(totalCal)

        if (entries.isEmpty()) {
            cardDietList.visibility = View.GONE
            return
        }

        cardDietList.visibility = View.VISIBLE
        tvTotal.text = "합계 ${String.format("%,d", totalCal)} kcal"

        llEntries.removeAllViews()
        entries.forEachIndexed { index, record ->
            val row = LayoutInflater.from(this).inflate(R.layout.item_diet_entry, llEntries, false)
            row.findViewById<TextView>(R.id.tvMealType).text = record.mealType
            row.findViewById<TextView>(R.id.tvFoodName).text = record.foodName
            row.findViewById<TextView>(R.id.tvFoodCalories).text = "${record.calories} kcal"
            row.findViewById<ImageButton>(R.id.btnDeleteDiet).setOnClickListener {
                dietRepository.deleteEntry(date, index)
                refreshDietList(date)
            }
            llEntries.addView(row)
        }
    }

    private fun updateCalorieRing(intake: Int) {
        val goal = goalCalories
        val progress = ((intake.toFloat() / goal) * 100).toInt().coerceIn(0, 100)
        calorieRing.setProgressCompat(progress, true)
        tvCalorieIntake.text = String.format("%,d", intake)

        val remaining = goal - intake
        if (remaining >= 0) {
            tvCalorieRemaining.text = "남은 칼로리 ${String.format("%,d", remaining)} kcal"
            tvCalorieRemaining.setTextColor(ContextCompat.getColor(this, R.color.on_surface_secondary))
            calorieRing.setIndicatorColor(ContextCompat.getColor(this, R.color.primary))
        } else {
            tvCalorieRemaining.text = "목표 초과 ${String.format("%,d", -remaining)} kcal"
            tvCalorieRemaining.setTextColor(ContextCompat.getColor(this, R.color.cal_sunday))
            calorieRing.setIndicatorColor(ContextCompat.getColor(this, R.color.cal_sunday))
        }
    }
}
