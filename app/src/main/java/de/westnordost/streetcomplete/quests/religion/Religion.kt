package de.westnordost.streetcomplete.quests.religion

enum class Religion(val osmValue: String) {
    // sorted by worldwide usages, *minus* country specific ones
    CHRISTIAN("christian"),
    MUSLIM("muslim"),
    BUDDHIST("buddhist"),
    HINDU("hindu"),

    JEWISH("jewish"),
    // difficult to get the numbers on this, as they are counted alternating as buddhists,
    // taoists, confucianists, not religious or "folk religion" in statistics. See
    // https://en.wikipedia.org/wiki/Chinese_folk_religion
    // sorting relatively far up because there are many Chinese expats around the world
    CHINESE_FOLK("chinese_folk"),
    BAHAI("bahai"),
    SIKH("sikh"),

    TAOIST("taoist"),
    JAIN("jain"), // India
    SHINTO("shinto"), // Japan
    CAODAISM("caodaism"), // Vietnam

    MULTIFAITH("multifaith"),
}
