package com.example.main.information

import android.os.Bundle
import android.view.View
import android.net.Uri
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.main.R
import android.content.Intent
import android.util.Log
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.os.bundleOf

class ExplanationFragment : Fragment(R.layout.fragment_explanation) {

    companion object {
        private const val KEY = "exercise"
        fun newInstance(ex: Exercise): ExplanationFragment =
            ExplanationFragment().apply {
                arguments = Bundle().apply { putParcelable(KEY, ex) }
            }
    }

    // Safe Args 가 아니라, 기존 방식으로 Parcelable 로 받은 Exercise 객체
    private lateinit var ex: Exercise

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ex = requireArguments().getParcelable(KEY) ?: run {
            parentFragmentManager.popBackStack()
            return
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- 뷰 참조 ---
        val ivImage   = view.findViewById<ImageView>(R.id.iv_ex_image)
        val tvName    = view.findViewById<TextView>(R.id.tv_ex_name)
        val tvCat     = view.findViewById<TextView>(R.id.tv_ex_category)
        val tvEquipment = view.findViewById<TextView>(R.id.tv_equipment)
        val tvTarget  = view.findViewById<TextView>(R.id.tv_ex_target)
        val tvDesc    = view.findViewById<TextView>(R.id.tv_ex_desc)
        val tvMethod  = view.findViewById<TextView>(R.id.tv_ex_method)
        val tvCaution = view.findViewById<TextView>(R.id.tv_ex_caution)
        val tvTip     = view.findViewById<TextView>(R.id.tv_ex_tip)
        val btnYouTube = view.findViewById<Button>(R.id.btn_youtube)
        val btnReview   = view.findViewById<Button>(R.id.btn_review)
        val container  = view.findViewById<FrameLayout>(R.id.fragment_container)

        // --- 데이터 세팅 ---
        ivImage.setImageResource(ex.imageRes)
        tvName.text    = ex.name
        tvCat.text     = "분류: ${ex.category}"
        tvEquipment.text = "장비: ${ex.equipment}"
        tvTarget.text  = "타겟: ${ex.target}"
        tvDesc.text    = ex.description
        tvMethod.text  = "방법:\n${ex.method}"
        tvCaution.text = "주의사항:\n${ex.caution}"
        tvTip.text     = "팁:\n${ex.tip}"


        // 3) 리스너 붙이기
        btnReview.setOnClickListener {
            // action에 정의된 인자 이름(exerciseName)에 맞춰 전달
            val args = bundleOf("exerciseName" to ex.name)
            findNavController()
                .navigate(R.id.action_explanation_to_review, args)
        }


        // YouTube 버튼도 동일하게 찍어보기
        btnYouTube.setOnClickListener {
            Log.d("ExplanationFragment", "btnYouTube clicked!")
            Toast.makeText(requireContext(), "YouTube 버튼 눌림!", Toast.LENGTH_SHORT).show()
            val query = Uri.encode(ex.name)
            startActivity(Intent(Intent.ACTION_VIEW,
                Uri.parse("https://www.youtube.com/results?search_query=$query")))
        }

    }
}
