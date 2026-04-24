package com.example.main.routine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.main.R

class GenderFragment : Fragment() {

    private val viewModel: RecommendationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_gender, container, false)

        val btnMale = view.findViewById<Button>(R.id.btn_male)
        val btnFemale = view.findViewById<Button>(R.id.btn_female)
        val btnBack = view.findViewById<ImageButton>(R.id.btnBack)

        btnMale.setOnClickListener {
            viewModel.setGender("남성")
            findNavController().navigate(R.id.action_gender_to_age)
        }

        btnFemale.setOnClickListener {
            viewModel.setGender("여성")
            findNavController().navigate(R.id.action_gender_to_age)
        }

        // ✅ 뒤로가기 버튼 누르면 이전 화면으로
        btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        return view
    }
}
