package de.westnordost.streetcomplete.quests.sport

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.view.GroupedItem

enum class Sport(
    override val value: String,
    override val drawableId: Int,
    override val titleId: Int)
    : GroupedItem {

    AMERICAN_FOOTBALL  ("american_football",  R.drawable.ic_sport_american_football, R.string.quest_sport_american_football),
    ARCHERY            ("archery",            R.drawable.ic_sport_archery,         R.string.quest_sport_archery),
    AUSTRALIAN_FOOTBALL("australian_football", R.drawable.ic_sport_australian_football, R.string.quest_sport_australian_football),
    ATHLETICS          ("athletics",          R.drawable.ic_sport_athletics,       R.string.quest_sport_athletics),
    BADMINTON          ("badminton",          R.drawable.ic_sport_badminton,       R.string.quest_sport_badminton),
    BASEBALL           ("baseball",           R.drawable.ic_sport_baseball,        R.string.quest_sport_baseball),
    BASKETBALL         ("basketball",         R.drawable.ic_sport_basketball,      R.string.quest_sport_basketball),
    BEACHVOLLEYBALL    ("beachvolleyball",    R.drawable.ic_sport_beachvolleyball, R.string.quest_sport_beachvolleyball),
    BOULES             ("boules",             R.drawable.ic_sport_boules,          R.string.quest_sport_boules),
    BOWLS              ("bowls",              R.drawable.ic_sport_bowls,           R.string.quest_sport_bowls),
    CANADIAN_FOOTBALL  ("canadian_football",  R.drawable.ic_sport_canadian_football, R.string.quest_sport_canadian_football),
    CRICKET            ("cricket",            R.drawable.ic_sport_cricket,         R.string.quest_sport_cricket),
    EQUESTRIAN         ("equestrian",         R.drawable.ic_sport_equestrian,      R.string.quest_sport_equestrian),
    FIELD_HOCKEY       ("field_hockey",       R.drawable.ic_sport_field_hockey,    R.string.quest_sport_field_hockey),
    GAELIC_GAMES       ("gaelic_games",       R.drawable.ic_sport_gaelic_games,    R.string.quest_sport_gaelic_games),
    GOLF               ("golf",               R.drawable.ic_sport_golf,            R.string.quest_sport_golf),
    GYMNASTICS         ("gymnastics",         R.drawable.ic_sport_gymnastics,      R.string.quest_sport_gymnastics),
    HANDBALL           ("handball",           R.drawable.ic_sport_handball,        R.string.quest_sport_handball),
    ICE_HOCKEY         ("ice_hockey",         R.drawable.ic_sport_ice_hockey,      R.string.quest_sport_ice_hockey),
    ICE_SKATING        ("ice_skating",        R.drawable.ic_sport_ice_skating,     R.string.quest_sport_ice_skating),
    NETBALL            ("netball",            R.drawable.ic_sport_netball,         R.string.quest_sport_netball),
    PADDLE_TENNIS      ("paddle_tennis",      R.drawable.ic_sport_paddle_tennis,   R.string.quest_sport_paddle_tennis),
    RACQUET            ("racquet",            R.drawable.ic_sport_racquet,         R.string.quest_sport_racquet),
    ROLLER_SKATING     ("roller_skating",     R.drawable.ic_sport_roller_skating,  R.string.quest_sport_roller_skating),
    RUGBY              ("rugby",              R.drawable.ic_sport_rugby,           R.string.quest_sport_rugby),
    SEPAK_TAKRAW       ("sepak_takraw",       R.drawable.ic_sport_sepak_takraw,    R.string.quest_sport_sepak_takraw),
    SHOOTING           ("shooting",           R.drawable.ic_sport_shooting,        R.string.quest_sport_shooting),
    SKATEBOARD         ("skateboard",         R.drawable.ic_sport_skateboard,      R.string.quest_sport_skateboard),
    SOCCER             ("soccer",             R.drawable.ic_sport_soccer,          R.string.quest_sport_soccer),
    SOFTBALL           ("softball",           R.drawable.ic_sport_softball,        R.string.quest_sport_softball),
    TABLE_TENNIS       ("table_tennis",       R.drawable.ic_sport_table_tennis,    R.string.quest_sport_table_tennis),
    TENNIS             ("tennis",             R.drawable.ic_sport_tennis,          R.string.quest_sport_tennis),
    VOLLEYBALL         ("volleyball",         R.drawable.ic_sport_volleyball,      R.string.quest_sport_volleyball);
}
