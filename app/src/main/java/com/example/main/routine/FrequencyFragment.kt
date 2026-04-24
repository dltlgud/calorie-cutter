package com.example.main.routine

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.main.R
import com.google.android.material.card.MaterialCardView

class FrequencyFragment : Fragment() {

    private val viewModel: RecommendationViewModel by activityViewModels()
    private var selectedFrequency: Int = -1

    private lateinit var cards: Map<Int, MaterialCardView>
    private lateinit var checks: Map<Int, TextView>
    private lateinit var btnNext: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_frequency, container, false)

        btnNext = view.findViewById(R.id.btn_next_frequency)

        cards = mapOf(
            2 to view.findViewById(R.id.card_freq_2),
            3 to view.findViewById(R.id.card_freq_3),
            4 to view.findViewById(R.id.card_freq_4),
            5 to view.findViewById(R.id.card_freq_5),
            6 to view.findViewById(R.id.card_freq_6)
        )
        checks = mapOf(
            2 to view.findViewById(R.id.check_freq_2),
            3 to view.findViewById(R.id.check_freq_3),
            4 to view.findViewById(R.id.check_freq_4),
            5 to view.findViewById(R.id.check_freq_5),
            6 to view.findViewById(R.id.check_freq_6)
        )

        cards.forEach { (freq, card) ->
            card.setOnClickListener { selectFrequency(freq) }
        }

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        btnNext.setOnClickListener {
            if (selectedFrequency == -1) return@setOnClickListener
            viewModel.setFrequency(selectedFrequency)
            findNavController().navigate(R.id.action_frequency_to_result)
        }

        return view
    }

    private fun selectFrequency(freq: Int) {
        selectedFrequency = freq
        val primaryColor = ContextCompat.getColor(requireContext(), R.color.primary)
        val borderColor = ContextCompat.getColor(requireContext(), R.color.border)
        val surfaceColor = ContextCompat.getColor(requireContext(), R.color.surface)
        val primaryLightColor = ContextCompat.getColor(requireContext(), R.color.primary_light)

        cards.forEach { (f, card) ->
            val isSelected = f == freq
            card.strokeColor = if (isSelected) primaryColor else borderColor
            card.strokeWidth = if (isSelected) 3 else 1
            card.setCardBackgroundColor(if (isSelected) primaryLightColor else surfaceColor)
            checks[f]?.visibility = if (isSelected) View.VISIBLE else View.GONE
        }

        btnNext.isEnabled = true
        btnNext.alpha = 1f
    }
}
