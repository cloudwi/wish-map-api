package com.mindbridge.wishmap.domain.user

object NicknameGenerator {

    private const val MAX_NUMBER_SUFFIX = 999

    private val adjectives = listOf(
        "배고픈", "맛있는", "행복한", "신나는", "졸린",
        "용감한", "귀여운", "멋진", "상냥한", "즐거운",
        "씩씩한", "활발한", "느긋한", "다정한", "든든한",
        "포근한", "산뜻한", "깔끔한", "유쾌한", "따뜻한"
    )

    private val animals = listOf(
        "고양이", "강아지", "판다", "토끼", "여우",
        "곰", "사자", "펭귄", "코알라", "수달",
        "다람쥐", "햄스터", "올빼미", "돌고래", "기린",
        "치타", "알파카", "너구리", "두루미", "고래"
    )

    fun generate(): String {
        val adj = adjectives.random()
        val animal = animals.random()
        val number = (1..MAX_NUMBER_SUFFIX).random()
        return "${adj}${animal}${number}"
    }
}
