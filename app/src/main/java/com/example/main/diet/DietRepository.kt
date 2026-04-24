package com.example.main.diet

import android.content.Context

class DietRepository(context: Context) {

    private val prefs = context.getSharedPreferences("DietPrefs", Context.MODE_PRIVATE)

    fun addEntry(date: String, record: DietRecord) {
        val list = getEntries(date).toMutableList()
        list.add(record)
        save(date, list)
    }

    fun getEntries(date: String): List<DietRecord> {
        val raw = prefs.getString("diet_$date", "") ?: return emptyList()
        return raw.split("\n").filter { it.isNotBlank() }.mapNotNull { line ->
            val p = line.split("|")
            if (p.size == 3) DietRecord(p[0], p[1], p[2].toIntOrNull() ?: 0) else null
        }
    }

    fun deleteEntry(date: String, index: Int) {
        val list = getEntries(date).toMutableList()
        if (index in list.indices) {
            list.removeAt(index)
            save(date, list)
        }
    }

    fun getTotalCalories(date: String): Int = getEntries(date).sumOf { it.calories }

    fun hasEntries(date: String): Boolean = getEntries(date).isNotEmpty()

    fun getSummary(date: String): String {
        val entries = getEntries(date)
        if (entries.isEmpty()) return ""
        return "${entries.size}가지 식품 · 총 ${entries.sumOf { it.calories }}kcal"
    }

    private fun save(date: String, list: List<DietRecord>) {
        prefs.edit()
            .putString("diet_$date", list.joinToString("\n") { "${it.mealType}|${it.foodName}|${it.calories}" })
            .apply()
    }
}
