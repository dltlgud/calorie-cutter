package com.example.main.repository

import android.content.Context

class RoutineRepository(context: Context) {

    data class Routine(
        val id: String,
        val name: String,
        val exercises: List<String>
    )

    private val prefs = context.getSharedPreferences("RoutinePrefs", Context.MODE_PRIVATE)

    fun getAll(): List<Routine> {
        val ids = (prefs.getString("ids", "") ?: "").split(",").filter { it.isNotBlank() }
        return ids.mapNotNull { id ->
            val name = prefs.getString("name_$id", null) ?: return@mapNotNull null
            val exercises = parseExercises(prefs.getString("exercises_$id", "") ?: "")
            Routine(id, name, exercises)
        }
    }

    fun getById(id: String): Routine? {
        val name = prefs.getString("name_$id", null) ?: return null
        val exercises = parseExercises(prefs.getString("exercises_$id", "") ?: "")
        return Routine(id, name, exercises)
    }

    fun save(name: String, exercises: List<String>): String {
        val id = System.currentTimeMillis().toString()
        val ids = (prefs.getString("ids", "") ?: "").split(",").filter { it.isNotBlank() }.toMutableList()
        ids.add(0, id)
        prefs.edit()
            .putString("ids", ids.joinToString(","))
            .putString("name_$id", name)
            .putString("exercises_$id", exercises.joinToString(";"))
            .apply()
        return id
    }

    fun update(id: String, name: String, exercises: List<String>) {
        prefs.edit()
            .putString("name_$id", name)
            .putString("exercises_$id", exercises.joinToString(";"))
            .apply()
    }

    fun delete(id: String) {
        val ids = (prefs.getString("ids", "") ?: "").split(",").filter { it.isNotBlank() }.toMutableList()
        ids.remove(id)
        prefs.edit()
            .putString("ids", ids.joinToString(","))
            .remove("name_$id")
            .remove("exercises_$id")
            .apply()
    }

    private fun parseExercises(str: String): List<String> =
        if (str.isBlank()) emptyList()
        else str.split(";").filter { it.isNotBlank() }
}
