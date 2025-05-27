package de.westnordost.streetcomplete.quests.sport

import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.quests.sport.Sport.AMERICAN_FOOTBALL
import de.westnordost.streetcomplete.quests.sport.Sport.ARCHERY
import de.westnordost.streetcomplete.quests.sport.Sport.ATHLETICS
import de.westnordost.streetcomplete.quests.sport.Sport.AUSTRALIAN_FOOTBALL
import de.westnordost.streetcomplete.quests.sport.Sport.BADMINTON
import de.westnordost.streetcomplete.quests.sport.Sport.BASEBALL
import de.westnordost.streetcomplete.quests.sport.Sport.BASKETBALL
import de.westnordost.streetcomplete.quests.sport.Sport.BEACHVOLLEYBALL
import de.westnordost.streetcomplete.quests.sport.Sport.BOULES
import de.westnordost.streetcomplete.quests.sport.Sport.BOWLS
import de.westnordost.streetcomplete.quests.sport.Sport.CANADIAN_FOOTBALL
import de.westnordost.streetcomplete.quests.sport.Sport.CRICKET
import de.westnordost.streetcomplete.quests.sport.Sport.EQUESTRIAN
import de.westnordost.streetcomplete.quests.sport.Sport.FIELD_HOCKEY
import de.westnordost.streetcomplete.quests.sport.Sport.GAELIC_GAMES
import de.westnordost.streetcomplete.quests.sport.Sport.GOLF
import de.westnordost.streetcomplete.quests.sport.Sport.GYMNASTICS
import de.westnordost.streetcomplete.quests.sport.Sport.HANDBALL
import de.westnordost.streetcomplete.quests.sport.Sport.ICE_HOCKEY
import de.westnordost.streetcomplete.quests.sport.Sport.ICE_SKATING
import de.westnordost.streetcomplete.quests.sport.Sport.MULTI
import de.westnordost.streetcomplete.quests.sport.Sport.NETBALL
import de.westnordost.streetcomplete.quests.sport.Sport.PADDLE_TENNIS
import de.westnordost.streetcomplete.quests.sport.Sport.RACQUET
import de.westnordost.streetcomplete.quests.sport.Sport.ROLLER_SKATING
import de.westnordost.streetcomplete.quests.sport.Sport.RUGBY
import de.westnordost.streetcomplete.quests.sport.Sport.SEPAK_TAKRAW
import de.westnordost.streetcomplete.quests.sport.Sport.SHOOTING
import de.westnordost.streetcomplete.quests.sport.Sport.SKATEBOARD
import de.westnordost.streetcomplete.quests.sport.Sport.SOCCER
import de.westnordost.streetcomplete.quests.sport.Sport.SOFTBALL
import de.westnordost.streetcomplete.quests.sport.Sport.TABLE_TENNIS
import de.westnordost.streetcomplete.quests.sport.Sport.TENNIS
import de.westnordost.streetcomplete.quests.sport.Sport.VOLLEYBALL
import de.westnordost.streetcomplete.view.image_select.DisplayItem
import de.westnordost.streetcomplete.view.image_select.Item

fun Sport.asItem(): DisplayItem<Sport>? {
    val iconResId = iconResId ?: return null
    val titleResId = titleResId ?: return null
    return Item(this, iconResId, titleResId)
}

