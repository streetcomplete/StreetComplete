package de.westnordost.streetcomplete.quests.sport

enum class Sport(val osmValue: String) {
    MULTI("multi"),
    // sorted by ~worldwide usages, minus country specific ones
    // 250k - 10k
    SOCCER("soccer"),
    TENNIS("tennis"),
    BASKETBALL("basketball"),
    GOLF("golf"),
    VOLLEYBALL("volleyball"),
    BEACHVOLLEYBALL("beachvolleyball"),
    SKATEBOARD("skateboard"),
    SHOOTING("shooting"),
    // 7k - 5k
    BASEBALL("baseball"),
    ATHLETICS("athletics"),
    TABLE_TENNIS("table_tennis"),
    GYMNASTICS("gymnastics"),
    // 4k - 2k
    BOULES("boules"),
    HANDBALL("handball"),
    FIELD_HOCKEY("field_hockey"),
    ICE_HOCKEY("ice_hockey"),
    AMERICAN_FOOTBALL("american_football"),
    EQUESTRIAN("equestrian"),
    ARCHERY("archery"),
    ROLLER_SKATING("roller_skating"),
    // 1k - 0k
    BADMINTON("badminton"),
    CRICKET("cricket"),
    RUGBY("rugby"),
    BOWLS("bowls"),
    SOFTBALL("softball"),
    RACQUET("racquet"),
    ICE_SKATING("ice_skating"),
    PADDLE_TENNIS("paddle_tennis"),
    AUSTRALIAN_FOOTBALL("australian_football"),
    CANADIAN_FOOTBALL("canadian_football"),
    NETBALL("netball"),
    GAELIC_GAMES("gaelic_games"),
    SEPAK_TAKRAW("sepak_takraw"),
}
