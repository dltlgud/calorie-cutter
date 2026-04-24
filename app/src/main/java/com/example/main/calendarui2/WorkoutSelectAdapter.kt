package com.example.main.calendarui2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.main.R
import com.google.android.material.card.MaterialCardView

class WorkoutSelectAdapter(private var workoutList: List<String>) :
    RecyclerView.Adapter<WorkoutSelectAdapter.ViewHolder>() {

    var selectedName: String? = null

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val card: MaterialCardView = itemView as MaterialCardView
        val checkBox: CheckBox = itemView.findViewById(R.id.checkboxWorkout)
        val ivWorkoutImage: ImageView = itemView.findViewById(R.id.ivWorkoutImage)
        val workoutName: TextView = itemView.findViewById(R.id.textWorkoutName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_workout_select, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val name = workoutList[position]
        val isSelected = name == selectedName
        val context = holder.itemView.context

        holder.workoutName.text = name
        holder.ivWorkoutImage.setImageResource(getImageRes(name))
        holder.checkBox.visibility = if (isSelected) View.VISIBLE else View.INVISIBLE
        holder.checkBox.isChecked = isSelected

        if (isSelected) {
            holder.card.strokeColor = ContextCompat.getColor(context, R.color.primary)
            holder.card.strokeWidth = 3
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.primary_light))
        } else {
            holder.card.strokeColor = ContextCompat.getColor(context, R.color.border)
            holder.card.strokeWidth = 1
            holder.card.setCardBackgroundColor(ContextCompat.getColor(context, R.color.surface))
        }

        holder.itemView.setOnClickListener {
            selectedName = if (selectedName == name) null else name
            notifyDataSetChanged()
        }
    }

    override fun getItemCount() = workoutList.size

    fun updateList(newList: List<String>) {
        workoutList = newList
        notifyDataSetChanged()
    }

    fun getSelectedWorkout(): String? = selectedName

    private fun getImageRes(name: String): Int = when (name) {
        "레그 익스텐션"              -> R.drawable.ic_leg_extension
        "런지"                      -> R.drawable.ic_lunge
        "레그 프레스"                -> R.drawable.ic_leg_press
        "스쿼트"                    -> R.drawable.ic_squat
        "레그 컬"                   -> R.drawable.ic_leg_curl
        "힙 쓰러스트"               -> R.drawable.ic_hip_thrust
        "스티프 레그 데드리프트"     -> R.drawable.ic_stiff_leg_deadlift
        "스플릿 스쿼트"             -> R.drawable.ic_split_squat
        "케이블 킥백"               -> R.drawable.ic_cable_kickback
        "카프 레이즈"               -> R.drawable.ic_calf_raise
        "벤치프레스", "바벨 벤치프레스" -> R.drawable.ic_barbell_bench_press
        "인클라인 벤치프레스"        -> R.drawable.ic_incline_bench_press
        "덤벨 벤치프레스"           -> R.drawable.ic_dumbbell_bench_press
        "체스트 프레스 머신"         -> R.drawable.ic_chest_press_machine
        "펙덱 플라이"               -> R.drawable.ic_pec_deck_fly
        "케이블 크로스오버"          -> R.drawable.ic_cable_crossover
        "딥스"                      -> R.drawable.ic_dips
        "푸쉬업"                    -> R.drawable.ic_push_up
        "인클라인 덤벨 플라이"       -> R.drawable.ic_incline_dumbbell_fly
        "디클라인 벤치프레스"        -> R.drawable.ic_decline_bench_press
        "랫풀다운"                  -> R.drawable.ic_lat_pulldown
        "바벨로우"                  -> R.drawable.ic_barbell_row
        "데드리프트"                -> R.drawable.ic_deadlift
        "시티드 케이블 로우"         -> R.drawable.ic_seated_cable_row
        "풀업"                      -> R.drawable.ic_pull_up
        "덤벨 로우"                 -> R.drawable.ic_dumbbell_row
        "T바 로우"                  -> R.drawable.ic_t_bar_row
        "슈러그"                    -> R.drawable.ic_shrug
        "백 익스텐션"               -> R.drawable.ic_back_extension
        "체스트 서포티드 로우"       -> R.drawable.ic_chest_supported_row
        "오버헤드 프레스"            -> R.drawable.ic_overhead_press
        "덤벨 숄더 프레스"          -> R.drawable.ic_dumbbell_shoulder_press
        "레터럴 레이즈"             -> R.drawable.ic_lateral_raise
        "프론트 레이즈"             -> R.drawable.ic_front_raise
        "리어 델트 레이즈"          -> R.drawable.ic_rear_delt_raise
        "업라이트 로우"             -> R.drawable.ic_upright_row
        "아놀드 프레스"             -> R.drawable.ic_arnold_press
        "케이블 레터럴 레이즈"      -> R.drawable.ic_cable_lateral_raise
        "머신 숄더 프레스"          -> R.drawable.ic_machine_shoulder_press
        "페이스풀"                  -> R.drawable.ic_face_pull
        "바벨 컬"                   -> R.drawable.ic_barbell_curl
        "덤벨 컬"                   -> R.drawable.ic_dumbbell_curl
        "해머 컬"                   -> R.drawable.ic_hammer_curl
        "컨센트레이션 컬"           -> R.drawable.ic_concentration_curl
        "프리처 컬"                 -> R.drawable.ic_preacher_curl
        "트라이셉스 푸시다운"       -> R.drawable.ic_triceps_pushdown
        "스컬크러셔"                -> R.drawable.ic_skull_crusher
        "덤벨 킥백"                 -> R.drawable.ic_dumbbell_kickback
        "오버헤드 트라이셉스 익스텐션" -> R.drawable.ic_overhead_triceps_extension
        "케이블 로프 푸시다운"      -> R.drawable.ic_cable_rope_pushdown
        else                        -> R.drawable.ic_exercise_sample
    }
}
