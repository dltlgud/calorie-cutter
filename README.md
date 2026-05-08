<div align="center">

  <img src="https://capsule-render.vercel.app/api?type=waving&color=gradient&customColorList=12,20,24&height=200&section
  =header&text=Calorie%20Cutter&fontSize=60&fontColor=ffffff&animation=twinkling&fontAlignY=38&desc=Android%20운동%20관
  리%20앱&descAlignY=60&descSize=20" width="100%"/>

  ### 운동 기록부터 루틴 추천까지, 나만의 피트니스 파트너
  <br/>

  [![Figma](https://img.shields.io/badge/화면설계서_보러가기-F24E1E?style=for-the-badge&logo=figma&logoColor=white)](htt
  ps://www.figma.com/design/CtSrSESCVJpADG6VetZWrv/calorie-cutter?node-id=0-1&p=f&t=kc52dThcGUc59xIU-0)

  </div>

  <br/>

  ## 📌 프로젝트 개요

  | 항목 | 내용 |
  |------|------|
  | 📅 기간 | 2024.08 – 2025.06 |
  | 👥 구성 | 5인 팀 프로젝트 (졸업작품) |
  | 👤 담당 | 팀장 · 운동 루틴 · 운동 라이브러리 · 커뮤니티 |

  <br/>

  ## ⚙️ 기술 스택

  **Android**

  ![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=kotlin&logoColor=white)
  ![Android](https://img.shields.io/badge/Android-3DDC84?style=flat&logo=android&logoColor=white)
  ![Material3](https://img.shields.io/badge/Material_3-757575?style=flat&logo=materialdesign&logoColor=white)
  ![Retrofit2](https://img.shields.io/badge/Retrofit2-48B983?style=flat&logoColor=white)
  ![Glide](https://img.shields.io/badge/Glide-4285F4?style=flat&logoColor=white)

  **Firebase**

  ![Firebase Auth](https://img.shields.io/badge/Firebase_Auth-FFCA28?style=flat&logo=firebase&logoColor=black)
  ![Firestore](https://img.shields.io/badge/Firestore-FFCA28?style=flat&logo=firebase&logoColor=black)
  ![RealtimeDB](https://img.shields.io/badge/Realtime_DB-FFCA28?style=flat&logo=firebase&logoColor=black)
  ![Storage](https://img.shields.io/badge/Storage-FFCA28?style=flat&logo=firebase&logoColor=black)

  **기타**

  ![MPAndroidChart](https://img.shields.io/badge/MPAndroidChart-FF6384?style=flat&logoColor=white)
  ![WorkManager](https://img.shields.io/badge/WorkManager-3DDC84?style=flat&logo=android&logoColor=white)

  <br/>

  ## ✨ 담당 기능

  <details>
  <summary><b>📢 팀장 / 발표</b></summary>

  - 팀장으로서 일정 관리 및 역할 분배
  - 최종 발표 담당

  </details>

  <details>
  <summary><b>📋 운동 루틴</b></summary>

  - 운동 목표에 따른 루틴 추천
  - 루틴 등록 · 수정 · 관리

  </details>

  <details>
  <summary><b>📚 운동 라이브러리</b></summary>

  - 부위별 운동 목록 조회
  - 운동별 상세 정보 및 리뷰 작성

  </details>

  <details>
  <summary><b>👥 커뮤니티</b></summary>

  - 일정 공유 게시판 (운동 일정 공유 및 참여)
  - 자유게시판 (게시글 · 댓글)
  - Firebase Firestore 기반 실시간 데이터 동기화

  </details>

  <br/>

  ## 🔄 리팩토링 (v1 → v2)

  | | v1 | v2 |
  |--|--|--|
  | 언어 | Java + Kotlin 혼합 | **100% Kotlin** |
  | 구조 | Firebase 쿼리가 Fragment에 분산 | **Repository 패턴** 중앙화 |
  | 보안 | 비밀번호 평문 저장 | **Firebase Auth** 전환 |
  | 기능 | 운동 기록만 존재 | **식단 관리(diet/)** 추가 |
  | 패키지 | 7개 | **11개** (auth · model · repository · diet 추가) |

  <br/>

  ## 🔧 기술적 도전

  - **WorkManager** — 앱 종료 후에도 48시간 미사용 시 백그라운드 알림 전송
  - **Firebase 이중 구조** — 실시간 피드는 RealtimeDB, 구조화 데이터는 Firestore로 분리
  - **MPAndroidChart** — 운동 기록 데이터를 BarChart · LineChart로 시각화

  <br/>

  <div align="center">
  <img src="https://capsule-render.vercel.app/api?type=waving&color=gradient&customColorList=12,20,24&height=100&section
  =footer" width="100%"/>
  </div>
