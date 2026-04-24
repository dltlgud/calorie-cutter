package com.example.main.routine

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.main.R
import com.example.main.repository.RoutineRepository

class RoutineManageActivity : AppCompatActivity() {

    private lateinit var repo: RoutineRepository
    private lateinit var llRoutines: LinearLayout
    private lateinit var tvEmpty: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_routine_manage)

        repo = RoutineRepository(this)
        llRoutines = findViewById(R.id.ll_routines)
        tvEmpty = findViewById(R.id.tv_empty)

        findViewById<View>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<View>(R.id.btn_new_routine).setOnClickListener {
            startActivity(Intent(this, RoutineEditActivity::class.java))
        }
    }

    override fun onResume() {
        super.onResume()
        loadRoutines()
    }

    private fun loadRoutines() {
        llRoutines.removeAllViews()
        val routines = repo.getAll()

        if (routines.isEmpty()) {
            tvEmpty.visibility = View.VISIBLE
        } else {
            tvEmpty.visibility = View.GONE
            val inflater = LayoutInflater.from(this)
            routines.forEach { routine ->
                val card = inflater.inflate(R.layout.item_routine_card, llRoutines, false)
                card.findViewById<TextView>(R.id.tv_routine_name).text = routine.name
                card.findViewById<TextView>(R.id.tv_exercises_summary).text =
                    if (routine.exercises.isEmpty()) "운동 없음"
                    else routine.exercises.joinToString(" · ")
                card.findViewById<Button>(R.id.btn_edit).setOnClickListener {
                    startActivity(Intent(this, RoutineEditActivity::class.java).apply {
                        putExtra("routine_id", routine.id)
                    })
                }
                card.findViewById<Button>(R.id.btn_delete).setOnClickListener {
                    AlertDialog.Builder(this)
                        .setTitle("그룹 삭제")
                        .setMessage("\"${routine.name}\" 그룹을 삭제할까요?")
                        .setPositiveButton("삭제") { _, _ ->
                            repo.delete(routine.id)
                            loadRoutines()
                        }
                        .setNegativeButton("취소", null)
                        .show()
                }
                llRoutines.addView(card)
            }
        }
    }
}
