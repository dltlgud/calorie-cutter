package com.example.main.routine

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.main.R
import com.example.main.calendarui2.WorkoutRecordActivity
import com.example.main.noticeboard.CommunityMainActivity
import com.example.main.repository.RoutineRepository
import java.text.SimpleDateFormat
import java.util.*

class ResultFragment : Fragment() {

    private val viewModel: RecommendationViewModel by activityViewModels()
    private lateinit var recommendations: List<ExerciseData>
    private lateinit var routineRepo: RoutineRepository

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_result, container, false)

        routineRepo = RoutineRepository(requireContext())

        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView_recommendations)
        val tvSubtitle = view.findViewById<TextView>(R.id.tv_result_subtitle)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        recommendations = viewModel.generateRecommendations()
        val freq = viewModel.frequency.value ?: recommendations.size
        tvSubtitle.text = "주 ${freq}일 기준 · 총 ${recommendations.size}개 운동"

        recyclerView.adapter = RecommendationAdapter(recommendations) { exercise ->
            val today = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            startActivity(Intent(requireContext(), WorkoutRecordActivity::class.java).apply {
                putExtra("selectedWorkout", exercise.name)
                putExtra("selectedDate", today)
            })
        }

        view.findViewById<Button>(R.id.btn_save_routine).setOnClickListener {
            showSaveToGroupDialog()
        }

        view.findViewById<Button>(R.id.btn_return).setOnClickListener {
            startActivity(
                Intent(requireContext(), CommunityMainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
            requireActivity().finish()
        }

        return view
    }

    private fun showSaveToGroupDialog() {
        val exerciseNames = recommendations.map { it.name }
        val routines = routineRepo.getAll()

        val options = buildList {
            routines.forEach { add(it.name) }
            add("＋ 새 그룹 만들기")
        }

        AlertDialog.Builder(requireContext())
            .setTitle("루틴 그룹에 저장")
            .setItems(options.toTypedArray()) { _, index ->
                if (index < routines.size) {
                    val routine = routines[index]
                    val merged = (routine.exercises + exerciseNames).distinct()
                    routineRepo.update(routine.id, routine.name, merged)
                    Toast.makeText(requireContext(), "\"${routine.name}\" 그룹에 추가되었습니다", Toast.LENGTH_SHORT).show()
                } else {
                    showNewGroupDialog(exerciseNames)
                }
            }
            .setNegativeButton("취소", null)
            .show()
    }

    private fun showNewGroupDialog(exerciseNames: List<String>) {
        val input = EditText(requireContext()).apply {
            hint = "그룹 이름 (예: 상체 루틴)"
            setPadding(60, 32, 60, 16)
        }
        AlertDialog.Builder(requireContext())
            .setTitle("새 그룹 만들기")
            .setView(input)
            .setPositiveButton("만들기") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(requireContext(), "그룹 이름을 입력해주세요", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                routineRepo.save(name, exerciseNames)
                Toast.makeText(requireContext(), "\"$name\" 그룹이 저장되었습니다", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("취소", null)
            .show()
    }
}