val Sport.titleResId: Int? get() = when (this) {
    MULTI ->               null
    SOCCER ->              R.string.quest_sport_soccer
    TENNIS ->              R.string.quest_sport_tennis
    BASKETBALL ->          R.string.quest_sport_basketball
    GOLF ->                R.string.quest_sport_golf
    VOLLEYBALL ->          R.string.quest_sport_volleyball
    BEACHVOLLEYBALL ->     R.string.quest_sport_beachvolleyball
    SKATEBOARD ->          R.string.quest_sport_skateboard
    SHOOTING ->            R.string.quest_sport_shooting
    BASEBALL ->            R.string.quest_sport_baseball
    ATHLETICS ->           R.string.quest_sport_athletics
    TABLE_TENNIS ->        R.string.quest_sport_table_tennis
    GYMNASTICS ->          R.string.quest_sport_gymnastics
    BOULES ->              R.string.quest_sport_boules
    HANDBALL ->            R.string.quest_sport_handball
    FIELD_HOCKEY ->        R.string.quest_sport_field_hockey
    ICE_HOCKEY ->          R.string.quest_sport_ice_hockey
    AMERICAN_FOOTBALL ->   R.string.quest_sport_american_football
    EQUESTRIAN ->          R.string.quest_sport_equestrian
    ARCHERY ->             R.string.quest_sport_archery
    ROLLER_SKATING ->      R.string.quest_sport_roller_skating
    BADMINTON ->           R.string.quest_sport_badminton
    CRICKET ->             R.string.quest_sport_cricket
    RUGBY ->               R.string.quest_sport_rugby
    BOWLS ->               R.string.quest_sport_bowls
    SOFTBALL ->            R.string.quest_sport_softball
    RACQUET ->             R.string.quest_sport_racquet
    ICE_SKATING ->         R.string.quest_sport_ice_skating
    PADDLE_TENNIS ->       R.string.quest_sport_paddle_tennis
    AUSTRALIAN_FOOTBALL -> R.string.quest_sport_australian_football
    CANADIAN_FOOTBALL ->   R.string.quest_sport_canadian_football
    NETBALL ->             R.string.quest_sport_netball
    GAELIC_GAMES ->        R.string.quest_sport_gaelic_games
    SEPAK_TAKRAW ->        R.string.quest_sport_sepak_takraw
}

val Sport.iconResId: Int? get() = when (this) {
    MULTI ->               null
    SOCCER ->              R.drawable.ic_sport_soccer
    TENNIS ->              R.drawable.ic_sport_tennis
    BASKETBALL ->          R.drawable.ic_sport_basketball
    GOLF ->                R.drawable.ic_sport_golf
    VOLLEYBALL ->          R.drawable.ic_sport_volleyball
    BEACHVOLLEYBALL ->     R.drawable.ic_sport_beachvolleyball
    SKATEBOARD ->          R.drawable.ic_sport_skateboard
    SHOOTING ->            R.drawable.ic_sport_shooting
    BASEBALL ->            R.drawable.ic_sport_baseball
    ATHLETICS ->           R.drawable.ic_sport_athletics
    TABLE_TENNIS ->        R.drawable.ic_sport_table_tennis
    GYMNASTICS ->          R.drawable.ic_sport_gymnastics
    BOULES ->              R.drawable.ic_sport_boules
    HANDBALL ->            R.drawable.ic_sport_handball
    FIELD_HOCKEY ->        R.drawable.ic_sport_field_hockey
    ICE_HOCKEY ->          R.drawable.ic_sport_ice_hockey
    AMERICAN_FOOTBALL ->   R.drawable.ic_sport_american_football
    EQUESTRIAN ->          R.drawable.ic_sport_equestrian
    ARCHERY ->             R.drawable.ic_sport_archery
    ROLLER_SKATING ->      R.drawable.ic_sport_roller_skating
    BADMINTON ->           R.drawable.ic_sport_badminton
    CRICKET ->             R.drawable.ic_sport_cricket
    RUGBY ->               R.drawable.ic_sport_rugby
    BOWLS ->               R.drawable.ic_sport_bowls
    SOFTBALL ->            R.drawable.ic_sport_softball
    RACQUET ->             R.drawable.ic_sport_racquet
    ICE_SKATING ->         R.drawable.ic_sport_ice_skating
    PADDLE_TENNIS ->       R.drawable.ic_sport_paddle_tennis
    AUSTRALIAN_FOOTBALL -> R.drawable.ic_sport_australian_football
    CANADIAN_FOOTBALL ->   R.drawable.ic_sport_canadian_football
    NETBALL ->             R.drawable.ic_sport_netball
    GAELIC_GAMES ->        R.drawable.ic_sport_gaelic_games
    SEPAK_TAKRAW ->        R.drawable.ic_sport_sepak_takraw
}
