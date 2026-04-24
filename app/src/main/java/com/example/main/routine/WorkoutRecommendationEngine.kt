package com.example.main.routine

object WorkoutRecommendationEngine {


    // 로컬 운동 데이터 (하드코딩)
    private val allStrength = listOf(
        // 하체 (Legs)
        ExerciseData("스쿼트", "하체", "웨이트"),
        ExerciseData("레그 프레스", "하체", "웨이트"),
        ExerciseData("런지", "하체", "하체"),
        ExerciseData("레그 익스텐션", "하체", "웨이트"),
        ExerciseData("레그 컬", "하체", "웨이트"),
        ExerciseData("힙 쓰러스트", "하체", "웨이트"),
        ExerciseData("스티프 레그 데드리프트", "하체", "웨이트"),
        ExerciseData("스플릿 스쿼트", "하체", "웨이트"),
        ExerciseData("카프 레이즈", "하체", "웨이트"),

        // 가슴 (Chest)
        ExerciseData("벤치프레스", "가슴", "웨이트"),
        ExerciseData("인클라인 벤치프레스", "가슴", "웨이트"),
        ExerciseData("덤벨 벤치프레스", "가슴", "웨이트"),
        ExerciseData("체스트 프레스 머신", "가슴", "웨이트"),
        ExerciseData("펙덱 플라이", "가슴", "웨이트"),
        ExerciseData("케이블 크로스오버", "가슴", "웨이트"),

        // 등 (Back)
        ExerciseData("랫풀다운", "등", "웨이트"),
        ExerciseData("바벨로우", "등", "웨이트"),
        ExerciseData("데드리프트", "등", "웨이트"),
        ExerciseData("시티드 케이블 로우", "등", "웨이트"),
        ExerciseData("풀업", "등", "웨이트"),
        ExerciseData("덤벨 로우", "등", "웨이트"),
        ExerciseData("T바 로우", "등", "웨이트"),

        // 어깨 (Shoulders)
        ExerciseData("오버헤드 프레스", "어깨", "웨이트"),
        ExerciseData("덤벨 숄더 프레스", "어깨", "웨이트"),
        ExerciseData("레터럴 레이즈", "어깨", "웨이트"),
        ExerciseData("프론트 레이즈", "어깨", "웨이트"),
        ExerciseData("리어 델트 레이즈", "어깨", "웨이트"),
        ExerciseData("업라이트 로우", "어깨", "웨이트"),
        ExerciseData("아놀드 프레스", "어깨", "웨이트"),

        // 팔 (Arms)
        ExerciseData("바벨 컬", "팔", "웨이트"),
        ExerciseData("덤벨 컬", "팔", "웨이트"),
        ExerciseData("해머 컬", "팔", "웨이트"),
        ExerciseData("컨센트레이션 컬", "팔", "웨이트"),
        ExerciseData("프리처 컬", "팔", "웨이트"),
        ExerciseData("트라이셉스 푸시다운", "팔", "웨이트"),
        ExerciseData("스컬크러셔", "팔", "웨이트"),
        ExerciseData("덤벨 킥백", "팔", "웨이트"),

        // 코어 (Core)
        ExerciseData("플랭크", "코어", "웨이트"),
        ExerciseData("케이블 킥백", "코어", "웨이트"),
        ExerciseData("크런치", "코어", "웨이트"),
        ExerciseData("러시안 트위스트", "코어", "웨이트")
    )
    private val allCardio = listOf(
        ExerciseData("러닝머신", "심폐", "유산소"),
        ExerciseData("실내자전거", "심폐", "유산소"),
        ExerciseData("스텝퍼", "심폐", "유산소"),
        ExerciseData("로잉머신", "심폐", "유산소"),
        ExerciseData("줄넘기", "심폐", "유산소"),
        ExerciseData("크로스 트레이너", "심폐", "유산소"),
        ExerciseData("싸이클", "심폐", "유산소"),
        ExerciseData("버피", "전신", "유산소"),
        ExerciseData("마운틴 클라이머", "전신", "유산소"),
        ExerciseData("하이 니즈", "전신", "유산소"),
        ExerciseData("점핑 잭", "전신", "유산소"),
        ExerciseData("트레드밀 인터벌", "심폐", "유산소"),
        ExerciseData("스핀 바이크", "심폐", "유산소"),
        ExerciseData("박스 점프", "심폐", "유산소"),
        ExerciseData("케틀벨 스윙", "전신", "유산소"),
        ExerciseData("배틀 로프", "전신", "유산소"),
        ExerciseData("사이클 인터벌", "심폐", "유산소"),
        ExerciseData("수영", "전신", "유산소"),
        ExerciseData("러닝 인터벌", "심폐", "유산소"),
        ExerciseData("웨이트 서킷", "전신", "유산소"),
        ExerciseData("HIIT", "전신", "유산소"),
        ExerciseData("파워워킹", "하체", "유산소"),
        ExerciseData("계단 오르기", "하체", "유산소"),
        ExerciseData("스피드 스케이터", "전신", "유산소"),
        ExerciseData("로드 사이클", "심폐", "유산소"),
        ExerciseData("스피닝 클래스", "심폐", "유산소"),
        ExerciseData("킥복싱", "전신", "유산소"),
        ExerciseData("점핑 런지", "전신", "유산소"),
        ExerciseData("사이드 샤플", "전신", "유산소")
    ) // 총 30개

    /**
     * @param gender "남성" or "여성"
     * @param age     사용자 나이
     * @param bmi     사용자 BMI (Double)
     * @param frequency 주당 운동 횟수
     */
    fun recommendWorkout(
        gender: String,
        age: Int,
        bmi: Double,
        frequency: Int
    ): List<ExerciseData> {

        // 1️⃣ 유산소 vs 웨이트 비율 결정
        val (weightRatio, cardioRatio) = when {
            bmi < 18.5 -> 1.0 to 0.0
            bmi < 25.0 -> 0.5 to 0.5
            bmi < 30.0 -> 0.4 to 0.6
            else        -> 0.2 to 0.8
        }

        // 2️⃣ 성별·나이 특수 조정 (덮어쓰기)
        val finalRatios = when {
            age >= 40 -> 0.4 to 0.6
            gender == "남성" -> 0.5 to 0.2 // (가중치는 상체/하체/유산소 이지만, 여기선 단순 weight/cardio)
            gender == "여성" -> 0.3 to 0.2
            else -> weightRatio to cardioRatio
        }

        val weightCount = (frequency * finalRatios.first).toInt()
        val cardioCount = frequency - weightCount

        // 3️⃣ 랜덤 추출
        val pickedStrength = allStrength.shuffled().take(weightCount)
        val pickedCardio = allCardio.shuffled().take(cardioCount)

        val result = mutableListOf<ExerciseData>()
        result += pickedStrength
        result += pickedCardio

        // 4️⃣ 아무것도 없으면 부위별 1개씩 보정
        if (result.isEmpty()) {
            result += listOf(
                allStrength.first { it.bodyPart == "가슴" },
                allStrength.first { it.bodyPart == "어깨" },
                allStrength.first { it.bodyPart == "팔" },
                allStrength.first { it.bodyPart == "등" },
                allStrength.first { it.bodyPart == "하체" }
            )
        }

        return result.shuffled()
    }
}
