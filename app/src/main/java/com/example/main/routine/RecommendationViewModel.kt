package com.example.main.routine

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecommendationViewModel : ViewModel() {

    private val _gender = MutableLiveData<String>()
    val gender: LiveData<String> = _gender

    private val _age = MutableLiveData<Int>()
    val age: LiveData<Int> = _age

    private val _bmi = MutableLiveData<Double>()
    val bmi: LiveData<Double> = _bmi

    private val _frequency = MutableLiveData<Int>()
    val frequency: LiveData<Int> = _frequency

    fun setGender(value: String) { _gender.value = value }
    fun setAge(value: Int) { _age.value = value }
    fun setBmi(value: Double) { _bmi.value = value }
    fun setFrequency(value: Int) { _frequency.value = value }

    // 🚩 중요: WorkoutRecommendationEngine.recommendWorkout 가 List<ExerciseData> 를 반환하도록 수정했어야 합니다.
    fun generateRecommendations(): List<ExerciseData> {
        return WorkoutRecommendationEngine.recommendWorkout(
            gender.value ?: "남성",
            age.value ?: 25,
            bmi.value ?: 22.0,
            frequency.value ?: 3
        )
    }
}
