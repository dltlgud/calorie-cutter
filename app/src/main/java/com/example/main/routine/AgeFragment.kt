package com.example.main.routine

import com.example.main.R
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.NumberPicker
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import java.lang.reflect.Field

class AgeFragment : Fragment() {

    private val viewModel: RecommendationViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_age, container, false)

        val numberPicker = view.findViewById<NumberPicker>(R.id.numberPicker_age)
        numberPicker.minValue = 10
        numberPicker.maxValue = 80
        numberPicker.value = 25

        // ✅ 가운데 숫자 색상 변경 (EditText)
        for (i in 0 until numberPicker.childCount) {
            val child = numberPicker.getChildAt(i)
            if (child is EditText) {
                child.setTextColor(Color.WHITE)
            }
        }

        // ✅ 위/아래 숫자까지 색상 변경 (mSelectorWheelPaint 리플렉션)
        try {
            val selectorWheelPaintField: Field = NumberPicker::class.java.getDeclaredField("mSelectorWheelPaint")
            selectorWheelPaintField.isAccessible = true
            val paint = selectorWheelPaintField.get(numberPicker) as android.graphics.Paint
            paint.color = Color.WHITE
            numberPicker.invalidate()  // 다시 그리기
        } catch (e: Exception) {
            e.printStackTrace()
        }

        view.findViewById<ImageButton>(R.id.btnBack).setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        val btnNext = view.findViewById<Button>(R.id.btn_next_age)
        btnNext.setOnClickListener {
            viewModel.setAge(numberPicker.value)
            findNavController().navigate(R.id.action_age_to_bmi)
        }

        return view
    }
}
