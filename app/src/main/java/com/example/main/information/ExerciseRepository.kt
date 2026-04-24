package com.example.main.information

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.main.R

object ExerciseRepository {

    /** assets/exercises_list.json 을 파싱해서 반환 */
    fun loadAll(context: Context): List<Exercise> {
        // 1) JSON 읽기
        val json = context.assets.open("exercise_list.json")
            .bufferedReader()
            .use { it.readText() }

        // 2) Gson 으로 List<Exercise> 타입으로 파싱
        val type = object : TypeToken<List<Exercise>>() {}.type
        val list: List<Exercise> = Gson().fromJson(json, type)

        // 3) imageName → 실제 drawable 리소스 ID 로 변환하고 ex.imageRes 에 대입
        return list.map { ex ->
            // getIdentifier 는 이름과 패키지를 기반으로 리소스 ID 조회
            val resId = context.resources.getIdentifier(
                ex.imageName,        // JSON 에 설정한 파일명 (확장자 제외)
                "drawable",          // drawable 폴더
                context.packageName  // 예: com.example.main
            )
            // 0 이면 못 찾은 것: ic_exercise_sample 로 대체
            ex.imageRes = if (resId != 0) resId else R.drawable.ic_exercise_sample
            ex
        }
    }
}
