package de.westnordost.streetcomplete.quests.sport

import de.westnordost.streetcomplete.quests.sport.Sport.*
import de.westnordost.streetcomplete.resources.*
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.StringResource

val Sport.title: StringResource get() = when (this) {
    MULTI ->               Res.string.quest_sport_answer_multi
    SOCCER ->              Res.string.quest_sport_soccer
    TENNIS ->              Res.string.quest_sport_tennis
    BASKETBALL ->          Res.string.quest_sport_basketball
    GOLF ->                Res.string.quest_sport_golf
    VOLLEYBALL ->          Res.string.quest_sport_volleyball
    BEACHVOLLEYBALL ->     Res.string.quest_sport_beachvolleyball
    SKATEBOARD ->          Res.string.quest_sport_skateboard
    SHOOTING ->            Res.string.quest_sport_shooting
    BASEBALL ->            Res.string.quest_sport_baseball
    ATHLETICS ->           Res.string.quest_sport_athletics
    TABLE_TENNIS ->        Res.string.quest_sport_table_tennis
    GYMNASTICS ->          Res.string.quest_sport_gymnastics
    BOULES ->              Res.string.quest_sport_boules
    HANDBALL ->            Res.string.quest_sport_handball
    FIELD_HOCKEY ->        Res.string.quest_sport_field_hockey
    ICE_HOCKEY ->          Res.string.quest_sport_ice_hockey
    AMERICAN_FOOTBALL ->   Res.string.quest_sport_american_football
    EQUESTRIAN ->          Res.string.quest_sport_equestrian
    ARCHERY ->             Res.string.quest_sport_archery
    ROLLER_SKATING ->      Res.string.quest_sport_roller_skating
    BADMINTON ->           Res.string.quest_sport_badminton
    CRICKET ->             Res.string.quest_sport_cricket
    RUGBY ->               Res.string.quest_sport_rugby
    BOWLS ->               Res.string.quest_sport_bowls
    SOFTBALL ->            Res.string.quest_sport_softball
    RACQUET ->             Res.string.quest_sport_racquet
    ICE_SKATING ->         Res.string.quest_sport_ice_skating
    PADDLE_TENNIS ->       Res.string.quest_sport_paddle_tennis
    AUSTRALIAN_FOOTBALL -> Res.string.quest_sport_australian_football
    CANADIAN_FOOTBALL ->   Res.string.quest_sport_canadian_football
    NETBALL ->             Res.string.quest_sport_netball
    GAELIC_GAMES ->        Res.string.quest_sport_gaelic_games
    SEPAK_TAKRAW ->        Res.string.quest_sport_sepak_takraw
}

val Sport.icon: DrawableResource get() = when (this) {
    MULTI ->               Res.drawable.empty_96
    SOCCER ->              Res.drawable.sport_soccer
    TENNIS ->              Res.drawable.sport_tennis
    BASKETBALL ->          Res.drawable.sport_basketball
    GOLF ->                Res.drawable.sport_golf
    VOLLEYBALL ->          Res.drawable.sport_volleyball
    BEACHVOLLEYBALL ->     Res.drawable.sport_beachvolleyball
    SKATEBOARD ->          Res.drawable.sport_skateboard
    SHOOTING ->            Res.drawable.sport_shooting
    BASEBALL ->            Res.drawable.sport_baseball
    ATHLETICS ->           Res.drawable.sport_athletics
    TABLE_TENNIS ->        Res.drawable.sport_table_tennis
    GYMNASTICS ->          Res.drawable.sport_gymnastics
    BOULES ->              Res.drawable.sport_boules
    HANDBALL ->            Res.drawable.sport_handball
    FIELD_HOCKEY ->        Res.drawable.sport_field_hockey
    ICE_HOCKEY ->          Res.drawable.sport_ice_hockey
    AMERICAN_FOOTBALL ->   Res.drawable.sport_american_football
    EQUESTRIAN ->          Res.drawable.sport_equestrian
    ARCHERY ->             Res.drawable.sport_archery
    ROLLER_SKATING ->      Res.drawable.sport_roller_skating
    BADMINTON ->           Res.drawable.sport_badminton
    CRICKET ->             Res.drawable.sport_cricket
    RUGBY ->               Res.drawable.sport_rugby
    BOWLS ->               Res.drawable.sport_bowls
    SOFTBALL ->            Res.drawable.sport_softball
    RACQUET ->             Res.drawable.sport_racquet
    ICE_SKATING ->         Res.drawable.sport_ice_skating
    PADDLE_TENNIS ->       Res.drawable.sport_paddle_tennis
    AUSTRALIAN_FOOTBALL -> Res.drawable.sport_australian_football
    CANADIAN_FOOTBALL ->   Res.drawable.sport_canadian_football
    NETBALL ->             Res.drawable.sport_netball
    GAELIC_GAMES ->        Res.drawable.sport_gaelic_games
    SEPAK_TAKRAW ->        Res.drawable.sport_sepak_takraw
}
