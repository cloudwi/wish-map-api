package com.mindbridge.wishmap.domain.user

object NicknameGenerator {

    private const val MAX_NUMBER_SUFFIX = 999

    private val adjectives = listOf(
        // 감정/성격
        "배고픈", "행복한", "신나는", "용감한", "느긋한",
        "유쾌한", "따뜻한", "씩씩한", "다정한", "든든한",
        // 음식 관련
        "맛있는", "배부른", "허기진", "미식가", "식탐의",
        "야식러", "먹보", "탐험하는", "골목길", "맛집찾는",
        "단맛좋아", "매운맛좋아", "불맛좋아", "국물좋아", "한입만"
    )

    private val nouns = listOf(
        // 동물
        "고양이", "판다", "토끼", "여우", "수달",
        "다람쥐", "햄스터", "펭귄", "코알라", "너구리",
        // 음식
        "떡볶이", "김밥", "라면", "만두", "붕어빵",
        "호떡", "치킨", "짜장면", "냉면", "비빔밥",
        "순대", "타코야끼", "마카롱", "크로플", "소금빵"
    )

    fun generate(): String {
        val adj = adjectives.random()
        val noun = nouns.random()
        val number = (1..MAX_NUMBER_SUFFIX).random()
        return "${adj}${noun}${number}"
    }
}
