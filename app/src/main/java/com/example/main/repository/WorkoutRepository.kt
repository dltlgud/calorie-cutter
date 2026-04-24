package com.example.main.repository

import android.content.Context

class WorkoutRepository(context: Context) {

    private val prefs = context.getSharedPreferences("WorkoutPrefs", Context.MODE_PRIVATE)

    // Raw entries include a timestamp prefix: "1234567890::display text"
    private fun getRawList(date: String): List<String> =
        (prefs.getString("summary_$date", "") ?: "").split("\n").filter { it.isNotBlank() }

    fun getSummaryList(date: String): List<String> =
        getRawList(date).map { it.substringAfter("::") }

    fun addEntry(date: String, entry: String, sets: List<Pair<Int, Int>>, maxEntries: Int = 10) {
        val ts = System.currentTimeMillis()
        val rawList = getRawList(date).toMutableList()
        rawList.add(0, "$ts::$entry")
        val setsStr = sets.joinToString(",") { "${it.first}:${it.second}" }
        prefs.edit()
            .putString("summary_$date", rawList.take(maxEntries).joinToString("\n"))
            .putString("detail_${date}_$ts", setsStr)
            .apply()
    }

    // Returns Pair(workoutName, list of kg to reps pairs) for a given display index
    fun getDetail(date: String, index: Int): Pair<String, List<Pair<Int, Int>>>? {
        val raw = getRawList(date).getOrNull(index) ?: return null
        val ts = raw.substringBefore("::")
        val entry = raw.substringAfter("::")
        val workoutName = entry.substringBefore(" /")
        val setsStr = prefs.getString("detail_${date}_$ts", null)
            ?: return Pair(workoutName, emptyList())
        val sets = setsStr.split(",").mapNotNull {
            val parts = it.split(":")
            if (parts.size == 2) Pair(parts[0].toIntOrNull() ?: 0, parts[1].toIntOrNull() ?: 0)
            else null
        }
        return Pair(workoutName, sets)
    }

    fun deleteEntry(date: String, index: Int) {
        val rawList = getRawList(date).toMutableList()
        if (index in rawList.indices) {
            val ts = rawList[index].substringBefore("::")
            rawList.removeAt(index)
            prefs.edit()
                .putString("summary_$date", rawList.joinToString("\n"))
                .remove("detail_${date}_$ts")
                .apply()
        }
    }

    fun saveSummary(date: String, summary: String) {
        prefs.edit().putString("summary_$date", summary).apply()
    }

    fun getSummary(date: String): String =
        prefs.getString("summary_$date", "") ?: ""
}
