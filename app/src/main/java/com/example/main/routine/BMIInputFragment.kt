package com.example.main.routine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import android.widget.Toast
import com.example.main.R

class BMIInputFragment : Fragment() {

    private val viewModel: RecommendationViewModel by activityViewModels()
    private var calculatedBmi: Double? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_bmi, container, false)

        val etHeight = view.findViewById<EditText>(R.id.etHeight)
        val etWeight = view.findViewById<EditText>(R.id.etWeight)
        val btnCalc = view.findViewById<Button>(R.id.btnCalcBmi)
        val tvResult = view.findViewById<TextView>(R.id.et_bmi)
        val btnNext = view.findViewById<Button>(R.id.btn_next_bmi)

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        btnCalc.setOnClickListener {
            val heightCm = etHeight.text.toString().toDoubleOrNull()
            val weightKg = etWeight.text.toString().toDoubleOrNull()
            if (heightCm == null || weightKg == null || heightCm <= 0) {
                tvResult.text = "키와 몸무게를 올바르게 입력해주세요."
                calculatedBmi = null
                return@setOnClickListener
            }

            val heightM = heightCm / 100.0
            val bmi = weightKg / (heightM * heightM)
            calculatedBmi = bmi
            viewModel.setBmi(bmi)
            tvResult.text = "당신의 BMI: %.2f".format(bmi)
        }

        btnNext.setOnClickListener {
            if (calculatedBmi == null) {
                Toast.makeText(requireContext(), "BMI를 먼저 계산해주세요.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            findNavController().navigate(R.id.action_bmi_to_frequency)
        }

        return view
    }
}
